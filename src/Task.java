/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.util.List;

public class Task {

    private Integer taskId;
    
    private Pair<Integer, String> node;
    
    private List<Integer> data;
    private Integer dataStartLine;
    private Integer dataEndLine;
    
    public enum TaskType {MAP, REDUCE};
    
    private TaskType currentTaskType;
    
    private Double expectedLoad;

    public Double getExpectedLoad() {
        return expectedLoad;
    }

    public void setExpectedLoad(Double expectedLoad) {
        this.expectedLoad = expectedLoad;
    }

    public TaskType getCurrentTaskType() {
        return currentTaskType;
    }

    public void setCurrentTaskType(TaskType currentTaskType) {
        this.currentTaskType = currentTaskType;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Pair<Integer, String>  getNode() {
        return node;
    }

    public void setNode(Pair<Integer, String>  node) {
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

    public void setData(List<Integer> _data) {
        data = _data;
    }
    
    public List<Integer> getData() {
        return data;
    }
}
