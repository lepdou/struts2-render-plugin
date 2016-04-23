package org.le.bean;

/**
 * pagelet in one page. every implements class must binding View annotaion.
 * for example:@View(ftlPath = "demo/one.ftl" key = "one")
 */
public interface Pipe {
    final String SUCCESS = "success";
    final String ERROR = "error";
    final String NONE = "none";
    /**
     * implement business logic
     */
    String execute();
}
