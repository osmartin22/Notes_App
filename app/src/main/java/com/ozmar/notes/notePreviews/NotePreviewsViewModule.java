package com.ozmar.notes.notePreviews;


import com.ozmar.notes.notePreviews.NotePreviewsActivity;
import com.ozmar.notes.notePreviews.NotePreviewsView;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class NotePreviewsViewModule {
    @Binds
    abstract NotePreviewsView provideNotePreviewsView(NotePreviewsActivity notePreviewsActivity);
}
