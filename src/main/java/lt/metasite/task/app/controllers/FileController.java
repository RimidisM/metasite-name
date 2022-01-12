package lt.metasite.task.app.controllers;

import lt.metasite.task.app.services.FileService;
import lt.metasite.task.app.utils.RequestValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/parse")
public class FileController {

    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService service;

    private String fileExtension = "";

    public FileController(FileService service, @Value("${application.fileExtension}") String fileExtension) {
        this.service = service;
        this.fileExtension = fileExtension;
    }

    @PostMapping(value = "/files", consumes = {"multipart/form-data"}, produces = {"application/zip"})
    public InputStreamResource insertRecord(@RequestParam(value = "files") List<MultipartFile> files,
                                            HttpServletResponse response) throws IOException {

        logger.info("Received request to parse files. Files in request: {}", files.size());

        RequestValidation.validateRequest(files, fileExtension);

        response.setHeader("Content-Disposition", "attachment;filename=result.zip");

        return service.parse(files);
    }
}
