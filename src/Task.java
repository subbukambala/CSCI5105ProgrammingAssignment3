/**
 * @description Holds meta information and data about Task
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.Serializable;

public abstract class Task<T> implements Serializable {

    private Integer taskId;
    
    private Pair<Integer, String> node;
    
    public enum TaskType {MAP, REDUCE};

    private Double expectedLoad;
    
    private Long startTaskTime;    
    private Long endTaskTime;

    public Long getStartTaskTime() {
        return startTaskTime;
    }

    public void setStartTaskTime(Long startTaskTime) {
        this.startTaskTime = startTaskTime;
    }

    public Long getEndTaskTime() {
        return endTaskTime;
    }

    public void setEndTaskTime(Long endTaskTime) {
        this.endTaskTime = endTaskTime;
    }

    public Long getComputationTime() {
        return endTaskTime - startTaskTime;
    }
    
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Double getExpectedLoad() {
        return expectedLoad;
    }

    public void setExpectedLoad(Double expectedLoad) {
        this.expectedLoad = expectedLoad;
    }

    public Pair<Integer, String>  getNode() {
        return node;
    }

    public void setNode(Pair<Integer, String>  node) {
        this.node = node;
    }


    public abstract TaskType getTaskType();

    public abstract void setData(T _data);
    
    public abstract T getData();
   
/*    private TaskType currentTaskType;
    

    public TaskType getCurrentTaskType() {
        return currentTaskType;
    }

    public void setCurrentTaskType(TaskType currentTaskType) {
        this.currentTaskType = currentTaskType;
    }


    public void setData(List<Integer> _data) {
        data = _data;
    }
    
    public List<Integer> getData() {
        return data;
    }
*/
}
