package cn.syxx.fs;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class MqSyncer {

    @Value("${syxfs.consumerGroup}")
    private String group;
    @Value("${syxfs.path}")
    private String uploadPath;
    @Value("${syxfs.downloadUrl}")
    private String localDownloadUrl;

    @Autowired
    private RocketMQTemplate mqTemplate;

    private static final String TOPIC = "syx_fs";

    public void sync(FileMeta meta) {
        log.info("send file with mq ...");
        Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(meta)).build();
        mqTemplate.send(TOPIC, message);
        log.info("send mq: {}", meta.getName());
    }

    //    @Slf4j
    @Service
    @RocketMQMessageListener(topic = TOPIC, consumerGroup = "${syxfs.consumerGroup}")
    public class MqListener implements RocketMQListener<MessageExt> {

        private final Logger logger = LoggerFactory.getLogger(MqListener.class);

        @Override
        public void onMessage(MessageExt message) {
            logger.info(" ===> recv message id: {}", message.getMsgId());
            String body = new String(message.getBody());
            logger.info(" ===> recv message body: {}", body);

            FileMeta meta = JSON.parseObject(body, FileMeta.class);
            String downloadUrl = meta.getDownloadUrl();
            if (StringUtils.isBlank(downloadUrl)) {
                logger.error("download url is empty");
                return;
            }

            // fixme 本地忽律
            if (Objects.equals(downloadUrl, localDownloadUrl)) {
                logger.info(" ===> local message ignore. msgFlag: {}, localFlag: {}", downloadUrl, localDownloadUrl);
                return;
            }

            String dir = uploadPath + FileUtils.getSubDir(meta.getName());
            File metaFile = new File(dir, meta.getName() + ".meta");
            if (!metaFile.exists()) {
                logger.warn("meta file not exist: {}", metaFile.getAbsolutePath());
                try {
                    FileUtils.writeContent(metaFile, body);
                    logger.info("write meta file success: {}", metaFile.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("write meta file error: {}", metaFile.getAbsolutePath(), e);
                }
            }

            File file = new File(dir, meta.getName());
            if (file.exists() && file.length() == meta.getSize()) {
                logger.info("file already exist: {}", file.getAbsolutePath());
                return;
            }

            String download = downloadUrl + "?name=" + meta.getName();
            FileUtils.download(download, file);
            logger.info("download file success: {} from {}", file.getAbsolutePath(), download);
        }
    }
}
