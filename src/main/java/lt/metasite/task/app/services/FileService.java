package lt.metasite.task.app.services;

import lt.metasite.task.app.domains.SortedResult;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final String FILE_EXTENSION = ".txt";
    private static final String FILE_NAME_RESULT = "result_";
    private static final String WORD_SEPARATOR = ",\\t\\n\\r";
    private static final String NEW_LINE_REGEX = "[\\t\\n\\r]+";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String REGEX_FOR_ALPHABET = "[^A-Za-z ]+";
    private static final String REGEX_FOR_A_G = "^[A-Ga-g]*$";
    private static final String REGEX_FOR_H_N = "^[H-Nh-n]*$";
    private static final String REGEX_FOR_O_U = "^[O-Uo-u]*$";
    private static final String REGEX_FOR_V_Z = "^[V-Zv-z]*$";
    private static final String A_G = "A-G";
    private static final String H_N = "H-N";
    private static final String O_U = "O-U";
    private static final String V_Z = "V-Z";

    public InputStreamResource parse(List<MultipartFile> files) throws IOException {

        List<String> main = files.parallelStream().map(r -> {

            try {
                return convertFileContentToList(r.getInputStream().readAllBytes());
            } catch (IOException e) {
                logger.error("Exception when converting file content to list. Error: {}", e.getLocalizedMessage());
            }
            return null;
        }).filter(Objects::nonNull).flatMap(Collection::parallelStream)
                .collect(Collectors.toList());

        logger.info("Words from files: {}", main);

        List<SortedResult> results = getSortedResults(main.parallelStream().collect(Collectors
                .toConcurrentMap(Function.identity(), v -> 1L, Long::sum)));

        logger.info("Sorted words: {}", results);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        results.forEach(stringLongMap -> {
            try {

                if (!CollectionUtils.isEmpty(Optional.ofNullable(stringLongMap).map(SortedResult::getResult)
                        .orElse(new HashMap<>()))) {

                    String name = Optional.ofNullable(stringLongMap).map(SortedResult::getName).orElse(EMPTY);

                    createZip(createFile(FILE_NAME_RESULT + name, Optional.ofNullable(stringLongMap)
                                    .map(SortedResult::getResult).orElse(new HashMap<>())),
                            FILE_NAME_RESULT + name, zipOutputStream);
                }
            } catch (IOException e) {
               logger.error("Exception during response construction. Error: {}", e.getLocalizedMessage());
            }
        });

        zipOutputStream.close();

        return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    private List<String> convertFileContentToList(byte[] bytes) {

        String content = new String(bytes).replaceAll(NEW_LINE_REGEX, SPACE).replaceAll(REGEX_FOR_ALPHABET, EMPTY);

        logger.info("File content: {}", content);

        return Stream.of(content.split(SPACE, -1))
                .collect(Collectors.toList())
                .parallelStream().filter(x -> !x.equalsIgnoreCase(EMPTY)).collect(Collectors.toList());
    }

    private String mapToString(Map<String, Long> map) {
        return map.keySet().stream()
                .map(key -> key + " = " + map.get(key))
                .collect(Collectors.joining(WORD_SEPARATOR));
    }

    private File createFile(String fileName, Map<String, Long> map) throws IOException {

        File file = File.createTempFile(fileName, FILE_EXTENSION);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(mapToString(map).getBytes());
            fos.flush();
        }

        return file;
    }

    private byte[] getFileBytes(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file)) {
            DataInputStream dis = new DataInputStream(fis);

            byte[] bytes = dis.readAllBytes();

            dis.close();

            return bytes;
        }
    }

    private void createZip(File file, String fileName, ZipOutputStream zipOutputStream) throws IOException {

        ZipEntry e = new ZipEntry(fileName + FILE_EXTENSION);
        e.setSize(file.length());
        e.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(e);
        InputStream inputStream = new ByteArrayInputStream(getFileBytes(file));
        IOUtils.copy(inputStream, zipOutputStream);
        inputStream.close();
        zipOutputStream.closeEntry();
    }

    private List<SortedResult> getSortedResults(ConcurrentMap<String, Long> collect) {

        Map<String, Long> ag = new HashMap<>();
        Map<String, Long> hn = new HashMap<>();
        Map<String, Long> ou = new HashMap<>();
        Map<String, Long> vz = new HashMap<>();

        collect.forEach((s, aLong) -> {

            if (s.substring(0, 1).matches(REGEX_FOR_A_G)) {
                ag.put(s, aLong);
            }

            if (s.substring(0, 1).matches(REGEX_FOR_H_N)) {
                hn.put(s, aLong);
            }

            if (s.substring(0, 1).matches(REGEX_FOR_O_U)) {
                ou.put(s, aLong);
            }

            if (s.substring(0, 1).matches(REGEX_FOR_V_Z)) {
                vz.put(s, aLong);
            }
        });

        List<SortedResult> results = new ArrayList<>();

        results.add(SortedResult.builder().name(A_G).result(ag).build());
        results.add(SortedResult.builder().name(H_N).result(hn).build());
        results.add(SortedResult.builder().name(O_U).result(ou).build());
        results.add(SortedResult.builder().name(V_Z).result(vz).build());

        return results;
    }
}
