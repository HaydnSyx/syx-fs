package cn.syxx.fs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;

import static cn.syxx.fs.HttpSyncer.HEAD_FILENAME;

@Slf4j
@RestController
public class FileController {

    @Value("${syxfs.path}")
    private String uploadPath;
    @Value("${syxfs.backupUrl}")
    private String syncUrl;

    @Autowired
    private HttpSyncer syncer;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpServletRequest request
    ) throws IOException {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean syncFlag = false;
        String filename = request.getHeader(HEAD_FILENAME);
        if (Objects.isNull(filename) || filename.isBlank()) {
            filename = file.getOriginalFilename();
            syncFlag = true;
        }

        log.info("recv file name: {}", filename);

        File dest = new File(uploadPath + filename);
        file.transferTo(dest);

        if (syncFlag) {
            syncer.sync(dest, syncUrl);
        }

        return filename;
    }

    @RequestMapping("download")
    public void download(@RequestParam("name") String fileName, HttpServletResponse response) {
        File file = new File(uploadPath + fileName);
        if (!file.exists()) {
            return;
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            byte[] buffer = new byte[bis.available()];
            OutputStream os = response.getOutputStream();
            while (bis.read(buffer) != -1) {
                os.write(buffer);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
