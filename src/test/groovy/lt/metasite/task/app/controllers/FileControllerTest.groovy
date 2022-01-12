package lt.metasite.task.app.controllers

import lt.metasite.task.app.TestSpecifications
import lt.metasite.task.app.exceptions.RequestValidationException
import lt.metasite.task.app.services.FileService
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.spockframework.spring.SpringBean
import org.springframework.core.io.InputStreamResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.ResourceUtils
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileControllerTest extends TestSpecifications {

    @SpringBean
    FileService service = Mock()

    HttpServletResponse response = Mock()

    FileController controller = new FileController(service, EXTENSION_TXT)

    def "Should return ok status and zip file"() {

        setup:
        service.parse(*_) >> createResponse(CORRECT_OUTPUT)

        when:
        def result = controller.insertRecord(createRequest(CORRECT_INPUT), response)
        def bytes = result.inputStream.readAllBytes()

        then:
        getString(bytes) == "output"
        result.exists()
        bytes.length == 130
    }

    def "Should throw exception when file extension is not valid"() {

        when:
        def result = controller.insertRecord(createRequest(INCORRECT_INPUT), response)

        then:
        def ex = thrown(RequestValidationException.class)
        ex.localizedMessage == "There is files with not valid extension. Only .txt files are available"
        result == null
    }

    def "Should throw exception when file is empty"() {

        when:
        def result = controller.insertRecord(createRequest(EMPTY_FILE), response)

        then:
        def ex = thrown(RequestValidationException.class)
        ex.localizedMessage == "There is empty files in request"
        result == null
    }

    def "Should throw exception when file name is empty"() {

        when:
        def result = controller.insertRecord(createRequestWithFileWithoutName(CORRECT_INPUT), response)

        then:
        def ex = thrown(RequestValidationException.class)
        ex.localizedMessage == "Files must have names"
        result == null
    }
}
