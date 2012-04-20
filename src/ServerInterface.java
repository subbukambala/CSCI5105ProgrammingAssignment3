/**
 * @description 
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.rmi.*;
import java.rmi.server.*;
import java.util.List;

public interface ServerInterface extends Remote {
    
    /**
     * File server joins in network to serve collaborative document edit
     */
    public Integer registerNode() throws Exception;

    /**
     * Returns active nodes
     */
    public List<Pair<Integer,String>> getActiveNodes() throws RemoteException;
    
    /**
     * Client submits job using this method
     */
    public Boolean submitJob(List<Integer> data) throws Exception;


    /**
     * Compute node calls this method after executing map task
     */
    public Boolean aggregateMapTasks(MapTask t) throws RemoteException;


    /**
     * Compute node calls this method after executing reduce task
     */
    public Boolean aggregateReduceTasks(ReduceTask t) throws RemoteException;
    
    /**
     * Compute node executes this method periodically to inform node is alive
     */
    public void heartBeatMsg(Integer nodeId) throws RemoteException;
    
    /**
     * Client calls this method to execute server stats
     */
    public String getServerStats() throws RemoteException;
    
    /**
     * Compute node calls this method when task is transferred to another node.
     */
    public void updateTaskTransfer(Task task) throws RemoteException;
}
