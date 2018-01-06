package com.ozmar.notes.databaseTests;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.RecycleBinNote;

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

import io.reactivex.functions.Consumer;


@RunWith(AndroidJUnit4.class)
public class MultiSelectDaoTests {

    private AppDatabase mDb;

    private MainNote mMainNote;
    private Reminder mReminder;


    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        mMainNote = new MainNote(1, "Title", "Content", 1, 1, 0, -1);
        mReminder = new Reminder(1, DateTime.now(), new FrequencyChoices(0, null));
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

    private List<Integer> getMainNoteIdsList(List<MainNote> list) throws Exception {
        List<Integer> idsList = new ArrayList<>();
        for (MainNote note : list) {
            idsList.add(note.getId());
        }
        Log.d("List", idsList.toString());
        return idsList;
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

    private List<Integer> getArchiveNoteIdsList(List<ArchiveNote> list) throws Exception {
        List<Integer> idsList = new ArrayList<>();
        for (ArchiveNote note : list) {
            idsList.add(note.getId());
        }
        return idsList;
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

    private List<Integer> getRecycleBinNoteIdsList(List<RecycleBinNote> list) throws Exception {
        List<Integer> idsList = new ArrayList<>();
        for (RecycleBinNote note : list) {
            idsList.add(note.getId());
        }
        return idsList;
    }

    private List<Reminder> createReminderList() throws Exception {
        List<Reminder> list = new ArrayList<>();
        list.add(mReminder);

        for (int i = 2; i < 6; i++) {
            Reminder tempReminder = mReminder.clone();
            tempReminder.setId(i);
            list.add(tempReminder);
        }

        return list;
    }


    @Test
    public void deleteRemindersList() throws Exception {
        List<Reminder> reminderList = createReminderList();
        for (Reminder reminder : reminderList) {
            mDb.remindersDao().addReminder(reminder);
        }

        List<Integer> reminderIdsList = new ArrayList<>();
        for (Reminder reminder : reminderList) {
            reminderIdsList.add(reminder.getId());
            Assert.assertEquals(reminder, mDb.remindersDao().getReminder(reminder.getId()));
        }

        mDb.multiSelectDao().deleteReminders(reminderIdsList);

        for (Reminder reminder : reminderList) {
            Assert.assertNull(mDb.remindersDao().getReminder(reminder.getId()));
        }
    }


    @Test
    public void addToMainNotesList() throws Exception {
        List<MainNote> list = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(list);

        for (MainNote mainNote : list) {
            mDb.notesDao().getAUserNote(mainNote.getId())
                    .test()
                    .assertValue(note -> note.equals(mainNote));
        }
    }

    @Test
    public void deleteFromMainNotesList() throws Exception {
        List<MainNote> list = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(list);

        mDb.multiSelectDao().deleteMainNotes(getMainNoteIdsList(list));

        for (MainNote note : list) {
            mDb.notesDao().getAUserNote(note.getId())
                    .test()
                    .assertNoValues();
        }
    }

    @Test
    public void getMainNotesList() throws Exception {
        List<MainNote> list = createMainNoteList();
        mDb.multiSelectDao().addListToMainNotes(list);

        List<MainNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getMainNotes(getMainNoteIdsList(list))
                .subscribe((Consumer<List<MainNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(list.get(i), databaseList.get(i));
        }
    }


    @Test
    public void addToArchiveNotesList() throws Exception {
        List<ArchiveNote> list = createArchiveNoteList();
        mDb.multiSelectDao().addListToArchiveNotes(list);

        for (ArchiveNote archiveNote : list) {
            mDb.notesDao().getAnArchiveNote(archiveNote.getId())
                    .test()
                    .assertValue(note -> note.equals(archiveNote));
        }
    }

    @Test
    public void deleteFromArchiveNotesList() throws Exception {
        List<ArchiveNote> list = createArchiveNoteList();
        mDb.multiSelectDao().addListToArchiveNotes(list);

        mDb.multiSelectDao().deleteMainNotes(getArchiveNoteIdsList(list));

        for (ArchiveNote note : list) {
            mDb.notesDao().getAUserNote(note.getId())
                    .test()
                    .assertNoValues();
        }
    }

    @Test
    public void getArchiveNotesList() throws Exception {
        List<ArchiveNote> list = createArchiveNoteList();
        mDb.multiSelectDao().addListToArchiveNotes(list);

        List<ArchiveNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getArchiveNotes(getArchiveNoteIdsList(list))
                .subscribe((Consumer<List<ArchiveNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(list.get(i), databaseList.get(i));
        }
    }


    @Test
    public void addToRecycleBinNotesList() throws Exception {
        List<RecycleBinNote> list = createRecycleBinNoteList();
        mDb.multiSelectDao().addListToRecycleBinNotes(list);

        for (RecycleBinNote recycleBinNote : list) {
            mDb.notesDao().getARecycleBinNotes(recycleBinNote.getId())
                    .test()
                    .assertValue(note -> note.equals(recycleBinNote));
        }
    }

    @Test
    public void deleteFromRecycleBinNotesList() throws Exception {
        List<RecycleBinNote> list = createRecycleBinNoteList();
        mDb.multiSelectDao().addListToRecycleBinNotes(list);

        mDb.multiSelectDao().deleteRecycleBinNotes(getRecycleBinNoteIdsList(list));

        for (RecycleBinNote note : list) {
            mDb.notesDao().getAUserNote(note.getId())
                    .test()
                    .assertNoValues();
        }
    }

    @Test
    public void getRecycleBinNotesList() throws Exception {
        List<RecycleBinNote> list = createRecycleBinNoteList();
        mDb.multiSelectDao().addListToRecycleBinNotes(list);

        List<RecycleBinNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getRecycleBinNotes(getRecycleBinNoteIdsList(list))
                .subscribe((Consumer<List<RecycleBinNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(list.get(i), databaseList.get(i));
        }
    }


    @Test
    public void addArchiveListToMainNote() throws Exception {
        List<ArchiveNote> archiveNoteList = createArchiveNoteList();

        // Pass a copy of the same list since this transaction will modify the passed list
        mDb.multiSelectDao().addArchiveListToMainNote(createArchiveNoteList());

        List<MainNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getMainNotes(getArchiveNoteIdsList(archiveNoteList))
                .subscribe((Consumer<List<MainNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(new MainNote(archiveNoteList.get(i)), databaseList.get(i));
        }
    }

    @Test
    public void addRecycleBinListToMainNote() throws Exception {
        List<RecycleBinNote> recycleBinNoteList = createRecycleBinNoteList();

        // Pass a copy of the same list since this transaction will modify the passed list
        mDb.multiSelectDao().addRecycleBinListToMainNote(createRecycleBinNoteList());

        List<MainNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getMainNotes(getRecycleBinNoteIdsList(recycleBinNoteList))
                .subscribe((Consumer<List<MainNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(new MainNote(recycleBinNoteList.get(i)), databaseList.get(i));
        }
    }


    @Test
    public void addMainListToArchive() throws Exception {
        List<MainNote> mainNoteList = createMainNoteList();

        // Pass a copy of the same list since this transaction will modify the passed list
        mDb.multiSelectDao().addMainListToArchive(createMainNoteList());

        List<ArchiveNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getArchiveNotes(getMainNoteIdsList(mainNoteList))
                .subscribe((Consumer<List<ArchiveNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(new ArchiveNote(mainNoteList.get(i)), databaseList.get(i));
        }
    }


    @Test
    public void addMainListToRecycleBin() throws Exception {
        List<MainNote> mainNoteList = createMainNoteList();

        // Pass a copy of the same list since this transaction will modify the passed list
        mDb.multiSelectDao().addMainListToRecycleBin(createMainNoteList());

        List<RecycleBinNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getRecycleBinNotes(getMainNoteIdsList(mainNoteList))
                .subscribe((Consumer<List<RecycleBinNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(new RecycleBinNote(mainNoteList.get(i)), databaseList.get(i));
        }
    }

    @Test
    public void addArchiveListToRecycleBin() throws Exception {
        List<ArchiveNote> archiveNoteList = createArchiveNoteList();

        // Pass a copy of the same list since this transaction will modify the passed list
        mDb.multiSelectDao().addArchiveListToRecycleBin(createArchiveNoteList());

        List<RecycleBinNote> databaseList = new ArrayList<>();
        mDb.multiSelectDao().getRecycleBinNotes(getArchiveNoteIdsList(archiveNoteList))
                .subscribe((Consumer<List<RecycleBinNote>>) databaseList::addAll);

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(new RecycleBinNote(archiveNoteList.get(i)), databaseList.get(i));
        }
    }
}

