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
                  IPT[i] = new Information(null, null);
                }
                swapFile = ThreadedKernel.fileSystem.open("swapFile", true);
                freeSwapPages = new LinkedList<Integer>();
                num_sp = 0;
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

        protected class Information{
          public VMProcess process;
          public TranslationEntry entry;

          public Information(VMProcess process, TranslationEntry entry){
            this.process = process;
            this.entry = entry;
          }           
        }
}
