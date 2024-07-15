package cn.syxx.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMeta {

    private String name;
    private String originalFilename;
    private long size;
//    private String md5;
    private Map<String, String> tags = new HashMap<>();
}
