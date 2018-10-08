package com.peterjurkovic.backend;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private Tracer tracer;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @KafkaListener(topics = "messages")
    public void onMessage(ConsumerRecord<String,String> message){
        log.info("Received message: {}", message.value());

        SpanContext activeSpan = TracingKafkaUtils.extractSpanContext(message.headers(),tracer);


        Span span = tracer.buildSpan("sending_to_provider")
                          .asChildOf(activeSpan)
                          .start();

        try {
            Thread.sleep(20L);
            log.info("Message sent to a provider");
        }catch (Exception e){
            // ingore
        }finally {
            span.finish();
        }

    }
}
