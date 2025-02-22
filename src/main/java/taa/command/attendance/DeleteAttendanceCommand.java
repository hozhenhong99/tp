package taa.command.attendance;

//@@author daknam2001
import taa.Ui;
import taa.attendance.Attendance;
import taa.attendance.AttendanceList;
import taa.command.Command;
import taa.exception.TaaException;
import taa.teachingclass.TeachingClass;
import taa.teachingclass.ClassList;
import taa.storage.Storage;
import taa.student.Student;
import taa.util.Util;

public class DeleteAttendanceCommand extends Command {
    private static final String KEY_CLASS_ID = "c";
    private static final String KEY_STUDENT_INDEX = "s";
    private static final String KEY_LESSON_NUMBER = "l";
    private static final String[] DELETE_ATTENDANCE_ARGUMENT_KEYS = {
        KEY_CLASS_ID,
        KEY_STUDENT_INDEX,
        KEY_LESSON_NUMBER
    };

    private static final String MESSAGE_FORMAT_DELETE_ATTENDANCE_USAGE =
        "%s %s/<CLASS_ID> %s/<STUDENT_INDEX> %s/<LESSON_NUMBER>";

    private static final String MESSAGE_FORMAT_ATTENDANCE_DELETED = "Attendance removed for %s:\n  Lesson %s";

    public DeleteAttendanceCommand(String argument) {
        super(argument, DELETE_ATTENDANCE_ARGUMENT_KEYS);
    }

    @Override
    public void checkArgument() throws TaaException {
        if (argument.isEmpty()) {
            throw new TaaException(getUsageMessage());
        }

        if (!hasAllArguments()) {
            throw new TaaException(getMissingArgumentMessage());
        }

        String studentIndexInput = argumentMap.get(KEY_STUDENT_INDEX);
        if (!Util.isStringInteger(studentIndexInput)) {
            throw new TaaException(MESSAGE_INVALID_STUDENT_INDEX);
        }

        String lessonNumInput = argumentMap.get(KEY_LESSON_NUMBER);
        if (!Util.isStringInteger(lessonNumInput)) {
            throw new TaaException(MESSAGE_INVALID_LESSON_NUMBER);
        }
    }

    /**
     * Executes the delete_attendance command and deletes a student's attendance from the attendanceList.
     *
     * @param classList The list of classes.
     * @param ui         The ui instance to handle interactions with the user.
     * @param storage    The storage instance to handle saving.
     * @throws TaaException If the user inputs an invalid command or has missing/invalid argument(s).
     */
    @Override
    public void execute(ClassList classList, Ui ui, Storage storage) throws TaaException {
        String classId = argumentMap.get(KEY_CLASS_ID);
        TeachingClass teachingClass = classList.getClassWithId(classId);
        if (teachingClass == null) {
            throw new TaaException(MESSAGE_CLASS_NOT_FOUND);
        }

        String studentIndexInput = argumentMap.get(KEY_STUDENT_INDEX);
        assert Util.isStringInteger(studentIndexInput);
        int studentIndex = Integer.parseInt(studentIndexInput) - 1;

        Student student = teachingClass.getStudentList().getStudentAt(studentIndex);
        assert studentIndex >= 0 && studentIndex < teachingClass.getStudentList().getSize();
        if (student == null) {
            throw new TaaException(MESSAGE_INVALID_STUDENT_INDEX);
        }

        String lessonNumInput = argumentMap.get(KEY_LESSON_NUMBER);
        assert Util.isStringInteger(lessonNumInput);
        AttendanceList attendanceList = student.getAttendanceList();
        Attendance attendance = attendanceList.deleteAttendance(lessonNumInput);
        if (attendance == null) {
            throw new TaaException(MESSAGE_INVALID_LESSON_NUMBER);
        }

        storage.save(classList);
        ui.printMessage(String.format(MESSAGE_FORMAT_ATTENDANCE_DELETED, student, lessonNumInput));
    }

    @Override
    protected String getUsage() {
        return String.format(
            MESSAGE_FORMAT_DELETE_ATTENDANCE_USAGE,
            COMMAND_DELETE_ATTENDANCE,
            KEY_CLASS_ID,
            KEY_STUDENT_INDEX,
            KEY_LESSON_NUMBER
        );
    }
}
