package org.mifos.workflow.tools;

import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.languages.JavaClientCodegen;

import java.util.List;
import java.util.Map;

public class UsecaseClientCodegen extends JavaClientCodegen {
    public UsecaseClientCodegen() {
        super();
        // Don't append "Api" suffix to class names; we'll add "UseCase" in the template
        apiNameSuffix = "";
    }

    @Override
    public String getName() {
        return "usecase-java";
    }

    @Override
    public String getHelp() {
        return "Generates one use-case class per OpenAPI operation.";
    }

    /**
     * Force each operation into its own group so the template engine
     * is invoked once per operation. The operationId (camel-cased)
     * becomes the group key and therefore the class name root.
     */
    @Override
    public void addOperationToGroup(String tag,
                                    String resourcePath,
                                    Operation operation,
                                    CodegenOperation co,
                                    Map<String, List<CodegenOperation>> operations) {
        String groupName = co.operationId;       // e.g. getUserById
        super.addOperationToGroup(groupName, resourcePath, operation, co, operations);
    }

    @Override
    public void postProcess() {
        System.out.println("UseCaseClientCodegen: one class per operation will be emitted.");
    }}
