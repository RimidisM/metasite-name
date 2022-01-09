package lt.metasite.task.app.services;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
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

    public InputStreamResource parse(List<MultipartFile> files) throws IOException {

        List<String> main = files.parallelStream().map(r -> {

            try {
                return convertFileContentToList(r.getInputStream().readAllBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).flatMap(Collection::parallelStream)
                .collect(Collectors.toList());


        ConcurrentMap<String, Long> collect = main.parallelStream().collect(Collectors
                .toConcurrentMap(Function.identity(), v -> 1L, Long::sum));

        Map<String, Long> ag = new HashMap<>();
        Map<String, Long> hn = new HashMap<>();
        Map<String, Long> ou = new HashMap<>();
        Map<String, Long> vz = new HashMap<>();

        collect.forEach((s, aLong) -> {

            if (s.substring(0, 1).matches("^[A-Ga-g]*$")) {
                ag.put(s, aLong);
            }

            if (s.substring(0, 1).matches("^[H-Nh-n]*$")) {
                hn.put(s, aLong);
            }

            if (s.substring(0, 1).matches("^[O-Uo-u]*$")) {
                ou.put(s, aLong);
            }

            if (s.substring(0, 1).matches("^[V-Zv-z]*$")) {
                vz.put(s, aLong);
            }
        });


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        List<Map<String, Long>> result = new ArrayList<>();

        result.add(ag);
        result.add(hn);
        result.add(ou);
        result.add(vz);

        result.forEach(stringLongMap -> {
            try {
                String word = stringLongMap.entrySet().iterator().next().getKey();

                createZip(createFile(FILE_NAME_RESULT + word, stringLongMap), FILE_NAME_RESULT + word, zipOutputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zipOutputStream.close();

        return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    private List<String> convertFileContentToList(byte[] bytes) {

        String content = new String(bytes).replaceAll("[\\t\\n\\r]+", " ").replaceAll("[^A-Za-z ]+", "");

        return Stream.of(content.split(" ", -1))
                .collect(Collectors.toList())
                .parallelStream().filter(x -> !x.equalsIgnoreCase("")).collect(Collectors.toList());
    }

    private String mapToString(Map<String, Long> map) {
        return map.keySet().stream()
                .map(key -> key + " = " + map.get(key))
                .collect(Collectors.joining(", "));
    }

    private File createFile(String fileName, Map<String, Long> map) throws IOException {

        File file = File.createTempFile(fileName, ".txt");

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
}
