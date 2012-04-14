/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class NodeStats {

    public Integer currentLoad;
    public Integer averageLoad;
    public Integer noOfJobs;
    public Integer noOfFaults;
    public Integer noOfMigratedJobs;
    
    public Integer getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
    }

    public Integer getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad(Integer averageLoad) {
        this.averageLoad = averageLoad;
    }

    public Integer getNoOfJobs() {
        return noOfJobs;
    }

    public void setNoOfJobs(Integer noOfJobs) {
        this.noOfJobs = noOfJobs;
    }

    public Integer getNoOfFaults() {
        return noOfFaults;
    }

    public void setNoOfFaults(Integer noOfFaults) {
        this.noOfFaults = noOfFaults;
    }

    public Integer getNoOfMigratedJobs() {
        return noOfMigratedJobs;
    }

    public void setNoOfMigratedJobs(Integer noOfMigratedJobs) {
        this.noOfMigratedJobs = noOfMigratedJobs;
    }
    
    
    
}
