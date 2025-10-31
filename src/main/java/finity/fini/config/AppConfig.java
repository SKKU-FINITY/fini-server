package finity.fini.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.ProxySelector;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // HttpComponentsClientHttpRequestFactory 생성
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // 리다이렉션을 자동으로 처리하는 HttpClient 생성
        // SystemDefaultRoutePlanner를 사용하여 시스템 프록시 설정을 따르도록 구성
        HttpClient httpClient = HttpClientBuilder.create()
                .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                .build();

        factory.setHttpClient(httpClient);

        // 타임아웃 설정 (연결 및 읽기)
        factory.setConnectTimeout(5000); // 5초

        return new RestTemplate(factory);
    }
}