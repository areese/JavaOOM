// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the New-BSD license. Please see LICENSE file in the project root for terms.
package com.yahoo.wildwest;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class MUnsafe {
    /**
     * The instance of unsafe everyone can use
     */
    private static final Unsafe unsafe;

    /**
     * This is required by unsafe to access a char array, it's the offset
     */
    private static final long charArrayBaseOffset;

    /**
     * This is required by unsafe to access a char array, it's the scale for indexing.
     */
    private static final long charArrayIndexScale;

    /**
     * This is required by unsafe to access a byte array, it's the offset
     */
    private static final long byteArrayBaseOffset;

    /**
     * This is required by unsafe to access a byte array, it's the scale for indexing.
     */
    private static final long byteArrayIndexScale;

    /**
     * For convenience.
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};

    // public static final long ISA_ADDR_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            charArrayBaseOffset = unsafe.arrayBaseOffset(char[].class);
            charArrayIndexScale = unsafe.arrayIndexScale(char[].class);
            byteArrayBaseOffset = unsafe.arrayBaseOffset(byte[].class);
            byteArrayIndexScale = unsafe.arrayIndexScale(byte[].class);

            // ISA_HOLDER_OFFSET =
            // unsafe.objectFieldOffset(InetSocketAddress.class.getDeclaredField("holder"));

        } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
            throw new Error(e);
        }
    }

    /**
     * Access to unsafe
     * 
     * @return Unsafe accessor.
     */
    public static Unsafe getUnsafe() {
        return unsafe;
    }

    /**
     * Given an index into a char array, return the correct offset to use
     * 
     * @param index index into the array to get.
     * @return offset into the array for a specific index.
     */
    public static long charArrayOffset(int index) {
        return calculateOffset(index, charArrayBaseOffset, charArrayIndexScale);
    }

    /**
     * Given a char array, calculate the correct amount of memory it will take. ??? Doesn't take into account null and
     * probably should.
     * 
     * @param from array to use in calculation.
     * @return length to allocate.
     */
    public static long charArraySize(char[] from) {
        return from.length * charArrayIndexScale;
    }


    /**
     * Given an index into a byte array, return the correct offset to use
     * 
     * @param index index into the array to get.
     * @return offset into the array for a specific index.
     */
    public static long byteArrayOffset(int index) {
        return calculateOffset(index, byteArrayBaseOffset, byteArrayIndexScale);
    }

    /**
     * Given a byte array, calculate the correct amount of memory it will take.
     * 
     * @param from array to use in calculation.
     * @return length to allocate.
     */
    public static long byteArraySize(byte[] from) {
        return from.length * byteArrayIndexScale;
    }

    /**
     * The object referred to by o is an array, and the offset is an integer of the form B+N*S, where N is a valid index
     * into the array, and B and S are the values obtained by #arrayBaseOffset and #arrayIndexScale (respectively) from
     * the array's class. The value referred to is the Nth element of the array.
     * 
     * @param index into the array to find the offset for.
     * @param base base from Unsafe to use.
     * @param scale scale from Unsafe to use
     * @return value usable by copyMemory
     **/
    public static long calculateOffset(int index, long base, long scale) {
        return (base + (index * scale));
    }

    /**
     * Allocate memory from unsafe of cap bytes
     * 
     * @param size number of bytes to allocate
     * @return memory that must be free'd with Unsafe.free memory
     */
    public static long allocateMemory(long size) {
        // http://bugs.java.com/view_bug.do?bug_id=4837564, remove page alignment as that could explode in our faces. =)
        long address = unsafe.allocateMemory(size);
        unsafe.setMemory(address, size, (byte) 0);

        return address;
    }

    /**
     * Copy the contents of a byte array into native memory
     * 
     * @param destAddress native memory
     * @param from where to copy from
     */
    public static void copyMemory(long destAddress, byte[] from) {
        copyMemory(destAddress, from.length, from);
    }

    /**
     * Copy the contents of a byte array into native memory
     * 
     * @param destAddress native memory
     * @param totalSize amount to copy
     * @param from where to copy from
     */
    public static void copyMemory(long destAddress, long totalSize, byte[] from) {
        if (0 == destAddress || 0 == totalSize || null == from || 0 == from.length) {
            return;
        }

        long bytes = Math.min(from.length, totalSize);
        unsafe.copyMemory(from, byteArrayBaseOffset, null, index(destAddress, 0), bytes);
    }

    /**
     * Copy the contents of a byte array into native memory
     * 
     * @param dest where to copy to
     * @param srcAddress where to copy from
     * @param totalSize amount to copy
     */
    public static void copyMemory(byte[] dest, long srcAddress, long totalSize) {
        if (0 == srcAddress || 0 == totalSize || null == dest || 0 == dest.length) {
            return;
        }

        long bytes = Math.min(dest.length, totalSize);
        unsafe.copyMemory(null, index(srcAddress, 0), dest, byteArrayBaseOffset, bytes);
    }

    /**
     * given an address find it's index
     * 
     * @param address native memory
     * @param i index
     * @return index
     */
    private static long index(long address, int i) {
        return address + ((long) i << 0);
    }

    /**
     * Free's native memory
     * 
     * @param address native memory to free
     */
    public static void freeMemory(long address) {
        if (0 != address) {
            unsafe.freeMemory(address);
            address = 0;
        }
    }

    /**
     * Given a String, encode it into MissingFingers address,length tuple
     * 
     * @param s input
     * @return MissingFingers, allocated address + length
     */
    public static MissingFingers encodeString(String s) {
        if (null == s || 0 == s.length() || s.isEmpty()) {
            // null begets null
            return new MissingFingers(0, 0);
        }

        byte[] fromBytes = s.getBytes(StandardCharsets.UTF_8);

        // gotta null terminate
        long totalSize = byteArraySize(fromBytes) + 1;
        long destAddress = unsafe.allocateMemory(totalSize);
        copyMemory(destAddress, totalSize, fromBytes);
        unsafe.putByte(destAddress + (totalSize - 1), (byte) 0);

        return new MissingFingers(destAddress, totalSize);
    }

    /**
     * Given an address into memory, copy the bytes into a new String assuming UTF-8 encoding. Also free the memory upon
     * completion using Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you
     * allocated with malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len bytes to copy.
     * @return String, null on null, 0 on empty string
     */
    public static String decodeStringAndFree(long srcAddress, long len) {
        try {
            return decodeString(srcAddress, len);
        } finally {
            if (0 != srcAddress) {
                freeMemory(srcAddress);
            }
        }
    }

    /**
     * Given an address into memory, copy the bytes into a new String assuming UTF-8 encoding.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len bytes to copy.
     * @return String, null on null, 0 on empty string
     */
    public static String decodeString(long srcAddress, long len) {
        if (0 == srcAddress) {
            return null;
        }

        if (0 == len) {
            return "";
        }

        byte[] toBytes = new byte[(int) len];
        copyMemory(toBytes, srcAddress, len);

        // strip off null terminator
        if (0 == toBytes[(int) len - 1]) {
            len -= 1;
        }

        return new String(toBytes, 0, (int) len, StandardCharsets.UTF_8);
    }

    /**
     * Given an address into memory, copy the bytes into a new String assuming UTF-8 encoding. Also free the memory upon
     * completion using Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you
     * allocated with malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param encodedString unsafe allocated address that we can copy from/
     * @return String, null on null, 0 on empty string
     */
    public static String decodeString(MissingFingers encodedString) {
        if (null == encodedString) {
            return null;
        }
        return decodeString(encodedString.getAddress(), encodedString.getLength());
    }

    /**
     * Given an address into memory, copy the bytes into a new String assuming UTF-8 encoding. Also free the memory upon
     * completion using Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you
     * allocated with malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param encodedString unsafe allocated address that we can copy from/
     * @return String, null on null, 0 on empty string
     */
    public static String decodeStringAndFree(MissingFingers encodedString) {
        try {
            if (null == encodedString) {
                return null;
            }
            return decodeString(encodedString.getAddress(), encodedString.getLength());
        } finally {
            if (null != encodedString) {
                encodedString.close();
            }
        }
    }


    /**
     * Given an address into memory, copy the bytes into a new InetAddress.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len length of bytes to copy.
     * @return InetAddress, null on null, 0 on empty string
     */
    public static InetAddress decodeInetAddress(long srcAddress, long len) {
        // TODO: implement
        return null;
    }


    /**
     * Given an address into memory, copy the bytes into a new InetAddress. Also free the memory upon completion using
     * Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you allocated with
     * malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len length of bytes to copy.
     * @return InetAddress, null on null, 0 on empty string
     */
    public static InetAddress decodeInetAddressAndFree(long srcAddress, long len) {
        try {
            return decodeInetAddress(srcAddress, len);
        } finally {
            if (0 != srcAddress) {
                freeMemory(srcAddress);
            }
        }
    }


    /**
     * Given an address into memory, copy the bytes into a new InetAddress. Also free the memory upon completion using
     * Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you allocated with
     * malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param encodedInetAddress unsafe allocated address that we can copy from/
     * @return InetAddress, null on null, 0 on empty string
     */
    public static InetAddress decodeInetAddressAndFree(MissingFingers encodedInetAddress) {
        try {
            if (null == encodedInetAddress) {
                return null;
            }
            return decodeInetAddress(encodedInetAddress.getAddress(), encodedInetAddress.getLength());
        } finally {
            if (null != encodedInetAddress) {
                encodedInetAddress.close();
            }
        }
    }

    /**
     * Given an address into memory, copy the bytes into a new byte array. Also free the memory upon completion using
     * Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you allocated with
     * malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len length of bytes to copy.
     * @return Java ByteArray, null on null, 0 on empty string
     */
    public static byte[] decodeByteArrayAndFree(long srcAddress, long len) {
        try {
            return decodeByteArray(srcAddress, len);
        } finally {
            if (0 != srcAddress) {
                freeMemory(srcAddress);
            }
        }
    }

    /**
     * Given an address into memory, copy the bytes into a new byte array.
     * 
     * @param srcAddress unsafe allocated address that we can copy from/
     * @param len length of bytes to copy.
     * @return Java ByteArray, null on null, 0 on empty string
     */
    public static byte[] decodeByteArray(long srcAddress, long len) {
        if (0 == srcAddress) {
            return null;
        }

        if (0 == len) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] toBytes = new byte[(int) len];
        copyMemory(toBytes, srcAddress, len);

        return toBytes;
    }

    /**
     * Given an address into memory, copy the bytes into a new byte array.
     * 
     * @param encodedByteArray unsafe allocated address that we can copy from/
     * @return Java ByteArray, null on null, 0 on empty string
     */
    public static byte[] decodeByteArray(MissingFingers encodedByteArray) {
        if (null == encodedByteArray) {
            return null;
        }
        return decodeByteArray(encodedByteArray.getAddress(), encodedByteArray.getLength());
    }

    /**
     * Given an address into memory, copy the bytes into a new byte array. Also free the memory upon completion using
     * Unsafe.freeMemory If you didn't allocate this via unsafe, things will likely go south. If you allocated with
     * malloc, they might not unless you turn on NativeMemoryTracking then they will for sure. haha.
     * 
     * @param encodedByteArray unsafe allocated address that we can copy from/
     * @return Java ByteArray, null on null, 0 on empty string
     */
    public static byte[] decodeByteArrayAndFree(MissingFingers encodedByteArray) {
        try {
            if (null == encodedByteArray) {
                return null;
            }
            return decodeByteArray(encodedByteArray.getAddress(), encodedByteArray.getLength());
        } finally {
            if (null != encodedByteArray) {
                encodedByteArray.close();
            }
        }
    }

    public static long arrayIndexScale(Class<?> arrayClass) {
        return unsafe.arrayIndexScale(arrayClass);
    }

    public static void putLong(long address, long data) {
        unsafe.putLong(address, data);
    }

    public static void putInt(long address, int data) {
        unsafe.putInt(address, data);
    }

    public static void putShort(long address, short data) {
        unsafe.putShort(address, data);
    }

    public static void putByte(long address, byte data) {
        unsafe.putByte(address, data);
    }

    public static void putAddress(long address, long newAddress) {
        unsafe.putAddress(address, newAddress);
    }

    public static byte getByte(long address) {
        return unsafe.getByte(address);
    }

    public static short getShort(long address) {
        return unsafe.getShort(address);
    }

    public static int getInt(long address) {
        return unsafe.getInt(address);
    }

    public static long getLong(long address) {
        return unsafe.getLong(address);
    }

    public static long getAddress(long address) {
        return unsafe.getAddress(address);
    }

    public static int getStringLength(byte[] b) {
        if (null == b) {
            return 0;
        }

        for (int i = 0; i < b.length; i++) {
            if (0 == b[i]) {
                return i;
            }
        }

        return b.length;
    }

    public static String decodeStringWithLength(long address) {
        if (0 == address) {
            return null;
        }

        // first int is the length.
        int len = getInt(address);
        if (0 == len) {
            return "";
        }

        // then we can just decode from there. we add 32 bits for the length.
        return decodeString(address + 4, len);
    }
}
