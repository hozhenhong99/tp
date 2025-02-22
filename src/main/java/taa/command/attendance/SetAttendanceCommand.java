package taa.command.attendance;

//@@author daknam2001
import taa.attendance.AttendanceList;
import taa.teachingclass.ClassList;
import taa.command.Command;
import taa.exception.TaaException;
import taa.Ui;
import taa.attendance.Attendance;
import taa.teachingclass.TeachingClass;
import taa.storage.Storage;
import taa.student.Student;
import taa.student.StudentList;
import taa.util.Util;

public class SetAttendanceCommand extends Command {
    private static final String KEY_CLASS_ID = "c";
    private static final String KEY_STUDENT_INDEX = "s";
    private static final String KEY_LESSON_NUMBER = "l";
    private static final String KEY_PRESENT = "p";
    private static final String[] SET_ATTENDANCE_ARGUMENT_KEYS = {
        KEY_CLASS_ID,
        KEY_STUDENT_INDEX,
        KEY_LESSON_NUMBER,
        KEY_PRESENT
    };

    private static final String MESSAGE_INVALID_LESSON_NUMBER = "Invalid lesson number.";

    private static final String MESSAGE_FORMAT_INVALID_PRESENT = "Invalid present value.\n"
        + "Possible values: %s (Present), %s (Absent)";
    private static final String MESSAGE_FORMAT_SET_ATTENDANCE = "Attendance set for %s:\n  %s";

    private static final String PRESENT_VALUE = "1";
    private static final String ABSENT_VALUE = "0";

    private static final String MESSAGE_FORMAT_SET_ATTENDANCE_USAGE = "%s %s/<CLASS_ID> %s/<STUDENT_INDEX> "
        + "%s/<LESSON_NUMBER> %s/<PRESENT>";

    public SetAttendanceCommand(String argument) {
        super(argument, SET_ATTENDANCE_ARGUMENT_KEYS);
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

        String lessonNumberInput = argumentMap.get(KEY_LESSON_NUMBER);
        if (!Util.isStringInteger(lessonNumberInput)) {
            throw new TaaException(MESSAGE_INVALID_LESSON_NUMBER);
        }
    }

    /**
     * Executes the set_attendance command and sets the attendance of a student.
     *
     * @param classList The list of classes.
     * @param ui         The ui instance to handle interactions with the user.
     * @param storage    The storage instance to handle saving.
     * @throws TaaException If the user inputs an invalid command or has missing/invalid argument(s).
     */
    public void execute(ClassList classList, Ui ui, Storage storage) throws TaaException {
        String classId = argumentMap.get(KEY_CLASS_ID);
        TeachingClass teachingClass = classList.getClassWithId(classId);
        if (teachingClass == null) {
            throw new TaaException(MESSAGE_CLASS_NOT_FOUND);
        }

        String studentIndexInput = argumentMap.get(KEY_STUDENT_INDEX);
        assert Util.isStringInteger(studentIndexInput);
        int studentIndex = Integer.parseInt(studentIndexInput) - 1;

        StudentList studentList = teachingClass.getStudentList();
        Student student = studentList.getStudentAt(studentIndex);
        if (student == null) {
            throw new TaaException(MESSAGE_INVALID_STUDENT_INDEX);
        }

        String lessonNumberInput = argumentMap.get(KEY_LESSON_NUMBER);
        assert Util.isStringInteger(lessonNumberInput);
        int lessonNumber = Integer.parseInt(lessonNumberInput);
        if (lessonNumber < Attendance.getMinLessonNumber()) {
            throw new TaaException(MESSAGE_INVALID_LESSON_NUMBER);
        }

        String present = argumentMap.get(KEY_PRESENT);
        boolean isPresent;
        switch (present) {
        case PRESENT_VALUE:
            isPresent = true;
            break;

        case ABSENT_VALUE:
            isPresent = false;
            break;

        default:
            throw new TaaException(String.format(MESSAGE_FORMAT_INVALID_PRESENT, PRESENT_VALUE, ABSENT_VALUE));
        }

        AttendanceList attendanceList = student.getAttendanceList();
        Attendance attendance = attendanceList.getAttendance(lessonNumber);
        if (attendance == null) {
            attendance = new Attendance(lessonNumber, isPresent);
            attendanceList.addAttendance(attendance);
        } else {
            attendance.setPresent(isPresent);
        }

        storage.save(classList);

        ui.printMessage(String.format(MESSAGE_FORMAT_SET_ATTENDANCE, student.getName(), attendance));
    }

    @Override
    protected String getUsage() {
        return String.format(
            MESSAGE_FORMAT_SET_ATTENDANCE_USAGE,
            COMMAND_SET_ATTENDANCE,
            KEY_CLASS_ID,
            KEY_STUDENT_INDEX,
            KEY_LESSON_NUMBER,
            KEY_PRESENT
        );
    }
}
