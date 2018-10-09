package com.peterjurkovic.api;

import io.opentracing.Span;
import io.opentracing.log.Fields;
import io.opentracing.util.GlobalTracer;

public class Logger {

    public static void info(String message){
        GlobalTracer.get().activeSpan().log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, message));
    }

    public static void warn(String message, Exception ex){
        GlobalTracer.get().activeSpan().log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.INFO, Fields.MESSAGE, message, "ex", ex));
    }

}
