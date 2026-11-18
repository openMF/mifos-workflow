///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

/// Minimal Camunda 8 BPMN process definitions used by the Camunda usecase tests.
final class CamundaTestProcesses {
    private static final String HEADER = """
        <?xml version="1.0" encoding="UTF-8"?>
        <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"
                     targetNamespace="http://mifos.org/workflow/test">
        """;

    private CamundaTestProcesses() {}

    /// start -> end; finishes immediately.
    static String straightThrough(String key) {
        return HEADER + """
              <process id="%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toEnd" sourceRef="start" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(key);
    }

    /// start -> Camunda user task "Review" assigned to the given user -> end.
    static String userTask(String key, String assignee) {
        return HEADER + """
              <process id="%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toTask" sourceRef="start" targetRef="review"/>
                <userTask id="review" name="Review">
                  <extensionElements>
                    <zeebe:userTask/>
                    <zeebe:assignmentDefinition assignee="%s"/>
                  </extensionElements>
                </userTask>
                <sequenceFlow id="toEnd" sourceRef="review" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(key, assignee);
    }

    /// start -> waits for the given signal -> end.
    static String signalCatch(String key, String signalName) {
        return HEADER + """
              <signal id="signal" name="%s"/>
              <process id="%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toWait" sourceRef="start" targetRef="wait"/>
                <intermediateCatchEvent id="wait">
                  <signalEventDefinition signalRef="signal"/>
                </intermediateCatchEvent>
                <sequenceFlow id="toEnd" sourceRef="wait" targetRef="end"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """.formatted(signalName, key);
    }
}
