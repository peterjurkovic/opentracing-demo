package main

import (
	"./config"
	ot "./tracer"
	"fmt"
	"github.com/opentracing/opentracing-go"
	"net/http"
	"net/http/httputil"
	"time"
)


 var tracer opentracing.Tracer = ot.Init(config.ServiceName, config.HttpCollectorURL)

func quota(w http.ResponseWriter, r *http.Request) {

	// Save a copy of this request for debugging.
	requestDump, err := httputil.DumpRequest(r, true)
	if err != nil {
		fmt.Println(err)
	}

	fmt.Println(string(requestDump))
	ctx := r.Context()

	span, ctx := opentracing.StartSpanFromContext(ctx, "balance_check")

	time.Sleep(config.SleepTimeout)

	span.Finish()

	w.Header().Set("Content-Type", "application/json")

	w.Write([]byte("{\"balance\":\"ok\"}"))
}

func main() {

	opentracing.SetGlobalTracer(tracer)

	http.HandleFunc("/", quota)

	http.ListenAndServe(config.ServerAddress, nil)

	fmt.Println("Server started ", config.ServerAddress)
	fmt.Println("Collectors URL ", config.HttpCollectorURL)
}


