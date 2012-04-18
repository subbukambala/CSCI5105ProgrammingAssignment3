/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.util.List;

public class ReduceTask extends Task<List<MapTask>> {

    private List<MapTask> data;

    public TaskType getTaskType() {
        return Task.TaskType.REDUCE;
    }

    public void setData(List<MapTask> _data) {
        data = _data;
    }
    
    public List<MapTask> getData() {
        return data;
    }

}