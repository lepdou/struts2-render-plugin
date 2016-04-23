package Pipes;

import lombok.Getter;
import lombok.Setter;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.Pipe;
import util.SleepUtils;

/**
 * 正常模块渲染
 */
@View(ftlPath = "demo/one.ftl", weight = Weight.HEIGHT)
public class PipeOne implements Pipe {

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
//            user = null;
//            user.setName("");
        }
        SleepUtils.sleep(time);
        return SUCCESS;
    }
}
