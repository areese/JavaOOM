This is an experiment for running java out of heap or out of memory in limited situations.

 java -Xmx100m -cp bin/ JavaOOM 1000 1000 50 300

args are:
 size in bytes of byte[] to allocate
 size in bytes of memory to allocate via Unsafe (replicates how jni would behave)
 size in MB of max amount of java-heap to allocate
 size in MB of max amount of c-heap to use