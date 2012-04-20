/**
 * @descriptrion Implements a Client to query the psuedo-mapreduce framework
 * for statistics and to submit a job.
 *
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;


public class Client extends UnicastRemoteObject implements ClientInterface {

    private static Logger lg;

    private static ServerInterface server;

    private List<Integer> results;

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
    synchronized public void jobResponse(TaskStats stats, List<Integer> _results) throws RemoteException {
        results = _results;
        
        // If results are null, server couldn't process job
        if (_results == null) {
            System.out.println("\nServer couldn't process job. All compute nodes are dead");
        }
        else {
            // Printing job statistics
            if (stats != null) {
                System.out.println("********         Job Stats          *******");
                System.out.println("No of Map tasks: " + stats.getNoOfMapTasks());
                System.out.println("No of Reduce tasks: " + stats.getNoOfReduceTasks());
                System.out.println("Total run time (millis): " + stats.getTotalTime());
            }
        }
        notify();
    }

    /**
     * Submit a job to the server.
     * A job requires a filename.
     * The filename should store a file containing 0 or more integers separated
     * by newlines.
     */
    public void submitJob(String filename) throws Exception {
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
        if (server.submitJob(data))
            synchronized(this){wait();}
        else
            System.out.println("Server has declined job!");
    }

    /**
     * Returns Node stats
     */
    private static void getNodeStats(Integer nodeId) {
        String url = "";
        try {
            // Finding IP address using node id
            String ipAddr = "";
            List<Pair<Integer, String> > nodes = server.getActiveNodes();
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) != null && nodes.get(i).fst() == nodeId) {
                    ipAddr = nodes.get(i).snd();
                }
            }
            url = "//" + ipAddr + "/ComputeNode" + nodeId;

            ComputeNodeInterface computeNode = (ComputeNodeInterface) Naming
                    .lookup(url);

            // Getting node stats
            String stats = computeNode.getNodeStats();

            lg.log(Level.INFO, "\n Node Stats :\n" + stats);

        } catch (ConnectException ce) {
            lg.log(Level.SEVERE, "Unable to connect to node using url:" + url + "\n\n"
                    + "Exception is : " + ce.getStackTrace());
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (RemoteException re) {
            re.printStackTrace();
        }

    }

    /**
     * Returns server statistics
     */
    private static void getServerStats() {
        try {
            // Getting server stats
            String stats = server.getServerStats();
            
            lg.log(Level.INFO, "\nServer Stats :\n" + stats);
            
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        
    }

    public static void main(String[] argv) {
        String server = "localhost";
        boolean fileSwitch = false;
        boolean nodeStatsSwitch = false;
        boolean serverStatsSwitch = false;
        ArgumentHandler cli = new ArgumentHandler
            (
             "Client [-h|-n|-s] [Server address] [-f filename] "
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
            cli.usage("Client only accepts one argument!\n\n");
            System.exit(1);
        }

        if (server == null) {
            cli.usage("Server address required!\n\n");
            System.exit(1);
        }
        
        int count = 0;
        
        if (commandLine.hasOption('n')) {
            nodeStatsSwitch = true;
            count++;
        }

        if (commandLine.hasOption('s')) {
            serverStatsSwitch = true;
            count++;
        }

        String filepath = null;
        if (commandLine.hasOption('f')) {
            count++;
            fileSwitch = true;
            filepath = commandLine.getOptionValue('f');
            if (filepath == null) {
                cli.usage("No file provided!\n");
                System.exit(1);
            }

        }

        // TODO: If these flags are no longer mutually exclusive this
        // code should be adjusted to account for whatever constraint are
        // needed.
        if (count > 1) {
                cli.usage("-n -s -f switches are mutually exclusive!\n");
                //System.exit(1);
        }


        Client client = null;
        try {
            // Binding client
            client = new Client(server);
            Naming.rebind("Client", client);
            
            // If input is file
            if ( fileSwitch ) {
                client.submitJob(filepath);
                System.out.println("\nSorted results: ");
                if (client.results != null) {
                    Iterator<Integer> iterator = client.results.iterator();
                    while (iterator.hasNext()) {
                        System.out.println("result = " + iterator.next());
                    }
                }
            }
            else {
                // get server stats
                if (serverStatsSwitch) {
                    getServerStats();
                }
                
                // get node stats
                if (nodeStatsSwitch) {
                    Integer nodeId = Integer.parseInt(commandLine.getOptionValue('n'));
                    getNodeStats(nodeId);
                }
            }
            
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Client failed: ");
            e.printStackTrace();
        }
    }

}
