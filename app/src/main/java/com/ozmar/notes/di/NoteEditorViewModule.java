package com.ozmar.notes.di;

import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.noteEditor.NoteEditorView;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class NoteEditorViewModule {
    @Binds
    abstract NoteEditorView provideNoteEditorView(NoteEditorActivity noteEditorActivity);
}
