/**
 * @description Server accepts job from client and schedules Map/Reduce tasks on compute nodes. 
 * If any compute node fails, this also reschedules the task of failed compute node.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
     * This variable is to ensure that Collector generates unique Compute node
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
    
    /**
     * List of map tasks
     */
    private List<MapTask> myMaps;

    /**
     * Reduce Task instance
     */
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
    
    /**
     * Re-assigns all the tasks of nodeId to first node in list which is not nodeId.
     * 
     * @param nodeId
     */
    private void reassignTasks(Integer nodeId) throws Exception {
        // Returns if there is no Map task exists.
        if (myMaps.size() == 0) {
            return;
        }
        
        // Returns if no compute node exists to execute
        if (myComputeNodesList.size() == 0) {
            lg.log(Level.SEVERE, "All computenodes are dead.");

            myServerStats.getNoOfFailedJobs().incrementAndGet();
            client.jobResponse(null, null);
            
            // clearing job data 
            clearJobData();
            return;
        }
        
        // A node can have many tasks
        for (Integer i = 0; i < myMaps.size(); i++) {
            Boolean isAssigned = false;
            if (myMaps.get(i).getNode() == null || myMaps.get(i).getNode().fst() == nodeId) {

                // If we have nodes in the list, loop through them ..
                for (int j = 0; j < myComputeNodesList.size(); j++) {
                    if (myComputeNodesList.get(j) != null
                            && myComputeNodesList.get(j).fst() != nodeId) {

                        try {
                            String url = "";
                            url = "//" + myComputeNodesList.get(j).snd()
                                    + "/ComputeNode" + myComputeNodesList.get(j).fst();

                            ComputeNodeInterface computeNode = (ComputeNodeInterface)
                                    Naming.lookup(url);

                            lg.log(Level.INFO, "Map task " + i + " has been re-assigned to node "
                                    + myComputeNodesList.get(j).fst());

                            myMaps.get(i).setNode(myComputeNodesList.get(j));
                            
                            myServerStats.getNoOfRedundantTasks().incrementAndGet();
                            isAssigned = true;
                            // Assigning ith task to J node
                            computeNode.executeTask(myMaps.get(i));
                            break;

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
                
                // All nodes are died
                if (! isAssigned) {
                    
                    myServerStats.getNoOfFailedJobs().incrementAndGet();
                    client.jobResponse(null, null);
                    clearJobData();
                
                    lg.log(Level.SEVERE, "submitJob(list): All compute nodes "
                           +"are dead. Ignoring job!");
                    return;
               
                }

            }
        }
    }
    
    /**
     * Releases node if server didn't listen to heart beat message.     
     */
    private void releaseNode(Pair<Integer, String> node) {
        // remove entry
        lg.log(Level.INFO, "Compute Node " + node.fst() + " is declared as DEAD");
        myComputeNodesList.remove(node);
    }

    /**
     * This method clears all the data after the job submission
     */
    private void clearJobData() {
        if (myReduce.getData() != null) {
            myReduce.getData().clear();
        }
        
        if (myMaps != null) {
            myMaps.clear();
        }
	client = null;
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
	        if(client != null && myComputeNodesList.size()==0) {
			lg.log(Level.SEVERE, "NodeStatusChecker: All nodes  "
			       +"dead returning null.");
			client.jobResponse(null,null);
			clearJobData();
			
		}
                for (Integer i = 0; i < myComputeNodesList.size(); i++) {
                    if (myComputeNodesList.get(i) != null) {

                        Long diff = 0l;
                        if (heartBeatStatus.get(myComputeNodesList.get(i).fst()) != null) {
                            diff = System.currentTimeMillis()
                                            - heartBeatStatus.get(myComputeNodesList.get(i).fst());
                        }
                        //
                        // If difference is greater than 30 secs or node it didn't register 
                        // its heart beat at all, then release node
                        //
                        if (diff > 10 * 1000
                                || heartBeatStatus.get(myComputeNodesList.get(i).fst()) == null) {
                            Integer nodeId = myComputeNodesList.get(i).fst();

                            // Incrementing no of faults
                            myServerStats.getNoOfFaults().incrementAndGet();

                            reassignTasks(nodeId);

                            releaseNode(myComputeNodesList.get(i));

                            // Deleted current and Size is reduced by 1
                            i--;
                        }
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
        myTaskStats.setStartJobTime(System.currentTimeMillis());
        
        // Incrementing the no of jobs
        myServerStats.getNoOfJobs().incrementAndGet();
        
        // Ignore any submitted jobs if another job is in progress
        if(myReduce.getData().size() != 0) {
            lg.log(Level.SEVERE, "submitJob(list): Reduce task in progress."
                   +" Ignoring job!");
            return false;
        }
        if(myMaps.size() != 0) {
            lg.log(Level.SEVERE, "submitJob(list): Map task in progress."
                   +" Ignoring job!");
            return false;
        }

        if (myComputeNodesList.size() == 0) {
            myServerStats.getNoOfFailedJobs().incrementAndGet();
            client.jobResponse(null, null);
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
        
        // These tasks should really be built using the sub-list functions.
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

        myTaskStats.setNoOfMapTasks(myMaps.size());
        
        // Assigning tasks to nodes
        for (i = 0; i < myMaps.size(); i++) {
            
            //
            // Iterates through all nodes in case if a node is
            // down but not yet cleared from list.
            //
            boolean isAssigned = false;
            for (int j=0; j < myComputeNodesList.size(); j++) {
                int k = i+j;
                if(k>myComputeNodesList.size()) k %=myComputeNodesList.size(); 
                try {
                    url = "//" 
                        + myComputeNodesList.get(k).snd() 
                        + "/ComputeNode" 
                        + myComputeNodesList.get(k).fst();
                    ComputeNodeInterface computeNode = (ComputeNodeInterface) 
                        Naming.lookup(url);
                    
                    myMaps.get(i).setNode(myComputeNodesList.get(k));

                    // Exectuing task
                    computeNode.executeTask(myMaps.get(i));
                    isAssigned = true;
                    break;
                } catch (java.rmi.ConnectException e) {
                    lg.log(Level.SEVERE,"submitJob(list): failure on "
                           +"executeTask with url = "+ url + 
                           "\n\n Exception is" + e.getStackTrace());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // All nodes are died
            if (isAssigned == false) {
                
                myServerStats.getNoOfFailedJobs().incrementAndGet();
                client.jobResponse(null, null);
                clearJobData();
            
                lg.log(Level.SEVERE, "submitJob(list): All compute nodes "
                       +"are dead. Ignoring job!");
                return false;
           
            }

        }
        lg.log(Level.FINEST, "submitJob(list): Exit");        
        
        // Clearing job data
        //clearJobData();
        
        return true;
    }

    // XXX: Does not handle duplicate task aggregations
    // XXX: Does not handle errant tasks
    synchronized public Boolean aggregateMapTasks(MapTask t) throws RemoteException {
        lg.log(Level.FINEST, "aggregateMapTasks: Enter");
        lg.log(Level.FINER,"aggregateMapTasks: Have "
               + myReduce.getData().size() +" MapTasks, waiting for "
               + myMaps.size() + " (going to add "  + t.getTaskId() + " + done by " + t.getNode().fst() + ")");
        
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
            
            // Iterating through the list of nodes incase if all nodes are died.
            boolean isAssigned = false;
            for (int j = 0; j < myComputeNodesList.size(); j++) {
                try {
                    // XXX: Need to check if there is any compute node in 
                    // our list!
                    url = "//" 
                        + myComputeNodesList.get(j).snd() 
                        + "/ComputeNode" 
                        + myComputeNodesList.get(j).fst();
                    ComputeNodeInterface computeNode = (ComputeNodeInterface) 
                        Naming.lookup(url);
                    
                    isAssigned = true;
                    
                    computeNode.executeTask(myReduce);
                    break;
                } catch (Exception e) {
                    lg.log(Level.SEVERE,"aggregateMapTasks: failure on "
                           +"executeTask with url = "+ url);
                    e.printStackTrace();
                    //System.exit(1);
                }
            }
            // Couldn't be able to assign a reduce task
            if (! isAssigned) {
                myServerStats.getNoOfFailedJobs().incrementAndGet();
                
                client.jobResponse(null, null);
                clearJobData();
                return false;
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
        
        myTaskStats.setEndJobTime(System.currentTimeMillis());
        
        client.jobResponse(myTaskStats, sortedlist);
        
        clearJobData();
        
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
        buf.append("\nNo of Failed jobs: " + myServerStats.getNoOfFailedJobs());
        
        return buf.toString();
    }
    
    @Override
    public void updateTaskTransfer(Task task) throws RemoteException {
        lg.log(Level.INFO, "Task has been migrated to node: " + task.getNode().fst());
        
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
            
            t.schedule(h, 0, 10 * 1000);
            
        } catch (Exception e) {
            System.out.println("Server failed: ");
            e.printStackTrace();
        }
    }
}