/**
 * @descriptrion Holds general contents of a request.
 * 
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.Serializable;


/**
 * TODO: Everything
 */
public class Response implements Serializable {

    private Integer myRequestId;
    private String  myHostName;
    private String  myFileServer;
    private String  myFileName;
    
    public Integer getRequestId() {
        return myRequestId;
    }
    
    public void setRequestId(Integer myRequestId) {
        this.myRequestId = myRequestId;
    }
    
    public String getFileServerUrl() {
        return myFileServer;
    }

    public void setFileServerUrl(String myFileServer) {
        this.myFileServer = myFileServer;
    }
    
    public String getHostName() {
        return myHostName;
    }

    public void setHostName(String myHostName) {
        this.myHostName = myHostName;
    }
    
    public String getFileName() {
        return myFileName;
    }

    public void setFileName(String myFileName) {
        this.myFileName = myFileName;
    }

}
