package com.ozmar.notes.databaseTests;


import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NotePreview;
import com.ozmar.notes.database.NotePreviewWithReminderId;
import com.ozmar.notes.database.ReminderPreview;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class NotePreviewsDaoTests {
    private AppDatabase mDb;

    private MainNote mMainNote;

    private NotePreview mNotePreview;
    private ReminderPreview mReminderPreview;
    private NotePreviewWithReminderId mNotePreviewWithReminderId;


    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        String title = "Title";
        String content = "Content";
        mMainNote = new MainNote(1, title, content, 1, 1, 0, -1);
        mNotePreview = new NotePreview(1, title, content);
        mReminderPreview = new ReminderPreview(1, 0);
        mNotePreviewWithReminderId = new NotePreviewWithReminderId(1, title, content, -1);

    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    private List<MainNote> createMainNoteList() throws Exception {
        List<MainNote> list = new ArrayList<>();
        list.add(mMainNote.clone());

        for (int i = 2; i < 6; i++) {
            MainNote tempNote = mMainNote.clone();
            tempNote.setId(i);
            tempNote.setTitle(tempNote.getTitle() + i);
            tempNote.setContent(tempNote.getContent() + i);
            list.add(tempNote);
        }

        return list;
    }

    private List<NotePreviewWithReminderId> createPreviewsWithReminderId(List<MainNote> list) {
        List<NotePreviewWithReminderId> previewList = new ArrayList<>();
        for (MainNote note : list) {
            previewList.add(new NotePreviewWithReminderId(note.getId(), note.getTitle(),
                    note.getContent(), note.getReminderId()));
        }

        return previewList;
    }

    private void turnNotesToFavorite(List<MainNote> list) throws Exception {
        for (MainNote note : list) {
            note.setFavorite(1);
        }
    }


    @Test
    public void getAMainNote() throws Exception {
        mDb.notesDao().addToUserNotes(mMainNote);
        Assert.assertEquals(mNotePreviewWithReminderId, mDb.previewsDao().getAMainPreview(mMainNote.getId()));
    }

    @Test
    public void getMainPreviewList() throws Exception {
        List<MainNote> mainNoteList = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(mainNoteList);

        List<NotePreviewWithReminderId> previewsList = createPreviewsWithReminderId(mainNoteList);

        Assert.assertEquals(previewsList, mDb.previewsDao().getMainPreviewList());
    }

    @Test
    public void getFavoritePreviewList() throws Exception {

    }
}
