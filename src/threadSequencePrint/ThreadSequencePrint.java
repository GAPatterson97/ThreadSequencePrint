package threadSequencePrint;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSequencePrint {
    private static final int MAX = 50;
    private int count = 1;
    private int turn = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition[] conditions = new Condition[3];

    public ThreadSequencePrint() {
        for (int i = 0; i < 3; i++) {
            conditions[i] = lock.newCondition();
        }
    }

    public void start() {
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            new Thread(() -> print(threadId)).start();
        }
    }

    private void print(int threadId) {
        while (true) {
            lock.lock();
            try {
                while (count <= MAX && turn != threadId) {
                    conditions[threadId].await();
                }

                if (count > MAX) {
                    conditions[(threadId + 1) % 3].signal(); // Wake next thread to exit
                    break;
                }

                System.out.println("ThreadID: " + threadId + "   " + count);
                count++;
                turn = (turn + 1) % 3;
                conditions[turn].signal(); // Wake up next thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        new ThreadSequencePrint().start();
    }
}