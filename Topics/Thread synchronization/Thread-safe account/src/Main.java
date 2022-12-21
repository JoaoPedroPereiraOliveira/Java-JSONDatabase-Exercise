
class Account {

    private long balance = 0;

    public synchronized boolean withdraw(long amount) {
        if (balance - amount >= 0) {
            balance -= amount;
            return true;
        }


        return false;
    }

    public synchronized void deposit(long amount) {
        balance += amount;
    }

    public long getBalance() {
        return balance;
    }
}
