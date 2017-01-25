package com.yahoo.wildwest;

public class BoundsCheckException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BoundsCheckException(long address, long offset, long length) {
        super("Bounds checking failure for address " + Long.toHexString(address) + " new offset "
                        + Long.toUnsignedString(offset) + " exceeds length " + Long.toUnsignedString(length));
    }
}
