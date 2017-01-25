
public class Args {

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    public int byteSize = 1000;
    public int unsafeSize = 0;
    public long totalSize = 0;
    public long byteBufferSize = 0;
    public int printAt = 100;
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
                case "-d":
                case "-debug":
                    debug = true;
                    break;

                case "-bytes":
                case "-bytesize":
                    byteSize = Integer.parseInt(args[i++]);
                    break;

                case "-unsafe":
                case "-unsafesize":
                    unsafeSize = Integer.parseInt(args[i++]);
                    break;

                case "-print":
                case "-printat":
                    printAt = Integer.parseInt(args[i++]);
                    break;

                case "-bytebuffer":
                case "-bytebuffersize":
                    byteBufferSize = Integer.parseInt(args[i++]);
                    break;

                case "-maxheapusage": {
                    maxheapusage = Integer.parseInt(args[i++]) * MB;
                }
                    break;

                case "-maxunsafeusage":
                    maxunsafeusage = Integer.parseInt(args[i++]) * MB;
                    break;

                case "-maxbytebufferusage":
                    maxbytebufferusage = Integer.parseInt(args[i++]) * MB;
                    break;
            }
        }
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

