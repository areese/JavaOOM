This is an experiment for running java out of heap or out of memory in limited situations.

Build using:

./gradlew build

Download and untar the tarballs for jdk8 and 9ea into a directory.
(In my case the directories are jre-9 and jdk1.8.0_121)



args are:
[-bytes <1024>] [-debug] [-bytebuffer <0>]  [-maxbytebufferusage <0>(MB)] [-maxheapusage <100>(MB)] [-maxunsafeusage <0>(MB)][-print <10000>] [-unsafe <0>]

* Any allocation size of 0 means that type will not be allocated.

-bytes is the size in bytes of each byte[] to allocate, default is 1k
-bytebuffer is the size in bytes of each DirectByteBuffer to allocate, default is 0
-maxbytebufferusage is an approximate of how much to allocate using ByteBuffer.allocateDirect(), total allocations is (maxbytebufferusage * 1024) / bytebuffer
-maxheapusage is an approximate of how many byte[] to allocate, total allocations is (maxheapusage * 1024) / bytes
-maxunsafeusage is an approximate of how much to allocate using Unsafe.alllocateMemory(), total allocations is (maxunsafeusage * 1024) / unsafe



Run docker with memory and cpu limits:

```
sudo docker run -v `pwd`:/ws -i -t --detach=false --cpuset-cpus="1" -m 200MB --memory-swap 200MB docker.io/centos /bin/bash
```

Try JDK8 without a limiting heap, allocating 1k byte arrays at a time up to an approximate of 500MB printing every 20k iterations, the JVM is killed:

```
/ws/jdk1.8.0_121/bin/java -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000

[root@07c2e4abe802 /]# /ws/jdk1.8.0_121/bin/java -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000
currentSize = 0 totalSize = 0 heapSize = 59 heapMaxSize = 880 heapFreeSize = 58
Args [bytesize=1024, unsafeSize=0, totalSize=0, byteBufferSize=0, printAt=20000, debug=false, maxheapusage=500MB, maxunsafeusage=0MB, maxbytebufferusage=0MB]
Starting, allocating 1024 sized byte buffers  up to 500 and 0 unsafe buffers up to 0 for a total of 500
currentSize = 19 totalSize = 500 heapSize = 59 heapMaxSize = 880 heapFreeSize = 37
currentSize = 39 totalSize = 500 heapSize = 101 heapMaxSize = 880 heapFreeSize = 60
currentSize = 58 totalSize = 500 heapSize = 101 heapMaxSize = 880 heapFreeSize = 40
currentSize = 78 totalSize = 500 heapSize = 156 heapMaxSize = 880 heapFreeSize = 73
currentSize = 97 totalSize = 500 heapSize = 217 heapMaxSize = 880 heapFreeSize = 115
currentSize = 117 totalSize = 500 heapSize = 217 heapMaxSize = 880 heapFreeSize = 94
Killed
```

JDK9 without max heap, and without cgroups flags:
```
/ws/jre-9/bin/java -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000

[root@07c2e4abe802 /]# /ws/jre-9/bin/java -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000
currentSize = 0 totalSize = 0 heapSize = 59 heapMaxSize = 957 heapFreeSize = 58
Args [bytesize=1024, unsafeSize=0, totalSize=0, byteBufferSize=0, printAt=20000, debug=false, maxheapusage=500MB, maxunsafeusage=0MB, maxbytebufferusage=0MB]
Starting, allocating 1024 sized byte buffers  up to 500 and 0 unsafe buffers up to 0 for a total of 500
currentSize = 19 totalSize = 500 heapSize = 59 heapMaxSize = 957 heapFreeSize = 38
currentSize = 39 totalSize = 500 heapSize = 59 heapMaxSize = 957 heapFreeSize = 18
currentSize = 58 totalSize = 500 heapSize = 112 heapMaxSize = 957 heapFreeSize = 51
currentSize = 78 totalSize = 500 heapSize = 112 heapMaxSize = 957 heapFreeSize = 30
currentSize = 97 totalSize = 500 heapSize = 112 heapMaxSize = 957 heapFreeSize = 10
currentSize = 117 totalSize = 500 heapSize = 253 heapMaxSize = 957 heapFreeSize = 129
currentSize = 136 totalSize = 500 heapSize = 253 heapMaxSize = 957 heapFreeSize = 110
currentSize = 156 totalSize = 500 heapSize = 253 heapMaxSize = 957 heapFreeSize = 89
Killed
```

JDK9 without max heap, and with cgroups flags:
Nice OOM.
```
/ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000

[root@07c2e4abe802 /]# /ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 1024 -maxheapusage 500 -print 20000
currentSize = 0 totalSize = 0 heapSize = 7 heapMaxSize = 96 heapFreeSize = 6
Args [bytesize=1024, unsafeSize=0, totalSize=0, byteBufferSize=0, printAt=20000, debug=false, maxheapusage=500MB, maxunsafeusage=0MB, maxbytebufferusage=0MB]
Starting, allocating 1024 sized byte buffers  up to 500 and 0 unsafe buffers up to 0 for a total of 500
currentSize = 19 totalSize = 500 heapSize = 31 heapMaxSize = 96 heapFreeSize = 10
currentSize = 39 totalSize = 500 heapSize = 72 heapMaxSize = 96 heapFreeSize = 30
currentSize = 58 totalSize = 500 heapSize = 72 heapMaxSize = 96 heapFreeSize = 10
currentSize = 78 totalSize = 500 heapSize = 96 heapMaxSize = 96 heapFreeSize = 14
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at JavaOOM.main(JavaOOM.java:119)

```

JDK9 with max heap, and with cgroups flags, allocating DirectByteBuffers
Nice OOM.
```
/ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 0 -bytebuffer 1024 -maxbytebufferusage 500 -print 20000

[root@07c2e4abe802 /]# /ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 0 -bytebuffer 1024 -maxbytebufferusage 500 -print 20000
currentSize = 0 totalSize = 0 heapSize = 7 heapMaxSize = 96 heapFreeSize = 6
Args [bytesize=0, unsafeSize=0, totalSize=0, byteBufferSize=1024, printAt=20000, debug=false, maxheapusage=100MB, maxunsafeusage=0MB, maxbytebufferusage=500MB]
Starting, allocating 0 sized byte buffers  up to 100 and 0 unsafe buffers up to 0 for a total of 100
currentSize = 19 totalSize = 100 heapSize = 7 heapMaxSize = 96 heapFreeSize = 3
currentSize = 39 totalSize = 100 heapSize = 13 heapMaxSize = 96 heapFreeSize = 7
currentSize = 58 totalSize = 100 heapSize = 13 heapMaxSize = 96 heapFreeSize = 4
currentSize = 78 totalSize = 100 heapSize = 13 heapMaxSize = 96 heapFreeSize = 0
Exception in thread "main" java.lang.OutOfMemoryError: Direct buffer memory
	at java.base/java.nio.Bits.reserveMemory(Unknown Source)
	at java.base/java.nio.DirectByteBuffer.<init>(Unknown Source)
	at java.base/java.nio.ByteBuffer.allocateDirect(Unknown Source)
	at JavaOOM.main(JavaOOM.java:133)
```


JDK9 with max heap, and with cgroups flags, allocating using Unsafe:

Unexpectedly killed, I expected it to act the way ByteBuffer does, but I have not looked at
changes to DirectByteBuffer and how it allocates.

I need to add a JNI test that allocates the same way, as I expect similiar behaviour.

```
/ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 0 -unsafe 1024 -maxunsafeusage 500 -print 20000

[root@07c2e4abe802 /]# /ws/jre-9/bin/java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap  -cp /ws/JavaOOM/build/libs/JavaOOM.jar  JavaOOM -bytes 0 -unsafe 1024 -maxunsafeusage 500 -print 20000
currentSize = 0 totalSize = 0 heapSize = 7 heapMaxSize = 96 heapFreeSize = 6
Args [bytesize=0, unsafeSize=1024, totalSize=0, byteBufferSize=0, printAt=20000, debug=false, maxheapusage=100MB, maxunsafeusage=500MB, maxbytebufferusage=0MB]
Starting, allocating 0 sized byte buffers  up to 100 and 1024 unsafe buffers up to 500 for a total of 600
currentSize = 19 totalSize = 600 heapSize = 7 heapMaxSize = 96 heapFreeSize = 5
currentSize = 39 totalSize = 600 heapSize = 7 heapMaxSize = 96 heapFreeSize = 4
currentSize = 58 totalSize = 600 heapSize = 7 heapMaxSize = 96 heapFreeSize = 3
currentSize = 78 totalSize = 600 heapSize = 7 heapMaxSize = 96 heapFreeSize = 2
currentSize = 97 totalSize = 600 heapSize = 13 heapMaxSize = 96 heapFreeSize = 7
currentSize = 117 totalSize = 600 heapSize = 13 heapMaxSize = 96 heapFreeSize = 6
currentSize = 136 totalSize = 600 heapSize = 13 heapMaxSize = 96 heapFreeSize = 5
currentSize = 156 totalSize = 600 heapSize = 13 heapMaxSize = 96 heapFreeSize = 4
Killed

```

