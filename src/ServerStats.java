import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class ServerStats {

    private AtomicInteger noOfJobs;
    private AtomicInteger noOfFaults;
    private AtomicInteger noOfRedundantTasks;
    private AtomicInteger noOfTaskMigrations;

    public ServerStats () {
        noOfJobs = new AtomicInteger();
        noOfFaults = new AtomicInteger();
        noOfRedundantTasks = new AtomicInteger();
        noOfTaskMigrations = new AtomicInteger();
    }
    
    private AtomicInteger AtomicInteger() {
        // TODO Auto-generated method stub
        return null;
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
}
