package com.ozmar.notes;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExampleUnitTest {

//    String originalTitle = "Title";
//    String originalContent = "content";
//    String newTitle = "newTitle";
//    String newContent = "newContent";
//    SingleNote note = new SingleNote(originalTitle, originalContent, 0);
//
//    SingleNote note1 = new SingleNote("1", "1", 0);
//    SingleNote note2 = new SingleNote("2", "2", 0);
//    SingleNote note3 = new SingleNote("3", "3", 0);
//    SingleNote note4 = new SingleNote("4", "4", 0);
//    SingleNote note5 = new SingleNote("5", "5", 0);
//    List<SingleNote> list = new ArrayList<>(Arrays.asList(note1, note2, note3, note4, note5));
//
//
//    DatabaseHandler db;
//
//    @Before
//    public void db_Setup() {
//        db = new DatabaseHandler(RuntimeEnvironment.application);
//    }
//
//
//
//    @Test
//    public void differenceFromOriginal_isCorrect() throws Exception {
//        Context context = RuntimeEnvironment.application;
//        String changed = "changed";
//
//        String noteChanges[] = context.getResources().getStringArray(R.array.noteChangesArray);
//
//        // Note modified
//        assertEquals(noteChanges[0], NoteEditorUtils.changesToNote(context, changed,
//                originalContent, note));
//        assertEquals(noteChanges[0], NoteEditorUtils.changesToNote(context, originalTitle,
//                changed, note));
//        assertEquals(noteChanges[0], NoteEditorUtils.changesToNote(context, changed,
//                changed, note));
//
//        // New note
//        assertEquals(noteChanges[1], NoteEditorUtils.changesToNote(context, originalTitle,
//                originalContent, null));
//
//        // Note not modified (does not include noteChanges to favorite)
//        note.setTitle(originalTitle);
//        note.setContent(originalContent);
//        assertEquals(noteChanges[2], NoteEditorUtils.changesToNote(context,
//                originalTitle, originalContent, note));
//
//        // New empty note
//        assertEquals("", NoteEditorUtils.changesToNote(context, "", "", null));
//    }
//
//
//    @Test
//    public void updateNoteObject_isCorrect() throws Exception {
//        NoteEditorUtils.updateNoteObject(note, newTitle, newContent, false, false);
//        assertEquals(originalTitle, note.getTitle());
//        assertEquals(originalContent, note.getContent());
//
//        NoteEditorUtils.updateNoteObject(note, newTitle, newContent, true, false);
//        assertEquals(newTitle, note.getTitle());
//        assertEquals(originalContent, note.getContent());
//
//        NoteEditorUtils.updateNoteObject(note, newTitle, newContent, false, true);
//        assertEquals(newTitle, note.getTitle());
//        assertEquals(newContent, note.getContent());
//
//        NoteEditorUtils.updateNoteObject(note, originalTitle, originalContent, true, true);
//        assertEquals(originalTitle, note.getTitle());
//        assertEquals(originalContent, note.getContent());
//    }
//
//
//    @Test
//    public void undoBuffer_isCorrect() throws Exception {
//        UndoBuffer buffer = new UndoBuffer();
//
//        assertEquals(0, db.getNotesCount());
//
//        buffer.addDataToBuffer(note1, 0);
//        buffer.addDataToBuffer(note2, 1);
//        buffer.addDataToBuffer(note3, 2);
//        buffer.addDataToBuffer(note4, 3);
//        buffer.addDataToBuffer(note5, 4);
//
//        // buffer0
//        assertEquals(true, buffer.isBufferAvailable());
//        assertEquals(5, buffer.currentBufferSize());
//        assertEquals(list, buffer.currentBufferNotes());
//        assertEquals(true, buffer.isBufferAvailable());
//        assertEquals(true, buffer.otherBuffer().checkIfEmpty());
//
//        buffer.removeDataFromPosition(4);
//        assertEquals(4, buffer.currentBufferSize());
//
//        // buffer1
//        buffer.swapBuffer();
//        assertEquals(true, buffer.currentBuffer().checkIfEmpty());
//
//        // Both buffers now have data
//        buffer.addDataToBuffer(note, 0);
//        assertEquals(false, buffer.isBufferAvailable());
//
//        // buffer0
//        buffer.clearOtherBuffer();
//        assertEquals(true, buffer.isBufferAvailable());
//        buffer.swapBuffer();
//        assertEquals(0, buffer.currentBufferSize());
//    }
}