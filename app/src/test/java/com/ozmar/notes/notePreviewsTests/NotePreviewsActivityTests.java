package com.ozmar.notes.notePreviewsTests;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ozmar.notes.BuildConfig;
import com.ozmar.notes.R;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.notePreviews.NotePreviewsActivity;
import com.ozmar.notes.notePreviews.NotePreviewsPresenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NotePreviewsActivityTests {

    @Mock
    private NotePreviewsPresenter mPreviewsPresenter;

    private NotePreviewsActivity mActivity;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mActivity = Robolectric.buildActivity(NotePreviewsActivity.class).create().visible().get();
        mActivity.mActivityPresenter = mPreviewsPresenter;
    }

    @Test
    public void activityNotNull() throws Exception {
        Assert.assertNotNull(mActivity);
    }

    @Test
    public void viewsAndWidgetsNotNull() throws Exception {
        DrawerLayout drawerLayout = mActivity.findViewById(R.id.drawerLayout);
        Assert.assertNotNull(drawerLayout);

        Toolbar toolbar = mActivity.findViewById(R.id.my_toolbar);
        Assert.assertNotNull(toolbar);

        NavigationView navigationView = mActivity.findViewById(R.id.nav_view);
        Assert.assertNotNull(navigationView);

        RecyclerView recyclerView = mActivity.findViewById(R.id.recyclerView);
        Assert.assertNotNull(recyclerView);

        FloatingActionButton fab = mActivity.findViewById(R.id.fab);
        Assert.assertNotNull(fab);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void clickingFabOpensActivityToEditAndCreateNote() throws Exception {

        int notePosition = -1;
        int listUsed = 0;

        // This method in the presenter calls
        // openNoteEditorActivity, called manually later in this test
        doNothing().when(mPreviewsPresenter).onNoteClick(notePosition);

        FloatingActionButton fab = mActivity.findViewById(R.id.fab);
        fab.performClick();

        mActivity.openNoteEditorActivity(notePosition, listUsed);
        Intent intent = Shadows.shadowOf(mActivity).peekNextStartedActivity();
        Assert.assertEquals(NoteEditorActivity.class.getCanonicalName(),
                intent.getComponent().getClassName());

        verify(mPreviewsPresenter, times(1)).onNoteClick(notePosition);
    }

    @Test
    public void optionsMenuInflated() throws Exception {
        Menu menu = Shadows.shadowOf(mActivity).getOptionsMenu();
        Assert.assertNotNull(menu);

        MenuItem layoutIcon = menu.findItem(R.id.layout);
        Assert.assertNotNull(layoutIcon);
    }

    @Test
    public void pressingIconToSwitchRecyclerViewLayout() throws Exception {
        Menu menu = Shadows.shadowOf(mActivity).getOptionsMenu();
        MenuItem menuItem = menu.findItem(R.id.layout);

        doNothing().when(mPreviewsPresenter).onLayoutIconClicked(anyInt());
        mActivity.onOptionsItemSelected(menuItem);
        verify(mPreviewsPresenter, times(1)).onLayoutIconClicked(anyInt());
    }

    @Test
    public void selectingListOfNOtesToDisplay() throws Exception {
        Toolbar toolbar = mActivity.findViewById(R.id.my_toolbar);
        FloatingActionButton fab = mActivity.findViewById(R.id.fab);
        NavigationView navigationView = mActivity.findViewById(R.id.nav_view);

        MenuItem mainNotes = navigationView.getMenu().findItem(R.id.all_notes_drawer);
        MenuItem favoriteNotes = navigationView.getMenu().findItem(R.id.favorite_notes_drawer);
        MenuItem archiveNotes = navigationView.getMenu().findItem(R.id.archive_drawer);
        MenuItem recycleBinNotes = navigationView.getMenu().findItem(R.id.recycle_bin_drawer);


        doNothing().when(mPreviewsPresenter).onGetPreviewList(anyInt());

        mActivity.onNavigationItemSelected(mainNotes);
        Assert.assertEquals(mActivity.getString(R.string.toolbarMainNotes), toolbar.getTitle());
        Assert.assertEquals(View.VISIBLE, fab.getVisibility());

        mActivity.onNavigationItemSelected(favoriteNotes);
        Assert.assertEquals(mActivity.getString(R.string.toolbarFavoriteNotes), toolbar.getTitle());
        Assert.assertEquals(View.VISIBLE, fab.getVisibility());

        mActivity.onNavigationItemSelected(archiveNotes);
        Assert.assertEquals(mActivity.getString(R.string.toolbarArchiveNotes), toolbar.getTitle());
        Assert.assertEquals(View.INVISIBLE, fab.getVisibility());

        mActivity.onNavigationItemSelected(recycleBinNotes);
        Assert.assertEquals(mActivity.getString(R.string.toolbarRecycleBinNotes), toolbar.getTitle());
        Assert.assertEquals(View.INVISIBLE, fab.getVisibility());

        verify(mPreviewsPresenter, times(4)).onGetPreviewList(anyInt());
    }

    @Test
    public void swapLayoutOfRecyclerViewToStaggered_IconShowsNextLayout() throws Exception {
        Menu menu = Shadows.shadowOf(mActivity).getOptionsMenu();
        MenuItem layoutIcon = menu.findItem(R.id.layout);

        mActivity.swapLayout(1);

        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_linear_layout);
        assert expectedDrawable != null;
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();
        Bitmap actualBitMap = ((BitmapDrawable) layoutIcon.getIcon()).getBitmap();
        Assert.assertEquals(expectedBitMap.toString(), actualBitMap.toString());


        RecyclerView recyclerView = mActivity.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        Assert.assertTrue(layoutManager instanceof StaggeredGridLayoutManager);
    }

    @Test
    public void swapLayoutOfRecyclerViewToLinear_IconShowsNextLayout() throws Exception {
        Menu menu = Shadows.shadowOf(mActivity).getOptionsMenu();
        MenuItem layoutIcon = menu.findItem(R.id.layout);

        mActivity.swapLayout(0);

        Drawable expectedDrawable = mActivity.getDrawable(R.drawable.ic_staggered_grid_layout);
        assert expectedDrawable != null;
        Bitmap expectedBitMap = ((BitmapDrawable) expectedDrawable).getBitmap();
        Bitmap actualBitMap = ((BitmapDrawable) layoutIcon.getIcon()).getBitmap();
        Assert.assertEquals(expectedBitMap.toString(), actualBitMap.toString());


        RecyclerView recyclerView = mActivity.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        Assert.assertTrue(layoutManager instanceof LinearLayoutManager);
    }

    @Test
    public void createSnackBarAndDismissingIt() throws Exception {
        doNothing().when(mPreviewsPresenter).processChosenNotes();
        mActivity.showSnackBar(0, 1);
        mActivity.dismissSnackBar();
        verify(mPreviewsPresenter, times(1)).processChosenNotes();
    }
}
