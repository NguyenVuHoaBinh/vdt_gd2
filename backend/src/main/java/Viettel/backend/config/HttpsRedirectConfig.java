package Viettel.backend.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpsRedirectConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(org.apache.catalina.Context context) {
                org.apache.tomcat.util.descriptor.web.SecurityConstraint securityConstraint = new org.apache.tomcat.util.descriptor.web.SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                org.apache.tomcat.util.descriptor.web.SecurityCollection collection = new org.apache.tomcat.util.descriptor.web.SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    private org.apache.catalina.connector.Connector httpConnector() {
        org.apache.catalina.connector.Connector connector = new org.apache.catalina.connector.Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8889);
        connector.setSecure(false);
        connector.setRedirectPort(8888);
        return connector;
    }
}

