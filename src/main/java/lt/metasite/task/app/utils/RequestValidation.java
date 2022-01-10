package lt.metasite.task.app.utils;

import lt.metasite.task.app.exceptions.RequestValidationException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RequestValidation {

    private RequestValidation() {
    }

    public static void validateRequest(List<MultipartFile> files, String fileExtension) {

        files.parallelStream().forEach(multipartFile -> {

            if (multipartFile.isEmpty()) {
                throw new RequestValidationException("There is empty files in request");
            }

            if (StringUtils.hasText(multipartFile.getOriginalFilename())) {
                String extension = null;
                try {

                    extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
                } catch (Exception e) {
                    throw new RequestValidationException("Request is not valid. Please check files you have attached");
                }

                if (!StringUtils.hasText(extension) || !fileExtension.equalsIgnoreCase(extension)) {
                    throw new RequestValidationException("There is files with not valid extension. Only ." + fileExtension + " files are available");
                }
            } else {
                throw new RequestValidationException("Files must have names");
            }
        });
    }
}
