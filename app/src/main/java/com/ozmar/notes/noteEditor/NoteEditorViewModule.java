package com.ozmar.notes.noteEditor;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class NoteEditorViewModule {
    @Binds
    abstract NoteEditorView provideNoteEditorView(NoteEditorActivity noteEditorActivity);
}
