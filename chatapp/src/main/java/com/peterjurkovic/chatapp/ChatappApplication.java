package com.peterjurkovic.chatapp;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static java.util.Arrays.asList;

@SpringBootApplication
public class ChatappApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappApplication.class, args);
    }


    @Bean
    RestTemplate restTemplate(Tracer tracer) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(asList(
                                new TracingRestTemplateInterceptor(tracer),
                                new LoggingRequestInterceptor()));
        return restTemplate;
    }


    @Component
    static class Props{

        Logger log = LoggerFactory.getLogger(getClass());

        @Value("${auth.host}")
        String authHost;

        @Value("${auth.port}")
        int authPort;

        @Value("${quota.host}")
        String quotaHost;

        @Value("${quota.port}")
        int quotaPort;


        public String getQuotaUrl(){
            return url(quotaHost, quotaPort);
        }

        public String getAuthUrl(){
            return url(authHost, authPort);
        }

        public String url(String host, int port){
            return "http://"+host+":"+port;
        }

        @PostConstruct
        public void log(){
            log.info("Quota: {}, Auth {} ",getQuotaUrl(), getAuthUrl());
        }
    }

}
