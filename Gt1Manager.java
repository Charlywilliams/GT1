package uk.co.diegesis.Charlotte.Williams.gt1;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;

public class Gt1Manager {
	// runOneThread() method

		private ArrayList<Gt1Thread> threadList;

		public Gt1Manager() {}
	
	public void runOneThread() {

		// inside of this method create a thread
		Gt1Thread gt1Thrd = new Gt1Thread();
		// start thread 
		System.out.println(this.toString() + "Starting thread");
		gt1Thrd.start();
		// thread wait
		try {
			Thread.sleep(GT1C.MANAGER_SLEEP);
			System.out.println(this.toString() + "Woken up from sleep");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// tell thread to shut down
		System.out.println(this.toString() + "Shutting down threads");
		gt1Thrd.interrupt();
		// while thread is alive sleep
		System.out.println(this.toString() + "Waiting for threads to die");
		while (gt1Thrd.isAlive()) {
			try {
				Thread.sleep(GT1C.ALIVE_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		System.out.println(this.toString() + "Exiting run one thread");
		return;
	}
	
	// runManyThreads() method
	public void runManyThreads() {
		// this creates a thread list
		this.threadList = new ArrayList<>();
		// add threads into this list
		Gt1Thread threadOne = new Gt1Thread();
		Gt1Thread threadTwo = new Gt1Thread();
		Gt1Thread threadThree = new Gt1Thread();
		// list=array
		this.threadList.add(threadOne);
		this.threadList.add(threadTwo);
		this.threadList.add(threadThree);
		// threads wait 
		this.threadList.forEach(thread ->{
			thread.start();
			try {
				Thread.sleep(GT1C.MANAGER_SLEEP);
	            thread.setExit();
				System.out.println(this.toString() + "Woken up from sleep");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		for(Gt1Thread thread : this.threadList) {
		    if(thread.getExit()){ 
		        thread.setExit();
		}
		    Iterator<Gt1Thread> itThreadList = this.threadList.iterator();
		     //Iterator<Integer> itr = numbers.iterator();
		     while (itThreadList.hasNext()) {
		        Gt1Thread gtThread = itThreadList.next();

		         if (!gtThread.getIsStarted()) {
		            itThreadList.remove();
		         }
		         else if (gtThread.getExit()){
		            gtThread.setExit(); 
		         }
		                         
		}
		// tell each thread to stop in list
		
		// wait until shut down
	
		}
		return;
		
	}
	
	
	
	
	// runManyThreadsData method
	public void runManyThreadsData() {
		
		
		// instance of shared data object
		
		// create thread list
		// add threads to thread list
		// share a lock and data
		// start each thread
		// wait around threads
		// each thread stop
		// wait until threads shut down
		return;
	}
}


	
	


