import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.yahoo.wildwest.MissingFingers;

// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the New-BSD license. Please see LICENSE file in the project root for terms.

public class JavaOOM {
    static final long MB = 1024 * 1024;

    public static long mb(long bytes) {
        return bytes / MB;
    }

    static interface Holder<T> extends Closeable {
        void add(T v);

        long size();
    }

    static final class Bytes implements Holder<byte[]> {
        List<byte[]> list = new LinkedList<>();
        long size = 0;

        @Override
        public void close() throws IOException {
            list.clear();
        }

        @Override
        public void add(byte[] v) {
            size += v.length;
            list.add(v);
        }

        @Override
        public long size() {
            return size;
        }
    }

    static final class Unsafes implements Holder<MissingFingers> {
        List<MissingFingers> list = new LinkedList<>();
        long size = 0;

        @Override
        public void close() throws IOException {
            Iterator<MissingFingers> itr = list.iterator();
            while (itr.hasNext()) {
                itr.next().close();
                itr.remove();
            }
            assert (list.isEmpty());
        }

        @Override
        public void add(MissingFingers v) {
            size += v.getLength();
            list.add(v);
        }

        @Override
        public long size() {
            return size;
        }
    }


    public static void main(String[] args) {
        int bytesize = 0;
        int unsafeSize = 0;
        long totalSize = 0;
        long currentSize = 0;
        long heapmax = 0;
        long unsafemax = 0;
        int printCount=100;

        printHeap(currentSize, totalSize);

        if (args.length < 4) {
            System.err.println("Need at least 4 args");
            System.exit(-1);
        }

        int i = 0;
        bytesize = Integer.parseInt(args[i++]);
        unsafeSize = Integer.parseInt(args[i++]);
        heapmax = Integer.parseInt(args[i++]) * MB;
        unsafemax = Integer.parseInt(args[i++]) * MB;
        totalSize = heapmax + unsafemax;

        if (i < args.length) {
            printCount = Integer.parseInt(args[i++]);
        }

        System.out.println("Starting, allocating " + bytesize + " sized byte buffers  up to " + mb(heapmax) + " and "
                        + unsafeSize + " unsafe buffers up to " + mb(unsafemax) + " for a total of " + mb(totalSize));

        Bytes b = new Bytes();
        Unsafes u = new Unsafes();

        i = 0;
        while (currentSize < totalSize) {
            boolean added = false;
            i++;
            if (b.size() < heapmax) {
                byte[] n = new byte[bytesize];
                b.add(n);
                currentSize += bytesize;
                added = true;
            }

            if (u.size() < unsafemax) {
                MissingFingers m = new MissingFingers(unsafeSize);
                u.add(m);
                currentSize += unsafeSize;
                added = true;
            }

            if (i % printCount == 0) {
                printHeap(currentSize, totalSize);
            }

            if (!added) {
                break;
            }
        }


    }


    private static void printHeap(long currentSize, long totalSize) {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        System.out.println("currentSize = " + mb(currentSize) + " totalSize = " + mb(totalSize) + " heapSize = "
                        + mb(heapSize) + " heapMaxSize = " + mb(heapMaxSize) + " heapFreeSize = " + mb(heapFreeSize));
    }
}
