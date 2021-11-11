package co.uk.diegesis.lsapps.tcpipapp;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientManager {
   boolean exitClientManager = false;
   boolean dbconnected = false;
   ArrayList<ClientThread> clientThreads = null;

   int clientThreadCount = 0;
   String configProp = null;
   TCPProp propMgr = null;
   int threadSleep;
   
   Logger pLogger = null;
   DerbyClient db = null;


   ClientManager (Logger iLogger, String iconfigProps ) {
      pLogger = iLogger;
      configProp = iconfigProps;
      propMgr = new TCPProp(pLogger, configProp );
   }
    
   
   void manageClients () {
      
      clientThreadCount = propMgr.getPropertyInt(TCPC.CLIENTCOUNT);
      threadSleep = propMgr.getThreadSleep();
      
      clientThreads = new ArrayList<ClientThread>();
      
      db = new DerbyClient( pLogger, configProp);
      
      dbconnected = db.connect();
      if ( dbconnected ) {
         
         initialStartClients();
         waitForComd();
         stopClients();
      }

      return;
   }
   
   
   void initialStartClients () {
      boolean success = false;
      // Read the client records create an object for each client
      // if the command is to start it up, then start a thread on the object.
      
      //ResultSet clntres = 
      success = db.getClientList();
      ClientDetails clntDet = null;
      
      if ( success ) {
         clntDet = db.getClientDetails();
         
         while(clntDet != null ) {
            ClientThread clnt = new ClientThread( pLogger, configProp, clntDet.client_id, this );
            clientThreads.add(clnt);
            if (clntDet.client_status.equals(TCPC.OPEN)) {
               clnt.start();
            }
            clntDet = db.getClientDetails();
         }
         db.closeClientResults();
      }
      db.commitTx();
      
      return;
   }
   
   /*
   void startClients() {
      
      for (int i = 0 ; i < clientThreadCount ; i++) {
         int client_id = i + 1;
         ClientThread clnt = new ClientThread( pLogger, configProp, client_id, this );
         
         clientThreads.add(clnt);
         pLogger.debug("clientMain : startClients : 1 : Adding client [" + client_id + "]");
         clnt.start();
      }
      
      
      return;
   }
   */
   // Here ideally I would have a hash table and could lookup the object based on client id
   // However I went for array list so have to loop.
   ClientThread getClient (int client_id ) {
      
      ClientThread clntFound = null;
      
      for ( ClientThread clnt : clientThreads ) {
         if ( clnt.client_id == client_id ) {
            clntFound = clnt;
            break;
         }
      }
      
      return clntFound;
   }
   boolean checkClientCommands() {
      boolean flag;
   
      flag = db.getClientCommands();
      ClientDetails clntDet = null;
      if ( flag ) {
         clntDet = db.getClientDetails();
         
         while(clntDet != null ) {
            
            ClientThread clnt = getClient(clntDet.client_id);

            if (clntDet.client_command.toUpperCase().equals(TCPC.OPEN)) {

               boolean threadState = clnt.isAlive();
               if ( !threadState )
                  clnt.start();
            } else if (clntDet.client_command.toUpperCase().equals(TCPC.CLOSE)) {
               boolean threadState = clnt.isAlive();
               if ( threadState ) {
                  clnt.setToExit();
                  try {
                     clnt.join();
                  }
                  catch (InterruptedException io) {
                     pLogger.error("DerbyClient : checkClientCommands : 15 : Message [" +
                           io.getMessage() + "]" );
                  }
                  // It appears that you are not allowed to re-start a thread object.
                  // the thread removes itself from the array so add another now.
                  clnt = new ClientThread( pLogger, configProp, clntDet.client_id, this );
                  clientThreads.add(clnt);
               }
            } 
            db.completeCommand(clntDet.client_id, clntDet.client_command);
            clntDet = db.getClientDetails();
         }
         db.closeClientResults();
         db.commitTx();
      }
      return flag;
   }
   
   void waitForComd () {
      
      exitClientManager = db.checkForClientExit();
      
      if (!exitClientManager)
         checkClientCommands();

      while (!exitClientManager) {
         try {
            Thread.sleep(threadSleep);

         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            pLogger.debug("clientMain : main : 1 : Wakeup from sleep interval [" + threadSleep + "]");
            //e.printStackTrace();
         }
         exitClientManager = db.checkForClientExit();
         if (!exitClientManager)
            checkClientCommands();
      }
      
      return;
   }
   
   /*
    * Any thread that gets a close message will call this method
    * and the server which then tell all threads to close.
    */
   synchronized void setClientsClosed() {
      
      /* 
       * Use an itterator in case the thread removes itself from the 
       */
      Iterator<ClientThread> itThreadList = clientThreads.iterator();
      while (itThreadList.hasNext()) {
         ClientThread iThread = itThreadList.next();
         iThread.setToExit();

      }
   }
   
   void removeThread ( ClientThread clt) {
      clientThreads.remove(clt);
   }
   
   void stopClients () {
      
       
      Iterator<ClientThread> itThreadList = null;
      
      setClientsClosed();

      itThreadList = clientThreads.iterator();

      while (itThreadList.hasNext()) {
      
         try {
            Thread.sleep(threadSleep);
         } catch (InterruptedException e) {
         // TODO Auto-generated catch block
            pLogger.debug("clientMain : main : 1 : Wakeup from sleep interval [" + threadSleep + "]");
         }
         itThreadList = clientThreads.iterator();

      }
      
      return;
   }

}
