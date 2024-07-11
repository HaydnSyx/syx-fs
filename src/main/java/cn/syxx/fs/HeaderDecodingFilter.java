package cn.syxx.fs;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HeaderDecodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            Map<String, String> newHeadMap = new HashMap<>();
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = httpRequest.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    headerValue = URLDecoder.decode(headerValue, StandardCharsets.UTF_8);
                    newHeadMap.put(headerName, headerValue);
                }
            }

            if (!newHeadMap.isEmpty()) {
                HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        String decode = newHeadMap.get(name);
                        if (decode != null) {
                            return decode;
                        }
                        return super.getHeader(name);
                    }
                };
                log.info("用新的request执行后续操作...");
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }
        log.info("用原始的request执行后续操作...");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
