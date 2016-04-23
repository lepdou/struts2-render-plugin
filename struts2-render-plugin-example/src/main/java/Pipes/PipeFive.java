package Pipes;

import action.User;
import lombok.Getter;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.Pipe;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 模拟freemarker渲染错误
 */
@View(ftlPath = "demo/five.ftl", weight = Weight.NORMALL)
public class PipeFive implements Pipe {

    @Getter
    private String nam;
    @Getter
    @Autowired
    private User user;

    @Override
    public String execute() {
        return SUCCESS;
    }


}
