package lt.metasite.task.app.exceptions;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {FileUploadException.class})
    protected ResponseEntity<Object> handleFileUploadException(FileUploadException ex, WebRequest request) {
        return handleExceptionInternal(ex, ErrorResponse.builder().error(ex.getLocalizedMessage()).build(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {RequestValidationException.class})
    protected ResponseEntity<Object> handleRequestValidationException(RequestValidationException ex, WebRequest request) {
        return handleExceptionInternal(ex, ErrorResponse.builder().error(ex.getLocalizedMessage()).build(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
