
class UseThreadLocal {
    public static ThreadLocal<Integer> makeThreadLocal(int counter) {
        ThreadLocal<Integer> sol = new ThreadLocal<>();
        sol.set(counter + 1);
        return sol;
    }
}