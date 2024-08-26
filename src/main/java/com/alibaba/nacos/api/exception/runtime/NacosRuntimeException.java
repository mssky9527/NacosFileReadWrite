package com.alibaba.nacos.api.exception.runtime;

public class NacosRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3513491993982293262L;

    public static final String ERROR_MESSAGE_FORMAT = "errCode: %d, errMsg: %s ";

    private int errCode;

    public NacosRuntimeException(int errCode) {
        super();
        this.errCode = errCode;
    }

    public NacosRuntimeException(int errCode, String errMsg) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg));
        this.errCode = errCode;
    }

    public NacosRuntimeException(int errCode, Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
    }

    public NacosRuntimeException(int errCode, String errMsg, Throwable throwable) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg), throwable);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}