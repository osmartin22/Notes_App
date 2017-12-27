package com.ozmar.notes;

import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.noteEditor.NoteEditorInteractor;
import com.ozmar.notes.noteEditor.NoteEditorPresenter;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Maybe;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
//@Config(constants = BuildConfig.class)
public class ExampleUnitTest {

    @Mock
    private NoteEditorActivity mEditorActivity;

    @Mock
    private NoteEditorInteractor mEditorInteractor;

    private NoteEditorPresenter mEditorPresenter;

    private MainNote mNote;
    private Reminder mReminder;

    @Before
    public void setUpNoteEditorPresenter() {
        mEditorPresenter = new NoteEditorPresenter(mEditorActivity);
        mNote = new MainNote(1, "Test Title", "Test Content", 1, 1, 0, -1);
        mReminder = new Reminder(2, new DateTime(2017, 12, 22, 11, 30), null);
        doReturn(Maybe.just(mNote)).when(mEditorInteractor).getNote(1,0);
    }

    @Test
    public void initialize_IsCorrect() {
        mEditorPresenter.initialize(1, 0);

        Assert.assertEquals(mEditorPresenter.getNote().getTitle(), mNote.getTitle());

    }
}