package lt.metasite.task.app.services

import lt.metasite.task.app.TestSpecifications

class FileServiceTests extends TestSpecifications{

    FileService service = new FileService()

    def "Should return zip file"() {

        setup:
        service.parse(*_) >> createResponse(CORRECT_OUTPUT)

        when:
        def result = service.parse(createRequest(CORRECT_INPUT))
        def bytes = result.inputStream.readAllBytes()

        then:
        getString(bytes) == "a = 1,\\t\\n\\rb = 1,\\t\\n\\rg = 1"
        result.exists()
        bytes.length == 578
    }
}
