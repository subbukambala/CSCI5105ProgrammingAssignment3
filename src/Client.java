/**
 * @descriptrion Implements a Client to query the psuedo-mapreduce framework
 * for statistics and to submit a job.
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
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;


public class Client extends UnicastRemoteObject implements ClientInterface {

    private static Logger lg;

    private static ServerInterface server;

    /**
     * The Client constructor.
     */
    Client(String servername ) throws Exception {
        server = (ServerInterface) 
            Naming.lookup("//" + servername + "/Server");
        lg = new Logger("Client");
        lg.log(Level.FINER, "Client started.");
    }

    @Override
        public void jobResponse(TaskStats stats, String outputFilePath) throws RemoteException {
        // prints job stats
    }

    /**
     * Submit a job to the server.
     * A job requires a filename.
     * The filename should store a file containing 0 or more integers separated
     * by newlines.
     */
    public  void submitJob(String filename) throws Exception {
        lg.log(Level.FINEST, "File = " + filename);
        FileInputStream fstream = new FileInputStream(filename);

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        
        List<Integer> data = new ArrayList<Integer>();
        String strInt = null;
        try {
            // Read a list of integers from a file.
            // The format of the file is very specific.
            // Each integer will be separated by a new line.
            while ((strInt = br.readLine()) != null) {
                System.out.println(strInt);
                Integer i = new Integer(strInt);
                data.add(i);
            }
        } catch(Exception e) {
            System.out.println
                (
                 "File must consist of 0 or more integers separated "
                 +"by newlines!"
                 );
            System.exit(1);
        }
        // Close the input stream
        in.close();
        server.submitJob(data);        
    }

    /**
     *
     */
    private void getNodeStats() {

    }

    public void transferData(
                             DataTransferInterface DTI,
                             Integer maxBufferWindow,
                             Integer jobId,
                             String srcFilePath,
                             String destFilePath) throws RemoteException

    {
        DataTransferHandler d = new DataTransferHandler();
        d.transferData(DTI, jobId, maxBufferWindow, srcFilePath, destFilePath);
        
        DTI.onDataTransferComplete(jobId, destFilePath);
    }

    public void storeData(String data, String filePath) 
        throws RemoteException {
        DataTransferHandler d = new DataTransferHandler();
        d.storeData(data, filePath);
    }

    public void onDataTransferComplete(Integer jobId, String filePath) 
        throws RemoteException {
        // print sorted data here
        System.out.println("data transfer is complete");
    }
 
    /**
     *
     */
    private void getServerStats() {

    }

    public static void main(String[] argv) {
        String server = "localhost";
        boolean fileSwitch = false;
        boolean nodeStatsSwitch = false;
        boolean serverStatsSwitch = false;
        ArgumentHandler cli = new ArgumentHandler
            (
             "Client [-h|-n|-s] [Server address]  "
             ,
             "A Client interface to Programming Assignment 3 for CSci5105."
             +" This client serves three primary objectives: 1) Submission"
             +" of files to the pseudo-mapreduce framework for sorting,"
             +" 2) Querying the server for compute node statistics,"
             +" 3) Querying the server for server statistics."
             ,"Bala Subrahmanyam Kambala, Daniel William DaCosta - GPLv3 "
             +"(http://www.gnu.org/copyleft/gpl.html)"
             );

        cli.addOption("h", "help", false, "Print this usage information.");
        cli.addOption("n","nodestats",true,"Prints the statistics of a node");
        cli.addOption
            ("s"
             ,"serverstats"
             ,false
             ,"Prints statistics of a server");
        cli.addOption
            ("f"
             ,"file"
             ,true
             ,"Submit a sort job to the server as a file.");

        // parse command line
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }

        if (commandLine.getArgs().length == 1)
            server = commandLine.getArgs()[0];
        else {
            cli.usage("Client only accepts one argument!");
            System.exit(1);
        }

        if (server == null) {
            cli.usage("Server address required!");
            System.exit(1);
        }

        if (commandLine.hasOption('n')) {
            nodeStatsSwitch = true;
            cli.usage("Unimplemented option!");
            System.exit(1);
        }

        if (commandLine.hasOption('s')) {
            serverStatsSwitch = true;
            cli.usage("Unimplemented option!");
            System.exit(1);
        }

        String filepath = null;
        if (commandLine.hasOption('f')) {
            fileSwitch = true;
            filepath = commandLine.getOptionValue('f');
            if (filepath == null) {
                cli.usage("No file provided!\n");
                System.exit(1);
            }

        }
        
        if (
            !((serverStatsSwitch ^ fileSwitch) 
            && (nodeStatsSwitch ^ fileSwitch))
           ) {
                cli.usage("-n -s -f switches are mutually exclusive!\n");
                System.exit(1);
        }


        Client client = null;
        try {
            client = new Client(server);
            Naming.rebind("Client", client);
            if ( fileSwitch ) client.submitJob(filepath);
        } catch (Exception e) {
            System.out.println("Client failed: ");
            e.printStackTrace();
        }
    }

}
