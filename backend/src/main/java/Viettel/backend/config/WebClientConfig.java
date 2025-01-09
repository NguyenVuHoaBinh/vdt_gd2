package Viettel.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebClientConfig {
    @Value("${proxy.host}")
    private String PROXY_HOST;

    @Value("${proxy.port}")
    private int PROXY_PORT;

    @Bean
    public RestTemplate restTemplate() {
        //TODO: Define proxy base on spring profile

        // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        // SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // requestFactory.setProxy(proxy);

        return new RestTemplate();
    }

//    @Bean
//    public WebClient webClient() {}
}
