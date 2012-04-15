/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;

public class Server extends UnicastRemoteObject implements ServerInterface {

    private Logger lg;

    /**
     * This variable is to ensure that Collector generates unique Compute node id
     * id.
     */
    private Integer maxComputeNodeId;

    /**
     * This variable is to ensure that Collector generates unique request id.
     */
    private Integer maxJobId;
    
    private ServerStats serverStats;
    
    /**
     * Contains registered Compute node list.
     */
    private List<Pair<Integer, String>> myComputeNodesList;
    
    Map<Integer, Boolean> heartBeatStatus = new ConcurrentHashMap<Integer, Boolean>();


    public Server() throws Exception {
        lg = new Logger("Server");
        lg.log(Level.FINER, "Server started.");
        
        maxComputeNodeId = 0;
        maxJobId = 0;
        
        myComputeNodesList = new ArrayList<Pair<Integer,String>> ();
    }

    @Override
    synchronized public Integer registerNode() throws Exception {
        
        maxComputeNodeId++;
        
        lg.log(Level.FINER, "ComputeNode " + maxComputeNodeId + " joined.");
        
        myComputeNodesList.add(new Pair<Integer, String>(maxComputeNodeId, getClientHost()));
        return maxComputeNodeId;
    }

    @Override
    public List<Pair<Integer, String>> getActiveNodes() throws RemoteException {
        return myComputeNodesList;
    }
    
    private class TaskHandler extends Thread{
       
        public Integer jobId;
        public String filePath;
        private List<Task> tasks;
        
        public TaskHandler(Integer _jobId, String _filePath) {
            jobId = _jobId;
            filePath = _filePath;
        }
        
        public void run() {
            // calls the methods in appropriate order 'map','reduce'
            
        }
        
        private void scheduleMapJob() {
            // can use split command to split data in terms lines.
            // convention: file name jobid_taskid
        }
        
        private void scheduleReduceJob() {
            
        }
        
        private void taskStatusChecker() {
            
        }
        
        private void reassignTask() {
            
        }
    }
    
    public void transferData(DataTransferInterface DTI, 
                             Integer jobId,
                             Integer maxBufferWindow, 
                             String srcFilePath, 
                             String destFilePath) throws RemoteException
    {
        DataTransferHandler d = new DataTransferHandler();
        d.transferData(DTI, jobId, maxBufferWindow, srcFilePath, destFilePath);
    }
    
    public void storeData(String data, String filePath) throws RemoteException {
        DataTransferHandler d = new DataTransferHandler();
        d.storeData(data, filePath);
    }
    
    public void onDataTransferComplete(Integer jobId, String filePath) throws RemoteException {
        //Thread t =  new TaskHandler(jobId, filePath);
        //t.start();
        
        System.out.println("data transfer is complete");
    }
    
    /**
     * Releases node if server didn't listen to heart beat message.
     * 
     * @param nodeId
     */
    private void releaseNode(Integer nodeId) {
        // remove entry
    }

    @Override
    public void heartBeatMsg(Integer nodeId)  throws RemoteException{
        // put true flag
    }
    
    @Override
    public Boolean submitJob(Integer clientId, Integer jobId, String filePath) throws RemoteException {
        
        ClientInterface c;
        try {
            c = (ClientInterface) Naming.lookup("//" + getClientHost() + "/Client" + clientId);
            c.transferData(this, jobId, 1024, filePath, "../data/server/NGramLZOProcessor.java");
            
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServerNotActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return false;
    }
    
    @Override
    public String getServerStats() throws RemoteException {
        return "";
    }
    
    @Override
    public void updateTaskTransfer(Task task) throws RemoteException {
        // Increment task migration
        
        
    }
    
    /**
     * The good stuff.
     */
    public static void main(String[] argv) {
      
        ArgumentHandler cli = new ArgumentHandler
                            (
                                    "Server [-h] "
                                    ,
                                    "TBD."
                                    ,
                                    "Daniel William DaCosta, Bala Subrahmanyam Kambala - GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
                             );
        cli.addOption("h", "help", false, "Print this usage information.");
        
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }
        
        try {
            Naming.rebind("Server", new Server());
        } catch (Exception e) {
            System.out.println("Server failed: ");
            e.printStackTrace();
        }
    }
}