package com.ozmar.notes.di;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.noteEditor.NoteEditorInteractor;
import com.ozmar.notes.noteEditor.NoteEditorPresenter;
import com.ozmar.notes.noteEditor.NoteEditorView;

import dagger.Module;
import dagger.Provides;


@Module
public class NoteEditorModule {

    @Provides
    NoteEditorInteractor provideNoteEditorInteractor(AppDatabase database) {
        return new NoteEditorInteractor(database);
    }

    @Provides
    NoteEditorPresenter provideNoteEditorPresenter(NoteEditorView noteEditorView, NoteEditorInteractor noteEditorInteractor){
        return new NoteEditorPresenter(noteEditorView, noteEditorInteractor);
    }


}
