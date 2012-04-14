/**
 * @description 
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.rmi.*;
import java.rmi.server.*;
import java.util.List;

public interface ServerInterface extends Remote, DataTransferInterface {
    
    /**
     * File server joins in network to serve collaborative document edit
     */
    public Integer registerNode() throws Exception;

    /**
     */
    public List<Pair<Integer,String>> getActiveNodes() throws RemoteException;
    
    /**
     */
    public Boolean submitJob() throws RemoteException;
    
    /**
     */
    public void heartBeatMsg(Integer nodeId) throws RemoteException;
    
    public String getServerStats() throws RemoteException;
    
    /**
     * 
     */
    public void updateTaskTransfer(Task task) throws RemoteException;
    
    
    /**
     * 
     */
    public void storeData(String data, String filePath) throws RemoteException;
    
    /**
     * This method is called by client on data transferComplete
     */
    public void onDataTransferComplete(Integer jobId, String filePath) throws RemoteException;
    
}
