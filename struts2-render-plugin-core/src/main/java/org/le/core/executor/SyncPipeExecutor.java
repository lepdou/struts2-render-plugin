package org.le.core.executor;

import org.apache.log4j.Logger;
import org.le.Exception.PipeParamInitException;
import org.le.bean.Pipe;
import org.le.bean.PipeProxy;
import org.le.core.DefaultFreemarkerRenderer;
import org.le.core.FreemarkerRenderer;
import org.le.core.PipeExecutor;
import org.le.core.extention.PipeBackup;
import org.le.core.extention.PipeCache;
import org.le.core.extention.PipeDowngrade;
import org.le.util.ExceptionHandlerUtils;
import org.le.util.InjectUtils;
import org.le.util.YamlConfig;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncPipeExecutor implements PipeExecutor {
    static Logger logger = Logger.getLogger("logger");

    private static final String NONE_RENDER_RESULT = "";

    private static SyncPipeExecutor instance = new SyncPipeExecutor();
    private FreemarkerRenderer renderer = DefaultFreemarkerRenderer.newIntance();
    private static PipeDowngrade downgrade;
    private static PipeBackup backup;
    private static PipeCache cache;
    private boolean devMode;

    //初始化参数
    static {
        if (YamlConfig.hasConfigFile()) {
            Map<String, String> extenInfo = new HashMap<String, String>();
            List<String> extenKeys = Arrays.asList("downgrade", "cache", "backup");
            for (String key : extenKeys) {
                extenInfo.put(key, YamlConfig.getAsString(key));
            }
            initExtentionParam(extenInfo);
        } else {
            logger.info("project has not config downgrade|backup|cache class");
        }
    }

    private static void initExtentionParam(Map<String, String> classNames) {
        if (CollectionUtils.isEmpty(classNames)) {
            return;
        }
        for (String extenType : classNames.keySet()) {
            try {
                Class clazz = Class.forName(classNames.get(extenType));
                logger.info("init extention plugin >> " + clazz.getName());
                if ("downgrade".equals(extenType)) {
                    downgrade = (PipeDowngrade) clazz.newInstance();
                } else if ("cache".equals(extenType)) {
                    cache = (PipeCache) clazz.newInstance();
                } else if ("backup".equals(extenType)) {
                    backup = (PipeBackup) clazz.newInstance();
                }

            } catch (Exception e) {
                logger.error("init pipe cache object error!", e);
                throw new PipeParamInitException("init pipe cache object error!");
            }
        }
    }

    private SyncPipeExecutor() {
    }

    public static SyncPipeExecutor newInstance() {
        return instance;
    }

    @Override
    public Map<String, Object> execute(List<PipeProxy> pipes) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (PipeProxy pipe : pipes)
            result.put(pipe.getKey(), execute(pipe));
        return result;
    }

    @Override
    public Object execute(PipeProxy pipe) {
        //先从cache里获取
        Object backupRenderResult = getResultFromCache(pipe);
        if (backupRenderResult != null) {
            logger.info("get render result from cache");
            return backupRenderResult;
        }

        try {
            String result = pipe.execute();

            if (Pipe.SUCCESS.equals(result)) {
                return doSuccess(pipe);
            } else if (Pipe.NONE.equals(result)) {
                return doNone(pipe);
            } else if (Pipe.ERROR.equals(result)) {
                return doError(pipe);
            }

        } catch (Exception e) {
            return doException(pipe, e);
        }
        return NONE_RENDER_RESULT;
    }

    private Object getResultFromCache(PipeProxy pipe) {
        if (pipe.isCacheable() && cache != null) {
            return cache.getCachedPipe(pipe);
        }
        return null;
    }

    private Object doSuccess(PipeProxy pipe) {
        try {
            Object renderResult = render(pipe);
            backup(pipe, renderResult);
            pipe.setRenderResult(renderResult);
            return renderResult;
        } catch (Exception e) {
            return doException(pipe, e);
        }
    }


    private Object doException(PipeProxy pipe, Exception e) {
        logger.error("pipe execute error for pipe [" + pipe + "]", e);
        if (isDevMode())
            return ExceptionHandlerUtils.generateExceptionToPrintStack(e);

        //降级
        if (downgrade != null) {
            Object backupResult = downgrade.downgrade(pipe);
            if (backupResult != null) {
                logger.info("downgrade success [" + pipe + "]");
                return backupResult;
            } else {
                logger.info("downgrade fail [" + pipe + "]");
                return NONE_RENDER_RESULT;
            }
        } else {
            return NONE_RENDER_RESULT;
        }
    }

    /**
     * 返回error或者none的时候，现阶段统一返回空字符串，后面可扩展
     *
     * @param pipe
     * @return
     */
    private Object doNone(PipeProxy pipe) {
        return NONE_RENDER_RESULT;
    }

    private Object doError(PipeProxy pipe) {
        return NONE_RENDER_RESULT;
    }

    private void backup(PipeProxy pipe, Object renderResult) {
        if (backup != null) {
            logger.info("backup pipe[" + pipe + "]");
            backup.backup(pipe, renderResult);
        }
    }

    private Object render(PipeProxy pipe) {
        Map<String, Object> context = InjectUtils.getFieldValueForFreemarker(pipe.getPipe());
        String ftl = pipe.getFtl();
        return renderer.render(ftl, context, devMode);
    }


    public PipeDowngrade getDowngrade() {
        return downgrade;
    }

    public void setDowngrade(PipeDowngrade downgrade) {
        this.downgrade = downgrade;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public void setBackup(PipeBackup backup) {
        this.backup = backup;
    }

    public void setCache(PipeCache cache) {
        this.cache = cache;
    }
}
