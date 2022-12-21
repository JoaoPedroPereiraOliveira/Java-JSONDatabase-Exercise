class Starter {

    public static void startRunnables(Runnable[] runnables) {
        int count = 0;
        for (Runnable r : runnables) {
            Thread thread = new Thread(r, count + "");
            thread.start();
            count++;
        }
    }
}