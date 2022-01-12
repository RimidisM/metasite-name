package lt.metasite.task.app

import org.apache.tomcat.util.http.fileupload.IOUtils
import org.springframework.core.io.InputStreamResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.ResourceUtils
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class TestSpecifications extends Specification {

    def EXTENSION_TXT = "txt"
    def CORRECT_INPUT = "classpath:static/input.txt"
    def CORRECT_OUTPUT = "classpath:static/output.txt"
    def INCORRECT_INPUT = "classpath:static/withoutExtension"
    def EMPTY_FILE = "classpath:static/empty.txt"

    static def createResponse(String fileName) {

        File file = getFile(fileName)

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)

        ZipEntry e = new ZipEntry("test")
        e.setSize(file.length())
        e.setTime(System.currentTimeMillis())
        zipOutputStream.putNextEntry(e)

        FileInputStream fis = new FileInputStream(file)
        DataInputStream dis = new DataInputStream(fis)

        byte[] bytes = dis.readAllBytes()

        dis.close()
        fis.close()

        InputStream inputStream = new ByteArrayInputStream(bytes)
        IOUtils.copy(inputStream, zipOutputStream)
        inputStream.close()
        zipOutputStream.closeEntry()
        zipOutputStream.close()

        return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
    }

    static def createRequest(String fileName) {

        return getMultiPartFileList(fileName, fileName, fileName)
    }

    static def createRequestWithFileWithoutName(String fileName) {

        return getMultiPartFileList(fileName, "", fileName)
    }

    static def getMultiPartFileList(String name, String originalFileName, String fileName) {

        def contentType = "text/plain"

        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, getFile(fileName).bytes)

        List<MultipartFile> files = new ArrayList<>()

        files.add(result)

        return files
    }

    static def getFile(String fileName) {
        return ResourceUtils.getFile(fileName)
    }

    static def getString(byte[] bytes) {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
        ZipInputStream zis = new ZipInputStream(bais)

        zis.getNextEntry()

        Scanner sc = new Scanner(zis)
        String text = null
        while (sc.hasNextLine()) {
            text = sc.nextLine()
        }

        zis.close()
        bais.close()

        return text
    }
}
