package Pipes;

import lombok.Getter;
import lombok.Setter;
import org.le.anno.Param;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.PipeSupport;
import util.SleepUtils;

/**
 * 权重最低，但是渲染最快，应该最后吐出到页面
 */
@View(ftlPath = "demo/two.ftl", weight = Weight.LOW)
public class PipeTwo extends PipeSupport {

    @Setter
    private int t2;
    @Setter
    private String name;
    @Getter
    private int time;

    @Override
    public String execute() {
        time = t2;
        SleepUtils.sleep(time);
        return SUCCESS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
