package uk.co.diegesis.Charlotte.Williams.gt1;

public class Gt1Main {

	public static void main(String[] args) {
		// create an instance of gt1Manager
		Gt1Manager gt1Mngr = new Gt1Manager();
		// manager is going to run one thread
		gt1Mngr.runOneThread();
		// manager runs many threads
		gt1Mngr.runManyThreads();
		// manager is going to run many threads with data 
		gt1Mngr.runManyThreadsData();
		System.out.println("Exiting gt1Main");
	    System.exit(0);
	}
}
