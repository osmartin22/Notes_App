package com.ozmar.notes.di;


import com.ozmar.notes.NotePreviewsActivity;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.noteEditor.NoteEditorModule;
import com.ozmar.notes.noteEditor.NoteEditorViewModule;
import com.ozmar.notes.notifications.ReminderReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector(modules = {NotePreviewsViewModule.class, NotePreviewsModule.class})
    abstract NotePreviewsActivity bindNotePreviewsActivity();

    @ContributesAndroidInjector(modules = {NoteEditorViewModule.class, NoteEditorModule.class})
    abstract NoteEditorActivity bindNoteEditorActivity();

    @ContributesAndroidInjector
    abstract ReminderReceiver bindReminderReceiver();

}
