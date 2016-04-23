package org.le.bean;


import org.le.Exception.PipeActionAnnotationException;
import org.le.anno.View;
import org.le.anno.Weight;
import org.le.util.ViewAnnotationUtils;

public class PipeProxy {
    private String pipeName;
    private Pipe pipe;
    private String ftlPath;
    private String ftl;
    private String key;
    private Weight weight;
    private boolean cacheable;
    private Object renderResult;

    public PipeProxy(String pipeName, Pipe pipe) {
        this.pipeName = pipeName;
        this.pipe = pipe;
        this.ftlPath = ViewAnnotationUtils.generateFtlPath(pipe);
        this.ftl = ViewAnnotationUtils.generateFtl(pipe);
        this.key = ViewAnnotationUtils.generateKey(pipe);
        this.cacheable = ViewAnnotationUtils.generateCacheable(pipe);
        if ("".equals(key)) {//default ftl name
            key = generateDefaultKey();
        }
        this.weight = getWight(pipe);
    }

    private String generateDefaultKey() {
        return ftlPath.substring(ftlPath.lastIndexOf("/") + 1, ftlPath.indexOf("."));

    }

    public String execute() {
        String result = pipe.execute();
        return result;
    }

    private Weight getWight(Pipe pipe) {
        View view = pipe.getClass().getAnnotation(View.class);
        if (view == null) {
            throw new PipeActionAnnotationException("pipe components must " +
                    "have View annotation:" + pipe.getClass().getName());
        }
        return view.weight();
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    public String getFtlPath() {
        return ftlPath;
    }

    public void setFtlPath(String ftlPath) {
        this.ftlPath = ftlPath;
    }

    public String getFtl() {
        return ftl;
    }

    public void setFtl(String ftl) {
        this.ftl = ftl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Weight getWeight() {
        return weight;
    }

    public void setWeight(Weight weight) {
        this.weight = weight;
    }

    public Object getRenderResult() {
        return renderResult;
    }

    public void setRenderResult(Object renderResult) {
        this.renderResult = renderResult;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public String toString() {
        return pipeName;
    }

}
