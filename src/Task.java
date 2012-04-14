/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

public class Task {

    Integer taskId;
    
    ComputeNodeInterface node;
    
    Integer dataStartLine;
    Integer dataEndLine;
    
    public enum TaskType {MAP, REDUCE};
    
    Integer currentTaskType;

    public Integer getCurrentTaskType() {
        return currentTaskType;
    }

    public void setCurrentTaskType(Integer currentTaskType) {
        this.currentTaskType = currentTaskType;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public ComputeNodeInterface getNode() {
        return node;
    }

    public void setNode(ComputeNodeInterface node) {
        this.node = node;
    }

    public Integer getDataStartLine() {
        return dataStartLine;
    }

    public void setDataStartLine(Integer dataStartLine) {
        this.dataStartLine = dataStartLine;
    }

    public Integer getDataEndLine() {
        return dataEndLine;
    }

    public void setDataEndLine(Integer dataEndLine) {
        this.dataEndLine = dataEndLine;
    }
}
