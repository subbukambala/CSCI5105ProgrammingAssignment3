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
     */
    List<Pair<Integer,String>> getActiveNodes() throws RemoteException;
}
