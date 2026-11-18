///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

/// Minimal BPMN process definitions used by the remote Flowable usecase tests.
final class FlowableRemoteTestProcesses {
    private static final String HEADER = """
        <?xml version="1.0" encoding="UTF-8"?>
        <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     xmlns:flowable="http://flowable.org/bpmn"
                     targetNamespace="http://mifos.org/workflow/test">
        """;

    private FlowableRemoteTestProcesses() {}

    /// start -> end; finishes immediately, so the instance lands in history right away.
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

    /// start -> user task "Review" assigned to the given user -> end.
    static String userTask(String key, String assignee) {
        return HEADER + """
              <process id="%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toTask" sourceRef="start" targetRef="review"/>
                <userTask id="review" name="Review" flowable:assignee="%s"/>
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
