package com.ozmar.notes.database;


import com.ozmar.notes.SingleNote;

import java.util.ArrayList;
import java.util.List;

public class NoteConversion {

    private NoteConversion() {

    }

    public static MainNote getMainNoteFromSingleNote(SingleNote note) {
        MainNote mainNote = new MainNote();

        mainNote.setId(note.getId());
        mainNote.setTitle(note.getTitle());
        mainNote.setContent(note.getContent());
        mainNote.setFavorite(note.isFavorite() ? 1 : 0);
        mainNote.setTimeCreated(note.getTimeCreated());
        mainNote.setTimeModified(note.getTimeModified());
        mainNote.setReminderId(note.getReminderId());

        return mainNote;
    }

    public static SingleNote getSingleNoteFromMainNote(MainNote note) {
        SingleNote singleNote = new SingleNote();

        singleNote.setId(note.getId());
        singleNote.setTitle(note.getTitle());
        singleNote.setContent(note.getContent());
        singleNote.setFavorite(note.getFavorite() == 1);
        singleNote.setTimeCreated(note.getTimeCreated());
        singleNote.setTimeModified(note.getTimeModified());
        singleNote.setReminderId(note.getReminderId());

        return singleNote;
    }

    public static ArchiveNote getArchiveNoteFromSingleNote(SingleNote note) {
        ArchiveNote archiveNote = new ArchiveNote();

        archiveNote.setId(note.getId());
        archiveNote.setTitle(note.getTitle());
        archiveNote.setContent(note.getContent());
        archiveNote.setTimeCreated(note.getTimeCreated());
        archiveNote.setTimeModified(note.getTimeModified());
        archiveNote.setReminderId(note.getReminderId());

        return archiveNote;
    }

    public static SingleNote getSingleNoteFromArchiveNote(ArchiveNote note) {
        SingleNote singleNote = new SingleNote();

        singleNote.setId(note.getId());
        singleNote.setTitle(note.getTitle());
        singleNote.setContent(note.getContent());
        singleNote.setTimeCreated(note.getTimeCreated());
        singleNote.setTimeModified(note.getTimeModified());
        singleNote.setReminderId(note.getReminderId());

        return singleNote;
    }

    public static RecycleBinNote getRecycleBinNoteFromSingleNote(SingleNote note) {
        RecycleBinNote recycleBinNote = new RecycleBinNote();

        recycleBinNote.setId(note.getId());
        recycleBinNote.setTitle(note.getTitle());
        recycleBinNote.setContent(note.getContent());
        recycleBinNote.setTimeCreated(note.getTimeCreated());
        recycleBinNote.setTimeModified(note.getTimeModified());

        return recycleBinNote;
    }

    public static SingleNote getSingleNoteFromRecycleBinNote(RecycleBinNote note) {
        SingleNote singleNote = new SingleNote();

        singleNote.setId(note.getId());
        singleNote.setTitle(note.getTitle());
        singleNote.setContent(note.getContent());
        singleNote.setTimeCreated(note.getTimeCreated());
        singleNote.setTimeModified(note.getTimeModified());

        return singleNote;
    }

    public static Object getFromSingleNoteConversion(SingleNote note, int listUsed) {
        Object object = null;
        if (listUsed == 0 || listUsed == 1) {
            object = NoteConversion.getMainNoteFromSingleNote(note);
        } else if (listUsed == 2) {
            object = NoteConversion.getArchiveNoteFromSingleNote(note);
        } else if (listUsed == 3) {
            object = NoteConversion.getRecycleBinNoteFromSingleNote(note);
        }
        return object;
    }

    public static SingleNote getToSingleNoteConversion(Object object, int listUsed) {
        SingleNote note = null;
        if (listUsed == 0 || listUsed == 1) {
            note = NoteConversion.getSingleNoteFromMainNote((MainNote) object);
        } else if (listUsed == 2) {
            note = NoteConversion.getSingleNoteFromArchiveNote((ArchiveNote) object);
        } else if (listUsed == 3) {
            note = NoteConversion.getSingleNoteFromRecycleBinNote((RecycleBinNote) object);
        }
        return note;
    }


    public static List<SingleNote> tempList(List<MainNote> list) {
        List<SingleNote> newList = new ArrayList<>();
        if (!list.isEmpty()) {
            for (MainNote note : list) {
                newList.add(getSingleNoteFromMainNote(note));
            }
        }
        return newList;
    }

}
