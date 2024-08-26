package com.alibaba.nacos.naming.consistency.persistent.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * batch write request.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class BatchWriteRequest implements Serializable {

    private static final long serialVersionUID = 5620748357962129879L;

    private List<byte[]> keys = new ArrayList<>(16);

    private List<byte[]> values = new ArrayList<>(16);

    public List<byte[]> getKeys() {
        return keys;
    }

    public void setKeys(List<byte[]> keys) {
        this.keys = keys;
    }

    public List<byte[]> getValues() {
        return values;
    }

    public void setValues(List<byte[]> values) {
        this.values = values;
    }

    public void append(byte[] key, byte[] value) {
        keys.add(key);
        values.add(value);
    }
}