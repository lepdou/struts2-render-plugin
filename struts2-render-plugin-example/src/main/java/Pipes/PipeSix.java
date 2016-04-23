package Pipes;

import lombok.Getter;
import lombok.Setter;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.Pipe;
import util.SleepUtils;

/**
 * 测试缓存
 */
@View(ftlPath = "demo/six.ftl", weight = Weight.HEIGHT, cacheable = true)
public class PipeSix implements Pipe {

    @Setter
    private int t1;
    @Setter
    @Getter
    private String name;
    @Getter
    private int time;

    @Override
    public String execute() {
        time = t1;
        if (name.equals("down")) {
        }
        SleepUtils.sleep(time);
        return SUCCESS;
    }
}
