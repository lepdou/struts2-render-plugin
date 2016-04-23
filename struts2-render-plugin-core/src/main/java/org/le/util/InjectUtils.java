package org.le.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectUtils {


    public static Map<String, Object> getFieldValueWithGetterMethod(Object o) {
        Map<String, Object> context = new HashMap<String, Object>();
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (hasGetterMethod(o, field)) {
                context.put(field.getName(), getFieldValue(o, field));
            }
        }
        return context;
    }

    public static Map<String,Object> getFieldValueForFreemarker(Object pipe){
        Map<String, Object> pipeContext = new HashMap<String, Object>();
        pipeContext.putAll(getFieldValueWithGetterMethod(pipe));
        return pipeContext;
    }

    private static Object getFieldValue(Object o, Field field) {
        field.setAccessible(true);
        try {
            return field.get(o);
        } catch (IllegalAccessException e) {
            return null;
            //becase has set field accessible so do not throw this exception
        }
    }

    private static boolean hasGetterMethod(Object o, Field field) {
        String fieldName = field.getName().toLowerCase();
        Class clazz = field.getType();
        List<String> methods = getMethods(o);
        String targetMethodName;
        if (clazz == boolean.class){
            if (fieldName.startsWith("is")){
                targetMethodName = fieldName;
            }else{
                targetMethodName = "is" + fieldName;
            }
        }else {
            targetMethodName = "get" + fieldName;
        }
        return methods.contains((targetMethodName));
    }

    private static List<String> getMethods(Object o) {
        Method[] methods = o.getClass().getDeclaredMethods();
        List<String> result = new ArrayList<String>(methods.length);
        for (Method method : methods) {
            result.add(method.getName().toLowerCase());
        }
        return result;
    }
}