package com.peterjurkovic.api;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
public class MessagesController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    @Autowired
    private MessagesApiApplication.Props props;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping
    public Map<String,String> sendMessage(@RequestParam(value = "message", defaultValue = "Hi there!") String message,
                                          @RequestHeader(value = "uber-trace-id", required = false) String traceId){
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

        kafkaTemplate.send("messages", message);
        return Collections.singletonMap("traceId", traceId);
    }
}
