package com.peterjurkovic.chatapp;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
public class ChatappController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ChatappApplication.Props props;

    @GetMapping
    public Map<String,String> sendMessage(@RequestHeader(value = "uber-trace-id", required = false) String traceId){
        Span serverSpan = tracer.activeSpan();

        Span span = tracer.buildSpan("parsing")
                          .asChildOf(serverSpan)
                          .start();

        try {
            Thread.sleep(25L);
            serverSpan.setBaggageItem("messageID", UUID.randomUUID().toString());
            serverSpan.setBaggageItem("apiKey", "nexmo");
        } catch (InterruptedException e) {
            // ignore
        } finally {
            span.finish();
        }

        try{
            restTemplate.getForEntity(props.getAuthUrl()+ "/auth?fail=true", Object.class);
        }catch (Exception e){
            restTemplate.getForEntity(props.getAuthUrl()+ "/auth", Object.class);
        }
        restTemplate.getForEntity(props.getQuotaUrl() + "/balance", Object.class);

        return Collections.singletonMap("traceId", traceId);
    }
}
