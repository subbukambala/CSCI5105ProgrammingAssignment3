/**
 * @descriptrion Implements a Client read/write requests
 *  
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.rmi.*;
import java.rmi.server.*;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;


public class Client extends UnicastRemoteObject implements ClientInterface {

    private static Logger lg;
    
    private static ServerInterface server;
    
    String id;
    
    /**
     * The Client constructor.
     */
    Client(String servername, String _id, boolean _debug) throws Exception {
        id = _id;
        // Handles proper read and write behavior.
        if(!_debug) {
            server = (ServerInterface) Naming.lookup("//" + servername + "/Server");
            lg = new Logger("Client:" + id);
        }
        
        lg.log(Level.FINER, "Client started.");
    }
    
    @Override
    public void jobResponse(TaskStats stats, String outputFilePath) throws RemoteException {
        // prints job stats
    }
    
    /**
     * 
     */
    private void submitJob() {
       
    }
    
    /**
     * 
     */
    private void getNodeStats() {
        
    }
    
    public void transferData(
            DataTransferInterface DTI,
            Integer jobId,
            Integer maxBufferWindow,
            String srcFilePath,
            String destFilePath) throws RemoteException 
    {
        DataTransferHandler d = new DataTransferHandler();
        d.transferData(DTI, jobId, maxBufferWindow, srcFilePath, destFilePath);
        
        DTI.onDataTransferComplete(jobId, destFilePath);
    }

    public void storeData(String data, String filePath) throws RemoteException {
        DataTransferHandler d = new DataTransferHandler();
        d.storeData(data, filePath);
    }

    public void onDataTransferComplete(Integer jobId, String filePath) throws RemoteException {
        // print sorted data here
        System.out.println("data transfer is complete");
    }
    
    /**
     * 
     */
    private void getServerStats() {
        
    }

    public static void main(String[] argv) {

        String id = null;
        ArgumentHandler cli = new ArgumentHandler
                (
                        "Client [-h] [id] [Server address] [-n] [-s] "
                        ,
                        "A Client interface to Programming Assignment 3 for CSci5105."
                                + " This client requests collector collector to"
                                + " form a quorum which will ensure safe concurrent operations"
                        ,
                        "Bala Subrahmanyam Kambala, Daniel William DaCosta - GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
                );

        cli.addOption("h", "help", false, "Print this usage information.");
        cli.addOption("n", "nodestats", true, "Prints the statistics of a node");
        cli.addOption("s", "serverstats", false, "Prints sstatistics of a server");

        // parse command line
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }

        if (commandLine.getArgs().length != 0)
            id = commandLine.getArgs()[0];

        if (id == null) {
            cli.usage("Id required!");
            System.exit(1);
        }

        String server = "localhost";
        if (commandLine.getArgs().length >= 2)
            server = commandLine.getArgs()[1];

        if (commandLine.hasOption('n')) {
            System.out.print("Needs to be implemented.\n");
        }

        if (commandLine.hasOption('s')) {
            System.out.print("Needs to be implemented.\n");
        }

        Client client = null;
        try {
            client = new Client(server, id, false);
            Naming.rebind("Client" + id, client);
            
        } catch (Exception e) {
            System.out.println("Client failed: ");
            e.printStackTrace();
        }
    }

}
