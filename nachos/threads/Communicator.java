package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
    private Condition2 condition_speaker;
    private Condition2 condition_listen;
    private Condition2 condition_return_speaker;
    private Condition2 condition_return_listen;
    private int num_listen;
    private int num_speaker;
    private Lock lock;
    private int message;
    private boolean finished;
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        message = 0;
        lock = new Lock();
        num_speaker = 0;
        num_listen = 0;
        finished = true;
        condition_return_listen = new Condition2(lock);
        condition_return_speaker = new Condition2(lock);
        condition_speaker = new Condition2(lock);
        condition_listen = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     * 
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     * 
     * @param word the integer to transfer.
     */
    public void speak(int word) {
        boolean status = Machine.interrupt().disable();
        lock.acquire();
        if(num_listen > 0){
            while(!finished){
                lock.release();
                KThread.yield();
                lock.acquire();
            }

	    System.out.println( KThread.currentThread().getName() + " is speaking-----");

            finished = false;
            message = word;
            condition_listen.wake();
            num_listen--;
        }
        else{
            num_speaker++;
            condition_speaker.sleep();
            while(!finished){
                lock.release();
                KThread.yield();
                lock.acquire();
            }

	    System.out.println( KThread.currentThread().getName() + " is speaking-----");

            finished = false;
            message = word;
            condition_return_listen.wake();
        }
        
        lock.release();
        Machine.interrupt().restore(status);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return the
     * <i>word</i> that thread passed to <tt>speak()</tt>.
     * 
     * @return the integer transferred.
     */
    public int listen() {
        boolean status = Machine.interrupt().disable();
        lock.acquire();
        if(num_speaker > 0){
            condition_speaker.wake();
            num_speaker--;
            condition_return_listen.sleep();
	    System.out.println( KThread.currentThread().getName() + " is listening----");
            finished = true;
        }
        else{
            num_listen++;
            condition_listen.sleep();
	    System.out.println( KThread.currentThread().getName() + " is listening----");
            finished = true;
        }
        lock.release();
        Machine.interrupt().restore(status);
        return message;
    }
    
    // Place Communicator test code inside of the Communicator class.

    // A more complex test program for Communicator.  Do not use this
    // test program as your first Communicator test.  First test it
    // with more basic test programs to verify specific functionality,
    // and then try this test program.
    
    public static void commTest6() {
    final Communicator com = new Communicator();
    final long times[] = new long[20];
    final int words[] = new int[20];
    KThread speaker1 = new KThread( new Runnable () {
        public void run() {
            com.speak(4);
            times[0] = Machine.timer().getTime();
        }
        });
    speaker1.setName("S1");
    KThread speaker2 = new KThread( new Runnable () {
        public void run() {
            com.speak(7);
            times[1] = Machine.timer().getTime();
        }
        });
    speaker2.setName("S2");
    KThread listener1 = new KThread( new Runnable () {
        public void run() {
            times[2] = Machine.timer().getTime();
            words[0] = com.listen();
        }
        });
    listener1.setName("L1");
    KThread listener2 = new KThread( new Runnable () {
        public void run() {
            times[3] = Machine.timer().getTime();
            words[1] = com.listen();
        }
        });
    listener2.setName("L2");

    //new tests
    KThread speaker3 = new KThread( new Runnable () {
        public void run() {
            com.speak(1);
            times[4] = Machine.timer().getTime();
        }
        });
    speaker3.setName("S3");


    KThread speaker4 = new KThread( new Runnable () {
        public void run() {
            com.speak(2);
            times[5] = Machine.timer().getTime();
        }
        });
    speaker4.setName("S4");

    KThread speaker5 = new KThread( new Runnable () {
        public void run() {
            com.speak(3);
            times[6] = Machine.timer().getTime();
        }
        });
    speaker5.setName("S5");



    KThread listener3 = new KThread( new Runnable () {
        public void run() {
            times[7] = Machine.timer().getTime();
            words[2] = com.listen();
        }
        });
    listener3.setName("L3");

    KThread listener4 = new KThread( new Runnable () {
        public void run() {
            times[8] = Machine.timer().getTime();
            words[3] = com.listen();
        }
        });
    listener4.setName("L4");

    KThread speaker6 = new KThread( new Runnable () {
        public void run() {
            com.speak(4);
            times[9] = Machine.timer().getTime();
        }
        });
    speaker6.setName("S6");


    KThread listener5 = new KThread( new Runnable () {
        public void run() {
            times[10] = Machine.timer().getTime();
            words[4] = com.listen();
        }
        });
    listener5.setName("L5");

    KThread listener6 = new KThread( new Runnable () {
        public void run() {
            times[11] = Machine.timer().getTime();
            words[5] = com.listen();
        }
        });
    listener6.setName("L6");



    
    speaker1.fork(); speaker2.fork(); listener1.fork(); listener2.fork();
    speaker1.join(); speaker2.join(); listener1.join(); listener2.join();
    
    Lib.assertTrue(words[0] == 4, "Didn't listen back spoken word."); 
    Lib.assertTrue(words[1] == 7, "Didn't listen back spoken word.");
    Lib.assertTrue(times[0] > times[2], "speak() returned before listen() called.");
    Lib.assertTrue(times[1] > times[3], "speak() returned before listen() called.");
    System.out.println("commTest6 successful!");

    System.out.println("----------Additional Communicator Tests-------------");
    speaker3.fork(); speaker4.fork(); speaker5.fork(); listener3.fork(); listener4.fork(); speaker6.fork(); listener5.fork(); listener6.fork();
    speaker3.join(); speaker4.join(); speaker5.join(); speaker6.join(); listener3.join(); listener4.join(); listener5.join(); listener6.join();

    System.out.println(words[2]);
    System.out.println(words[3]);
    System.out.println(words[4]);
    System.out.println(words[5]);

    System.out.println("-----------All Communicator Tests Done-------------");


    }

    // Invoke Communicator.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
    // place calls to simpler Communicator tests that you implement here

    commTest6();
    }
}




