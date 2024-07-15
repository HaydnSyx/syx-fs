package cn.syxx.fs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;

import static cn.syxx.fs.HttpSyncer.HEAD_FILENAME;
import static cn.syxx.fs.HttpSyncer.HEAD_ORIGIN_FILENAME;

@Slf4j
@RestController
public class FileController {

    @Value("${syxfs.path}")
    private String uploadPath;
    @Value("${syxfs.backupUrl}")
    private String syncUrl;
    @Value("${syxfs.autoMd5}")
    private boolean autoMd5;

    @Autowired
    private HttpSyncer syncer;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpServletRequest request
    ) throws IOException {
        // 1.处理文件
        boolean syncFlag = false;
        String filename = request.getHeader(HEAD_FILENAME);
        String originalFilename = file.getOriginalFilename();
        if (Objects.isNull(filename) || filename.isBlank()) {
            filename = FileUtils.getUUIDFile(Objects.requireNonNull(originalFilename));
            syncFlag = true;
        } else {
            // 同步过来的信息
            originalFilename = request.getHeader(HEAD_ORIGIN_FILENAME);
        }

        log.info("recv file name: {}", filename);
        String subDir = FileUtils.getSubDir(filename);
        File dest = new File(uploadPath + subDir + "/" + filename);
        file.transferTo(dest);

        // 2.处理文件meta信息
        FileMeta meta = new FileMeta();
        meta.setOriginalFilename(originalFilename);
        meta.setName(filename);
        meta.setSize(file.getSize());
        if (autoMd5) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }

        String metaName = filename + ".meta";
        File metaFile = new File(uploadPath + subDir + "/" + metaName);
        FileUtils.writeMeta(metaFile, meta);

        // 3.同步文件
        if (syncFlag) {
            syncer.sync(originalFilename, dest, syncUrl);
        }

        return filename;
    }

    @RequestMapping("download")
    public void download(@RequestParam("name") String fileName, HttpServletResponse response) {
        String subdir = FileUtils.getSubDir(fileName);
        String path = uploadPath + subdir + "/" + fileName;
        File file = new File(path);

        response.setCharacterEncoding("UTF-8");
//        response.setContentType("application/octet-stream");
        response.setContentType(FileUtils.getMimeType(fileName));
//        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        response.setHeader("Content-Length", String.valueOf(file.length()));

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

    @RequestMapping("/meta")
    public String meta(String name) {
        String subdir = FileUtils.getSubDir(name);
        String path = uploadPath + subdir + "/" + name + ".meta";
        File file = new File(path);
        try (FileReader reader = new FileReader(file)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
