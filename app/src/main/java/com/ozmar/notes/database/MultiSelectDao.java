package com.ozmar.notes.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class MultiSelectDao {

    @Query("DELETE FROM remindersTable WHERE reminderId IN(:reminderId)")
    public abstract void deleteReminders(List<Integer> reminderId);


    @Query("SELECT * FROM userNotes WHERE id IN(:noteIds)")
    public abstract Single<List<MainNote>> getMainNotes(List<Integer> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addListToMainNotes(List<MainNote> list);

    @Query("DELETE FROM userNotes WHERE id IN(:noteIds)")
    public abstract void deleteMainNotes(List<Integer> noteIds);


    @Query("SELECT * FROM archiveNotes WHERE id IN(:noteIds)")
    public abstract Single<List<ArchiveNote>> getArchiveNotes(List<Integer> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addListToArchiveNotes(List<ArchiveNote> list);

    @Query("DELETE FROM archiveNotes WHERE id IN(:noteIds)")
    public abstract void deleteArchiveNotes(List<Integer> noteIds);


    @Query("SELECT * FROM recycleBinNotes WHERE id IN(:noteIds)")
    public abstract Single<List<RecycleBinNote>> getRecycleBinNotes(List<Integer> noteIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addListToRecycleBinNotes(List<RecycleBinNote> list);

    @Query("DELETE FROM recycleBinNotes WHERE id IN(:noteIds)")
    public abstract void deleteRecycleBinNotes(List<Integer> noteIds);


//    //---------------------------------------------------------------------------------------//
//    // Delete from reminders table transactions
//    //---------------------------------------------------------------------------------------//
//
//    @Transaction
//    public void deleteRemindersListInMain(List<MainNote> list) {
//        List<Integer> reminderIds = new ArrayList<>();
//        for (MainNote note : list) {
//            if (note.getReminderId() != -1) {
//                reminderIds.add(note.getReminderId());
//            }
//        }
//
//        deleteReminders(reminderIds);
//    }
//
//    @Transaction
//    public void deleteRemindersListInArchive(List<ArchiveNote> list) {
//        List<Integer> reminderIds = new ArrayList<>();
//        for (ArchiveNote note : list) {
//            if (note.getReminderId() != -1) {
//                reminderIds.add(note.getReminderId());
//            }
//        }
//
//        deleteReminders(reminderIds);
//    }


    //---------------------------------------------------------------------------------------//
    // Add to MainNote table transactions
    //---------------------------------------------------------------------------------------//

    @Transaction
    public void addArchiveListToMainNote(List<ArchiveNote> list) {
        List<MainNote> mainNoteList = new ArrayList<>();
        for (ArchiveNote note : list) {
            mainNoteList.add(new MainNote(note));
        }

        addListToMainNotes(mainNoteList);
    }

    @Transaction
    public void addRecycleBinListToMainNote(List<RecycleBinNote> list) {
        List<MainNote> mainNoteList = new ArrayList<>();
        for (RecycleBinNote note : list) {
            mainNoteList.add(new MainNote(note));
        }

        addListToMainNotes(mainNoteList);
    }


    //---------------------------------------------------------------------------------------//
    // Add to ArchiveNote table transactions
    //---------------------------------------------------------------------------------------//

    @Transaction
    public void addMainListToArchive(List<MainNote> list) {

        List<ArchiveNote> archiveNoteList = new ArrayList<>();
        for (MainNote note : list) {
            archiveNoteList.add(new ArchiveNote(note));
        }

        addListToArchiveNotes(archiveNoteList);
    }


    //---------------------------------------------------------------------------------------//
    // Add to RecycleBin table transactions
    //---------------------------------------------------------------------------------------//

    @Transaction
    public void addMainListToRecycleBin(List<MainNote> list) {
        List<RecycleBinNote> recycleBinNoteList = new ArrayList<>();
        for (MainNote note : list) {
            recycleBinNoteList.add(new RecycleBinNote(note));
        }

        addListToRecycleBinNotes(recycleBinNoteList);
    }

    @Transaction
    public void addArchiveListToRecycleBin(List<ArchiveNote> list) {
        List<RecycleBinNote> recycleBinNoteList = new ArrayList<>();
        for (ArchiveNote note : list) {
            recycleBinNoteList.add(new RecycleBinNote(note));
        }

        addListToRecycleBinNotes(recycleBinNoteList);
    }
}
