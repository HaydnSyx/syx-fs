package cn.syxx.fs;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HeaderEncodingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            List<String> values = entry.getValue();
            values.replaceAll(s -> {
                if (containsChinese(s)) {
                    return URLEncoder.encode(s, StandardCharsets.UTF_8);
                }
                return s;
            });
        }
        return execution.execute(request, body);
    }

    public static boolean containsChinese(String str) {
        String regex = ".*[\u4e00-\u9fa5]+.*";
        return str.matches(regex);
    }
}
