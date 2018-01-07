package com.ozmar.notes.databaseTests;


import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.NotePreview;
import com.ozmar.notes.database.NotePreviewWithReminderId;
import com.ozmar.notes.database.RecycleBinNote;
import com.ozmar.notes.database.ReminderPreview;

import org.joda.time.DateTime;
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
    private Reminder mReminder;

    private NotePreview mNotePreview;
    private NotePreviewWithReminderId mNotePreviewWithReminderId;
    private NoteAndReminderPreview mNoteAndReminderPreview;


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
        DateTime dateTime = DateTime.now();
        mMainNote = new MainNote(1, title, content, 1, 1, 0, -1);
        mReminder = new Reminder(1, dateTime, null);

        mNotePreview = new NotePreview(1, title, content);
        mNotePreviewWithReminderId = new NotePreviewWithReminderId(1, title, content, -1);
        ReminderPreview mReminderPreview = new ReminderPreview(dateTime.getMillis(), 0);
        mNoteAndReminderPreview = new NoteAndReminderPreview(mNotePreviewWithReminderId, mReminderPreview);

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

    private List<NotePreviewWithReminderId> createMainPreview(List<MainNote> list) {
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

    private List<MainNote> reverseMainList(List<MainNote> list) throws Exception {

        List<MainNote> reversedList = new ArrayList<>();
        for (MainNote note : list) {
            reversedList.add(0, note);
        }

        return reversedList;
    }

    private List<NotePreviewWithReminderId> getPreviewsOfMain(List<MainNote> list) {
        List<NotePreviewWithReminderId> previewList = new ArrayList<>();

        for (MainNote note : list) {
            NotePreviewWithReminderId preview = new NotePreviewWithReminderId(note.getId(),
                    note.getTitle(), note.getContent(), note.getReminderId());
            previewList.add(preview);
        }

        return previewList;
    }

    private List<NoteAndReminderPreview> getNoteAndReminderPreviewMain(List<MainNote> list) {
        List<NotePreviewWithReminderId> notePreviewList = getPreviewsOfMain(list);

        List<NoteAndReminderPreview> noteAndReminderPreviewList = new ArrayList<>();
        for (NotePreviewWithReminderId preview : notePreviewList) {
            NoteAndReminderPreview temp = new NoteAndReminderPreview(preview, null);
            noteAndReminderPreviewList.add(temp);
        }

        return noteAndReminderPreviewList;
    }


    private List<ArchiveNote> createArchiveNoteList() throws Exception {
        List<ArchiveNote> list = new ArrayList<>();
        ArchiveNote note = new ArchiveNote(mMainNote);
        list.add(note);

        for (int i = 2; i < 6; i++) {
            ArchiveNote tempNote = note.clone();
            tempNote.setId(i);
            tempNote.setTitle(tempNote.getTitle() + i);
            tempNote.setContent(tempNote.getContent() + i);
            list.add(tempNote);
        }

        return list;
    }

    private List<NotePreviewWithReminderId> createArchivePreview(List<ArchiveNote> list) {
        List<NotePreviewWithReminderId> previewList = new ArrayList<>();
        for (ArchiveNote note : list) {
            previewList.add(new NotePreviewWithReminderId(note.getId(), note.getTitle(),
                    note.getContent(), note.getReminderId()));
        }

        return previewList;
    }

    private List<ArchiveNote> reverseArchiveList(List<ArchiveNote> list) throws Exception {
        List<ArchiveNote> reversedList = new ArrayList<>();
        for (ArchiveNote note : list) {
            reversedList.add(0, note);
        }

        return reversedList;
    }

    private List<NotePreviewWithReminderId> getPreviewsOfArchive(List<ArchiveNote> list) {
        List<NotePreviewWithReminderId> previewList = new ArrayList<>();

        for (ArchiveNote note : list) {
            NotePreviewWithReminderId preview = new NotePreviewWithReminderId(note.getId(),
                    note.getTitle(), note.getContent(), note.getReminderId());
            previewList.add(preview);
        }

        return previewList;
    }

    private List<NoteAndReminderPreview> getNoteAndReminderPreviewArchive(List<ArchiveNote> list) {
        List<NotePreviewWithReminderId> notePreviewList = getPreviewsOfArchive(list);

        List<NoteAndReminderPreview> noteAndReminderPreviewList = new ArrayList<>();
        for (NotePreviewWithReminderId preview : notePreviewList) {
            NoteAndReminderPreview temp = new NoteAndReminderPreview(preview, null);
            noteAndReminderPreviewList.add(temp);
        }

        return noteAndReminderPreviewList;
    }


    private List<RecycleBinNote> createRecycleBinNoteList() throws Exception {
        List<RecycleBinNote> list = new ArrayList<>();
        RecycleBinNote note = new RecycleBinNote(mMainNote);
        list.add(note);

        for (int i = 2; i < 6; i++) {
            RecycleBinNote tempNote = note.clone();
            tempNote.setId(i);
            tempNote.setTitle(tempNote.getTitle() + i);
            tempNote.setContent(tempNote.getContent() + i);
            list.add(tempNote);
        }

        return list;
    }

    private List<NotePreview> createRecycleBinPreview(List<RecycleBinNote> list) {
        List<NotePreview> previewList = new ArrayList<>();
        for (RecycleBinNote note : list) {
            previewList.add(new NotePreview(note.getId(), note.getTitle(),
                    note.getContent()));
        }

        return previewList;
    }

    private List<RecycleBinNote> reverseRecycleBinList(List<RecycleBinNote> list) throws Exception {
        List<RecycleBinNote> reversedList = new ArrayList<>();
        for (RecycleBinNote note : list) {
            reversedList.add(0, note);
        }

        return reversedList;
    }

    private List<NotePreviewWithReminderId> getPreviewsOfRecycleBin(List<RecycleBinNote> list) {
        List<NotePreviewWithReminderId> previewList = new ArrayList<>();

        for (RecycleBinNote note : list) {
            NotePreviewWithReminderId preview = new NotePreviewWithReminderId(note.getId(),
                    note.getTitle(), note.getContent(), -1);
            previewList.add(preview);
        }

        return previewList;
    }

    private List<NoteAndReminderPreview> getNoteAndReminderPreviewRecycleBin(List<RecycleBinNote> list) {
        List<NotePreviewWithReminderId> notePreviewList = getPreviewsOfRecycleBin(list);

        List<NoteAndReminderPreview> noteAndReminderPreviewList = new ArrayList<>();
        for (NotePreviewWithReminderId preview : notePreviewList) {
            NoteAndReminderPreview temp = new NoteAndReminderPreview(preview, null);
            noteAndReminderPreviewList.add(temp);
        }

        return noteAndReminderPreviewList;
    }


    @Test
    public void getAMainPreview() throws Exception {
        mDb.notesDao().addToUserNotes(mMainNote);
        Assert.assertEquals(mNotePreviewWithReminderId, mDb.previewsDao().getAMainPreview(mMainNote.getId()));
    }

    @Test
    public void getMainPreviewList() throws Exception {
        List<MainNote> mainNoteList = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(mainNoteList);

        mainNoteList = reverseMainList(mainNoteList);
        List<NotePreviewWithReminderId> previewsList = createMainPreview(mainNoteList);
        Assert.assertEquals(previewsList, mDb.previewsDao().getMainPreviewList());
    }

    @Test
    public void getFavoritePreviewList() throws Exception {
        List<MainNote> mainNoteList = createMainNoteList();
        turnNotesToFavorite(mainNoteList);
        mDb.multiSelectDao().addListToMainNotes(mainNoteList);

        mainNoteList = reverseMainList(mainNoteList);
        List<NotePreviewWithReminderId> previewsList = createMainPreview(mainNoteList);
        Assert.assertEquals(previewsList, mDb.previewsDao().getFavoritePreviewList());
    }

    @Test
    public void getAnArchivePreview() throws Exception {
        ArchiveNote note = new ArchiveNote(mMainNote);
        mDb.notesDao().addToArchiveNotes(note);
        Assert.assertEquals(mNotePreviewWithReminderId, mDb.previewsDao().getAnArchivePreview(note.getId()));
    }

    @Test
    public void getArchivePreviewList() throws Exception {
        List<ArchiveNote> archiveNoteList = createArchiveNoteList();
        mDb.multiSelectDao().addListToArchiveNotes(archiveNoteList);

        archiveNoteList = reverseArchiveList(archiveNoteList);
        List<NotePreviewWithReminderId> previewsList = createArchivePreview(archiveNoteList);
        Assert.assertEquals(previewsList, mDb.previewsDao().getArchivePreviewList());
    }

    @Test
    public void getARecycleBinPreview() throws Exception {
        RecycleBinNote note = new RecycleBinNote(mMainNote);
        mDb.notesDao().addToRecycleBinNotes(note);
        Assert.assertEquals(mNotePreview, mDb.previewsDao().getARecycleBinPreview(note.getId()));
    }

    @Test
    public void getRecycleBinPreviewList() throws Exception {
        List<RecycleBinNote> recycleBinNoteList = createRecycleBinNoteList();
        mDb.multiSelectDao().addListToRecycleBinNotes(recycleBinNoteList);

        recycleBinNoteList = reverseRecycleBinList(recycleBinNoteList);
        List<NotePreview> previewsList = createRecycleBinPreview(recycleBinNoteList);
        Assert.assertEquals(previewsList, mDb.previewsDao().getRecycleBinPreviewList());
    }

    @Test
    public void getReminderPreview() throws Exception {
        Reminder reminder = new Reminder(1, DateTime.now(), null);
        ReminderPreview reminderPreview = new ReminderPreview(reminder.getDateTime().getMillis(), 0);

        mDb.remindersDao().addReminder(reminder);

        Assert.assertEquals(reminderPreview, mDb.previewsDao().getReminderPreview(reminder.getId()));
    }


    @Test
    public void getANoteAndReminderPreviewOfMain() throws Exception {
        mMainNote.setReminderId(1);
        mNotePreviewWithReminderId.setReminderId(1);
        mDb.notesDao().addToUserNotes(mMainNote);
        mDb.remindersDao().addReminder(mReminder);

        Assert.assertEquals(mNoteAndReminderPreview, mDb.previewsDao().getANotePreview(mMainNote.getId(), 0));
    }

    @Test
    public void getANoteAnReminderPreviewOfArchive() throws Exception {
        ArchiveNote note = new ArchiveNote(mMainNote);
        note.setReminderId(1);
        mNotePreviewWithReminderId.setReminderId(1);
        mDb.notesDao().addToArchiveNotes(note);
        mDb.remindersDao().addReminder(mReminder);
        Assert.assertEquals(mNoteAndReminderPreview, mDb.previewsDao().getANotePreview(note.getId(), 2));
    }

    @Test
    public void getANoteAndReminderPreviewOfRecycleBin() throws Exception {
        RecycleBinNote note = new RecycleBinNote(mMainNote);
        mDb.notesDao().addToRecycleBinNotes(note);
        mNoteAndReminderPreview.setReminderPreview(null);
        Assert.assertEquals(mNoteAndReminderPreview, mDb.previewsDao().getANotePreview(note.getId(), 3));
    }

    @Test
    public void getListOfNotePreviewsMain() throws Exception {
        List<MainNote> list = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(list);

        list = reverseMainList(list);
        List<NoteAndReminderPreview> previewList = getNoteAndReminderPreviewMain(list);

        Assert.assertEquals(previewList, mDb.previewsDao().getListOfNotePreviews(0));
    }

    @Test
    public void getListOfNotePreviewArchive() throws Exception {
        List<ArchiveNote> list = createArchiveNoteList();
        mDb.multiSelectDao().addListToArchiveNotes(list);

        list = reverseArchiveList(list);
        List<NoteAndReminderPreview> previewList = getNoteAndReminderPreviewArchive(list);

        Assert.assertEquals(previewList, mDb.previewsDao().getListOfNotePreviews(2));
    }

    @Test
    public void getListOfNotePreviewRecycleBin() throws Exception {
        List<RecycleBinNote> list = createRecycleBinNoteList();
        mDb.multiSelectDao().addListToRecycleBinNotes(list);

        list = reverseRecycleBinList(list);
        List<NoteAndReminderPreview> previewList = getNoteAndReminderPreviewRecycleBin(list);

        Assert.assertEquals(previewList, mDb.previewsDao().getListOfNotePreviews(3));
    }
}
