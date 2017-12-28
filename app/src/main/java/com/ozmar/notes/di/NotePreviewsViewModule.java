package com.ozmar.notes.di;


import com.ozmar.notes.NotePreviewsActivity;
import com.ozmar.notes.NotePreviewsView;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class NotePreviewsViewModule {
    @Binds
    abstract NotePreviewsView provideNotePreviewsView(NotePreviewsActivity notePreviewsActivity);
}
