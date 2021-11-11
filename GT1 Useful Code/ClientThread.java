package co.uk.diegesis.lsapps.tcpipapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientThread extends Thread {
   
   TCPComms comms = null;
   Logger pLogger = null;
   String configProp = null;
   int sleepInterval = 0;
   int threadSleep = 0;
   int client_id = 0;
   ClientManager cmgr = null;
   boolean exitThread = false;
   boolean removeThread = false;
   boolean connected = false;
   boolean dbconnected = false;
   
   ClientThread (Logger iLogger, String iConfigProp, int iclient_id, ClientManager cm ) {
      cmgr = cm;
      pLogger = iLogger;
      configProp = iConfigProp;
      client_id = iclient_id;
   }
   
   public void run() {
      
      int status = TCPC.SUCCESS;
      
      // We might start and stop this thread multiple times so reset
      // the core params
      
      sleepInterval = 0;
      threadSleep = 0;
      exitThread = false;
      removeThread = false;
      connected = false;
      dbconnected = false;
      
      TCPProp propMgr = new TCPProp(pLogger, configProp );
      
      threadSleep = propMgr.getPropertyInt(TCPC.THREADSLEEP);
      sleepInterval = propMgr.getPropertyInt(TCPC.SLEEPINTERVAL);
      //int iTimeout = propMgr.getTimeout();

      TCPClient tcpClient = new TCPClient( pLogger, configProp );
      
      DerbyClient db = new DerbyClient( pLogger, configProp);
      
      do {
         

         if ( !connected ) {
            connected = tcpClient.connectToServer();
         }
         
         if ( !dbconnected ) {
            dbconnected = db.connect();
         }

         // If we are not connected just sit in a not-connected loop.
         if (!connected) {
            try {
               Thread.sleep(sleepInterval);
               pLogger.debug("ClientThread : run : 1 : Wakeup from sleep interval while not connected [" + sleepInterval + "]");
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
              pLogger.debug("ClientThread : run : 2 : Wakeup from sleep interval [" + sleepInterval + "]");
          //e.printStackTrace();
            }
         }
            
         if (connected) {         
      
            MsgToSend msg = null;
      
            do {   

               db.prepareMsgs(client_id);
               msg = db.getmsg();
         
               if (msg == null ) {
                  try {
                     Thread.sleep(threadSleep);
                     pLogger.debug("ClientThread : run : 3 : Wakeup from sleep interval no messages [" + sleepInterval + "]");
                  } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     pLogger.debug("ClientThread : run : 4 : Wakeup from sleep interval [" + sleepInterval + "]");
                     //e.printStackTrace();
                  }
               }
      
               while ( connected && (msg != null && !exitThread )) {
                 //usrmsg = tcpClient.readFromUser();
                  String svrReply = null;
                  status = tcpClient.sendMsgtoServer(msg.msgtext);
                  pLogger.info("clientMain : main : 1 : Client Send [" + msg.msgtext + "]");
                  if ( status == TCPC.SUCCESS)
                  {
                     svrReply = tcpClient.readFromServerMsg();
                     pLogger.info("clientMain : main : 1 : Client Receive [" + svrReply + "]");

                     if ( svrReply != null) {
         
                        db.copyresult(msg.msg_id);
                        db.deletemsg(msg.msg_id);
                     }
                     if ( msg.msgtext.equals(TCPC.CLOSE) || msg.msgtext.equals(TCPC.EXIT) ) {
                        exitThread = true;
                     }
                     if (!exitThread) {
                        msg = db.getmsg();
                     }
                  }
                  if ( status == TCPC.CONNECTION_ERROR ) {
                     tcpClient.disconnectClient();
                     connected = false;
                  }
               }

               db.closeResults();
               if ( status == TCPC.SUCCESS )
                  db.commitTx();
               else
                  db.rollbackTx(); 
               
               //connected = tcpClient.checkConnected();
            }
            while ( connected && !exitThread );
         }
      }
      while ( !exitThread );

      //
      if (dbconnected) {
         db.disconnect();
         db = null;
      }
      
      if ( connected ) {
         tcpClient.disconnectClient();
         tcpClient = null;
      }
      
      cmgr.removeThread(this);
      
      return;
   }
   
   void setToExit () {
      exitThread = true;
   }

}
