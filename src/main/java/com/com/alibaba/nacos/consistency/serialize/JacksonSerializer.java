package com.com.alibaba.nacos.consistency.serialize;

import com.alibaba.nacos.common.utils.JacksonUtils;

public class JacksonSerializer  {

    private static final String NAME = "JSON";

    public static  <T> byte[] serialize(T obj) {
        return JacksonUtils.toJsonBytes(obj);
    }
}
