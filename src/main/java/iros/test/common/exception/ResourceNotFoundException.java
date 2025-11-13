package iros.test.common.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found 응답에 사용
 *
 * @author PUBC Test API
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자
     */
    public ResourceNotFoundException() {
        super("요청한 리소스를 찾을 수 없습니다");
    }

    /**
     * 메시지를 포함한 생성자
     *
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
