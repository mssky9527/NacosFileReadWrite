package com.alibaba.nacos.api.exception.runtime;

import static com.alibaba.nacos.api.common.Constants.Exception.SERIALIZE_ERROR_CODE;

/**
 * Nacos serialization exception.
 *
 * @author yangyi
 */
public class NacosSerializationException extends NacosRuntimeException {

    private static final long serialVersionUID = -4308536346316915612L;

    private static final String DEFAULT_MSG = "Nacos serialize failed. ";

    private static final String MSG_FOR_SPECIFIED_CLASS = "Nacos serialize for class [%s] failed. ";

    private Class<?> serializedClass;

    public NacosSerializationException() {
        super(SERIALIZE_ERROR_CODE);
    }

    public NacosSerializationException(Class<?> serializedClass) {
        super(SERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()));
        this.serializedClass = serializedClass;
    }

    public NacosSerializationException(Throwable throwable) {
        super(SERIALIZE_ERROR_CODE, DEFAULT_MSG, throwable);
    }

    public NacosSerializationException(Class<?> serializedClass, Throwable throwable) {
        super(SERIALIZE_ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()), throwable);
        this.serializedClass = serializedClass;
    }

    public Class<?> getSerializedClass() {
        return serializedClass;
    }
}