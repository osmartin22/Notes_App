package com.ozmar.notes.di;


import com.ozmar.notes.NotePreviewsActivity;
import com.ozmar.notes.noteEditor.NoteEditorActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract NotePreviewsActivity bindNotePreviewsActivity();

    @ContributesAndroidInjector (modules = {NoteEditorViewModule.class, NoteEditorModule.class})
    abstract NoteEditorActivity bindNoteEditorActivity();

}
