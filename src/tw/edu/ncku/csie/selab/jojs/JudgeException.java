package tw.edu.ncku.csie.selab.jojs;

public class JudgeException extends Exception {
    public enum ErrorCode {
        INVALID_STUDENT_ID,
        INVALID_INPUT,
        NO_MAIN_CLASS,
        COMPILE_ERROR,
        UNSUPPORTED_ENCODING,
        INVALID_CLASS
    }

    private ErrorCode code;

    public JudgeException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}
