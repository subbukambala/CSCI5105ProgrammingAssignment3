/**
 * @descriptrion Holds general contents of a request.
 *  
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.Serializable;

/**
 * This class is extended by both Read and Write request
 */
public class Request implements Serializable {

    private Integer myRequestId;
    private String myFileName;
    private String myClientId;
    private String myClientIP;
    private boolean isNewFile;
    
    /**
     * TODO: Everything
     */
    Request(String _fileName) {
        setFileName(_fileName);
    }
    

    public void setFileName(String str) {
        myFileName = str;
    }
    public String getFileName() {
        return myFileName;
    }
    
    public Integer getRequestId() {
        return myRequestId;
    }
    
    public void setRequestId(Integer myRequestId) {
        this.myRequestId = myRequestId;
    }
   
    public String getClientId() {
        return myClientId;
    }
    public void setClientId(String myClientId) {
        this.myClientId = myClientId;
    }

    public String getClientIP() {
        return myClientIP;
    }
    public void setClientIP(String myClientIP) {
        this.myClientIP = myClientIP;
    }

    public boolean isReadRequest() {
        return false;
    }
    
    public boolean getIsNewFile() {
        return isNewFile;
    }

    public void setIsNewFile(boolean isNewFile) {
        this.isNewFile = isNewFile;
    }
}
