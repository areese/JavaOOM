// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the New-BSD license. Please see LICENSE file in the project root for terms.
package com.yahoo.wildwest;

import java.io.Closeable;

public class MissingFingers implements Closeable {
    private long address;
    private long length;

    public MissingFingers() {
        this(0, 0);
    }

    public MissingFingers(long address, long length) {
        this.address = address;
        this.length = length;
    }

    /**
     * This version will _allocate_ This allocates space of at least byteArraySize. Looking at DirectByteBuffer, it
     * seems that you need to check if things are page aligned, and allocate on a page boundary. So this might allocate
     * more than that size as an unknown implementation detail.
     * 
     * @param byteArraySize size of array to allocate
     */
    public MissingFingers(long byteArraySize) {
        this(MUnsafe.allocateMemory(byteArraySize), byteArraySize);
    }

    /**
     * Given a byte array, _allocate_ and _copy_ the bytes. This is suitable for passing to the jni afterwards.
     * 
     * @param from bytes to copy
     */
    public MissingFingers(byte[] from) {
        this(MUnsafe.byteArraySize(from));
        if (isValidAddress()) {
            MUnsafe.copyMemory(address, length, from);
        }
    }

    public long getAddress() {
        return address;
    }

    public long getLength() {
        return length;
    }

    public boolean isValidAddress() {
        return 0 != address && 0 != length;
    }

    @Override
    public void close() {
        // only free if allocated
        if (isValidAddress()) {
            MUnsafe.freeMemory(address);
            address = 0;
            length = 0;
        }
    }

}
