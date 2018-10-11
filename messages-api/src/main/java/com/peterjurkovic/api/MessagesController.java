package com.peterjurkovic.api;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonMap;

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

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping
    public Map<String, String> sendMessage(@RequestParam(value = "fail", defaultValue = "false") boolean fail,
                                           @RequestParam(value = "retry", defaultValue = "false") boolean retry,
                                           @RequestParam(value = "message", defaultValue = "Hi there!") String message,
                                           @RequestHeader(value = "uber-trace-id", required = false) String traceId) {
        parseRequest();

        authCheck(fail, retry);

        balanceCheck();

        sendToKafka(message);

        Logger.info("Sent!");
        return singletonMap("traceId", traceId);
    }

    private void authCheck(boolean fail, boolean retry){
        Logger.info("Performing authentication check");
        try {
            restTemplate.getForEntity(props.getAuthUrl() + "/auth" + (fail || retry ? "?fail=true" : ""), Object.class);
        } catch (Exception e) {
            if (retry) {
                Logger.warn("Authentication check has failed, retrying...", e);
                restTemplate.getForEntity(props.getAuthUrl() + "/auth", Object.class);
            } else {
                throw e;
            }
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, ?>> handle(Exception e, WebRequest req){
        Tags.ERROR.set(tracer.activeSpan(), true);
        Logger.warn("Failed to authenticate user", e);
        return new ResponseEntity<>(singletonMap("traceId", req.getHeaderValues("uber-trace-id")),
                                    HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void sendToKafka(String message){
        Logger.info("Publishing message to kafka");
        kafkaTemplate.send("messages", message);
    }


    private void balanceCheck(){
        Logger.info("Checking if an account has enough balance");
        restTemplate.getForEntity(props.getQuotaUrl() + "/balance", Object.class);
    }


    private void parseRequest(){
        Span serverSpan = tracer.activeSpan();
        Span span = tracer.buildSpan("parsing")
                          .asChildOf(serverSpan)
                          .start();

        try {
            Thread.sleep(25L);
            Logger.info("Parsing request");
            serverSpan.setBaggageItem("messageID", UUID.randomUUID().toString());
            serverSpan.setBaggageItem("apiKey", "nexmo");
        } catch (InterruptedException e) {
            // ignore
        } finally {
            span.finish();
        }
    }

    private void checkBalance(){
        Logger.info("Checking if an account has enough balance");
        restTemplate.getForEntity(props.getQuotaUrl() + "/balance", Object.class);
    }
}
