class ThreadUtil {
    public static void printIfDaemon(Thread thread) {
        if (thread.isDaemon()) {
            System.out.println("daemon");
        } else {
            System.out.println("not daemon");
        }
    }
}