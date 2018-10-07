package main

import (
	"./config"
	ot "./tracer"
	"fmt"
	"github.com/opentracing/opentracing-go"
	"github.com/opentracing/opentracing-go/ext"
	tags "github.com/opentracing/opentracing-go/ext"
	"net/http"
	"net/http/httputil"
	"time"
)


var tracer opentracing.Tracer = ot.Init(config.ServiceName, config.HttpCollectorURL)
var dbTracer  opentracing.Tracer = ot.Init("couchbase", config.HttpCollectorURL)

func quota(w http.ResponseWriter, r *http.Request) {

	// Save a copy of this request for debugging.
	requestDump, err := httputil.DumpRequest(r, true)
	if err != nil {
		fmt.Println(err)
	}

	fmt.Println(string(requestDump))

	var wireContext, terr = opentracing.GlobalTracer().Extract(
		opentracing.HTTPHeaders,
		opentracing.HTTPHeadersCarrier(r.Header))

	if terr != nil {
		// Optionally record something about err here
	}

	serverSpan := opentracing.StartSpan(
		"balance_check",
		ext.RPCServerOption(wireContext))

	span := dbTracer.StartSpan("N1QL QUERY", opentracing.ChildOf(serverSpan.Context()))
	tags.SpanKindRPCClient.Set(span)
	tags.PeerService.Set(span, "couchbase")
	span.SetTag("n1ql.query", "SELECT balance FROM account_balance WHERE api_key=ANY")
	time.Sleep(50)
	span.Finish()

	time.Sleep(config.SleepTimeout)

	serverSpan.Finish()

	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("X-Powered-By", "Go lang")
	w.Write([]byte("{\"status\":\"ok\"}"))
}

func main() {

	opentracing.SetGlobalTracer(tracer)

	http.HandleFunc("/balance", quota)

	http.ListenAndServe(config.ServerAddress, nil)

	fmt.Println("Server started ", config.ServerAddress)
	fmt.Println("Collector URL ", config.HttpCollectorURL)
}


