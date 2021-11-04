package uk.co.diegesis.Charlotte.Williams.gt1;

public class Gt1Thread extends Thread {
	private boolean timeToExit = false;
	private long threadSleep = GT1C.THREAD_SLEEP;
	private boolean isStarted = true;
	private boolean isExiting = false; 
	

	public void run() {
	 // thread starting up
		
	System.out.println(this.getName() + "Thread starting up");
	
	
	// inherit thread class ie access all of lib
	// thread needs run method, action thread needs to do
	// have boolean variable
	
	// sit in a loop going to sleep in the loop and waking up 
	// until told to exit
	 while (!isExiting) {
		 System.out.println(this.getName() + "Going to sleep");
		 
		 try {
			Thread.sleep(threadSleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println(this.getName() + "Thread interrupted and exiting");
			if (isExiting) {
				 isStarted = true; 
				 setExit();
			 } 
		}
		 
	 }
	
	// isStarted(), T/F started?
	// is it starting? Is it exiting?

	   return;
	}
	// method called setExit() 
	synchronized void setExit() { 
		this.isExiting = true;
		
	}
	// method called getExit()
	synchronized boolean getExit() {
		return this.isExiting;
		
	}
	// method called getIsStarted()
	boolean getIsStarted() {
		return this.isStarted;

		
	}
	// method setIsStarted() 
	void setIsStarted(boolean started) {
		this.isStarted = started;
	}
		
		
	void setToExit () {
	   timeToExit = true;
	   return;
	}
}
