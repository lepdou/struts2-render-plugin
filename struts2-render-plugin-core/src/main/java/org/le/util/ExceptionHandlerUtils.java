package org.le.util;

public class ExceptionHandlerUtils {

    public static String generateExceptionToPrintStack(Exception e) {
        StringBuilder result = new StringBuilder();
        result.append("<div style=\"background-color: #eee;font-size:9px;font-family: " +
                "Consolas,Menlo,Monaco;height:250px;overflow:scroll\">");
        result.append("<font style=\"color:red\">")
                .append(e.toString())
                .append("</font></br>");
        for (StackTraceElement element : e.getStackTrace()) {
            result.append(element.toString() + "</br>");
        }
        result.append("</div>");
        return result.toString();
    }
}
