package co.uk.diegesis.lsapps.tcpipapp;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerThread extends Thread {
   
   Socket connectionSocket = null ;
   TCPComms comms = null;
   boolean isrunning = true;
   boolean isexit = false;
   boolean timeToExit = false;
   boolean exitingFlag = false;
   Logger pLogger = null;
   TCPProp propMgr = null;
   int server_id = 0;
   String configProp = null;
   ServerListener listener = null;

   
   ServerThread ( Logger iLogger, String iConfigProp, Socket iConnection, ServerListener ilistener ) {
      connectionSocket = iConnection;
      pLogger = iLogger;
      configProp = iConfigProp;
      propMgr = new TCPProp(pLogger, configProp );  // this provides access to properties for
                                                    // other aspects of this app.
      listener = ilistener;
   }
   
   public void run() {
      
      pLogger.info("ServerThread : run : 1 : Thread starting.");
      ExternalApp eap = new ExternalApp(pLogger, configProp);
      
      comms = new TCPComms(pLogger, connectionSocket);
      
      comms.createStreams();
      
      String inputMsg =  comms.readMsg();
      
      while (!timeToExit) {
         String outputMsg = eap.processInboundMsg(inputMsg, server_id);
         comms.writeMsg(outputMsg);
         inputMsg = comms.readMsg();
         if (inputMsg.equals(TCPC.CLOSE))
         {
            setToExit();
         }
      }
      if (inputMsg.equals(TCPC.CLOSE)) {
         eap.processInboundMsg(inputMsg, server_id);
         comms.writeMsg(inputMsg);
      }
 
      pLogger.info("ServerThread : run : 2 : Completion message [" + inputMsg + "] identified");

      comms.closeSocket();
      isrunning = false;
      if (timeToExit) {
         isexit = true;
         pLogger.info("ServerThread : run :  : exit detected.");
      }
      setExiting(true);
      listener.removeThread(this);
      return;
   }
   void setLogger ( Logger iLogger ) {
      pLogger = iLogger;
   }
   
   void setToExit () {
      timeToExit = true;
   }
   synchronized boolean setExiting( boolean flag ) {
      if ( flag )
         exitingFlag = true;
      
      return exitingFlag;
   }
   
   void setServerId ( int iserver_id ) {
      server_id = iserver_id;
   }
}
