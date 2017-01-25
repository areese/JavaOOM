import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    static final class ByteBuffers implements Holder<ByteBuffer> {
        List<ByteBuffer> list = new LinkedList<>();
        long size = 0;

        @Override
        public void close() throws IOException {
            list.clear();
        }

        @Override
        public void add(ByteBuffer v) {
            size += v.capacity();
            list.add(v);
        }

        @Override
        public long size() {
            return size;
        }
    }


    public static void main(String[] argv) {
        long totalSize = 0;
        long currentSize = 0;
        printHeap(currentSize, totalSize);

        Args args = new Args(argv);

        System.out.println(args);
        int i = 0;
        totalSize = args.maxheapusage + args.maxunsafeusage;

        System.out.println("Starting, allocating " + args.bytesize + " sized byte buffers  up to "
                        + Args.mb(args.maxheapusage) + " and " + args.unsafeSize + " unsafe buffers up to "
                        + Args.mb(args.maxunsafeusage) + " for a total of " + Args.mb(totalSize));

        Bytes b = new Bytes();
        Unsafes u = new Unsafes();
        ByteBuffers bb = new ByteBuffers();

        i = 0;
        while (currentSize < totalSize) {
            boolean added = false;
            i++;
            if (args.bytesize > 0 && b.size() < args.maxheapusage) {
                byte[] n = new byte[args.bytesize];
                b.add(n);
                currentSize += args.bytesize;
                added = true;
            }

            if (args.unsafeSize > 0 && u.size() < args.maxunsafeusage) {
                MissingFingers m = new MissingFingers(args.unsafeSize);
                u.add(m);
                currentSize += args.unsafeSize;
                added = true;
            }

            if (args.byteBufferSize > 0 && bb.size() < args.maxbytebufferusage) {
                ByteBuffer m = ByteBuffer.allocateDirect((int) args.byteBufferSize);
                bb.add(m);
                currentSize += args.byteBufferSize;
                added = true;
            }

            if (args.printAt > 0 && i % args.printAt == 0) {
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
