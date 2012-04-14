/**
 * @description Implements the FileServer interface
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.rmi.*;
import java.rmi.server.*;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.lang.Exception;
import java.net.MalformedURLException;

public class ComputeNode extends UnicastRemoteObject implements ComputeNodeInterface {

    private Logger lg;
    private ServerInterface server;
    private Integer id;
    
    ComputeNode(String servername) throws Exception {
        
        server = (ServerInterface) Naming.lookup("//" + servername + "/Server");

        id = server.registerNode();
        
        lg = new Logger("Compute Node:" + id);
        lg.log(Level.FINER, "ComputeNode " + id + " started.");
    }

    public Integer getID() {
        return id;
    }
    
    public static void main(String[] argv) {

        String fileservername = "localhost";
        String id = null;

        ArgumentHandler cli = new ArgumentHandler
                (
                        "FileServer [-h] [collector address]"
                        , "TBD - Currently does nothing."
                        ,
                        "Bala Subrahmanyam Kambala, Daniel William DaCosta - GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
                );
        cli.addOption("h", "help", false, "Print this usage information.");

        // parse command line
        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }

        if (commandLine.getArgs().length != 0)
            fileservername = commandLine.getArgs()[0];
        try {
            ComputeNode fileserver = new ComputeNode(fileservername);
            
            Naming.rebind("FileServer" + Integer.toString(fileserver.getID()), fileserver);
        } catch (Exception e) {
            System.out.println("FileServer exception: ");
            e.printStackTrace();
        }
    }
}
