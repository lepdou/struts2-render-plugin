package Pipes;

import action.User;
import lombok.Getter;
import lombok.Setter;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.Pipe;

/**
 * 模拟业务抛出异常
 */
@View(ftlPath = "demo/four.ftl", weight = Weight.NORMALL)
public class PipeFour implements Pipe {

    @Setter
    @Getter
    private String name;

    @Override
    public String execute() {
        User user = null;
        user.setName("");
        return SUCCESS;
    }


}
