/**
 * Collector implements methods required to maintain file servers, form quorums,   
 * 
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 * @descriptrion Implements the Super Peer Interface.
 */

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import org.apache.commons.cli.CommandLine;

import java.security.NoSuchAlgorithmException;
import java.io.FileNotFoundException;
import java.lang.Math.*;
import java.net.MalformedURLException;

import javax.naming.CannotProceedException;

public class Collector extends UnicastRemoteObject implements CollectorInterface {

    private Logger lg;
    
    /**
     * This variable is to ensure that Collector generates unique file server id. 
     */
    private Integer maxFileServerId;
    
    /**
     * This variable is to ensure that Collector generates unique request id. 
     */
    private Integer maxRequestId;
    
    /**
     * Key is filename, value is list of file servers
     */
    private Map<String, List<Integer> > myFileStore;
    
    /**
     * Contains registered File server list.
     */
    private List<Pair<Integer,String>> myFileServers;

    /**
     * Contains read quorum set. This varies for each request
     */
    private Map<Integer, List<Pair<Integer,String>> > myReadQuorum;
    
    /**
     * Input variable from config. Indicates read quorum size.
     */
    private Integer myReadQuorumSize;
    
    /**
     * Contains write quorum set. This varies for each request
     */
    private Map<Integer, List<Pair<Integer,String>>> myWriteQuorum;
    
    /**
     * Input variable from config. Indicates write quorum size.
     */
    private Integer myWriteQuorumSize;
    
    /**
     * Contains list of read/write requests
     */
    private List<Request> myReqQueue;
    
    Collector(int _readqorum, int _writeqorum) throws Exception {
        lg = new Logger("Collector");
        lg.log(Level.FINER, "Collector started.");
        myFileServers = new ArrayList<Pair<Integer,String>> ();
        myFileStore = new HashMap<String, List<Integer> > ();
        myWriteQuorumSize = _writeqorum;
        myReadQuorumSize = _readqorum;
        myReqQueue = new ArrayList<Request>();
        maxFileServerId = 0;
        maxRequestId = 0;
        
        myReadQuorum = new HashMap<Integer, List<Pair<Integer,String>> > ();
        myWriteQuorum = new HashMap<Integer, List<Pair<Integer,String>> > ();
    }
    
    /**
     * This method forms quorum and handles request to ProcessRequest class.
     */
    private synchronized void processRequest() {
        if (myReqQueue.isEmpty()) {
            return;
        }
        
        Request req = myReqQueue.get(0);
       
        // Forms quorum
        boolean isQuorumFormed = false;
        try {
            isQuorumFormed = formQuorum(req.getRequestId(), req.isReadRequest());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // If only quorum is formed, request is processed by ProcessRequest class.
        // otherwise, collector will wait till it gets response from File server
        if (isQuorumFormed) {
            myReqQueue.remove(0);
            (new ProcessRequest(req)).start();
            processRequest();
        }
    }

    /**
     * This class handles process all read requests concurrently and write requests 
     * in sequential manner.
     */
    private class ProcessRequest extends Thread {
        
        Request req;
        ProcessRequest(Request _req) {
            req = _req;
        }
        
        public void run() {
            try {
                // if request is read
                if (req.isReadRequest()) {
                    ReadRequest readReq = null;  
                    if (req instanceof ReadRequest) {
                        readReq = (ReadRequest) req;
                    }
                    
                    read(readReq);
                   
                }
                else {
                    // if request is write
                    WriteRequest writeReq = null;  
                    if (req instanceof WriteRequest) {
                        writeReq = (WriteRequest) req;
                    }
                   
                    write(writeReq);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This method is used to check existence of file.
     */
    private boolean isFileExists(String fileName) throws RemoteException {
        boolean fileExists = false;
        for (Integer i = 0; i < myFileServers.size(); i++) {
            String url = "//" + myFileServers.get(i).snd() + "/FileServer" 
                            + myFileServers.get(i).fst();
            FileServerInterface f;
            try {
                f = (FileServerInterface) Naming.lookup(url);
                if (f.containsFile(fileName)) {
                    fileExists = true;
                    break;
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NotBoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return fileExists;
    }
    
    @Override
    public synchronized void submitRequest(Request req) throws Exception {
        
        if (req.isReadRequest() || (! req.isReadRequest() && ! req.getIsNewFile())) {
            if (! isFileExists(req.getFileName())) {
                throw new FileNotFoundException ("File doesn't exists"); 
            }
        }
        
        // increments of number of requests requests 
        maxRequestId++;
        req.setRequestId(maxRequestId);
        // sets client IP address
        req.setClientIP(getClientHost());
        myReqQueue.add(req);
        processRequest();
    }
    
    /**
     * This method queries each file server and finds maximum version of file server and 
     * calls requested client call back method. 
     */
    private void read(ReadRequest readRequest) throws Exception {
        lg.log(Level.FINER, "Collector Read.");
        
        // Getting file server which has latest version
        Integer maxVersionId = -1;
        String readServer =  null;
        List<Pair<Integer,String>> readQuorumList = myReadQuorum.get(readRequest.getRequestId()); 
        for (Integer i = 0; i < myReadQuorumSize; i++) {
            
            String url = "//" + readQuorumList.get(i).snd() + "/FileServer" + readQuorumList.get(i).fst();
            FileServerInterface f = (FileServerInterface) Naming.lookup(url);
            Integer versionId = f.getFileVersion(readRequest.getFileName());
            
            // If file server has max version store its url and version id
            if (maxVersionId < versionId) {
                maxVersionId = versionId;
                readServer = url;
            }
        }
        
        // Construct response and calls client call back method.
        ReadResponse res = new ReadResponse();
        res.setRequestId(readRequest.getRequestId());
        res.setFileServerUrl(readServer);
        res.setFileName(readRequest.getFileName());

        ClientInterface c = (ClientInterface) Naming.lookup("//" + 
            readRequest.getClientIP() + "/Client" + readRequest.getClientId());
        c.read(res);
    }

    
    /**
     * This method queries each file server and finds maximum version of file server and 
     * calls requested client call back method. 
     */
    private void write(WriteRequest writeRequest) throws Exception {
        lg.log(Level.FINER, "Collector Write.");
        
        // Getting file server which has latest version
        Integer maxVersionId = -1;
        String writeServer =  null;
        List<Pair<Integer,String>> writeQuorumList = myWriteQuorum.get(writeRequest.getRequestId()); 
        
        for (Integer i = 0; i < myWriteQuorumSize; i++) {
            String url = "//" + writeQuorumList.get(i).snd() + "/FileServer" 
                            + writeQuorumList.get(i).fst();
            FileServerInterface f = (FileServerInterface) Naming.lookup(url);
            Integer versionId = f.getFileVersion(writeRequest.getFileName());
            
            // if it new file, return first server in quorum
            if (writeRequest.getIsNewFile()) {
                writeServer = url;
                break;
            }
            // If file server has max version store its url and version id
            if (maxVersionId < versionId) {
                maxVersionId = versionId;
                writeServer = url;
            }
        }
        
        // Construct WriteResponse and calls client call back method.
        WriteResponse res = new WriteResponse();
        res.setRequestId(writeRequest.getRequestId());
        res.setFileServerUrl(writeServer);
        res.setFileName(writeRequest.getFileName());
        
        ClientInterface c = (ClientInterface) Naming.lookup("//" + 
            writeRequest.getClientIP() + "/Client" + writeRequest.getClientId());
        c.write(res);
    }
    
    /**
     * This method asks each file server to participate quorum. If required number 
     * of file servers agrees then form quorum and proceeds request otherwise, 
     * disbands partial quorum.
     */
    private boolean formQuorum(Integer reqId, boolean isReadRequest) throws Exception {
        boolean isFormedQuorum = false;
        
        if (myWriteQuorumSize > myFileServers.size() ||  myReadQuorumSize > myFileServers.size()) {
            throw new Exception("Read | Write Quorum size shouldn't be greater than no of file servers");
        }
        
        if ((myWriteQuorumSize + myReadQuorumSize <= myFileServers.size()) || 
            (myWriteQuorumSize <= myFileServers.size() / 2)) 
        {
            throw new CannotProceedException ("Quorum requirement is not satisfied." +
            		"(Either Nr + Nw < N or Nw < N/2)");
        }
        
        // Generating array of n random numbers (shuffling numbers n times)
        int randArr[] = new int [myFileServers.size()];
        for (int i = 0; i < randArr.length; i++) {
            randArr[i] = i;
        }

        for (int i = 0; i < randArr.length; i++) {
            if (randArr.length > 1) {
                int random = (int) (Math.random() * 100) % randArr.length;
                int temp = randArr[i]; 
                randArr[i] = randArr[random];
                randArr[random] = temp;
            }
        }
        
        List<Pair<Integer,String>> quorumList = new ArrayList<Pair<Integer, String>> (); 
        
        // If request is read
        if (isReadRequest) {
            int count = 0;
            // Asking each server a vote for the request
            for (int i = 0; i < myFileServers.size(); i++) {
                String url = "//" + myFileServers.get(randArr[i]).snd() + "/FileServer" 
                            + myFileServers.get(randArr[i]).fst();
                FileServerInterface f = (FileServerInterface) Naming.lookup(url);
                
                boolean status = f.joinQuorum(isReadRequest);
                // if file server said YES
                if (status) {
                    count++;
                    quorumList.add(myFileServers.get(randArr[i]));
                    if (count == myReadQuorumSize) {
                        isFormedQuorum = true;
                        myReadQuorum.put(reqId, quorumList);
                        break;
                    }
                }
            }
        }
        else {
            // If request is write
            int count = 0;
            for (int i = 0; i < myFileServers.size(); i++) {
                String url = "//" + myFileServers.get(randArr[i]).snd() + "/FileServer" + myFileServers.get(randArr[i]).fst();
                FileServerInterface f = (FileServerInterface) Naming.lookup(url);
                boolean status = f.joinQuorum(isReadRequest);
                // if file server said YES
                if (status) {
                    count++;
                    quorumList.add(myFileServers.get(randArr[i]));
                    if (count == myWriteQuorumSize) {
                        isFormedQuorum = true;
                        myWriteQuorum.put(reqId, quorumList);
                        break;
                    }
                }
            }
        }
        
        // If quorum is not formed then release servers who said YES.
        if (! isFormedQuorum) {
            for (int i = 0; i < quorumList.size(); i++) {
                String url = "//" + quorumList.get(i).snd() + "/FileServer" + quorumList.get(i).fst();
                FileServerInterface f = (FileServerInterface) Naming.lookup(url);
                f.releaseQuorum(isReadRequest);
            }
        }
        
        return isFormedQuorum;
    }
    
    /**
     * This is a internal method to find a server which contains latest version
     */
    private FileServerInterface findLatestVersion(String fileName) {
        // Iterates through all servers and find latest version
        return null;
    }
    
    @Override
    synchronized public Integer join() throws Exception {
        lg.log(Level.FINER, "File server joined.");
        
        maxFileServerId++;
        myFileServers.add(new Pair<Integer,String>(maxFileServerId,getClientHost()));
        return maxFileServerId;
    }
    
    @Override
    public List<Pair<Integer,String>> getWriteQuorum(Integer reqId) throws RemoteException {
        return myWriteQuorum.get(reqId);
    }
    
    @Override
    public List<Pair<Integer,String>> getReadQuorum(Integer reqId) throws RemoteException {
        return myReadQuorum.get(reqId);
    }
    
    @Override
    public void notifyFileUpdate(String fileName) throws RemoteException {
        // when file is updated, notifies other file servers in quorum to get them updated
    }
    
    @Override
    public void onRequestComplete(Integer reqId) throws RemoteException {
        // Read next request from queue and process.
        processRequest();
    }

    /**
     * The good stuff.
     */
    public static void main(String[] argv) {
        int rq = 1;
        int wq = 1;
        ;
        ArgumentHandler cli = new ArgumentHandler
                        (
                                "Collector [-h] [-r int] [-w int]"
                                , "TBD."
                                ,
                                "Bala Subrahmanyam Kambala, Daniel William DaCosta - GPLv3 (http://www.gnu.org/copyleft/gpl.html)"
                         );
        cli.addOption("h", "help", false, "Print this usage information.");
        cli.addOption("r", "readqorum", true, "The number required for a read qorum (Default is "
                + Integer.toString(rq) + ").");
        cli.addOption("w", "writeqorum", true, "The number required for a write qorum (Default is "
                + Integer.toString(wq) + ").");

        CommandLine commandLine = cli.parse(argv);
        if (commandLine.hasOption('h')) {
            cli.usage("");
            System.exit(0);
        }
        if (commandLine.hasOption('r')) {
            rq = Integer.parseInt((commandLine.getOptionValue('r')));
        }
        if (commandLine.hasOption('w')) {
            wq = Integer.parseInt((commandLine.getOptionValue('w')));
        }
        try {
            Naming.rebind("Collector", new Collector(rq, wq));
        } catch (Exception e) {
            System.out.println("Collector failed: ");
            e.printStackTrace();
        }
    }
}
