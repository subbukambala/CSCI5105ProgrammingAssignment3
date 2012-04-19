/**
 * @description Implements the FileServer interface
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.rmi.*;
import java.rmi.server.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.Collections;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.MalformedURLException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class ComputeNode extends UnicastRemoteObject 
    implements ComputeNodeInterface {

    private static String defaultconf = "../cfg/default.config";

    private static Logger lg;
    
    private ServerInterface server;
    
    private Integer id;
    
    private Double underLoadThreshold;
    
    private Double overLoadThreshold;
    
    private Double failProbability;

    private Double loadConstant;
    
    private Pair<Double,Double> loadGaussian;
    
    private Double heartBeatInterval;
    
    private NodeStats nodeStats;
    
    private Boolean isExecutingSortTask = false;
    
    
    /**
     * Used in simulating load
     */
    private static int tasksCount;
    
    public ComputeNode(String servername
                       ,Double _underLoadThreshold
                       ,Double _overLoadThreshold
                       ,Double _failProbability
                       ,Double _loadConstant
                       ,Pair<Double,Double> _loadGaussian
                       ,String  configFile) throws Exception {
        
        server = (ServerInterface) Naming.lookup("//" + servername 
                                                 + "/Server");


        id = server.registerNode();
        
        lg = new Logger("Compute Node:" + id);
        lg.log(Level.INFO, "ComputeNode " + id + " started.");
        
        // If a config file was specified
        if(configFile == null) {
            configFile = defaultconf;
        }
        
        try {
            // TODO: Only load if we need to.
            Properties properties = new Properties();
            properties.load(new FileInputStream(configFile));

            if(_overLoadThreshold == null)
                _overLoadThreshold = 
                    new Double
                      (
                       properties.getProperty
                         ("computenode.overload_threshhold")
                      );
            overLoadThreshold = _overLoadThreshold;

            if(_underLoadThreshold == null)
                _underLoadThreshold = 
                    new Double
                      (
                       properties.getProperty
                         ("computenode.underload_threshhold")
                      );
            underLoadThreshold = _underLoadThreshold;

            if(_failProbability == null)
                _failProbability = 
                    new Double
                      (
                       properties.getProperty
                         ("computenode.fail_probability")
                      );
            failProbability = _failProbability;

            lg.log(Level.FINER, "ComputeNode " 
               + id 
               + ": under load threshhold = " 
               + underLoadThreshold);
            lg.log(Level.FINER, "ComputeNode " 
               + id 
               + ": over load threshhold = " 
               + overLoadThreshold);
            lg.log(Level.FINER, "ComputeNode " 
               + id 
               + ": fail probability = " 
               + failProbability);

            loadConstant = _loadConstant;
            if(loadConstant != null)
                lg.log(Level.FINER, "ComputeNode " 
                       + id 
                       + ": load constant = " 
                       + loadConstant);

            loadGaussian = _loadGaussian;
            if(loadGaussian != null)
                lg.log(Level.FINER, "ComputeNode " 
                       + id 
                       + ": load gaussian = " 
                       + loadGaussian.fst()+","+loadGaussian.snd());

        } catch (Exception e) {
            lg.log(Level.SEVERE, "ComputeNode " 
               + id 
               + ": Constructor failure! " 
               + underLoadThreshold);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Integer getID() {
        return id;
    }
    
    @Override
    public void executeTask(Task task) throws RemoteException {
       
        Double load = getCurrentLoad();
        // If load is over the treshold
        if (load > overLoadThreshold) {
            
            // Get active nodes
            List<Pair<Integer, String>> computeNodes = server.getActiveNodes();
            
            // Ask each node whether it can takes your load?
            for (Integer i = 0; i < computeNodes.size(); i++) {
                if (computeNodes.get(i).fst() != id) {
                    ComputeNodeInterface c;
                    String url =  "//" + computeNodes.get(i).snd() 
                        + "/ComputeNode" + computeNodes.get(i).fst();
                    try {
                        c = (ComputeNodeInterface) Naming.lookup(url);

                        lg.log(Level.FINEST, "Requesting node " + computeNodes.get(i).fst() +
                                " for task transfer");
                        
                        // Requesting node to take up the task 
                        Boolean isAccepted = c.taskTransferRequest(task);
                        
                        // If transfer request is accepted, update server
                        if (isAccepted) {
                            lg.log(Level.INFO, "Node " + computeNodes.get(i).fst() +
                            " accepted the task transfer request");
                    
                            task.setNode(computeNodes.get(i));
                            server.updateTaskTransfer(task);
                            return;
                        }
                    } catch (ConnectException e) {
                        lg.log(Level.SEVERE, "Node with  "
                           + "with url = "+ url + "is not responding");
                           
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NotBoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    
        //
        // If node couldn't transfer or load is less than overLoadThreshold, 
        // spawns worker thread.
        //
        Thread t = new TaskExecutor(task);
        t.start();
    
    }
    
    private class HeartBeatHandler extends TimerTask {
        public void run() {
            lg.log(Level.FINEST, " HeartBeatHandler.run: Enter");

            try {
                if (! isExecutingSortTask || getProbability() > failProbability) {
                    lg.log(Level.INFO, " HeartBeatHandler.run: Alive.");

                    server.heartBeatMsg(id);
                }
                else {
                    // Turning off node.
                    lg.log(Level.WARNING, 
                           " **HeartBeatHandler.run: DEAD!(exit)");
                    System.exit(0);
                }
            } catch (RemoteException e) {
                lg.log(Level.SEVERE, 
                " **Unable to connect to server" + e.getMessage());
            }
            lg.log(Level.FINEST, " HeartBeatHandler.run: Exit");
        }
    }
    
    public class TaskExecutor extends Thread {
        Task myTask;
        public TaskExecutor(Task t) {
            myTask = t;
        }
        
        public void run() {
            if (myTask.getTaskType().equals(Task.TaskType.MAP)) {
                sort((MapTask)myTask);
            }
            else {
                merge((ReduceTask)myTask);
            }
        }
    }
   
    /**
     * map task
     */
    private void sort(MapTask t) {
        
        lg.log(Level.FINEST,"sort: Enter");
        // This is to make sure compute node fails only during sorting.
        isExecutingSortTask = true;
        
        try {
            Thread.sleep(30 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Iterator<Integer> iterator = t.getData().iterator();
        while (iterator.hasNext()) {
            lg.log(Level.FINER, "sort: Received integer -> " 
                   + iterator.next());
        }
        try {
            Collections.sort(t.getData());
            synchronized(t) {
                t.wait(10*t.getData().size());
            }
            server.aggregateMapTasks(t);
        }
        catch (Exception e) {
            lg.log(Level.SEVERE,"Sort:Failure");
            e.printStackTrace();
            System.exit(1);
        }
        lg.log(Level.FINEST,"sort: Exit");
        
        // This is to make sure compute node fails only during sorting.
        isExecutingSortTask = false;
    }
    
    /**
     * reduce task
     */
    private void merge(ReduceTask t) {
        lg.log(Level.FINEST,"merge: Enter");
        List<MapTask> list = t.getData();
        List<Integer> rv = new ArrayList<Integer>();
        // TODO: Merge all the lists!
        
        try {
            // XXX: This algorithm for merging is brain-dead.
            // It could be much more efficient!
            for(;list.size() > 0;) {
                Integer mini = null;
                int i = 0;
                
                for(;i<list.size();i++) {

                    if(mini == null 
                       || list.get(i).getData().get(0) 
                          < list.get(mini).getData().get(0)) {
                        mini = i;
                    }  
                }
                
                rv.add(list.get(mini).getData().get(0));
                // XXX : remove must behave sensibly here and decrement the 
                // size
                list.get(mini).getData().remove(0);
                if(list.get(mini).getData().size() == 0) {
                    if(list.remove((int)mini)==null) 
                        lg.log(Level.SEVERE,"merge: remove returned null!");
                }
            }
            MapTask mt = new MapTask();
            mt.setData(rv);
            t.getData().clear();
            t.getData().add(mt);
            server.aggregateReduceTasks(t);
        }
        catch (Exception e) {
            lg.log(Level.SEVERE,"Merge: Failure");
            e.printStackTrace();
            System.exit(1);
        }
        lg.log(Level.FINEST,"merge: Exit");
    }
    
    /**
     * This method generates random number. Then this number will be checked 
     * against failed probability.
     */
    private Double getProbability() {
        Double random = Math.random() * 100 ;
        return random;
    }
    
    /**
     * Internal method to get load
     */
    private Double getCurrentLoad()  {
        // can be a simulated load or command output
        // can use taskCount
        
        Double currLoad = 0d;
        try {
            Process p = Runtime.getRuntime().exec("uptime");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command

            // 11:57:57 up 29 min,  2 users,  load average: 0.27, 0.12, 0.09
            
            String s;
            //System.out.println("Here is the standard output of the command:\n");

            s = stdInput.readLine();
            s = s.replace(':', ',');
            
            String data[] = s.split(",");
           
            currLoad = Double.parseDouble(data[data.length - 2]);
            
        } catch(Exception e) {
            lg.log(Level.WARNING, "getCurrentLoad: Unable to parse system "
                   +" load information! Using constant,guassian or 0 ");

                   
        }
        if(loadConstant!=null) {
            lg.log(Level.FINER, " getCurrentLoad: Load =" + loadConstant);
            return loadConstant;
        }
        else if(loadGaussian!=null)  {
            currLoad = RandomGaussian.getGaussian
            (
                    loadGaussian.fst()
                    ,loadGaussian.snd()
                    );
            return currLoad;
        }
        else  {
            lg.log(Level.FINER, " getCurrentLoad: Load =0.0");
            return 0.0;        
        }
    }
    
    @Override
    public Boolean taskTransferRequest(Task task) throws RemoteException {
        
        Double load =  getCurrentLoad();
        
        lg.log(Level.FINER, "currLoad :" + load + " expectedLoad: " + 
                task.getExpectedLoad() + " overLoadThreshold :" + overLoadThreshold);
        
        
        if (load + task.getExpectedLoad() > overLoadThreshold) {
            return false;
        }
        
        // spawn task request...
        return true;
    }
    
    @Override
    public String getNodeStats() throws RemoteException {
        return "";
    }
    
    public static void main(String[] argv) {

        String fileservername = "localhost";
        String id = null;
        Double underLoad = null;
        Double overLoad = null;
        Double failProb = null;
        Double constantload = null;
        Pair<Double,Double> gaussian = null;
        String fileName = null;

        ArgumentHandler cli = new ArgumentHandler
            (
             "FileServer [-h] [collector address] [-u underload] "
             +"[-o overload] [-c constant_load|-g mean variance] "
             +"[-p fail_prob] [-f configfile]"
             ,
             "Bala Subrahmanyam Kambala, Daniel William DaCosta - "
             +"GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
             ,""
             );
        cli.addOption("h", "help", false, "Print this usage information.");
        cli.addOption("u", "underLoad", true, "Under load threshold");
        cli.addOption("o", "overLoad", true, "Over load threshold");
        cli.addOption("c", "constant", true, "Generate constant load");
        cli.addOption("p", "probability", true, "Fail Probability(0-100)");
        cli.addOption("f", "configfile", true
                      ,"The configuration file to read parameters from. "
                      +"The default is "+defaultconf+". "
                      +"Command line arguments will override config file "
                      +"arguments."
                      );
        cli.addOption(OptionBuilder
                      .withLongOpt("gaussian")
                      .hasArgs(2)
                      .withDescription
                      (
                        "Generate a gaussian probability model for load "
                        + "simulation. The first parameter is the mean "
                        + "and the second parameter is the variance."
                      )
                      .create('g')); 
        


        // parse command line
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }
        
        if (commandLine.hasOption('u')) {
            // TODO : Ensure the number is within range
            underLoad = Double.parseDouble(commandLine.getOptionValue('u'));
        }
        
        if (commandLine.hasOption('o')) {
            // TODO : Ensure the number is within range
            overLoad = Double.parseDouble(commandLine.getOptionValue('o'));
        }

        if (commandLine.hasOption('p')) {
            // TODO : Ensure the number is within range
            failProb = Double.parseDouble(commandLine.getOptionValue('p'));
        }

        if (commandLine.hasOption('c')) {
            // TODO : Ensure the number is within range
            constantload = Double.parseDouble(commandLine.getOptionValue('c'));
        }

        if (commandLine.hasOption('g')) {
            // TODO : Ensure the number is within range
            gaussian = new Pair<Double,Double>
                (
                 Double.parseDouble(commandLine.getOptionValues('g')[0])
                 ,Double.parseDouble(commandLine.getOptionValues('g')[1])
                );
        }
        

        // TODO: If these flags are no longer mutually exclusive this
        // code should be adjusted to account for whatever constraint are
        // needed.
        if ( (constantload != null) && (gaussian!=null)) {
            cli.usage("-g -c switches are mutually exclusive!\n");
            System.exit(1);
        }
       

        if (commandLine.hasOption('f')) {
            fileName = commandLine.getOptionValue('f');
        }
        
        if (commandLine.getArgs().length != 0)
            fileservername = commandLine.getArgs()[0];
        try {
            ComputeNode node = 
                new ComputeNode(fileservername
                                ,underLoad
                                ,overLoad
                                ,failProb
                                ,constantload
                                ,gaussian
                                ,fileName);
            
            Naming.rebind("ComputeNode" 
                          + Integer.toString(node.getID()), node);
            
            // Scheduling heart beat message handler
            Timer t = new Timer();
            HeartBeatHandler h = node.new HeartBeatHandler();
            t.schedule(h, 0, 3 * 1000);

        } catch (ConnectException ce) {
            lg.log(Level.SEVERE, "Server is not alive");
            ce.printStackTrace();
        }
        catch (Exception e) {
            lg.log(Level.SEVERE, "Exception in file server");
            e.printStackTrace();
        }
    }
}
