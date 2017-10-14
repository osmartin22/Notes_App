package com.ozmar.notes;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ozmar.notes", appContext.getPackageName());
    }

//    @Test
//    public void differenceFromOriginal_isCorrect() {
//        Context context = getContext();
//        String originalTitle = "Title";
//        String originalContent = "content";
//        String changed = "changed";
//        SingleNote originalNote = new SingleNote(originalTitle, originalContent, 0);
//
//        android.content.res.Resources res = getInstrumentation().getTargetContext().getResources();
//        String[] noteChanges = res.getStringArray(R.array.noteChangesArray);
//
//        // Note modified
//        assertEquals(noteChanges[0], NoteEditorUtils.differenceFromOriginal(context, changed,
//                originalContent, originalNote));
//        assertEquals(noteChanges[0], NoteEditorUtils.differenceFromOriginal(context, originalTitle,
//                changed, originalNote));
//        assertEquals(noteChanges[0], NoteEditorUtils.differenceFromOriginal(context, changed,
//                changed, originalNote));
//
//        // New note
//        assertEquals(noteChanges[1], NoteEditorUtils.differenceFromOriginal(context, originalTitle,
//                originalContent, null));
//
//        // Note not modified (does not include noteChanges to favorite)
//        assertEquals(noteChanges[2], NoteEditorUtils.differenceFromOriginal(context,
//                originalTitle, originalContent, originalNote));
//
//        // New empty note
//        assertEquals("", NoteEditorUtils.differenceFromOriginal(context, "", "", null));
//    }
}
