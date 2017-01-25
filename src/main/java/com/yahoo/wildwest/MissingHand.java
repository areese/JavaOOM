package com.yahoo.wildwest;

import com.yahoo.wildwest.MUnsafe;
import com.yahoo.wildwest.MissingFingers;

public class MissingHand extends MissingFingers {
    private long[] childAllocations;

    public MissingHand() {
        this(0, 0, null);
    }

    public MissingHand(long address, long length, long[] childAllocations) {
        super(address, length);
        this.childAllocations = childAllocations;
    }

    @Override
    public void close() {
        // only free if allocated
        if (null != childAllocations) {
            for (long address : childAllocations) {
                if (0 != address)
                    MUnsafe.freeMemory(address);
            }
            childAllocations = null;
        }
        super.close();
    }
}
