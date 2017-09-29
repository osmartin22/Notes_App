package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import static com.ozmar.notes.MainActivity.currentList;
import static com.ozmar.notes.MainActivity.db;

public class NoteEditorActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote = null;
    private boolean favorite = false;
    private int notePosition;

    // Hide the soft keyboard
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // Show the soft keyboard
    private void showSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void hideMenuItem(View view) {
        // Might not be necessary
        view.setVisibility(View.INVISIBLE);
    }

    private void favoriteNoteMenu() {
        favorite = !favorite;
        Toast toast = Toast.makeText(getApplicationContext(), "Note is favorite: " + favorite, Toast.LENGTH_SHORT);
        toast.show();

        int temp = 0;
        if (favorite) {     // Show boolean as int to store in db
            temp = 1;
        }

        if (currentNote != null) {  // Save favorite even if user does not explicitly press save menu item
            currentNote.set_favorite(temp);
            db.updateNote(currentNote);
        }
    }

    private void goBackToMainActivity(int value) {
        Intent goToMain = new Intent(getApplicationContext(), MainActivity.class);
        goToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goToMain.putExtra("Note Success", value);

        if (notePosition == -1 && value == 3) {
            notePosition = 0;
        }
        goToMain.putExtra("Note Position", notePosition);

        Log.d("Value", Integer.toString(value));
        Log.d("Index", Integer.toString(notePosition));

        setResult(RESULT_OK, goToMain);
        finish();
    }

    private void deleteNoteMenu() {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (currentNote != null) {
                            db.deleteNote(currentNote);
                            goBackToMainActivity(4);
                        } else {
                            goBackToMainActivity(-1);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Check what the user changed from the original note
    private String checkForDifferenceFromOriginal() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (title.isEmpty() && content.isEmpty() && currentNote == null) {
            return "discardNote";
        } else if (currentNote != null) {
            boolean titleTheSame = title.equals(currentNote.get_title());
            boolean contentTheSame = content.equals(currentNote.get_content());

            if (!(titleTheSame && contentTheSame)) {
                if (titleTheSame) {
                    return "contentChanged";
                } else if (contentTheSame) {
                    return "titleChanged";
                } else {
                    return "titleAndContentChanged";
                }
            }
            return "notChanged";   // Note not changed
        }
        return "newNote";   // New note
    }

    // Save note into db and go back to MainActivity
    private void saveNoteMenu() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        String difference = checkForDifferenceFromOriginal();
        // Only modify the string with differences to prevent unnecessary object creation
        // 0 = new Note and empty, discard
        // 1 = note modified
        // 2 = note not changed
        // 3 = new note added
        switch (difference) {
            case "discardNote":
                goBackToMainActivity(0);
                break;

            case "contentChanged":
                currentNote.set_content(content);
                currentList.get(notePosition).set_content(content);
                db.updateNote(currentNote);
                goBackToMainActivity(1);
                break;

            case "titleChanged":
                currentNote.set_title(title);
                currentList.get(notePosition).set_title(title);
                db.updateNote(currentNote);
                goBackToMainActivity(1);
                break;

            case "titleAndContentChanged":
                currentNote.set_title(title);
                currentNote.set_content(content);
                currentList.get(notePosition).set_title(title);
                currentList.get(notePosition).set_content(content);
                db.updateNote(currentNote);
                goBackToMainActivity(1);
                break;

            case "notChanged":
                goBackToMainActivity(2);
                break;

            case "newNote":
                SingleNote temp;
                if (favorite) {
                    temp = new SingleNote(title, content, 1);
                } else {
                    temp = new SingleNote(title, content);
                }

                db.addNote(temp);
//                currentList.add(0, temp);     // TODO: Fix
                goBackToMainActivity(3);
                break;
        }
    } // saveNoteMenu() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("noteID", -1);
        Log.d("Note Received", Integer.toString(notePosition));

        if (notePosition != -1) {
            currentNote = currentList.get(notePosition);
            if (currentNote.get_favorite() == 1) {
                favorite = true;
            }

            editTextTitle.setText(currentNote.get_title());
            editTextTitle.setFocusable(false);
            editTextContent.setText(currentNote.get_content());
            editTextContent.setFocusable(false);

            // Keyboard does not pop up until the user clicks on the screen
            // allowing the user to see the entire note at the start
            View.OnTouchListener editTextListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    view.setFocusableInTouchMode(true);
                    view.requestFocus();
                    return false;
                }
            };
            editTextTitle.setOnTouchListener(editTextListener);
            editTextContent.setOnTouchListener(editTextListener);
        }

        // New note is being created, show keyboard at the start
        else {
            editTextContent.requestFocus();
        }
    } // onCreate() end

    // Only show alert dialog if there is a difference from when the note was opened
    @Override
    public void onBackPressed() {

        final String difference = checkForDifferenceFromOriginal();
        Toast.makeText(getApplicationContext(), difference, Toast.LENGTH_SHORT).show();
        if (!difference.equals("discardNote") && !difference.equals("notChanged")) {
            new AlertDialog.Builder(NoteEditorActivity.this)
                    .setMessage("Save your changes or discard them?")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveNoteMenu();
                            if (currentNote != null) {
                                goBackToMainActivity(1);
                            } else {
                                goBackToMainActivity(3);
                            }
                        }
                    })
                    .setNeutralButton("Cancel", null)
                    .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goBackToMainActivity(2);
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // To use for menu button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.save_note) {
            saveNoteMenu();
            return true;
        } else if (item.getItemId() == R.id.delete_note) {
            deleteNoteMenu();
            return true;
        } else if (item.getItemId() == R.id.favorite_note) {
            favoriteNoteMenu();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        View view = this.getCurrentFocus();
        if (view != null) {
            int viewId = view.getId();
            outState.putInt("focusID", viewId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int viewId = savedInstanceState.getInt("focusID", View.NO_ID);
        View view = findViewById(viewId);
        if (view != null) {
            Toast.makeText(getApplicationContext(), "Restored View " + view.toString(), Toast.LENGTH_SHORT).show();
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
        }
    }
} // NoteEditorActivity() end
