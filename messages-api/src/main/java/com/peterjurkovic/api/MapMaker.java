package com.peterjurkovic.api;

import java.util.HashMap;
import java.util.Map;

public class MapMaker {

    public static Map<String, Object> fields(Object... keyAndValue){
        int lg = keyAndValue.length / 2;
        HashMap<String, Object> m = new HashMap<>();
        for(int i = 0; i < lg; i++) {
            m.put(String.valueOf(keyAndValue[i*2]), keyAndValue[i*2+1]);
        }
        return m;
    }
}
