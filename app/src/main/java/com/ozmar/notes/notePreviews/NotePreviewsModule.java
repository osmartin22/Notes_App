package com.ozmar.notes.notePreviews;

import com.ozmar.notes.database.AppDatabase;

import dagger.Module;
import dagger.Provides;


@Module
public class NotePreviewsModule {

    @Provides
    NotePreviewsInteractor provideNotePreviewsInteractor(AppDatabase database) {
        return new NotePreviewsInteractor(database);
    }

    @Provides
    NotePreviewsPresenter provideNotePreviewsPresenter(NotePreviewsView notePreviewsView,
                                                     NotePreviewsInteractor notePreviewsInteractor) {
        return new NotePreviewsPresenter(notePreviewsView, notePreviewsInteractor);
    }
}
