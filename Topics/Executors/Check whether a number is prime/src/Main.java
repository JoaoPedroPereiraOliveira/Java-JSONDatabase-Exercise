import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        while (scanner.hasNext()) {
            int number = scanner.nextInt();
            executor.submit(() -> {
                PrintIfPrimeTask printIfPrimeTask = new PrintIfPrimeTask(number);
                Thread thread = new Thread(printIfPrimeTask);
                thread.start();
            });
        }
        executor.shutdown();
    }
}

class PrintIfPrimeTask implements Runnable {
    private final int number;

    public PrintIfPrimeTask(int number) {
        this.number = number;
    }

    @Override
    public void run() {
        // Check if number is less than
        // equal to 1
        if (number <= 1)
            return ;

            // Check if number is 2
        else if (number == 2)
            System.out.println(number);

            // Check if n is a multiple of 2
        else if (number % 2 == 0)
            return ;

        // If not, then just check the odds
        for (int i = 3; i <= Math.sqrt(number); i += 2) {
            if (number % i == 0)
                return ;
        }
        System.out.println(number);
    }
}