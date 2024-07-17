package cn.syxx.fs;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

public class FileUtils {

    static String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String content = fileNameMap.getContentTypeFor(fileName);
        return content == null ? DEFAULT_MIME_TYPE : content;
    }

    public static void init(String uploadPath) {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < 256; i++) {
            String subdir = String.format("%02x", i);
            File file = new File(uploadPath + subdir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static String getUUIDFile(String file) {
        return UUID.randomUUID() + getExt(file);
    }

    public static String getSubDir(String file) {
        return file.substring(0, 2);
    }

    public static String getExt(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    public static void writeMeta(File metaFile, FileMeta meta) throws IOException {
        String json = JSON.toJSONString(meta);
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), json,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public static void writeContent(File file, String content) throws IOException {
        Files.writeString(Paths.get(file.getAbsolutePath()), content,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public static void download(String download, File file) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<Resource> exchange = restTemplate.exchange(download, HttpMethod.GET, entity, Resource.class);

        try (BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(exchange.getBody()).getInputStream());
             OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[bis.available()];
            while (bis.read(buffer) != -1) {
                os.write(buffer);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
