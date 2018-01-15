package com.ozmar.notes.noteEditorTests;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ozmar.notes.BuildConfig;
import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.noteEditor.NoteEditorPresenter;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NoteEditorActivityTests {

    @Mock
    private NoteEditorPresenter mEditorPresenter;

    private NoteEditorActivity mActivity;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    private Intent createStartingIntent(int id, int listUsed) throws Exception {
        Context context = RuntimeEnvironment.application;
        Intent startingIntent = new Intent(context, NoteEditorActivity.class);
        startingIntent.putExtra(context.getString(R.string.noteIdIntent), id);
        startingIntent.putExtra(context.getString(R.string.listUsedIntent), listUsed);

        return startingIntent;
    }

    private void buildActivity(int id, int listUsed) throws Exception {
        Intent intent = createStartingIntent(id, listUsed);
        mActivity = Robolectric.buildActivity(NoteEditorActivity.class, intent).create().visible().get();
        mActivity.noteEditorPresenter = mEditorPresenter;
    }

    private Menu getOptionsMenu() throws Exception {
        Toolbar toolbar = mActivity.findViewById(R.id.my_toolbar);

        ShadowActivity shadowActivity = shadowOf(mActivity);
        shadowActivity.onCreateOptionsMenu(toolbar.getMenu());

        assertTrue(shadowActivity.getOptionsMenu().hasVisibleItems());
        return shadowActivity.getOptionsMenu();
    }

    private Reminder createReminder() throws Exception {
        return new Reminder(1, DateTime.now(), null);
    }

    private Reminder createRepeatReminder() throws Exception {
        FrequencyChoices choices = new FrequencyChoices(0, null);
        return new Reminder(1, DateTime.now(), choices);
    }

    private MainNote createNote() throws Exception {
        return new MainNote(1, "Title", "Content", 2, 3, 0, -1);
    }


    @Test
    public void activityNotNull() throws Exception {
        buildActivity(-1, 0);
        assertNotNull(mActivity);
    }

    @Test
    public void viewsAndWidgetsNotNull() throws Exception {
        buildActivity(-1, 0);

        String emptyString = "";

        Toolbar toolbar = mActivity.findViewById(R.id.my_toolbar);
        assertNotNull(toolbar);

        EditText title = mActivity.findViewById(R.id.editTextTitle);
        assertEquals(emptyString, title.getText().toString());
        assertEquals(View.VISIBLE, title.getVisibility());

        EditText content = mActivity.findViewById(R.id.editTextContent);
        assertEquals(emptyString, content.getText().toString());
        assertEquals(View.VISIBLE, content.getVisibility());

        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        assertEquals(emptyString, reminderText.getText().toString());
        assertEquals(View.INVISIBLE, reminderText.getVisibility());

        TextView lastModified = mActivity.findViewById(R.id.lastModified);
        assertEquals(emptyString, lastModified.getText().toString());
        assertEquals(View.VISIBLE, lastModified.getVisibility());
    }

    @Test
    public void optionsMenuNotNull() throws Exception {
        buildActivity(-1, 0);
        Menu menu = getOptionsMenu();
        assertNotNull(menu);

        assertNotNull(menu.findItem(R.id.favorite_note));
        assertNotNull(menu.findItem(R.id.archive_note));
        assertNotNull(menu.findItem(R.id.unarchive_note));
        assertNotNull(menu.findItem(R.id.delete_note));
        assertNotNull(menu.findItem(R.id.delete_note_forever));
        assertNotNull(menu.findItem(R.id.restore_note));
    }

    @Test
    public void menuOptionsMainListVisibility() throws Exception {
        buildActivity(-1, 0);

        Menu menu = getOptionsMenu();
        assertNotNull(menu);

        assertTrue(menu.findItem(R.id.favorite_note).isVisible());
        assertTrue(menu.findItem(R.id.archive_note).isVisible());
        assertTrue(menu.findItem(R.id.delete_note).isVisible());

        assertFalse(menu.findItem(R.id.delete_note_forever).isVisible());
        assertFalse(menu.findItem(R.id.restore_note).isVisible());
        assertFalse(menu.findItem(R.id.unarchive_note).isVisible());
    }

    @Test
    public void menuOptionsArchiveListVisibility() throws Exception {
        buildActivity(-1, 2);

        Menu menu = getOptionsMenu();
        assertNotNull(menu);

        assertTrue(menu.findItem(R.id.favorite_note).isVisible());
        assertTrue(menu.findItem(R.id.unarchive_note).isVisible());
        assertTrue(menu.findItem(R.id.delete_note).isVisible());

        assertFalse(menu.findItem(R.id.delete_note_forever).isVisible());
        assertFalse(menu.findItem(R.id.restore_note).isVisible());
        assertFalse(menu.findItem(R.id.archive_note).isVisible());
    }

    @Test
    public void menuOptionsRecycleBinListVisibility() throws Exception {
        buildActivity(-1, 3);

        Menu menu = getOptionsMenu();
        assertNotNull(menu);

        assertTrue(menu.findItem(R.id.delete_note_forever).isVisible());
        assertTrue(menu.findItem(R.id.restore_note).isVisible());

        assertFalse(menu.findItem(R.id.favorite_note).isVisible());
        assertFalse(menu.findItem(R.id.unarchive_note).isVisible());
        assertFalse(menu.findItem(R.id.delete_note).isVisible());
        assertFalse(menu.findItem(R.id.archive_note).isVisible());
    }

    @Test
    public void reminderFragmentCreated() throws Exception {
        buildActivity(-1, 0);

        when(mEditorPresenter.getReminder()).thenReturn(createReminder());
        mActivity.addReminder(null);

        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        assertNotNull(fragmentManager.findFragmentByTag("reminder_dialog_layout"));
    }

    @Test
    public void onBackPressedCalled() throws Exception {
        buildActivity(-1, 0);
        doNothing().when(mEditorPresenter).onSaveNote(anyString(), anyString());
        mActivity.onBackPressed();
        verify(mEditorPresenter, times(1)).onSaveNote(anyString(), anyString());
    }

    @Test
    public void onReminderPicked() throws Exception {
        buildActivity(-1, 0);

        doNothing().when(mEditorPresenter).onReminderPicked(any(Reminder.class), anyString());
        mActivity.onReminderPicked(createReminder());
        verify(mEditorPresenter, times(1)).onReminderPicked(any(Reminder.class), anyString());
    }

    @Test
    public void onReminderDeleted() throws Exception {
        buildActivity(-1, 0);

        doNothing().when(mEditorPresenter).onReminderDeleted();
        mActivity.onReminderDelete();
        verify(mEditorPresenter, times(1)).onReminderDeleted();
    }

    @Test
    public void contentEdiTextRequestingFocus() throws Exception {
        buildActivity(-1, 0);

        EditText content = mActivity.findViewById(R.id.editTextContent);
        mActivity.requestFocusOnContent();
        assertTrue(content.hasFocus());
    }

    @Test
    public void editTextsSetUpCorrectly() throws Exception {
        buildActivity(-1, 0);

        EditText title = mActivity.findViewById(R.id.editTextTitle);
        EditText content = mActivity.findViewById(R.id.editTextContent);
        TextView lastModified = mActivity.findViewById(R.id.lastModified);

        MainNote note = createNote();
        mActivity.setupNoteEditTexts(note);

        assertEquals(note.getTitle(), title.getText().toString());
        assertFalse(title.hasFocus());

        assertEquals(note.getContent(), content.getText().toString());
        assertFalse(content.hasFocus());

        String expectedReminderText = FormatUtils.lastUpdated(mActivity.getApplicationContext(), note.getTimeModified());
        assertEquals(expectedReminderText, lastModified.getText().toString());
    }

    @Test
    public void showReminderText_NoRepeat() throws Exception {
        buildActivity(-1, 0);

        Reminder reminder = createReminder();
        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        String text = FormatUtils.getReminderText(mActivity.getApplicationContext(), reminder.getDateTime());

        mActivity.showReminder(reminder);
        assertEquals(View.VISIBLE, reminderText.getVisibility());
        assertEquals(text, reminderText.getText().toString());

        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_reminder_dark_gray_small);
        Bitmap actualBitMap = ((BitmapDrawable) reminderText.getCompoundDrawables()[0]).getBitmap();

        assert expectedDrawable != null;
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();

        assertEquals(expectedBitMap.toString(), actualBitMap.toString());
    }

    @Test
    public void showReminderText_WithRepeat() throws Exception {
        buildActivity(-1, 0);

        Reminder reminder = createRepeatReminder();
        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        String text = FormatUtils.getReminderText(mActivity.getApplicationContext(), reminder.getDateTime());

        mActivity.showReminder(reminder);
        assertEquals(View.VISIBLE, reminderText.getVisibility());
        assertEquals(text, reminderText.getText().toString());

        Drawable[] actualDrawable = reminderText.getCompoundDrawables();
        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_repeat_dark_gray_small);

        Bitmap actualBitMap = ((BitmapDrawable) actualDrawable[0]).getBitmap();
        //noinspection ConstantConditions
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();
        assertEquals(expectedBitMap.toString(), actualBitMap.toString());
    }

    @Test
    public void updateFavorite_setTrue() throws Exception {
        buildActivity(-1, 0);

        Menu menu = getOptionsMenu();
        MenuItem favorite = menu.findItem(R.id.favorite_note);

        mActivity.updateFavoriteIcon(true);

        Drawable expectedFavorite = mActivity.getDrawable(R.drawable.ic_favorite_star_on);
        Bitmap actualFavoriteBitMap = ((BitmapDrawable) favorite.getIcon()).getBitmap();
        //noinspection ConstantConditions
        Bitmap expectedFavoriteBitMap = ((BitmapDrawable) expectedFavorite).getBitmap();

        assertEquals(expectedFavoriteBitMap.toString(), actualFavoriteBitMap.toString());
    }

    @Test
    public void updateFavorite_setFalse() throws Exception {
        buildActivity(-1, 0);

        Menu menu = getOptionsMenu();
        MenuItem favorite = menu.findItem(R.id.favorite_note);

        mActivity.updateFavoriteIcon(false);

        Drawable expectedFavorite = mActivity.getDrawable(R.drawable.ic_favorite_star_off);
        Bitmap actualFavoriteBitMap = ((BitmapDrawable) favorite.getIcon()).getBitmap();
        //noinspection ConstantConditions
        Bitmap expectedFavoriteBitMap = ((BitmapDrawable) expectedFavorite).getBitmap();

        assertEquals(expectedFavoriteBitMap.toString(), actualFavoriteBitMap.toString());
    }

    @Test
    public void updateReminderDisplay_NoRepeat() throws Exception {
        buildActivity(-1, 0);

        Reminder reminder = createReminder();
        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        String newReminderText = "New Text";

        mActivity.updateReminderDisplay(newReminderText, reminder.getFrequencyChoices());

        assertEquals(newReminderText, reminderText.getText().toString());
        assertEquals(View.VISIBLE, reminderText.getVisibility());

        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_reminder_dark_gray_small);
        //noinspection ConstantConditions
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();
        Bitmap actualBitMap = ((BitmapDrawable) reminderText.getCompoundDrawables()[0]).getBitmap();

        assertEquals(expectedBitMap.toString(), actualBitMap.toString());
    }

    @Test
    public void updateReminderDisplay_WithRepeat() throws Exception {
        buildActivity(-1, 0);

        Reminder reminder = createRepeatReminder();
        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        String newReminderText = "New Text";

        mActivity.updateReminderDisplay(newReminderText, reminder.getFrequencyChoices());

        assertEquals(newReminderText, reminderText.getText().toString());
        assertEquals(View.VISIBLE, reminderText.getVisibility());

        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_repeat_dark_gray_small);
        //noinspection ConstantConditions
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();
        Bitmap actualBitMap = ((BitmapDrawable) reminderText.getCompoundDrawables()[0]).getBitmap();

        assertEquals(expectedBitMap.toString(), actualBitMap.toString());
    }

    @Test
    public void hideReminder() throws Exception {
        buildActivity(-1, 0);

        TextView reminderText = mActivity.findViewById(R.id.reminderText);
        mActivity.hideReminder();

        assertEquals(View.INVISIBLE, reminderText.getVisibility());
    }


    @Test
    public void goBackToNotePreviewsActivity_ActivityOpened_NoActivityStack() throws Exception {
        buildActivity(-1, 0);

        mActivity.goBackToMainActivity(createNote(), 0, 0);
        assertTrue(mActivity.isTaskRoot());
    }

}
