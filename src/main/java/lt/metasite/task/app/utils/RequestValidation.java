package lt.metasite.task.app.utils;

import lt.metasite.task.app.exceptions.RequestValidationException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
                    extension = Optional.ofNullable(multipartFile.getOriginalFilename())
                            .filter(f -> f.contains("."))
                            .map(f -> f.substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1)).get();
                } catch (Exception e) {
                    throw new RequestValidationException("Request is not valid. Please check files you have attached");
                }

                if (!fileExtension.equalsIgnoreCase(extension)) {
                    throw new RequestValidationException("There is files with not valid extension. Only ." + fileExtension + " files are available");
                }
            } else {
                throw new RequestValidationException("Files must have names");
            }
        });
    }
}
