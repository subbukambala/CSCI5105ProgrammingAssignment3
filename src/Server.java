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
import java.util.Vector;
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
    private Integer myMaxTaskId;
    
    /**
     * Contains statistics of whole distributed system(DS).
     */
    private ServerStats myServerStats;
    
    /**
     * Contains statistics of a task. (Not maintaining list because DS handles job at a time)
     */
    private TaskStats myTaskStats;
    
    private List<MapTask> myMaps;

    private ReduceTask myReduce;

    private ClientInterface client;
    
    /**
     * Contains registered Compute node list.
     */
    private Vector<Pair<Integer, String>> myComputeNodesList;
    
    /**
     * Key is node id, value is time at which last message is called. 
     */
    Map<Integer, Long> heartBeatStatus = new ConcurrentHashMap<Integer, Long>();


    public Server() throws Exception {
        lg = new Logger("Server");
        lg.log(Level.FINER, "Server started.");
        
        maxComputeNodeId = 0;
        myMaxTaskId = 0;
        myServerStats =  new ServerStats();
        
        myComputeNodesList = new Vector<Pair<Integer,String>> ();
        myMaps = new ArrayList<MapTask> ();
        myReduce = new ReduceTask();
        List<MapTask> list = new ArrayList<MapTask>();
        myReduce.setData(list);
    }

    @Override
    synchronized public Integer registerNode() throws Exception {
        
        maxComputeNodeId++;
        
        lg.log(Level.INFO, "ComputeNode " + maxComputeNodeId + " joined.");
        
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
    }
    
    /**
     * Re-assigns all the tasks of nodeId to first node in list which is not nodeId.
     * 
     * @param nodeId
     */
    private void reassignTasks(Integer nodeId) throws Exception {
        if (myMaps.size() == 0) {
            return;
        }
        
        if (myComputeNodesList.size() == 0) {
            lg.log(Level.SEVERE, "All compute nodes are died. Inform client gracefully");
            throw new Exception("All Compute nodes are died");
        }
        
        for (Integer i = 0; i < myMaps.size(); i++) {
            if (myMaps.get(i).getNode() == null || myMaps.get(i).getNode().fst() == nodeId) {
                int j = 0;
                while (myComputeNodesList.get(j) != null && myComputeNodesList.get(j).fst() == nodeId) {
                    j++;
                }
                
                // If all compute nodes are died.
                if (j == 0 || j == myComputeNodesList.size()) {
                    System.out.println("All compute nodes are died");
                    client.jobResponse(null, null);
                    return;
                }
                
                try {
                    ComputeNodeInterface computeNode = (ComputeNodeInterface) 
                                Naming.lookup("//" + myComputeNodesList.get(j).snd() + "/ComputeNode" + myComputeNodesList.get(j).fst());
                    
                    // Assigning ith task to J node
                    computeNode.executeTask(myMaps.get(i));

                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        }
    }
    
    /**
     * Releases node if server didn't listen to heart beat message.
     * 
     * @param nodeId
     */
    private void releaseNode(Pair<Integer, String> node) {
        // remove entry
        lg.log(Level.INFO, "Compute Node " + node.fst() + " is declared as DEAD");
        myComputeNodesList.remove(node);
    }

    @Override
    public void heartBeatMsg(Integer nodeId)  throws RemoteException {
        heartBeatStatus.put(nodeId, System.currentTimeMillis());
    }
    
    /**
     * This class periodically checks whether node is alive or not. 
     * If not, removes node from list and re-assigns the tasks.
     */
    private class NodeStatusChecker extends TimerTask {
        public void run() {
            try {
                for (Integer i = 0; i < myComputeNodesList.size(); i++) {
                    if (myComputeNodesList.get(i) != null && heartBeatStatus.get(myComputeNodesList.get(i).fst()) != null) {
                        Long diff = 
                            System.currentTimeMillis()
                            - heartBeatStatus.get(myComputeNodesList.get(i).fst());
                        if (diff > 30 * 1000) {
                            Integer nodeId = myComputeNodesList.get(i).fst();
                            
                            // Incrementing no of faults
                            myServerStats.getNoOfFaults().incrementAndGet();
                            
                            releaseNode(myComputeNodesList.get(i));
                            
                            reassignTasks(nodeId);
                            i--;
                        }
                    }
                    else {
                        if (myComputeNodesList.get(i) != null && heartBeatStatus.get(myComputeNodesList.get(i).fst()) == null) {
                            reassignTasks(myComputeNodesList.get(i).fst());
                            
                            releaseNode(myComputeNodesList.get(i));
                        }
                        System.out.println("============= NULL " + i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // ASSUMPTION: submitJob is called by a single client and therefore
    // does not require thread safe programming!
    @Override 
    synchronized public Boolean submitJob(List<Integer> data) throws Exception
    {
        lg.log(Level.FINEST, "submitJob(list): Entry");
        
        myTaskStats =  new TaskStats();
        
        // Incrementing the no of jobs
        myServerStats.getNoOfJobs().incrementAndGet();
        
        // Ignore any submitted jobs if another job is in progress
        if(myReduce.getData().size()!=0) {
            lg.log(Level.SEVERE, "submitJob(list): Reduce task in progress."
                   +" Ignoring job!");
            return false;
        }
        if(myMaps.size()!=0) {
            lg.log(Level.SEVERE, "submitJob(list): Map task in progress."
                   +" Ignoring job!");
            return false;
        }

        String url = "//" + getClientHost() + "/Client";
        client = (ClientInterface) Naming.lookup(url);

            
        Iterator<Integer> iterator = data.iterator();
        while (iterator.hasNext()) {
            lg.log(Level.FINER, "submitJob(list): Received integer -> " 
                   + iterator.next());
        }
        int cnodes = myComputeNodesList.size();
        int datums = data.size();
        int tasksize = 0;
        while ((tasksize = datums / cnodes) == 0)
            cnodes--;
        

        // For each node but the last ...
        List<Integer> tdata = null;
        MapTask ttask = null;
        int i = 0;
        for (; i < (cnodes - 1); i++) {
            tdata = new ArrayList<Integer>();
            // ... iterate through that nodes chunk of the data.
            for (int j = 0; j < tasksize; j++) {
                tdata.add(data.get((i * tasksize) + j));
            }
            // ... build a task for a node.
            ttask = new MapTask();
            myMaxTaskId++;
            ttask.setTaskId(myMaxTaskId);
            ttask.setData(tdata);
            myMaps.add(ttask);
            lg.log(Level.FINER, "submitJob(list): Added a task with "
                    + tdata.size() + " elements to task list");
        }
        tdata = new ArrayList<Integer>();
        // The last node will handle any remainder data.
        for (int j = i * tasksize; j < data.size(); j++) {
            tdata.add(data.get(j));
        }
        ttask = new MapTask();
        myMaxTaskId++;
        ttask.setTaskId(myMaxTaskId);
        ttask.setData(tdata);
        myMaps.add(ttask);
        lg.log(Level.FINER, "submitJob(list): Added a task with "
                + tdata.size() + " elements to task list");

        lg.log(Level.FINEST, "submitJob(list): Task list of size "
                + myMaps.size() + " created.");

        // Assigning tasks to nodes
        for (i = 0; i < myMaps.size(); i++) {
            
            //
            // Iterates through all nodes in case if a node is
            // down but not yet cleared from list.
            //
            int j = 0;
            boolean isAssigned = false;
            while (j + i < 2 * myComputeNodesList.size()) {
                try {
                    url = "//" 
                        + myComputeNodesList.get((i + j) % myComputeNodesList.size()).snd() 
                        + "/ComputeNode" 
                        + myComputeNodesList.get((i + j) % myComputeNodesList.size()).fst();
                    ComputeNodeInterface computeNode = (ComputeNodeInterface) 
                        Naming.lookup(url);
                    computeNode.executeTask(myMaps.get(i));
                    isAssigned = true;
                    j++;
                } catch (Exception e) {
                    lg.log(Level.SEVERE,"submitJob(list): failure on executeTask "
                           +"with url = "+ url);
                    //e.printStackTrace();
                    //System.exit(1);
                }
            }
            
            if (isAssigned == false) {
                throw new Exception ("All nodes are died. Handle it gracefully!");
            }
        }
        lg.log(Level.FINEST, "submitJob(list): Exit");        
        myReduce.getData().clear();
        return true;
    }

    // XXX: Does not handle duplicate task aggregations
    // XXX: Does not handle errant tasks
    synchronized public Boolean aggregateMapTasks(MapTask t) throws RemoteException {
        lg.log(Level.FINEST, "aggregateMapTasks: Enter");
        lg.log(Level.FINER,"aggregateMapTasks: Have  "
               + myReduce.getData().size() +" MapTasks, waiting for "
               + myMaps.size());
        
        myReduce.getData().add(t);
        // If I have received all the maps then 
        // pick a node and send them the merge job.
        // I suppose that means I should send a list of 
        // tasks. Not really sure yet what should be sent.
        lg.log(Level.FINER,"aggregateMapTasks: Have  "
               + myReduce.getData().size() +" MapTasks, waiting for "
               + myMaps.size());
        
        if(myReduce.getData().size() == myMaps.size()) {
            String url = "";
            myMaps.clear();
            try {
                // XXX: Need to check if there is any compute node in 
                // our list!
                url = "//" 
                    + myComputeNodesList.get(0).snd() 
                    + "/ComputeNode" 
                    + myComputeNodesList.get(0).fst();
                ComputeNodeInterface computeNode = (ComputeNodeInterface) 
                    Naming.lookup(url);
                computeNode.executeTask(myReduce);
            } catch (Exception e) {
                lg.log(Level.SEVERE,"aggregateMapTasks: failure on "
                       +"executeTask with url = "+ url);
                e.printStackTrace();
                //System.exit(1);
            }
            lg.log(Level.FINEST, "aggregateMapTasks: Merge sent to "+url);
        }
        lg.log(Level.FINEST, "aggregateMapTasks: Exit");        
        return true;
    }

    // XXX: Does not handle duplicate task aggregations
    // XXX: Does not handle errant tasks
    public Boolean aggregateReduceTasks(ReduceTask t) throws RemoteException {

        lg.log(Level.FINEST, "aggregateReduceTasks: Enter");
        if(myMaps.size() != 0) {
            lg.log(Level.SEVERE,"aggregateReduceTasks: myMaps is non-empty!.");
            //System.exit(1);
        }
        
        List<Integer> sortedlist = new ArrayList<Integer>();
        
        for(int i = 0;i<t.getData().size();i++) {
            sortedlist.addAll(t.getData().get(i).getData());
        }
        client.jobResponse(null,sortedlist);
        myReduce.getData().clear();
        
        lg.log(Level.FINEST, "aggregateReduceTasks: Exit");

        return true;
    }
    
    @Override
    public String getServerStats() throws RemoteException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("No of handled Jobs: " + myServerStats.getNoOfJobs());
        buf.append("\nNo of task transfers: " + myServerStats.getNoOfTaskMigrations());
        buf.append("\nNo of redundant tasks: " + myServerStats.getNoOfRedundantTasks());
        buf.append("\nNo of Faults: " + myServerStats.getNoOfFaults());
        
        return buf.toString();
    }
    
    @Override
    public void updateTaskTransfer(Task task) throws RemoteException {
        // Incrementing task migration
        myServerStats.getNoOfTaskMigrations().incrementAndGet();
        
        for (Integer i = 0; i < myMaps.size(); i++) {
            if(myMaps.get(i).getTaskId() == task.getTaskId()) {
                myMaps.get(i).setNode(((MapTask)task).getNode());
            }
        }
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
             "Daniel William DaCosta, Bala Subrahmanyam Kambala - GPLv3 "
             +"(http://www.gnu.org/copyleft/gpl.html)"
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