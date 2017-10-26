package nachos.threads;

import java.util.*;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     * 
     * <p>
     * <b>Note</b>: Nachos will not function correctly with more than one alarm.
     */
    public Queue<AKThread> threadQ = new PriorityQueue<AKThread>(5000, new Comparator<AKThread> (){
        public int compare(AKThread t1, AKThread t2){
            if(t1.wake_time > t2.wake_time){
                return 1;
            }

            else if(t1.wake_time < t2.wake_time){
                return -1;
            }

            else {
                return 0;
            }


        }});


    class AKThread  {
        KThread thread;
        long wake_time;


        AKThread( KThread k, long t){
            thread = k;
            wake_time = t;
        }

    }

    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt();
            }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread that
     * should be run.
     */
    public void timerInterrupt() {
        boolean status = Machine.interrupt().disable();
        if(!threadQ.isEmpty()){
            AKThread toCheck = threadQ.peek();
            if(toCheck.wake_time <= Machine.timer().getTime()){
                toCheck.thread.ready();
                threadQ.poll();
            }
        }
        Machine.interrupt().restore(status);
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
     * in the timer interrupt handler. The thread must be woken up (placed in
     * the scheduler ready set) during the first timer interrupt where
     * 
     * <p>
     * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
     * 
     * @param x the minimum number of clock ticks to wait.
     * 
     * @see nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        boolean status = Machine.interrupt().disable();
        if(x <= 0){
            return;
        }
        long wakeTime = Machine.timer().getTime() + x;
        if(wakeTime > Machine.timer().getTime()){
            AKThread toWait = new AKThread(KThread.currentThread(), wakeTime);
            if(threadQ.size() < 5000){
              threadQ.add(toWait);
              KThread.sleep();
            }
        }
        Machine.interrupt().restore(status);
    }

    // Add Alarm testing code to the Alarm class

    public static void alarmTest1() {
        int durations[] = {1000, 10*1000, 100*1000};
        long t0, t1;

        for (int d : durations) {
            t0 = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil (d);
            t1 = Machine.timer().getTime();
            System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
        }
    }
     private static class alarmTest2 implements Runnable {
         int durations[] = {1000, 10*1000, 100*1000};
            long t0, t1;
            int name;
            alarmTest2(int input){
                name = input;
            }
              public void run() {
                  for (int d : durations) {
                    t0 = Machine.timer().getTime();
                    ThreadedKernel.alarm.waitUntil (d);
                    t1 = Machine.timer().getTime();
                    System.out.println("d" + d);
                    System.out.println("t0: " + t0);
                    System.out.println("t1: " + t1);
                    System.out.println (name + "alarmTest1: waited for " + (t1 - t0) + " ticks");
                }
              }
          }
    

    // Implement more test methods here ...

    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
        alarmTest1();
        alarmTest2 test2 = new alarmTest2(999);
        new KThread(test2).fork();
        new alarmTest2(11).run();
        // Invoke your other test methods here ...
    }
}



