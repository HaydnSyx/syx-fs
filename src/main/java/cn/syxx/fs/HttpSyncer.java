package cn.syxx.fs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Slf4j
@Component
public class HttpSyncer {

    public static final String HEAD_FILENAME = "x-filename";
    public static final String HEAD_ORIGIN_FILENAME = "x-origin-filename";

    @Autowired
    private RestTemplate restTemplate;

    public String sync(String originFilename, File file, String syncUrl) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HEAD_FILENAME, file.getName());
        headers.add(HEAD_ORIGIN_FILENAME, originFilename);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> entity = new HttpEntity<>(builder.build(), headers);

        ResponseEntity<String> result = restTemplate.postForEntity(syncUrl, entity, String.class);
        log.info("sync result: {}", result.getBody());
        return result.getBody();
    }
}
