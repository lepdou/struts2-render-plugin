package org.le.core;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.log4j.Logger;
import org.le.Exception.FtlRenderException;
import org.le.util.ExceptionHandlerUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class DefaultFreemarkerRenderer implements FreemarkerRenderer {
    private Logger logger = Logger.getLogger("logger");
    private static DefaultFreemarkerRenderer instance = new DefaultFreemarkerRenderer();
    private Configuration cfg;

    private void init() {
        cfg = new freemarker.template.Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringLoader);
        cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        cfg.setTemplateUpdateDelay(600000);
        cfg.setNumberFormat("#");
        cfg.setClassicCompatible(true);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
    }

    private DefaultFreemarkerRenderer() {
        init();
    }

    public static DefaultFreemarkerRenderer newIntance() {
        return instance;
    }

    @Override
    public Object render(String ftl, Map<String, Object> context) {
        return render(ftl, context, false);
    }

    @Override
    public Object render(String ftl, Map<String, Object> context, boolean isDevMode) {
        String renderResult = "";
        try {
//            Template template = cfg.getTemplate(ftl, cfg.getLocale());
            Template template = new Template(ftl, new StringReader(ftl), cfg);
            StringWriter writer = new StringWriter();
            template.process(context, writer);
            renderResult = writer.toString();
        } catch (Exception e) {
            logger.error(e);
            if (isDevMode){
                return ExceptionHandlerUtils.generateExceptionToPrintStack(e);
            }

        }
        return renderResult;
    }

}
