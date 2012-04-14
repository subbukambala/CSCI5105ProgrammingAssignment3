/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class ServerStats {

    private Integer noOfFaults;
    private Integer noOfRedundantTasks;
    private Integer noOfTaskMigrations;

    public Integer getNoOfFaults() {
        return noOfFaults;
    }

    public void setNoOfFaults(Integer noOfFaults) {
        this.noOfFaults = noOfFaults;
    }

    public Integer getNoOfRedundantTasks() {
        return noOfRedundantTasks;
    }

    public void setNoOfRedundantTasks(Integer noOfRedundantTasks) {
        this.noOfRedundantTasks = noOfRedundantTasks;
    }

    public Integer getNoOfTaskMigrations() {
        return noOfTaskMigrations;
    }

    public void setNoOfTaskMigrations(Integer noOfTaskMigrations) {
        this.noOfTaskMigrations = noOfTaskMigrations;
    }

}
