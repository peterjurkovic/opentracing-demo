package tracer

import (
	"github.com/opentracing/opentracing-go"
	"github.com/uber/jaeger-client-go"
	"github.com/uber/jaeger-client-go/config"
	"github.com/uber/jaeger-client-go/log"
	"github.com/uber/jaeger-client-go/transport"
	 slog "log"
)

func Init(serviceName string, collectorUrl string ) opentracing.Tracer{

	//  HTTP collector accept jaeger.thrift directly from clients
	sender := transport.NewHTTPTransport(
		collectorUrl,
		transport.HTTPBatchSize(1),
	)

	// Sample configuration for testing. Use constant sampling to sample every trace
	// and enable LogSpan to log every span via configured Log
	cfg := config.Configuration{
		Sampler: &config.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
		Reporter: &config.ReporterConfig{
			LogSpans: true,
		},
	}

	cfg.ServiceName = serviceName

	// Example logger and metrics factory. Use github.com/uber/jaeger-client-go/log
	// and github.com/uber/jaeger-lib/metrics respectively to bind to real logging and metrics
	// frameworks.
	logger := log.StdLogger

	tracer, _, err := cfg.NewTracer(
		config.Reporter(jaeger.NewRemoteReporter(
			sender,
			jaeger.ReporterOptions.BufferFlushInterval(1),
			jaeger.ReporterOptions.Logger(logger),
		)),
		config.Logger(logger),
	)

	if err != nil {
		slog.Fatal("cannot initialize Jaeger Tracer")
	}

	return tracer
}