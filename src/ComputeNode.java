/**
 * @description Implements the FileServer interface
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.rmi.*;
import java.rmi.server.*;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.MalformedURLException;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class ComputeNode extends UnicastRemoteObject implements ComputeNodeInterface {

    private Logger lg;
    
    private ServerInterface server;
    
    private Integer id;
    
    private Integer underLoadThreshold;
    
    private Integer overLoadThreshold;
    
    private Double failProbability;
    
    private Double heartBeatInterval;
    
    private NodeStats nodeStats;
    
    /**
     * Used in simulating load
     */
    private static int tasksCount;
    
    
    public ComputeNode(String servername, Integer _underLoadThreshold, Integer _overLoadThreshold) throws Exception {
        
        server = (ServerInterface) Naming.lookup("//" + servername + "/Server");

        id = server.registerNode();
        
        lg = new Logger("Compute Node:" + id);
        lg.log(Level.FINER, "ComputeNode " + id + " started.");
        
        overLoadThreshold = _overLoadThreshold;
        underLoadThreshold = _underLoadThreshold;
        
        // Loading config file
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("../cfg/node.config"));
            failProbability = 
                Double.parseDouble(properties.getProperty("fail.probability"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getID() {
        return id;
    }
    
    @Override
    public void executeTask(Task task) throws RemoteException {
        
        Double load = getCurrentLoad();
        if (load > overLoadThreshold) {
            List<Pair<Integer, String> > computeNodes = server.getActiveNodes();
            
            for (Integer i = 0; i < computeNodes.size(); i++) {
                if (computeNodes.get(i).fst() != id) {
                    ComputeNodeInterface c;
                    try {
                        c = (ComputeNodeInterface) Naming.lookup("//" + computeNodes.get(i).snd()
                                + "/ComputeNode");

                        // Requesting node to take up the task 
                        Boolean isAccepted = c.taskTransferRequest(task);
                        
                        // If transfer request is accepted, update server
                        if (isAccepted) {
                            task.setNode(computeNodes.get(i));
                            server.updateTaskTransfer(task);
                            return;
                        }
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
            try {
                if (getProbability() > failProbability) {
                    server.heartBeatMsg(id);
                }
                else {
                    // Turning off node.
                    System.exit(0);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private class TaskExecutor extends Thread {
        Task myTask;
        public TaskExecutor(Task t) {
            myTask = t;
        }
        
        public void run() {
            if (myTask.getCurrentTaskType().equals(Task.TaskType.MAP)) {
                sort();
            }
            else {
                merge();
            }
        }
    }
   
    /**
     * map task
     */
    private void sort() {
        System.out.println("Sorting...");
    }
    
    /**
     * reduce task
     */
    private void merge() {
        System.out.println("Merging...");
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
            
            lg.log(Level.FINER, " Load :" + currLoad);
            
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        return currLoad * 100;
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
    
    @Override
    public void transferData(
            DataTransferInterface DTI,
            Integer jobId, 
            Integer maxBufferWindow,
            String srcFilePath,
            String destFilePath) throws RemoteException 
    {
        DataTransferHandler d = new DataTransferHandler();
        d.transferData(DTI, jobId, maxBufferWindow, srcFilePath, destFilePath);
    }

    @Override
    public void storeData(String data, String filePath) throws RemoteException {
        DataTransferHandler d = new DataTransferHandler();
        d.storeData(data, filePath);
    }

    @Override
    public void onDataTransferComplete(Integer jobId, String filePath) throws RemoteException {
        // Start executing task

        System.out.println("data transfer is complete");
    }

    public static void main(String[] argv) {

        String fileservername = "localhost";
        String id = null;

        ArgumentHandler cli = new ArgumentHandler
                (
                        "FileServer [-h] [collector address] -u -o"
                        , "TBD - Currently does nothing."
                        ,
                        "Bala Subrahmanyam Kambala, Daniel William DaCosta - GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
                );
        cli.addOption("h", "help", false, "Print this usage information.");
        cli.addOption("u", "underLoad", true, "Under load threshold");
        cli.addOption("o", "overLoad", true, "Over load threshold");
        
        // parse command line
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }
        
        Integer underLoad = 30;
        Integer overLoad = 90;
        if (commandLine.hasOption('u')) {
            underLoad = Integer.parseInt(commandLine.getOptionValue('u'));
        }
        
        if (commandLine.hasOption('o')) {
            overLoad = Integer.parseInt(commandLine.getOptionValue('o'));
        }
        
        if (commandLine.getArgs().length != 0)
            fileservername = commandLine.getArgs()[0];
        try {
            ComputeNode node = new ComputeNode(fileservername, underLoad, overLoad);
            
            Naming.rebind("ComputeNode" + Integer.toString(node.getID()), node);
            
            // Scheduling heart beat message handler
            Timer t = new Timer();
            HeartBeatHandler h = node.new HeartBeatHandler();
            
            t.schedule(h, 0, 30 * 1000);
        } catch (Exception e) {
            System.out.println("FileServer exception: ");
            e.printStackTrace();
        }
    }
}
