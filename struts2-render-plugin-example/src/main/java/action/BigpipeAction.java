package action;

import com.opensymphony.xwork2.ActionSupport;
import lombok.Getter;
import lombok.Setter;
import org.le.anno.ExecuteType;
import org.le.anno.View;
import org.springframework.beans.factory.annotation.Autowired;

@View(ftlPath = "index.ftl", type = ExecuteType.BIGPIPE)
public class BigpipeAction extends ActionSupport{

    @Setter
    @Getter
    private int t1;
    @Getter
    @Setter
    private int t2;
    @Setter
    @Getter
    private int t3;
    @Getter
    @Setter
    String name;
    @Autowired
    public User user;
    @Getter
    private String title = "struts2-concurrent-plugin demo";

    @Override
    public String execute() throws Exception {
        return "pipe";
    }

}

