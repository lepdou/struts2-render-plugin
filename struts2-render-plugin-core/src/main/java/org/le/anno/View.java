package org.le.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark for pipe and action.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {
    /**
     * binding freemarker file
     *
     * @return freemarker file's path
     */
    String ftlPath();

    /**
     * pipe'key for reference in action's freemarker. default pipe's freemarker's filename
     *
     * @return pipe'key
     */
    String key() default "";

    /**
     *
     * @see org.le.anno.ExecuteType
     */
    ExecuteType type() default ExecuteType.SYNC;

    /**
     * pipe display wight,at same level has no difference priority
     * @return
     */
    Weight weight() default Weight.NORMALL;

    /**
     * 在后端并发模式下，整个页面可以设置超时时间,单位毫秒
     * @return
     */
    int timeout() default Integer.MAX_VALUE;

    /**
     * 是否可以被缓存，默认false
     * @return
     */
    boolean cacheable() default false;

}
