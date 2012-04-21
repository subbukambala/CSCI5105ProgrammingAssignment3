/**
 * @description Holds statistics of Task.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */
import java.io.Serializable;

public class TaskStats implements Serializable {

    private Integer noOfMapTasks;
    private Integer noOfReduceTasks;
    private Long computationTime;
    
    private Long startJobTime;
    private Long endJobTime;

    public TaskStats() {
        noOfMapTasks = Integer.valueOf(0);
        noOfReduceTasks = Integer.valueOf(1);
        computationTime = Long.valueOf(0);
    }
    
    public Long getStartJobTime() {
        return startJobTime;
    }

    public void setStartJobTime(Long startJobTime) {
        this.startJobTime = startJobTime;
    }

    public Long getEndJobTime() {
        return endJobTime;
    }

    public void setEndJobTime(Long endJobTime) {
        this.endJobTime = endJobTime;
    }

    public Integer getNoOfMapTasks() {
        return noOfMapTasks;
    }

    public void setNoOfMapTasks(Integer noOfMapTasks) {
        this.noOfMapTasks = noOfMapTasks;
    }

    public Integer getNoOfReduceTasks() {
        return noOfReduceTasks;
    }

    public void setNoOfReduceTasks(Integer noOfReduceTasks) {
        this.noOfReduceTasks = noOfReduceTasks;
    }

    public double getTotalTime() {
        return endJobTime - startJobTime;
    }

    public Long getComputationTime() {
        return computationTime;
    }

    public void setComputationTime(Long computationTime) {
        this.computationTime = computationTime;
    }
}
