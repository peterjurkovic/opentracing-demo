# opentracing-demo
A demo application demonstrating Opentracing API &amp; Jaeger capabilities. The demo consist of a following mock service:

- Nginx
- Messages API (front-end service) - written in Java & Spring 
- Auth, written in NodeJS - express, talks to mock MYSQL service
- Quota, written in GO lang , talks to mock Couchbase service
- Kafka
- Messages backend - written in Java & Spring

Here it is how it looks like:
-- 
![Architecture](http://upload.peterjurkovic.com/uploaded_files/demo-architecture.png)


## How to run it locally

Just install Docker, copy `docker-compose.yml` file and run follwoing command:

```
docker-compose up
```

## How to use it

In order to create a trace send a request:

### curl request
```
curl -X POST http://localhost:8080/messages
````

### httpie request

```
http POST :8080/messages
````


then go to Jaeger UI `http://localhost:16686/`

You can also simulate a failure, just send GET parameter `fail=true` or retry `retry=true`


![Jaeger](http://upload.peterjurkovic.com/uploaded_files/opentracing.png)


