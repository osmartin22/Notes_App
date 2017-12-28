package com.ozmar.notes.noteEditor;

import com.ozmar.notes.database.AppDatabase;

import dagger.Module;
import dagger.Provides;


@Module
public class NoteEditorModule {

    @Provides
    NoteEditorInteractor provideNoteEditorInteractor(AppDatabase database) {
        return new NoteEditorInteractor(database);
    }

    @Provides
    NoteEditorPresenter provideNoteEditorPresenter(NoteEditorView noteEditorView,
                                                   NoteEditorInteractor noteEditorInteractor){
        return new NoteEditorPresenter(noteEditorView, noteEditorInteractor);
    }


}
