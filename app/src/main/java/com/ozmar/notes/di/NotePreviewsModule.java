package com.ozmar.notes.di;

import com.ozmar.notes.NotePreviewsInteractor;
import com.ozmar.notes.NotePreviewsPresenter;
import com.ozmar.notes.NotePreviewsView;
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
