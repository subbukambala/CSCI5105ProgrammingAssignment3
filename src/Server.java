/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    
    /**
     * Key is node id, value is time at which last message is called. 
     */
    Map<Integer, Long> heartBeatStatus = new ConcurrentHashMap<Integer, Long>();


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
    
    /**
     * Releases node if server didn't listen to heart beat message.
     * 
     * @param nodeId
     */
    private void releaseNode(Integer nodeId) {
        // remove entry
    }

    @Override
    public void heartBeatMsg(Integer nodeId)  throws RemoteException {
        heartBeatStatus.put(nodeId, System.currentTimeMillis());
    }
    
    private class NodeStatusChecker extends TimerTask {
        public void run() {
            for (Integer i = 0; i < myComputeNodesList.size(); i++) {
                Long diff = System.currentTimeMillis() - heartBeatStatus.get(myComputeNodesList.get(i));
                if (diff > 30 * 1000) {
                    myComputeNodesList.remove(i);
                    i--;
                    
                    // XXX: move task to other nodes...
                    
                }
            }
        }
    }
    
    @Override 
    public Boolean submitJob(List<Integer> data)
    {
        lg.log(Level.FINEST, "submitJob(list): Entry");
        Iterator<Integer> iterator = data.iterator();
        while (iterator.hasNext()) {
            lg.log(Level.FINER,"submitJob(list): Recevied integer -> "+ iterator.next());
        }
        int cnodes = myComputeNodesList.size();
        int datums = data.size();
        int tasksize = 0; 
        while((tasksize=datums/cnodes)==0) cnodes--; 
        List<Task> tasks = new ArrayList<Task>();

        // For each node but the last ...
        List<Integer> tdata = null;
        Task ttask = null;
        int i = 0;
        for(; i < (cnodes-1) ; i++ ) {
            tdata = new ArrayList<Integer>();
            // ... iterate through that nodes chunk of the data.
            for(int j = 0; j < tasksize ; j++) {
                tdata.add(data.get((i*tasksize)+j));
            }
            // ... build a task for a node.
            ttask = new Task();
            ttask.setData(tdata);
            tasks.add(ttask);
            lg.log(Level.FINER, "submitJob(list): Added a task with "
                   +tdata.size()+" elements to task list");
        }
        tdata = new ArrayList<Integer>();
        // The last node will handle any remainder data.
        for(int j = i*tasksize; j < data.size() ; j++) {
            tdata.add(data.get(j));
        }
        ttask = new Task();
        ttask.setData(tdata);
        tasks.add(ttask);
        lg.log(Level.FINER, "submitJob(list): Added a task with "
               +tdata.size()+" elements to task list");

        lg.log(Level.FINEST, "submitJob(list): Task list of size "
               +tasks.size()+" created.");


        lg.log(Level.FINEST, "submitJob(list): Exit");
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
        
        Server server;
        try {
            server =  new Server();
            
            Naming.rebind("Server", server);
            
            // Scheduling node status checker
            Timer t = new Timer();
            NodeStatusChecker h = server.new NodeStatusChecker();
            
            t.schedule(h, 0, 30 * 1000);
            
        } catch (Exception e) {
            System.out.println("Server failed: ");
            e.printStackTrace();
        }
    }
}