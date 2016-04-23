package org.le.core.factory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.le.Exception.PipeClassDefinationException;
import org.le.Exception.PipeFieldInjectException;
import org.le.anno.Param;
import org.le.bean.Pipe;
import org.le.bean.PipeProxy;
import org.le.bean.PipeSupport;
import org.le.core.Cache;
import org.le.core.SimpleMemaryCache;
import org.le.util.InjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


public class DefaultPipeFactory implements PipeFactory {

    public static DefaultPipeFactory instance = new DefaultPipeFactory();

    private ActionInvocation invocation;
    private SpringBeanFactory springBeanFactory = SpringBeanFactory.newInstance();

    private DefaultPipeFactory() {

    }

    public static DefaultPipeFactory newInstance() {
        return instance;
    }

    @Override
    public List<PipeProxy> create(List<String> classNames, ActionInvocation invocation) {
        this.invocation = invocation;
        Map<String, Object> pipeContext = InjectUtils.getFieldValueWithGetterMethod(invocation.getAction());
        List<PipeProxy> pipes = new ArrayList<PipeProxy>();
        for (String className : classNames)
            pipes.add(create(className, pipeContext));
        return pipes;
    }

    private PipeProxy create(String className, Map<String, Object> pipeContext) {
        PipeProxy pipeProxy = null;
        Object pipe = null;
        Class pipeClazz = null;
        try {
            pipeClazz = Class.forName(className);
            pipe = pipeClazz.newInstance();
        } catch (Exception e) {
            throw new PipeClassDefinationException("create pipe failed,please check class name config right:" + className);
        }
        injectBusinessPipeField(pipe, pipeContext);
        injectSpringBeanField(pipe);
        if (pipe != null && pipe instanceof PipeSupport)
            injectPipeSupportField(pipe);
        if (pipe != null && pipe instanceof Pipe)
            return new PipeProxy(className, (Pipe) pipe);
        else
            throw new PipeClassDefinationException("pipe must implements inteface Pipe :" + className);
    }

    private void injectBusinessPipeField(Object pipe, Map<String, Object> context) {
        Field[] fields = pipe.getClass().getDeclaredFields();
        Method[] methods = pipe.getClass().getDeclaredMethods();
        for (Field field : fields) {
            if (isNeedAuthwiredField(field, methods) && context.containsKey(field.getName())) {
                String fieldName = field.getName();
                Object injectValue = context.get(fieldName);
                field.setAccessible(true);
                try {
                    field.set(pipe, injectValue);
                } catch (IllegalAccessException e) {
                    //becase has set field accessible so do not throw this exception
                }
            }
        }
    }

    //如果Field有对应的Setter方法则自动注入
    private boolean isNeedAuthwiredField(Field field, Method[] methods) {
        if (methods == null || methods.length == 0) {
            return false;
        }
        for (Method method : methods) {
            if (isSetterMethodByName(method, field)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSetterMethodByName(Method method, Field field) {
        String methodName = method.getName().toLowerCase();
        String fieldName = field.getName().toLowerCase().replace("is", "");
        if (methodName.startsWith("set") && methodName.contains(fieldName)) {
            return true;
        } else {
            return false;
        }
    }

    private void injectPipeSupportField(Object pipeSupport) {
        Field[] fields = PipeSupport.class.getDeclaredFields();
        ActionContext actionContext = invocation.getInvocationContext();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if ("request".equals(fieldName)) {
                    HttpServletRequest request = (HttpServletRequest) actionContext.get(ServletActionContext.HTTP_REQUEST);
                    field.set(pipeSupport, request);
                } else if ("response".equals(fieldName)) {
                    field.set(pipeSupport, actionContext.get(ServletActionContext.HTTP_RESPONSE));
                } else if ("servletContext".equals(fieldName)) {
                    field.set(pipeSupport, actionContext.get(ServletActionContext.SERVLET_CONTEXT));
                } else {
                    HttpServletRequest request = (HttpServletRequest) actionContext.get(ServletActionContext.HTTP_REQUEST);
                    field.set(pipeSupport, request.getCookies());
                }
            }
        } catch (IllegalAccessException e) {
        }
    }

    private void injectSpringBeanField(Object pipe) {
        Field[] fields = pipe.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Autowired.class) != null) {
                String beanName = field.getName();
                if (field.getAnnotation(Qualifier.class) != null) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    beanName = qualifier.value();
                }
                field.setAccessible(true);
                try {
                    field.set(pipe, springBeanFactory.getBean(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
