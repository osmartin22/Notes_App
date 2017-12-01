package com.ozmar.notes.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface NotesDao {

    //---------------------------------------------------------------------------------------//
    // User Notes Table Specific Methods
    //---------------------------------------------------------------------------------------//
    @Query("SELECT * FROM userNotes")
    List<MainNote> getAllUserNotes();

    @Query("SELECT * FROM userNotes WHERE favorite = 1")
    List<MainNote> getAllFavoriteNotes();

    @Query("SELECT * FROM userNotes WHERE id = :noteId")
    MainNote getAUserNote(int noteId);

    @Update
    void updateAUserNote(MainNote note);

    @Insert
    long addToUserNotes(MainNote note);

    @Delete
    void deleteFromUserNotes(MainNote note);

    @Query("DELETE FROM userNotes WHERE id = :noteId")
    void deleteFromUserNotes(int noteId);

    @Insert
    void addListToUserNotes(List<MainNote> list);

    @Delete
    void deleteListFromUserNotes(List<MainNote> list);


    //---------------------------------------------------------------------------------------//
    // Archive Table Specific Methods
    //---------------------------------------------------------------------------------------//

    @Query("SELECT * FROM archiveNotes")
    List<ArchiveNote> getAllArchiveNotes();

    @Query("SELECT * FROM archiveNotes WHERE id = :noteId")
    ArchiveNote getAnArchiveNote(int noteId);

    @Update
    void updateAnArchiveNote(ArchiveNote note);

    @Insert
    long addToArchiveNotes(ArchiveNote note);

    @Delete
    void deleteFromArchiveNotes(ArchiveNote note);

    @Query("DELETE FROM archiveNotes WHERE id = :noteId")
    void deleteFromArchiveNotes(int noteId);

    @Insert
    void addListToArchiveNotes(List<ArchiveNote> list);

    @Delete
    void deleteListFromArchiveNotes(List<ArchiveNote> list);


    //---------------------------------------------------------------------------------------//
    // Recycle Bin Table Specific Methods
    //---------------------------------------------------------------------------------------//

    @Query("SELECT * FROM recycleBinNotes")
    List<RecycleBinNote> getAllRecycleBinNotes();

    @Query("SELECT * FROM recycleBinNotes WHERE id = :noteId")
    RecycleBinNote getARecycleBinNotes(int noteId);

    @Insert
    long addToRecycleBinNotes(RecycleBinNote note);

    @Delete
    void deleteFromRecycleBinNotes(RecycleBinNote note);

    @Query("DELETE FROM recycleBinNotes WHERE id = :noteId")
    void deleteFromRecycleBinNotes(int noteId);


    @Insert
    void addListToRecycleBinNotes(List<RecycleBinNote> list);

    @Delete
    void deleteListFromRecycleBinNotes(List<RecycleBinNote> list);

    // TODO: Add query to delete notes too long in trash

}
