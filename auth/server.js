const express = require('express')
const initJaegerTracer = require('jaeger-client').initTracer;
const middleware = require('express-opentracing').default;
const opentracing = require("opentracing");
const sleep = require('sleep');
const app = express()
const PORT = 3000;
const HOST = '0.0.0.0';

function initTracer(serviceName) {
  const config = {
    serviceName: serviceName,
    sampler: {
      type: "const",
      param: 1,
    },
    reporter: {
      logSpans: true,
      collectorEndpoint : 'http://jaeger:14268/api/traces'
    },
  };
  const options = {
    logger: {
      info(msg) {
        console.log("INFO ", msg);
      },
      error(msg) {
        console.log("ERROR", msg);
      },
    },
  };
  return initJaegerTracer(config, options);
}

const tracer = initTracer("auth-service");

app.use(middleware({tracer: tracer}));

app.get('/auth', function(req, res) { 
	 console.log( 'Chil span: ' + req.span ? 'PRESENT' : 'UNDEFINED');

	const span = tracer.startSpan("user_looup", {childOf: req.span});	
		span.setTag("db.type", "sql");
		span.setTag("db.statement", "SELECT apiKey, secret FROM account WHERE `apiKey`='nexmo'");
		sleep.msleep(250);
		span.logEvent("user_loaded");

	span.finish();

	const authCheckSpan = tracer.startSpan("credentials_check", {childOf: req.span});	
		
	sleep.msleep(30);
	
	authCheckSpan.finish();

	res.send({"status" : "ok"});	
  console.log( 'Auth chec: OK');
})

app.listen(PORT, HOST)
console.log(`Running on http://${HOST}:${PORT}`);

