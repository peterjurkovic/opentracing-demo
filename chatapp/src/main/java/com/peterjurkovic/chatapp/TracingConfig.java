package com.peterjurkovic.chatapp;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.metrics.Metrics;
import io.jaegertracing.internal.metrics.NoopMetricsFactory;
import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.LoggingReporter;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.MetricsFactory;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sampler;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TracingConfig {

    @Value("${service.name}")
    private String serviceName;

    @Value("${opentracing.jaeger.http-sender.url}")
    private String httpJeaegerSenderUrl;



    @Bean
    public Tracer tracer(Sampler sampler, Reporter reporter) {
        return new JaegerTracer.Builder(serviceName)
                .withTag("host", "myHost")
                .withReporter(reporter)
                .withSampler(sampler)
                .withExpandExceptionLogs()
                .build();
    }


    @Bean
    public Reporter reporter(Metrics metrics, RemoteReporter remoteReporter) {
        List<Reporter> reporters = new ArrayList<>();
        reporters.add(remoteReporter);
        reporters.add(new LoggingReporter());
        return new CompositeReporter(reporters.toArray(new Reporter[reporters.size()]));
    }

    @Bean
    public HttpSender httpSender() {
        return new HttpSender.Builder(httpJeaegerSenderUrl)
                .build();
    }

    @Bean
    public RemoteReporter remoteReporter(Metrics metrics, Sender udpSender) {
        return new RemoteReporter.Builder()
                .withSender(udpSender)
                .withMetrics(metrics)
                .build();

    }


    @Bean
    public Metrics reporterMetrics(MetricsFactory metricsFactory) {
        return new Metrics(metricsFactory);
    }


    @Bean
    public MetricsFactory metricsFactory() {
        return new NoopMetricsFactory();
    }

    /**
     * Decide on what Sampler to use based on the various configuration options in
     * JaegerConfigurationProperties Fallback to ConstSampler(true) when no Sampler is configured
     */
    @Bean
    public Sampler sampler(Metrics metrics) {
        return new ConstSampler(true);
    }
}
