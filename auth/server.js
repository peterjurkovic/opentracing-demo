const express = require('express')
const initJaegerTracer = require('jaeger-client').initTracer;
const middleware = require('express-opentracing').default;
const opentracing = require("opentracing");
const sleep = require('sleep');
const app = express()
const PORT = process.env.AUTH_SERVER_PORT || 3030;
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
const mysqlTracer = initTracer("mysql");

app.use(middleware({tracer: tracer}));

app.get('/auth', function(req, res) { 
  console.log( 'Auth request received. MessageId ',  req.span.getBaggageItem("messageId"));
  console.log(JSON.stringify(req.headers));

  const span = mysqlTracer.startSpan("SQL SELECT", {childOf: req.span}); 

  span.setTag("span.kind", "client");
  span.setTag("peer.service", "mysql");
  span.setTag("db.type", "sql");
  span.setTag("db.statement", "SELECT apiKey, secret FROM account WHERE `apiKey`='nexmo'");
  
  if (req.query.fail){
    span.logEvent("mysql_timeout", {"apiKey" : "nexmo"})
    span.setTag("error", true);
    sleep.msleep(150);
    res.status(503);
  }else{
    sleep.msleep(50);
    span.logEvent("user_loaded");
  }



  span.finish();

  const authCheckSpan = tracer.startSpan("credentials_check", {childOf: req.span}); 
    
  sleep.msleep(30);
  
  authCheckSpan.finish();

  res.send({"status" : "ok"});  
  console.log( 'Auth check: OK');
})

app.listen(PORT, HOST)
console.log(`Running on http://${HOST}:${PORT}`);

