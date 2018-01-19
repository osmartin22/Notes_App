package com.ozmar.notes.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;


@Dao
public interface NotesDao {

    //---------------------------------------------------------------------------------------//
    // User Notes Table Specific Methods
    //---------------------------------------------------------------------------------------//

    @Query("SELECT * FROM userNotes WHERE id = :noteId")
    Maybe<MainNote> getAUserNote(int noteId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAUserNote(MainNote note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addToUserNotes(MainNote note);

    @Delete
    void deleteFromUserNotes(MainNote note);

    @Query("DELETE FROM userNotes WHERE id = :noteId")
    void deleteFromUserNotes(int noteId);

    //---------------------------------------------------------------------------------------//
    // Archive Table Specific Methods
    //---------------------------------------------------------------------------------------//

    @Query("SELECT * FROM archiveNotes WHERE id = :noteId")
    Maybe<ArchiveNote> getAnArchiveNote(int noteId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAnArchiveNote(ArchiveNote note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addToArchiveNotes(ArchiveNote note);

    @Delete
    void deleteFromArchiveNotes(ArchiveNote note);

    @Query("DELETE FROM archiveNotes WHERE id = :noteId")
    void deleteFromArchiveNotes(int noteId);

    //---------------------------------------------------------------------------------------//
    // Recycle Bin Table Specific Methods
    //---------------------------------------------------------------------------------------//

    @Query("SELECT * FROM recycleBinNotes WHERE id = :noteId")
    Maybe<RecycleBinNote> getARecycleBinNotes(int noteId);

    @Query("SELECT * FROM recycleBinNotes")
    Single<List<RecycleBinNote>> getAllRecycleBinNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addToRecycleBinNotes(RecycleBinNote note);

    @Delete
    void deleteFromRecycleBinNotes(RecycleBinNote note);

    @Query("DELETE FROM recycleBinNotes WHERE id = :noteId")
    void deleteFromRecycleBinNotes(int noteId);


}
