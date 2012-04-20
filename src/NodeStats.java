import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class NodeStats {

    public Double currentLoad;
    public Double averageLoad;
    
    public Double totalLoad;
    public AtomicInteger noOfLoadChecks;
    
    public AtomicInteger noOfJobs;
    public AtomicInteger noOfFaults;
    public AtomicInteger noOfMigratedJobs;
    public AtomicInteger noOfTransferRequests;
    
    public NodeStats() {
        currentLoad = Double.valueOf(0);
        totalLoad = Double.valueOf(0);
        averageLoad = Double.valueOf(0);
        noOfFaults = new AtomicInteger(0);
        noOfJobs = new AtomicInteger(0);
        noOfMigratedJobs = new AtomicInteger(0);
        noOfLoadChecks =  new AtomicInteger(1);
        noOfTransferRequests = new AtomicInteger(0);
    }
    
    public Double getTotalLoad() {
        return totalLoad;
    }

    public void setTotalLoad(Double totalLoad) {
        this.totalLoad = totalLoad;
    }

    public AtomicInteger getNoOfLoadChecks() {
        return noOfLoadChecks;
    }

    public void setNoOfLoadChecks(AtomicInteger noOfLoadChecks) {
        this.noOfLoadChecks = noOfLoadChecks;
    }

    public Double getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(Double currentLoad) {
        this.currentLoad = currentLoad;
    }

    public Double getAverageLoad() {
        return totalLoad / noOfLoadChecks.intValue();
    }

    public void setAverageLoad(Double averageLoad) {
        this.averageLoad = averageLoad;
    }

    public AtomicInteger getNoOfJobs() {
        return noOfJobs;
    }

    public void setNoOfJobs(AtomicInteger noOfJobs) {
        this.noOfJobs = noOfJobs;
    }

    public AtomicInteger getNoOfFaults() {
        return noOfFaults;
    }

    public void setNoOfFaults(AtomicInteger noOfFaults) {
        this.noOfFaults = noOfFaults;
    }

    public AtomicInteger getNoOfMigratedJobs() {
        return noOfMigratedJobs;
    }

    public void setNoOfMigratedJobs(AtomicInteger noOfMigratedJobs) {
        this.noOfMigratedJobs = noOfMigratedJobs;
    }
    
    public AtomicInteger getNoOfTransferRequests() {
        return noOfTransferRequests;
    }

    public void setNoOfTransferRequests(AtomicInteger noOfTransferRequests) {
        this.noOfTransferRequests = noOfTransferRequests;
    }
}
