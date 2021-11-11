package co.uk.diegesis.lsapps.tcpipapp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerListener {
   
   ArrayList<ServerThread> connectionThreads = null;
   Socket connectionSocket = null ;
   boolean waitForConnection = true;
   boolean waitForExit = false;
   int server_id = 0;
   DerbyClient db = null;
   
   Logger pLogger = null;
   String configProp = null;

   ServerListener (Logger iLogger, String iconfigProp) {
      configProp = iconfigProp;
      pLogger = iLogger;
   }
   
   void listen () {
 
      TCPServer tcpServer = new TCPServer(pLogger);
      connectionThreads = new ArrayList<ServerThread>();
      
      TCPProp propMgr = new TCPProp(pLogger, configProp );
      db = new DerbyClient( pLogger, configProp);
      
      int port = propMgr.getPort();
      int iSocketSleep = propMgr.getPropertyInt(TCPC.SOCKETSLEEP);
      
      tcpServer.setPort(port);
      tcpServer.setTimeout(iSocketSleep);
      //tcpServer.setLogger(pLogger);
      
      tcpServer.create_socket();
      
      waitForConnection = db.connect();
      
      while ( waitForConnection || !connectionThreads.isEmpty()) {
      
         if ( waitForConnection )
            connectionSocket = tcpServer.server_listen();
         
         if (!(connectionSocket == null)) {
      
            ServerThread svrt = new ServerThread ( pLogger, configProp, connectionSocket, this );
            connectionThreads.add(svrt);

            svrt.setServerId(++server_id);
            db.createServerThread(server_id, 0, "", TCPC.STARTED );

            svrt.start();
         }
         
         checkServerCommands();
         

         for (ServerThread iThread : connectionThreads) {
            if (iThread.isexit)
               waitForExit = true;
         }
         
         Iterator<ServerThread> itThreadList = connectionThreads.iterator();
         //Iterator<Integer> itr = numbers.iterator();

         // the correct way to remove an entry from an array list.
         while (itThreadList.hasNext()) {
            ServerThread iThread = itThreadList.next();

             if (!iThread.isrunning) {
                itThreadList.remove();
             }
             else if (waitForExit){
                iThread.timeToExit = true;
             }
         }                      
         
         if ( waitForConnection ) {
            waitForConnection = !db.checkForServerExit();
         }
      }
      
      return;
   }
   
   /*
    * Any thread that gets a close message will call this method
    * and the server which then tell all threads to close.
    */
   synchronized void setThreadsClosed() {
      
      /* 
       * Use an itterator in case the thread removes itself from the 
       */
      Iterator<ServerThread> itThreadList = connectionThreads.iterator();
      while (itThreadList.hasNext()) {
         ServerThread iThread = itThreadList.next();
         iThread.setToExit();

      }
   }
   
   synchronized void  removeThread ( ServerThread iThread ) {
      connectionThreads.remove(iThread);
      db.deleteServerThread(server_id);
   }
   
   void checkServerCommands() {
      
      boolean flag;
      
      flag = db.getServerCommands();
      ServerThreadDetails svrThread = null;
      
      if ( flag ) {
         svrThread = db.getThreadDetails();
         while (svrThread != null) {
            if (svrThread.thread_command.toUpperCase().equals(TCPC.CLOSE)) {
               
               ServerThread svr = getServer(svrThread.server_id);
               svr.setToExit();
            }
            svrThread = db.getThreadDetails();
         }
      }
      return;
   }
   ServerThread getServer (int server_id ) {
      
      ServerThread svrFound = null;
      
      for ( ServerThread svrt : connectionThreads ) {
         if ( svrt.server_id == server_id ) {
            svrFound = svrt;
            break;
         }
      }
      
      return svrFound;
   }
}
