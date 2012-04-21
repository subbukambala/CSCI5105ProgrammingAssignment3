/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.rmi.RemoteException;

public interface DataTransferInterface {
    
    /**
     * This method is called by Component which needs data.
     * 
     * @param DTI 
     * @param maxBufferWindow Max size that component can handle
     * @param srcFilePath source file path
     * @param destFilePath destination file path
     * 
     * @throws RemoteException
     */
    public void transferData(DataTransferInterface DTI, 
                             Integer jobId, 
                             Integer maxBufferWindow, 
                             String srcFilePath, 
                             String destFilePath) throws RemoteException;
    
    /**
     * 
     * @param data
     * @param destFilePath
     * @throws RemoteException
     */
    public void storeData(String data, String destFilePath) throws RemoteException;
    
    /**
     * This is call back function after completion of data transfer
     * 
     * @param jobId
     * @param destFilePath
     * @throws RemoteException
     */
    public void onDataTransferComplete(Integer jobId, String destFilePath) throws RemoteException;
        
}
