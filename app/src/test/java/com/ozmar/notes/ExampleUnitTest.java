package com.ozmar.notes;

import com.ozmar.notes.utils.NoteEditorUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void differenceFromOriginal_isCorrect() throws Exception {
        SingleNote testNote = new SingleNote("Title", "Content", 0);
        assertEquals("discardNote", NoteEditorUtils.differenceFromOriginal("", "", null));
        assertEquals("newNote", NoteEditorUtils.differenceFromOriginal("Title", "Content", null));
        assertEquals("notChanged", NoteEditorUtils.differenceFromOriginal("Title", "Content", testNote));
        assertEquals("contentChanged", NoteEditorUtils.differenceFromOriginal("Title", "changed", testNote));
        assertEquals("titleChanged", NoteEditorUtils.differenceFromOriginal("changed", "Content", testNote));
        assertEquals("titleAndContentChanged", NoteEditorUtils.differenceFromOriginal("changed", "changed", testNote));
    }

}