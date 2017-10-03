package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static com.ozmar.notes.MainActivity.currentList;
import static com.ozmar.notes.MainActivity.db;

// If I do categories:
// Possibly pass a number 0-2 for the currentList being used
// 0 = all notes    1 = favorite notes  2 = category note
// Also pass a category name
// if 1 or 2 pass "" else pass the category name
// Use that to get the desired list and whether to add to the currentList or not
// i.e. if on favoriteList and new note being added and not a favorite not.
// do not add it to the currentList, only the db.
public class NoteEditorActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote = null;
    private MenuItem menuItem;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

    private void favoriteNoteMenu() {
        favorite = !favorite;
        Toast.makeText(getApplicationContext(), "Note is favorite: " + favorite, Toast.LENGTH_SHORT).show();

        int temp = 0;
        if (favorite) {
            menuItem.setIcon(R.drawable.ic_favorite_star_on);
            temp = 1;
        } else {
            menuItem.setIcon(R.drawable.ic_favorite_star_off);
        }

        // TODO: Put in onPause()/onStop(), have variable that checks if save was already done
        if (currentNote != null) {  // Save favorite even if user does not explicitly press save menu item
            currentNote.set_favorite(temp);
            db.updateNoteFromUserList(currentNote);
        }
    }

    private void deleteNoteMenu() {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (currentNote != null) {
                            db.deleteNoteFromUserList(currentNote);
                            db.addNoteToRecycleBin(currentNote);
                            goBackToMainActivity(noteResult[4]);
                        } else {
                            goBackToMainActivity(noteResult[0]);       // New empty note, discard
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 0 = discard note
    // 1 = existing note modified
    // 2 = existing note not changed
    // 3 = new note added
    // 4 = delete note
    // 5 = favorite was changed but note text not changed
    private void goBackToMainActivity(String value) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Set notePosition to 0 from -1 if the note being added is a new note
        if (value.equals(noteResult[3])) {
            Log.d("Position", value);
            if(favorite) {
                intent.putExtra("Note Favorite", true);
            }
            notePosition = 0;
        }

        intent.putExtra("Note Success", value);
        intent.putExtra("Note Position", notePosition);
        setResult(RESULT_OK, intent);
        finish();
    }

    // Check what the user changed from the original note
    @NonNull
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

            return "notChanged";
        } // else if() end

        return "newNote";
    } // checkForDifferenceFromOriginal() end

    // Update note if title and or content changed
    // NOTE: Should only be called if the note was modified
    private void updateNote(boolean titleChanged, boolean contentChanged) {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (titleChanged) {
            currentNote.set_title(title);
            currentList.get(notePosition).set_title(title);
        }

        if (contentChanged) {
            currentNote.set_content(content);
            currentList.get(notePosition).set_content(content);
        }

        db.updateNoteFromUserList(currentNote);
        goBackToMainActivity(noteResult[1]);
    }

    // Save note into db and go back to MainActivity
    private void saveNoteMenu() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        String difference = checkForDifferenceFromOriginal();
        switch (difference) {
            case "discardNote":
                goBackToMainActivity(noteResult[0]);
                break;

            case "contentChanged":
                updateNote(false, true);
                break;

            case "titleChanged":
                updateNote(true, false);
                break;

            case "titleAndContentChanged":
                updateNote(true, true);
                break;

            case "notChanged":

                // Used to update favorites list if it is the current list being looked at and the note
                // is no longer a favorite note
                if (listUsed == 1 && !favorite) {
                    goBackToMainActivity(noteResult[5]);
                } else {
                    goBackToMainActivity(noteResult[2]);    // No updates to RecyclerView
                }
                break;

            case "newNote":
                SingleNote temp;
                if (favorite) {
                    temp = new SingleNote(title, content, 1);
                } else {
                    temp = new SingleNote(title, content, 0);
                }

                db.addNoteToUserList(0, temp);
                goBackToMainActivity(noteResult[3]);
                break;
        }
    } // saveNoteMenu() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        noteResult = getResources().getStringArray(R.array.noteResultArray);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("noteID", -1);
        listUsed = intent.getIntExtra("listUsed", 0);

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

        // Only show AlertDialog if the note needs saving
        if (!difference.equals("discardNote") && !difference.equals("notChanged")) {
            new AlertDialog.Builder(NoteEditorActivity.this)
                    .setMessage("Save your changes or discard them?")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveNoteMenu();
                            if (currentNote != null) {
                                goBackToMainActivity(noteResult[1]);    // Write over existing note
                            } else {
                                goBackToMainActivity(noteResult[3]);    // New note saved
                            }
                        }
                    })
                    .setNeutralButton("Cancel", null)
                    .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goBackToMainActivity(noteResult[2]);    // No change to note
                        }
                    })
                    .show();
        } else {

            // Used to update favorites list if it is the current list being looked at and the note
            // is no longer a favorite note
            if (listUsed == 1 && !favorite) {
                goBackToMainActivity(noteResult[5]);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_editor_menu, menu);

        if(currentNote!= null && currentNote.get_favorite() == 1) {
            menu.findItem(R.id.favorite_note).setIcon(R.drawable.ic_favorite_star_on);
        }

        return super.onCreateOptionsMenu(menu);
    }

    // To use for menu button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.save_note:
                saveNoteMenu();
                return true;

            case R.id.delete_note:
                deleteNoteMenu();
                return true;

            case R.id.favorite_note:
                this.menuItem = item;
                favoriteNoteMenu();
                return true;

            case R.id.archive_note:
                if(currentNote !=null) {
                    db.addNoteToArchive(currentNote);
                    db.deleteNoteFromUserList(currentNote);
                    goBackToMainActivity(noteResult[4]);
                } else {
                    db.addNoteToArchive(new SingleNote(editTextTitle.getText().toString(), editTextContent.getText().toString()));
                    goBackToMainActivity(noteResult[2]);
                }
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
