package com.peterjurkovic.api;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static java.util.Collections.singletonMap;

@RestController
public class MessagesController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    @Autowired
    private MessagesApiApplication.Props props;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping
    public Map<String, String> sendMessage(@RequestParam(value = "fail", defaultValue = "false") boolean fail,
                                           @RequestParam(value = "message", defaultValue = "Hi there!") String message,
                                           @RequestHeader(value = "uber-trace-id", required = false) String traceId) {
        Span serverSpan = tracer.activeSpan();

        Span span = tracer.buildSpan("parsing")
                          .asChildOf(serverSpan)
                          .start();

        try {
            Thread.sleep(25L);
            span.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, "Parsing request"));
            serverSpan.setBaggageItem("messageID", UUID.randomUUID().toString());
            serverSpan.setBaggageItem("apiKey", "nexmo");
        } catch (InterruptedException e) {
            // ignore
        } finally {
            span.finish();
        }

        serverSpan.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, "Performing authentication check"));
        try {
            restTemplate.getForEntity(props.getAuthUrl() + "/auth" + (fail ? "?fail=true" : ""), Object.class);
        } catch (Exception e) {
            serverSpan.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.WARN, Fields.MESSAGE, "Authentication check has failed, retrying...", "ex", e));
            restTemplate.getForEntity(props.getAuthUrl() + "/auth", Object.class);
        }

        serverSpan.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, "Checking if an account has enough balance"));


        restTemplate.getForEntity(props.getQuotaUrl() + "/balance", Object.class);

        serverSpan.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, "Publishing message to kafka"));
        kafkaTemplate.send("messages", message);

        serverSpan.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, "Sent"));
        return singletonMap("traceId", traceId);
    }
}
