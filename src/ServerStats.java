/**
 * @description Holds the Server statistics
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.util.concurrent.atomic.AtomicInteger;

public class ServerStats {

    private AtomicInteger noOfJobs;
    private AtomicInteger noOfFaults;
    private AtomicInteger noOfRedundantTasks;
    private AtomicInteger noOfTaskMigrations;
    private AtomicInteger noOfFailedJobs;
   
    public ServerStats () {
        noOfJobs = new AtomicInteger();
        noOfFaults = new AtomicInteger();
        noOfRedundantTasks = new AtomicInteger();
        noOfTaskMigrations = new AtomicInteger();
        noOfFailedJobs = new AtomicInteger();
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

    public AtomicInteger getNoOfRedundantTasks() {
        return noOfRedundantTasks;
    }

    public void setNoOfRedundantTasks(AtomicInteger noOfRedundantTasks) {
        this.noOfRedundantTasks = noOfRedundantTasks;
    }

    public AtomicInteger getNoOfTaskMigrations() {
        return noOfTaskMigrations;
    }

    public void setNoOfTaskMigrations(AtomicInteger noOfTaskMigrations) {
        this.noOfTaskMigrations = noOfTaskMigrations;
    }
    
    public AtomicInteger getNoOfFailedJobs() {
        return noOfFailedJobs;
    }

    public void setNoOfFailedJobs(AtomicInteger noOfFailedJobs) {
        this.noOfFailedJobs = noOfFailedJobs;
    }
}
