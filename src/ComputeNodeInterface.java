/**
 * @description RMI interface for Compute node
 * 
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.rmi.*;
import java.rmi.server.*;
import java.security.NoSuchAlgorithmException;

public interface ComputeNodeInterface extends Remote {

    /**
     * This method is called by Server. 
     * 
     * Node checks its load. If it is more than threshold, tries to transfer its load.
     * Otherwise, node executes.
     *  
     * @throws RemoteException
     */
    public void executeTask(Task task) throws RemoteException;
    
    /**
     * Returns accept/reject based on load
     */
    public Boolean taskTransferRequest(Task task) throws RemoteException;
    
    /**
     * Returns Node statistics
     */
    public String getNodeStats() throws RemoteException;
}