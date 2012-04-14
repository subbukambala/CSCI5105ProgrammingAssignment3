/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class DataTransferHandler implements DataTransferInterface {
    
    public void storeData(String data, String filePath) throws RemoteException {
        try {
            // Create file
            FileWriter fstream = new FileWriter(filePath, true);
            
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(data);
            
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
    }
    
    public void onDataTransferComplete(Integer jobId, String destFilePath) throws RemoteException {
        System.out.println("data transfer is complete");
    }
    
    public void transferData(DataTransferInterface DTI, Integer maxBufferWindow, String srcFilePath, String destFilePath) {
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(srcFilePath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line
            Integer offset = 0;
            int x = 0;
            char[] chars = new char[maxBufferWindow];
            
            while ((x = br.read(chars ,offset, maxBufferWindow)) != -1) {
                offset += maxBufferWindow;
                String str = String.valueOf(chars);
                
                DTI.storeData(str, destFilePath);
            }
            
            // Close the input stream
            in.close();
            
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}
