package extend;

import org.le.bean.PipeProxy;
import org.le.core.extention.PipeBackup;
import org.le.core.extention.PipeCache;
import org.le.core.extention.PipeDowngrade;

import java.util.HashMap;
import java.util.Map;

public class MyPipeDowngrading implements PipeDowngrade, PipeBackup, PipeCache {
    private static Map<String, Object> backup = new HashMap<String, Object>();

    @Override
    public void backup(PipeProxy pipe, Object pipeResult) {
        System.out.println("========backup========");
        backup.put(pipe.getFtlPath(), pipeResult);
    }

    @Override
    public Object downgrade(PipeProxy pipe) {
        System.out.println("=======downgrade======");
        return backup.get(pipe.getFtlPath());
    }

    @Override
    public Object getCachedPipe(PipeProxy pipe) {
        System.out.println("=========cache==========");
        Object cachedResult = backup.get(pipe.getFtlPath());
        if (cachedResult != null) {
            return "<font style=\"color:red;\">From Cache\n</font>" + cachedResult;
        } else {
            return null;
        }

    }
}
