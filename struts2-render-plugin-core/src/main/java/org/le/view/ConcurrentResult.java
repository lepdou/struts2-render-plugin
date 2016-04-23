package org.le.view;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.le.Exception.PipeActionAnnotationException;
import org.le.anno.ExecuteType;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.bean.PipeProxy;
import org.le.common.MultiBitSet;
import org.le.core.*;
import org.le.core.executor.BigPipeExecutor;
import org.le.core.executor.ConcurrentPipeExecutor;
import org.le.core.executor.SyncPipeExecutor;
import org.le.core.factory.DefaultPipeFactory;
import org.le.core.factory.PipeFactory;
import org.le.util.InjectUtils;
import org.le.util.ViewAnnotationUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * bigpipe result for struts2
 * has 3 modes:
 * sync: synchronized render pagelet
 * concurrent: concurrent render pagelet in server,
 * after all pagelet renderd and merge them as one html reponse to browse
 * bigpipe: concurrent render pagelet in server, different from concurrent mode ,
 * bigpipe mode will flush pagelet result to brower as soon as one pagelet renderd
 * choose mode just set annotation View at struts action.
 * For example @View(ftlPath = "/index.ftl", type = ExecuteType.BIGPIPE) in bigpipe mode. just choose ExecuteType
 *
 * @Author lepdou
 */
public class ConcurrentResult extends StrutsResultSupport {
    private static Logger logger = Logger.getLogger("logger");

    private final static int RECYCLE_ASK_SLEEP_TIME = 5;

    private PipesParse pipesParse = DefaultPipesParse.newInstance();
    private PipeFactory pipeFactory = DefaultPipeFactory.newInstance();
    private PipeExecutor syncPipeExecutor = SyncPipeExecutor.newInstance();
    private ConcurrentPipeExecutor concurrentPipeExecutor;
    private PipeExecutor bigpipeExecutor;
    private FreemarkerRenderer renderer = DefaultFreemarkerRenderer.newIntance();
    private BigpipeSupportStrategy bigpipeSupportStrategy = SimpleBigpipeSupport.newInstance();

    @Inject(value = "struts.devMode")
    private String devMode;
    private boolean isDevMode;

    private static volatile boolean hasInitParams = false;
    private Object action;
    private MultiBitSet pipesWeight;
    private Map<String, Weight> pipeKeyWeightMap;

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        if (!hasInitParams)
            initParams();

        this.action = invocation.getAction();

        List<String> pipeClazzs = pipesParse.getPipes(finalLocation);
        List<PipeProxy> pipes = pipeFactory.create(pipeClazzs, invocation);

        PrintWriter writer = getWrite(invocation);
        //action's content for ftl render
        Map<String, Object> executeResults = buildFrameworkExecuteContext(pipes);
        switch (getExecuteType()) {
            case SYNC:
                executeResults.putAll(syncPipeExecutor.execute(pipes));
                doResponse(writer, executeResults);
                break;
            case CONCURRNET:
                concurrentPipeExecutor = new ConcurrentPipeExecutor();
                executeResults.putAll(concurrentPipeExecutor.execute(
                        pipes,
                        ViewAnnotationUtils.generateTimeout(this.action)));
                doResponse(writer, executeResults);
                break;
            case BIGPIPE: {
                bigpipeExecutor = new BigPipeExecutor();
                //first render html framework base action content
                renderPageFrameworkAndFlush(writer, executeResults);
                //get pipes renderd result and flush
                executeResults = bigpipeExecutor.execute(pipes);
                statisticPipeWight(pipes);
                geneatePipeKeyWeightMap(pipes);
                int flushedCount = 0;
                while (flushedCount != pipes.size()) {
                    if (!executeResults.isEmpty()) {
                        flushedCount += responsePipeToClient(executeResults, writer);
                    }
                    //sleep. prevent ask too times
                    Thread.sleep(RECYCLE_ASK_SLEEP_TIME);
                }

                //all pipes have flush to browse close the html
                closeHtml(writer);
            }
        }
    }

    private synchronized void initParams() {
        //double check
        if (hasInitParams)
            return;
        initDevMode();
        hasInitParams = true;
    }

    private void initDevMode() {
        if (StringUtils.isNotEmpty(devMode) && "true".equals(devMode))
            isDevMode = true;
        logger.info("devMod >>" + devMode);
        ((SyncPipeExecutor) syncPipeExecutor).setDevMode(isDevMode);
    }

    private Map<String, Object> buildFrameworkExecuteContext(List<PipeProxy> pipes) {
        Map<String, Object> actionContext = new HashMap<String, Object>();
        for (PipeProxy pipeProxy : pipes)
            actionContext.put(pipeProxy.getKey(), "");
        //获取action中的参数
        actionContext.putAll(InjectUtils.getFieldValueForFreemarker(action));
        return actionContext;
    }

    private PrintWriter getWrite(ActionInvocation invocation) throws IOException {
        ActionContext ctx = invocation.getInvocationContext();
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        PrintWriter writer = response.getWriter();
        return writer;
    }

    private ExecuteType getExecuteType() {
        View view = action.getClass().getAnnotation(View.class);
        if (view == null) {
            logger.error("struts action components must " +
                    "have View annotation:" + action.getClass().getName());
            throw new PipeActionAnnotationException("struts action components must " +
                    "have View annotation:" + action.getClass().getName());
        }
        return view.type();
    }

    private void doResponse(PrintWriter writer, Map<String, Object> pageContext) {
        String result = renderer.render(ViewAnnotationUtils.generateFtl(action), pageContext, isDevMode).toString();
        writer.println(result);
        writer.flush();
    }

    private void renderPageFrameworkAndFlush(PrintWriter writer, Map<String, Object> executeResults) {
        String ftl = ViewAnnotationUtils.generateFtl(action);
        String frameworkHtml = renderer.render(ftl, executeResults, isDevMode).toString();
        flush(writer, bigpipeSupportStrategy.execute(frameworkHtml));
    }

    private void flush(PrintWriter writer, Object content) {
        writer.println(content);
        writer.flush();
    }

    private void statisticPipeWight(List<PipeProxy> pipeProxies) {
        pipesWeight = new MultiBitSet(4);
        int[] sizes = new int[3];
        for (PipeProxy pipeProxy : pipeProxies) {
            switch (pipeProxy.getWeight()) {
                case HEIGHT:
                    sizes[0]++;
                    break;
                case NORMALL:
                    sizes[1]++;
                    break;
                case LOW:
                    sizes[2]++;
                    break;
            }
        }
        pipesWeight.set(0, sizes[0]);
        pipesWeight.set(1, sizes[1]);
        pipesWeight.set(2, sizes[2]);
    }

    private int responsePipeToClient(Map<String, Object> executeResults, PrintWriter writer) {
        int flushedCount = 0;
        int hSize = pipesWeight.get(0);
        int nSize = pipesWeight.get(1);
        for (Map.Entry entry : executeResults.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            switch (pipeKeyWeightMap.get(key)) {
                case HEIGHT: {
                    flushAndRemove(executeResults, writer, key, value);
                    hSize--;
                    flushedCount++;
                    pipesWeight.set(0, hSize);
                    break;
                }
                case NORMALL: {
                    if (hSize == 0) {
                        flushAndRemove(executeResults, writer, key, value);
                        flushedCount++;
                        nSize--;
                        pipesWeight.set(1, nSize);
                    }
                    break;
                }
                case LOW: {
                    if (hSize == 0 && nSize == 0) {
                        flushAndRemove(executeResults, writer, key, value);
                        flushedCount++;
                    }
                }
            }
        }
        return flushedCount;
    }

    private void flushAndRemove(Map<String, Object> executeResults, PrintWriter writer, String key, Object value) {
        flush(writer, value);
        executeResults.remove(key);
    }

    private void geneatePipeKeyWeightMap(List<PipeProxy> pipes) {
        pipeKeyWeightMap = new HashMap<String, Weight>(pipes.size() * 2);
        for (PipeProxy pipeProxy : pipes) {
            pipeKeyWeightMap.put(pipeProxy.getKey(), pipeProxy.getWeight());
        }
    }

    private void closeHtml(PrintWriter writer) {
        writer.print("</body>\n</html>");
        writer.flush();
    }

}
