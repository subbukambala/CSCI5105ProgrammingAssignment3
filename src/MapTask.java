/**
 * @description.
 *
 * @authors Daniel William DaCosta, Bala Subrahmanyam Kambala
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 */

import java.util.List;

public class MapTask extends Task<List<Integer>> {

    private List<Integer> data;

    public TaskType getTaskType() {
        return Task.TaskType.MAP;
    }

    public void setData(List<Integer> _data) {
        data = _data;
    }
    
    public List<Integer> getData() {
        return data;
    }
    
    public void setExpectedLoad(Double expectedLoad) {
        if (data != null) {
            if (data.size() < 100) {
                expectedLoad = 10d;
            }
            else {
                expectedLoad  = (double)((data.size() / 100) % 100); 
            }
        }
    }
}