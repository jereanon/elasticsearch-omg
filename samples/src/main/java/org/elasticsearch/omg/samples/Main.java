package org.elasticsearch.omg.samples;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class is responsible for hacking the project.
 */
public class Main {

    /**
     * Run the project.
     * @param args the arguments
     */
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/spring-config.xml");

        ExampleBusinessRunner runner = (ExampleBusinessRunner)ctx.getBean("exampleBusinessProcess");
        runner.doBusiness();
    }
}
