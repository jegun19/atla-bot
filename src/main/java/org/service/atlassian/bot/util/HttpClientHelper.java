package org.service.atlassian.bot.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HttpClientHelper {
    private final RestTemplate restTemplate;

    public <T> T get(String url, Class<T> responseType, Map<String, String> headers) {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders(headers));
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
    }

    public <T> T post(String url, Object request, Class<T> responseType, Map<String, String> headers) {
        HttpEntity<?> entity = new HttpEntity<>(request, createHeaders(headers));
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType).getBody();
    }

    private HttpHeaders createHeaders(Map<String, String> customHeaders) {
        HttpHeaders httpHeaders = new HttpHeaders();
        customHeaders.forEach(httpHeaders::set);
        return httpHeaders;
    }
}
