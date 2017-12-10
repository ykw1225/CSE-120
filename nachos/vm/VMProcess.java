package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
                pageTable = new TranslationEntry[numPages];
                for(int i = 0; i < numPages; i++){
                  pageTable[i] = new TranslationEntry(i, i, false, false, false, false);
                }
		// load sections
		return true;
		//return super.loadSections();
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

        public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

                if(vaddr < 0){
                  return 0;
                }
                int left = length;
                int amount = 0;
                int cur_offset = offset;
                int total_read = 0;
                int paddr = -1;
                int paddr_offset = Processor.offsetFromAddress(vaddr);
                int vpn = Processor.pageFromAddress(vaddr);
               
                
                if(vpn >= pageTable.length || vpn < 0){
                  return total_read;
                }
                VMKernel.pinLock.acquire();
                if(pageTable[vpn].valid){
                  VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                  VMKernel.pinCount++;
                  VMKernel.pinLock.release();
                  paddr = pageTable[vpn].ppn * pageSize + paddr_offset; // if paddr but not good used bit set?????
                  pageTable[vpn].used = true;
                }
                else{
                  VMKernel.pinLock.release();
                  handlePageFault(vaddr); // an error may occur??????
                  VMKernel.pinLock.acquire();
                  if(pageTable[vpn].valid){
                    VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                    VMKernel.pinCount++;
                    VMKernel.pinLock.release();
                    paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
                    pageTable[vpn].used = true;
                  }
                  else{
                    VMKernel.pinLock.release();
                    return total_read;
                  }
                }
		// for now, just assume that virtual addresses equal physical addresses
		if (paddr < 0 || paddr >= memory.length){
                        VMKernel.pinLock.acquire();
                        VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                        VMKernel.pinCount--;
                        VMKernel.CV.wake();
                        VMKernel.pinLock.release();
			return 0;
                }

                amount = Math.min(left, (pageSize - paddr_offset));
                System.arraycopy(memory, paddr, data, offset, amount);
                VMKernel.pinLock.acquire();
                VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                VMKernel.pinCount--;
                VMKernel.CV.wake();
                VMKernel.pinLock.release();
                total_read += amount;
                cur_offset += amount;
                left -= amount;
                while(left > 0){
                  vpn++;
                  if(vpn >= pageTable.length || vpn < 0){
                    return total_read;
                  }
                  VMKernel.pinLock.acquire();
                  if(pageTable[vpn].valid){
                    VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                    VMKernel.pinCount++;
                    VMKernel.pinLock.release();
                    paddr = pageTable[vpn].ppn * pageSize;
                    pageTable[vpn].used = true;
                  }
                  else{
                    vaddr = Processor.makeAddress(vpn, 0);
                    VMKernel.pinLock.release();
                    handlePageFault(vaddr); // an error may occurrrrrr?????
                    VMKernel.pinLock.acquire();
                    if(pageTable[vpn].valid){  // valid means correct?????
                      VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                      VMKernel.pinCount++;
                      VMKernel.pinLock.release();
                      paddr = pageTable[vpn].ppn * pageSize;
                      pageTable[vpn].used = true;
                    }
                    else{
                      VMKernel.pinLock.release();
                      return total_read; // else return immedia????????
                    }
                  }
                  
                  if(paddr < 0 || paddr >= memory.length){
                    VMKernel.pinLock.acquire();
                    VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                    VMKernel.pinCount--;
                    VMKernel.CV.wake();
                    VMKernel.pinLock.release();
                    return total_read;
                  }
                  amount = Math.min(left, pageSize);
                  System.arraycopy(memory, paddr, data, cur_offset, amount);
                  VMKernel.pinLock.acquire();
                  VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                  VMKernel.pinCount--;
                  VMKernel.CV.wake();
                  VMKernel.pinLock.release();
                  total_read += amount;
                  cur_offset += amount;
                  left -= amount;
                }

		return total_read;
	}


        public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

                if(vaddr < 0){
                  return 0;
                }
                int left = length;
                int amount = 0;
                int cur_offset = offset;
                int total_write = 0;
                int paddr = -1;
                int paddr_offset = Processor.offsetFromAddress(vaddr);
                int vpn = Processor.pageFromAddress(vaddr);
                
                if(vpn >= pageTable.length || vpn < 0){
                  return total_write;
                }

                VMKernel.pinLock.acquire();
                if(pageTable[vpn].valid){
                  VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                  VMKernel.pinCount++;
                  VMKernel.pinLock.release();
                  if(pageTable[vpn].readOnly == false){
                    paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
                  }
                  pageTable[vpn].used = true;
                }
                else{
                  VMKernel.pinLock.release();
                  handlePageFault(vaddr); // an error may occur??????
                  VMKernel.pinLock.acquire();
                  if(pageTable[vpn].valid){
                    if(pageTable[vpn].readOnly == false){
                      VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                      VMKernel.pinCount++;
                      VMKernel.pinLock.release();
                      paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
                    }
                    else{
                      VMKernel.pinLock.release();
                      return total_write;
                    }
                    pageTable[vpn].used = true; //?????????????????
                  }
                  else{
                    VMKernel.pinLock.release();
                    return total_write;
                  }
                }


		// for now, just assume that virtual addresses equal physical addresses
		if (paddr < 0 || paddr >= memory.length){
                        VMKernel.pinLock.acquire();
                        VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                        VMKernel.pinCount--;
                        VMKernel.CV.wake();
                        VMKernel.pinLock.release();
			return 0;
                }

                amount = Math.min(left, (pageSize - paddr_offset));
                System.arraycopy(data, offset, memory, paddr, amount);
                if(amount > 0){
                  pageTable[vpn].dirty = true;
                }
                VMKernel.pinLock.acquire();
                VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                VMKernel.pinCount--;
                VMKernel.CV.wake();
                VMKernel.pinLock.release();
                total_write += amount;
                cur_offset += amount;
                left -= amount;
                while(left > 0){
                  vpn++;
                  if(vpn >= pageTable.length || vpn < 0){
                    return total_write;
                  }
                  VMKernel.pinLock.acquire();
                  if(pageTable[vpn].valid){
                    pageTable[vpn].used = true; // two different confusion need to be confirmed 
                    if(pageTable[vpn].readOnly == false){
                      VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                      VMKernel.pinCount++;
                      VMKernel.pinLock.release();
                      paddr = pageTable[vpn].ppn * pageSize;
                      //?????????????????????????? what suan access guo?????
                    }
                    else{
                      VMKernel.pinLock.release();
                      return total_write;
                    }
                  }
                  else{
                    vaddr = Processor.makeAddress(vpn, 0);
                    VMKernel.pinLock.release();
                    handlePageFault(vaddr); // an error may occur??????
                    VMKernel.pinLock.acquire();
                    if(pageTable[vpn].valid){
                      pageTable[vpn].used = true;
                      if(pageTable[vpn].readOnly == false){
                        VMKernel.IPT[pageTable[vpn].ppn].pin = true;
                        VMKernel.pinCount++;
                        VMKernel.pinLock.release();
                        paddr = pageTable[vpn].ppn * pageSize;
                      }
                      else{
                        VMKernel.pinLock.release();
                        return total_write;
                      }
                    }
                    else{
                      VMKernel.pinLock.release();
                      return total_write;
                    }
                  }
                  
                  if(paddr < 0 || paddr >= memory.length){
                    VMKernel.pinLock.acquire();
                    VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                    VMKernel.pinCount--;
                    VMKernel.CV.wake();
                    VMKernel.pinLock.release();
                    return total_write;
                  }
                  amount = Math.min(left, pageSize);
                  System.arraycopy(data, cur_offset, memory, paddr, amount);
                  if(amount > 0){
                    pageTable[vpn].dirty = true;
                  }
                  VMKernel.pinLock.acquire();
                  VMKernel.IPT[pageTable[vpn].ppn].pin = false;
                  VMKernel.pinCount--;
                  VMKernel.CV.wake();
                  VMKernel.pinLock.release();
                  total_write += amount;
                  cur_offset += amount;
                  left -= amount;
                }

		return total_write;

		
	}


        protected void handlePageFault(int badVaddr){
                int badVpn = Processor.pageFromAddress(badVaddr);
                int coffVpn = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");
                        
                        
			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;
                                coffVpn = vpn;
                                if(vpn == badVpn){
                                  int ppn = 0;
				  // for now, just assume virtual addresses=physical addresses
				  if(!UserKernel.free_pages.isEmpty()){
				    ppn = UserKernel.free_pages.removeLast();
                                  }
                                  else{
                                    VMKernel.pinLock.acquire();
                                    if(VMKernel.pinCount == Machine.processor().getNumPhysPages()){
                                      VMKernel.CV.sleep();
                                    } // for what?????????? spinning lock???????
                                    VMKernel.pinLock.release();
                                    while(true){
                                      // add lock tomorrow
                                      VMKernel.pinLock.acquire();
                                      if(VMKernel.IPT[VMKernel.victim].pin == true){
                                        VMKernel.pinLock.release();
                                        VMKernel.victim = (VMKernel.victim + 1) % Machine.processor().getNumPhysPages();
                                        continue;
                                      }
                                      VMKernel.pinLock.release();
                                      if(VMKernel.IPT[VMKernel.victim].entry.used == false){
                                        break;
                                      }
                                      VMKernel.IPT[VMKernel.victim].entry.used = false;
                                      VMKernel.victim = (VMKernel.victim + 1) % Machine.processor().getNumPhysPages();
                                    }
                                    int toEvict = VMKernel.victim;
                                    VMKernel.victim = (VMKernel.victim + 1) % Machine.processor().getNumPhysPages();
                                    if(VMKernel.IPT[toEvict].entry.dirty){
                                      int spn = 0;
                                      if(!VMKernel.freeSwapPages.isEmpty()){
                                        spn = VMKernel.freeSwapPages.removeLast();
                                      }
                                      else{
                                        spn = VMKernel.num_sp;
                                        VMKernel.num_sp++;
                                      }
                                      VMKernel.swapFile.write(spn * Processor.pageSize, Machine.processor().getMemory(), Processor.makeAddress(VMKernel.IPT[toEvict].entry.ppn, 0), Processor.pageSize);
                                      
                                      VMKernel.IPT[toEvict].entry.vpn = spn;
                                      //swap out
                                      //spn
                                    }
                                    VMKernel.IPT[toEvict].process.used_pages.remove(new Integer(VMKernel.IPT[toEvict].entry.ppn)); //   ??????????????? remove   pages  actuall physicalllll
                                    VMKernel.IPT[toEvict].entry.valid = false;
                                    ppn = VMKernel.IPT[toEvict].entry.ppn;
                                  }
                                  used_pages.add(ppn);
                                  if(!pageTable[vpn].dirty){
				    section.loadPage(i, ppn); // this load to PMem?
                                    if(section.isReadOnly()){
                                      pageTable[vpn] = new TranslationEntry(vpn, ppn, true, true, true, false);
                                    }
                                    else{    
                                      pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, false);
                                    }
                                  }
                                  else{
                                    // swap in
                                    VMKernel.swapFile.read(pageTable[vpn].vpn * Processor.pageSize, Machine.processor().getMemory(), Processor.makeAddress(ppn, 0), Processor.pageSize);
                                    VMKernel.freeSwapPages.add(pageTable[vpn].vpn);
                                    pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, true);
                                  }
                                  VMKernel.IPT[ppn].process = this;
                                  VMKernel.IPT[ppn].entry = pageTable[vpn];
                                }
			}
		} 

                for(int i = coffVpn + 1; i < numPages; i++){
                  int vpn = i;

                  if(vpn == badVpn){
                    int ppn = 0;
                    if(!UserKernel.free_pages.isEmpty()){
		      ppn = UserKernel.free_pages.removeLast();
                    }
                    else{
                      while(VMKernel.IPT[VMKernel.victim].entry.used){
                        VMKernel.IPT[VMKernel.victim].entry.used = false;
                        VMKernel.victim = (VMKernel.victim + 1) % Machine.processor().getNumPhysPages();
                      }
                      int toEvict = VMKernel.victim;
                      VMKernel.victim = (VMKernel.victim + 1) % Machine.processor().getNumPhysPages();
                      if(VMKernel.IPT[toEvict].entry.dirty){
                         int spn = 0;
                         if(!VMKernel.freeSwapPages.isEmpty()){
                           spn = VMKernel.freeSwapPages.removeLast();
                         }
                         else{
                           spn = VMKernel.num_sp;
                           VMKernel.num_sp++;
                        }
                        VMKernel.swapFile.write(spn * Processor.pageSize, Machine.processor().getMemory(), Processor.makeAddress(VMKernel.IPT[toEvict].entry.ppn, 0), Processor.pageSize);
                                      
                        VMKernel.IPT[toEvict].entry.vpn = spn;
                      //swap out
                      }
                      VMKernel.IPT[toEvict].process.used_pages.remove(new Integer(VMKernel.IPT[toEvict].entry.ppn));
                      VMKernel.IPT[toEvict].entry.valid = false;
                      ppn = VMKernel.IPT[toEvict].entry.ppn;
                    }
                    used_pages.add(ppn);
                    if(!pageTable[vpn].dirty){
                      // fill with 000000???????????????
                      byte[] data = new byte[Processor.pageSize];
                      for(int j = 0; j < data.length; j++){
                        data[j] = 0;
                      }
                      System.arraycopy(data, 0, Machine.processor().getMemory(), Processor.makeAddress(ppn, 0), Processor.pageSize);
                      pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, false);
                    }
                    else{
                      // swap in
                      VMKernel.swapFile.read(pageTable[vpn].vpn * Processor.pageSize, Machine.processor().getMemory(), Processor.makeAddress(ppn, 0), Processor.pageSize);
                      VMKernel.freeSwapPages.add(pageTable[vpn].vpn);
                      pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, true);
                    }
                    // fill with 00000?????
                    VMKernel.IPT[ppn].process = this;
                    VMKernel.IPT[ppn].entry = pageTable[vpn];
                  }
                }   
        }

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
                case Processor.exceptionPageFault:
                        handlePageFault(processor.readRegister(Processor.regBadVAddr)); // need to return anything??????????
                        break;
		default:
			super.handleException(cause);
			break;
		}
	}

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}

