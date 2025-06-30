package org.mifos.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MifosWorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(MifosWorkflowApplication.class, args);
    }
}
