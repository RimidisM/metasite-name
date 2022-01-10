package lt.metasite.task.app.exceptions;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(RestResponseExceptionHandler.class);

    @ExceptionHandler(value = {FileUploadException.class})
    protected ResponseEntity<Object> handleFileUploadException(FileUploadException ex, WebRequest request) {

        logger.warn("Error: {}", ex.getLocalizedMessage());

        return handleExceptionInternal(ex, ErrorResponse.builder().error(ex.getLocalizedMessage()).build(),
                getApplicationJsonHeader(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {RequestValidationException.class})
    protected ResponseEntity<Object> handleRequestValidationException(RequestValidationException ex, WebRequest request) {

        logger.warn("Request validation error: {}", ex.getLocalizedMessage());

        return handleExceptionInternal(ex, ErrorResponse.builder().error(ex.getLocalizedMessage()).build(),
                getApplicationJsonHeader(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    protected ResponseEntity<Object> handleRequestValidationException(NoSuchElementException ex, WebRequest request) {

        logger.warn("Request validation error: {}", ex.getLocalizedMessage());

        return handleExceptionInternal(ex, ErrorResponse.builder().error(ex.getLocalizedMessage()).build(),
                getApplicationJsonHeader(), HttpStatus.BAD_REQUEST, request);
    }

    private HttpHeaders getApplicationJsonHeader() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
