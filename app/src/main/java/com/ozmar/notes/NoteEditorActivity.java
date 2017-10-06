package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ozmar.notes.utils.NoteEditorUtils;

import static com.ozmar.notes.MainActivity.db;

public class NoteEditorActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote = null;
    private MenuItem menuItem;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

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
    } // deleteNoteMenu() end

    // Check strings.xml for meaning of noteResult[]
    private void goBackToMainActivity(String value) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Set notePosition to 0 from -1 if the note being added is a new note
        if (value.equals(noteResult[3])) {
            if (favorite) {
                intent.putExtra("Note Favorite", true);
            }
            notePosition = 0;
        }

        intent.putExtra("Note Success", value);
        intent.putExtra("Note Position", notePosition);
        intent.putExtra("Note", currentNote);
        setResult(RESULT_OK, intent);
        finish();
    } // goBackToMainActivity() end

    private void saveNoteInDb() {
        switch (listUsed) {
            case 0:
            case 1:
                db.updateNoteFromUserList(currentNote);
                break;
            case 2:
                db.updateNoteFromArchive(currentNote);
                break;
        }
    } // saveNoteInDb() end

    // Update note if title and or content changed
    // NOTE: Should only be called if the note was modified
    private void updateNote(boolean titleChanged, boolean contentChanged) {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (titleChanged) {
            currentNote.set_title(title);
        }

        if (contentChanged) {
            currentNote.set_content(content);
        }

        saveNoteInDb();
        goBackToMainActivity(noteResult[1]);
    } // updateNote() end

    // Save note into db and go back to MainActivity
    private void saveNoteMenu() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        String difference = NoteEditorUtils.differenceFromOriginal(title, content, currentNote);
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
                temp = favorite ? new SingleNote(title, content, 1) : new SingleNote(title, content, 0);
                db.addNoteToUserList(temp);
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
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        noteResult = getResources().getStringArray(R.array.noteResultArray);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("noteID", -1);
        listUsed = intent.getIntExtra("listUsed", 0);
        currentNote = intent.getParcelableExtra("Note");

        setUpNoteView();
    } // onCreate() end

    private void setUpNoteView() {
        if (currentNote != null) {

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

                    if (listUsed == 3) {
                        Toast.makeText(getApplicationContext(), "No edit", Toast.LENGTH_SHORT).show();
                    } else {
                        view.setFocusableInTouchMode(true);
                        view.requestFocus();
                    }
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
    }

    // Only show alert dialog if there is a difference from when the note was opened
    @Override
    public void onBackPressed() {
        final String difference = NoteEditorUtils.differenceFromOriginal(
                editTextTitle.getText().toString(), editTextContent.getText().toString(), currentNote);

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
            if (listUsed == 1 && !favorite && currentNote != null) {
                goBackToMainActivity(noteResult[5]);
            } else {
                super.onBackPressed();
            }
        }
    } // onBackPressed() end

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_editor_menu, menu);

        menuItem = menu.findItem(R.id.favorite_note);

        if (listUsed != 3) {

            if (listUsed == 2) {
                menu.findItem(R.id.archive_note).setVisible(false);
                menu.findItem(R.id.unarchive_note).setVisible(true);
            }

            if (currentNote != null && currentNote.get_favorite() == 1) {
                menu.findItem(R.id.favorite_note).setIcon(R.drawable.ic_favorite_star_on);
            }

        } else {
            menu.findItem(R.id.restore_note).setVisible(true);
            menu.findItem(R.id.delete_note).setVisible(false);
            menu.findItem(R.id.archive_note).setVisible(false);
            menu.findItem(R.id.save_note).setVisible(false);
            menu.findItem(R.id.favorite_note).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    } // onCreateOptionsMenu() end

    // To use for menu button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.save_note:
                saveNoteMenu();
                return true;

            case R.id.delete_note:
                deleteNoteMenu();
                return true;

            case R.id.favorite_note:
                favorite = NoteEditorUtils.favoriteNote(favorite, menuItem);
                return true;

            case R.id.archive_note:
                int result = NoteEditorUtils.archiveNote(editTextTitle, editTextContent, currentNote, db);
                goBackToMainActivity(noteResult[result]);
                return true;

            case R.id.unarchive_note:
                NoteEditorUtils.unArchiveNote(favorite, currentNote, db);
                goBackToMainActivity(noteResult[4]);
                return true;

            case R.id.restore_note:
                NoteEditorUtils.restoreNote(currentNote, db);
                goBackToMainActivity(noteResult[4]);
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    @Override
    protected void onPause() {
        super.onPause();
        int temp = favorite ? 1 : 0;

        if (currentNote != null && listUsed != 2) {  // Save favorite even if user does not explicitly press save menu item
            currentNote.set_favorite(temp);
            db.updateNoteFromUserList(currentNote);
        }
    }
} // NoteEditorActivity() end