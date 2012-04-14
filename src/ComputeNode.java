/**
 * @description Implements the FileServer interface
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.rmi.*;
import java.rmi.server.*;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Exception;

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
    public void executeTask(Task t) throws RemoteException {
        // check load
        // Write threaded code
        
        // call sort/merge based on task type
    }
   
    /**
     * map task
     */
    private void sort() {
        
    }
    
    /**
     * reduce task
     */
    private void merge() {
        
    }
    
    /**
     * This method generates random number. Then this number will be checked 
     * against failed probability.
     */
    private Integer getProbability() {
        // Write code
        return 100;
    }
    
    /**
     * Internal method to get load
     */
    private void getCurrentLoad()  {
        // can be a simulated load or command output
        // can use taskCount
    }
    
    @Override
    public Boolean taskRequest() throws RemoteException {
        // returns yes/no based on load
        
        return null;
    }
    
    @Override
    public String getNodeStats() throws RemoteException {
        return "";
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
            ComputeNode fileserver = new ComputeNode(fileservername, underLoad, overLoad);
            
            Naming.rebind("FileServer" + Integer.toString(fileserver.getID()), fileserver);
        } catch (Exception e) {
            System.out.println("FileServer exception: ");
            e.printStackTrace();
        }
    }
}
