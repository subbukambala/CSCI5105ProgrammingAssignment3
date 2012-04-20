/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.io.Serializable;

public abstract class Task<T> implements Serializable {

    private Integer taskId;
    
    private Pair<Integer, String> node;
    
    public enum TaskType {MAP, REDUCE};

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public abstract Double getExpectedLoad();    

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
