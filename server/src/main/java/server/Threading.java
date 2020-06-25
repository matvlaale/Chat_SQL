package server;

public class Threading {

    static char nextLetter = 'A';
    static int iterations = 5;
    final static Object monitor = new Object();

    public static void main(String[] args) {
        Thread tA = new Thread(new ThreadLetter('A', 'B'));
        Thread tB = new Thread(new ThreadLetter('B', 'C'));
        Thread tC = new Thread(new ThreadLetter('C', 'A'));
        tA.start();
        tB.start();
        tC.start();
    }
}

class ThreadLetter implements Runnable {
    char letter;
    char next;

    @Override
    public void run() {
        try {
            for (int i = 0; i < Threading.iterations; i++) {
                synchronized (Threading.monitor) {
                    while (Threading.nextLetter != letter) Threading.monitor.wait();
                    System.out.print(letter);
                    Threading.nextLetter = next;
                    Threading.monitor.notifyAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ThreadLetter(char x, char y) {
        letter = x;
        next = y;
    }
}