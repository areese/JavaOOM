
public class Args {

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    public int byteSize = KB;
    public int unsafeSize = 0;
    public long totalSize = 0;
    public long byteBufferSize = 0;
    public int printAt = 10000;
    public boolean debug = false;
    public int maxheapusage = 100 * MB;
    public int maxunsafeusage = 0;
    public int maxbytebufferusage = 0;

    public Args(String[] args) {
        parseArgs(args);
    }

    void parseArgs(String[] args) {
        for (int i = 0; i < args.length;) {
            String arg = args[i++];

            switch (arg.toLowerCase()) {
                case "-h":
                case "-help":
                    help();
                    System.exit(0);
                    break;

                case "-d":
                case "-debug":
                    debug = true;
                    break;

                case "-bytes":
                case "-bytesize":
                    byteSize = Integer.parseInt(args[i++]);
                    break;

                case "-bytebuffer":
                case "-bytebuffersize":
                    byteBufferSize = Integer.parseInt(args[i++]);
                    break;

                case "-maxbytebufferusage":
                    maxbytebufferusage = Integer.parseInt(args[i++]) * MB;
                    break;

                case "-maxheapusage":
                    maxheapusage = Integer.parseInt(args[i++]) * MB;
                    break;

                case "-maxunsafeusage":
                    maxunsafeusage = Integer.parseInt(args[i++]) * MB;
                    break;

                case "-print":
                case "-printat":
                    printAt = Integer.parseInt(args[i++]);
                    break;

                case "-unsafe":
                case "-unsafesize":
                    unsafeSize = Integer.parseInt(args[i++]);
                    break;
            }
        }
    }

    public static void help() {
        System.out.println(
                        "JavaOOM [-bytes <1024>] [-debug] [-bytebuffer <0>]  [-maxbytebufferusage <0>(MB)] [-maxheapusage <100>(MB)] [-maxunsafeusage <0>(MB)][-print <10000>] [-unsafe <0>]");
        System.out.println("");
        System.out.println("* Any allocation size of 0 means that type will not be allocated.");
        System.out.println("");
        System.out.println("-bytes is the size in bytes of each byte[] to allocate, default is 1k");
        System.out.println("-bytebuffer is the size in bytes of each DirectByteBuffer to allocate, default is 0");
        System.out.println(
                        "-maxbytebufferusage is an approximate of how much to allocate using ByteBuffer.allocateDirect(), total allocations is (maxbytebufferusage * 1024) / bytebuffer");
        System.out.println(
                        "-maxheapusage is an approximate of how many byte[] to allocate, total allocations is (maxheapusage * 1024) / bytes");
        System.out.println(
                        "-maxunsafeusage is an approximate of how much to allocate using Unsafe.alllocateMemory(), total allocations is (maxunsafeusage * 1024) / unsafe");
    }

    public static long mb(long i) {
        return i / MB;
    }

    public static void main(String[] args) {
        Args a = new Args(args);
        System.out.println(a);
    }

    @Override
    public String toString() {
        return "Args [bytesize=" + byteSize + ", unsafeSize=" + unsafeSize + ", totalSize=" + totalSize
                        + ", byteBufferSize=" + byteBufferSize + ", printAt=" + printAt + ", debug=" + debug
                        + ", maxheapusage=" + mb(maxheapusage) + "MB, maxunsafeusage=" + mb(maxunsafeusage)
                        + "MB, maxbytebufferusage=" + mb(maxbytebufferusage) + "MB]";
    }
}

