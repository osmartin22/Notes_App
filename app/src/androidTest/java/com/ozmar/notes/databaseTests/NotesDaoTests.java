package com.ozmar.notes.databaseTests;


import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.RecycleBinNote;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
public class NotesDaoTests {

    private AppDatabase mDb;

    private MainNote mMainNote;

    private String updatedTitle = "Updated Title";
    private String updatedContent = "Updated Content";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        mMainNote = new MainNote(1, "Title", "Content", 1, 1, 0, -1);
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    private void updateMainNote(MainNote note) {
        note.setTitle(updatedTitle);
        note.setContent(updatedContent);
    }

    private void updateArchiveNote(ArchiveNote note) {
        note.setTitle(updatedTitle);
        note.setContent(updatedContent);
    }


    @Test
    public void getUsersWhenNoUserInserted() throws Exception{
        mDb.notesDao().getAUserNote(1)
                .test()
                .assertNoValues();
    }

    @Test
    public void writeToAndReadFromMainList() throws Exception {
        mDb.notesDao().addToUserNotes(mMainNote);

        mDb.notesDao().getAUserNote(mMainNote.getId())
                .test()
                .assertValue(note -> note.equals(mMainNote));
    }

    @Test
    public void updateAndReadFromMainList() throws Exception {
        mDb.notesDao().addToUserNotes(mMainNote);

        updateMainNote(mMainNote);
        mDb.notesDao().updateAUserNote(mMainNote);

        mDb.notesDao().getAUserNote(mMainNote.getId())
                .test()
                .assertValue(note -> note.equals(mMainNote));
    }

    @Test
    public void deleteFromMainList() throws Exception {
        // Passing entire object to dao
        mDb.notesDao().addToUserNotes(mMainNote);
        mDb.notesDao().deleteFromUserNotes(mMainNote);

        mDb.notesDao().getAUserNote(mMainNote.getId())
                .test()
                .assertNoValues();


        // Passing object id to dao
        mDb.notesDao().addToUserNotes(mMainNote);
        mDb.notesDao().deleteFromUserNotes(mMainNote.getId());

        mDb.notesDao().getAUserNote(mMainNote.getId())
                .test()
                .assertNoValues();
    }


    @Test
    public void writeToAndReadFromArchiveList() throws Exception {
        ArchiveNote archiveNote = new ArchiveNote(mMainNote);

        long id = mDb.notesDao().addToArchiveNotes(archiveNote);
        Assert.assertEquals(archiveNote.getId(), id);

        mDb.notesDao().getAnArchiveNote(archiveNote.getId())
                .test()
                .assertValue(note -> note.equals(archiveNote));
    }

    @Test
    public void updateAndReadFromArchiveList() throws Exception {
        ArchiveNote archiveNote = new ArchiveNote(mMainNote);
        mDb.notesDao().addToArchiveNotes(archiveNote);

        updateArchiveNote(archiveNote);
        mDb.notesDao().updateAnArchiveNote(archiveNote);

        mDb.notesDao().getAnArchiveNote(archiveNote.getId())
                .test()
                .assertValue(note -> note.equals(archiveNote));
    }

    @Test
    public void deleteFromArchiveList() throws Exception {
        // Passing entire object to dao
        ArchiveNote archiveNote = new ArchiveNote(mMainNote);
        mDb.notesDao().addToArchiveNotes(archiveNote);
        mDb.notesDao().deleteFromArchiveNotes(archiveNote);

        mDb.notesDao().getAnArchiveNote(archiveNote.getId())
                .test()
                .assertNoValues();


        // Passing object id to dao
        mDb.notesDao().addToArchiveNotes(archiveNote);
        mDb.notesDao().deleteFromArchiveNotes(archiveNote.getId());

        mDb.notesDao().getAnArchiveNote(archiveNote.getId())
                .test()
                .assertNoValues();
    }

    @Test
    public void writeToAndReadFromRecycleBinList() throws Exception {
        RecycleBinNote recycleBinNote = new RecycleBinNote(mMainNote);

        long id = mDb.notesDao().addToRecycleBinNotes(recycleBinNote);
        Assert.assertEquals(recycleBinNote.getId(), id);

        mDb.notesDao().getARecycleBinNotes(recycleBinNote.getId())
                .test()
                .assertValue(note -> note.equals(recycleBinNote));
    }

    @Test
    public void deleteFromRecycleBinList() throws Exception {
        // Passing entire object to dao
        RecycleBinNote recycleBinNote = new RecycleBinNote(mMainNote);
        mDb.notesDao().addToRecycleBinNotes(recycleBinNote);

        mDb.notesDao().deleteFromRecycleBinNotes(recycleBinNote);

        mDb.notesDao().getARecycleBinNotes(recycleBinNote.getId())
                .test()
                .assertNoValues();


        // Passing object id to dao
        mDb.notesDao().addToRecycleBinNotes(recycleBinNote);

        mDb.notesDao().deleteFromRecycleBinNotes(recycleBinNote.getId());

        mDb.notesDao().getARecycleBinNotes(recycleBinNote.getId())
                .test()
                .assertNoValues();
    }


    @Test
    public void noNotesPresentInTableWhenAddingToAnother() throws Exception {
        mDb.notesDao().addToUserNotes(mMainNote);

        mDb.notesDao().getAnArchiveNote(mMainNote.getId())
                .test()
                .assertNoValues();

        mDb.notesDao().getARecycleBinNotes(mMainNote.getId())
                .test()
                .assertNoValues();
    }
}
