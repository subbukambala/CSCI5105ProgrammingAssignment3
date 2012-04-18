/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class NodeStats {

    public Double currentLoad;
    public Double averageLoad;
    public Integer noOfJobs;
    public Integer noOfFaults;
    public Integer noOfMigratedJobs;
    
    public NodeStats() {
        currentLoad = Double.valueOf(0);
        averageLoad = Double.valueOf(0);
        noOfFaults = Integer.valueOf(0);
        noOfJobs = Integer.valueOf(0);
        noOfMigratedJobs = Integer.valueOf(0);
    }
    
    public Double getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(Double currentLoad) {
        this.currentLoad = currentLoad;
    }

    public Double getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad(Double averageLoad) {
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
