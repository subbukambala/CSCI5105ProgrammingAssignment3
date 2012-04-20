/**
 * @description Remote Interface for a client. To simplify the logic, in 
 * order to communicate with client asynchronously, implementing RMI 
 * interface to client.
 * 
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.util.List;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    /**
     * This method is called by Server to send response to client.
     */
    public void jobResponse(TaskStats stats,List<Integer> results) 
        throws RemoteException;
}
