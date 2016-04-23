package Pipes;

import action.User;
import lombok.Getter;
import lombok.Setter;
import org.le.anno.Param;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.Pipe;
import org.springframework.beans.factory.annotation.Autowired;
import util.SleepUtils;

/**
 * 渲染速度不是最快，但是优先级最高，比如头部，优先渲染，并且优先吐出到页面
 */
@View(ftlPath = "demo/three.ftl", weight = Weight.HEIGHT)
public class PipeThree implements Pipe {

    @Setter
    private int t3;

    @Autowired
    private User user;

    @Getter
    private String name;
    @Getter
    private int time;

    @Override
    public String execute() {
        time = t3;
        name = user.getName();
        SleepUtils.sleep(time);
        return SUCCESS;
    }
}
