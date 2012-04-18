/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class TaskStats {

    private Integer noOfMapTasks;
    private Integer noOfReduceTasks;
    private Double totalTime;
    private Double computationTime;
    private Double dataTransferTime;

    public TaskStats() {
        noOfMapTasks = Integer.valueOf(0);
        noOfReduceTasks = Integer.valueOf(0);
        totalTime = Double.valueOf(0);
        computationTime = Double.valueOf(0);
        dataTransferTime = Double.valueOf(0);
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
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getComputationTime() {
        return computationTime;
    }

    public void setComputationTime(double computationTime) {
        this.computationTime = computationTime;
    }

    public double getDataTransferTime() {
        return dataTransferTime;
    }

    public void setDataTransferTime(double dataTransferTime) {
        this.dataTransferTime = dataTransferTime;
    }
}
