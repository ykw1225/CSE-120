package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import java.util.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();        
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
                victim = 0;
                IPT = new Information[Machine.processor().getNumPhysPages()];
                for(int i = 0; i < Machine.processor().getNumPhysPages(); i++){
                  IPT[i] = new Information(null, null, false);
                }
                swapFile = ThreadedKernel.fileSystem.open("swapFile", true);
                freeSwapPages = new LinkedList<Integer>();
                num_sp = 0;
                vmmutex = new Lock();
                CV = new Condition(vmmutex);
                pinCount = 0;
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
                swapFile.close();
                ThreadedKernel.fileSystem.remove("swapFile");
		super.terminate();
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

        public static int victim;

        public static Information IPT[];

        public static LinkedList<Integer> freeSwapPages;

        public static OpenFile swapFile;

        public static int num_sp;
     
        public static Lock vmmutex;

        public static Condition CV;

        public static int pinCount;

        protected class Information{
          public VMProcess process;
          public TranslationEntry entry;
          public boolean pin;

          public Information(VMProcess process, TranslationEntry entry, boolean pin){
            this.process = process;
            this.entry = entry;
            this.pin = pin;
          }           
        }
}
