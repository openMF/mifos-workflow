///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.service.dispatch.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.json.JsonMapper;

final class DefaultProcessDispatcherTest {
    private static final String CANONICAL = GreetUsecase.Request.class.getCanonicalName();
    private static final String BINARY = GreetUsecase.Request.class.getName();

    @Test
    void dispatchesJsonInputByCanonicalName() {
        try (var context = context(GreetUsecase.class)) {
            var response = dispatcher(context).dispatch(CANONICAL, "{\"name\":\"Ada\"}");

            assertThat(response).isEqualTo(new GreetUsecase.Response("Hello Ada"));
        }
    }

    @Test
    void dispatchesMapInputByBinaryName() {
        try (var context = context(GreetUsecase.class)) {
            var response = dispatcher(context).dispatch(BINARY, Map.of("name", "Grace"));

            assertThat(response).isEqualTo(new GreetUsecase.Response("Hello Grace"));
        }
    }

    @Test
    void passesRequestObjectThrough() {
        try (var context = context(GreetUsecase.class)) {
            var response = dispatcher(context).dispatch(CANONICAL, new GreetUsecase.Request("Linus"));

            assertThat(response).isEqualTo(new GreetUsecase.Response("Hello Linus"));
        }
    }

    @Test
    void createsEmptyRequestForNullInput() {
        try (var context = context(GreetUsecase.class)) {
            var response = dispatcher(context).dispatch(CANONICAL, null);

            assertThat(response).isEqualTo(new GreetUsecase.Response("Hello null"));
        }
    }

    @Test
    void rejectsUnknownType() {
        try (var context = context(GreetUsecase.class)) {
            var dispatcher = dispatcher(context);

            assertThatThrownBy(() -> dispatcher.dispatch("java.lang.Runtime", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("java.lang.Runtime");
        }
    }

    @Test
    void failsStartupWhenTwoUsecasesShareRequestType() {
        try (var context = unrefreshedContext(GreetUsecase.class, AnotherGreetUsecase.class)) {
            assertThatThrownBy(context::refresh)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(GreetUsecase.class.getName())
                    .hasMessageContaining(AnotherGreetUsecase.class.getName());
        }
    }

    @Test
    void dispatchesToFlow() {
        try (var context = context(GreetUsecase.class, ShoutFlow.class)) {
            var response = dispatcher(context).dispatch(ShoutFlow.Ctx.class.getName(), "{\"text\":\"hi\"}");

            assertThat(response).isEqualTo(new ShoutFlow.Ctx("hi", "HI"));
        }
    }

    @Test
    void dispatchesToUsecaseAlongsideFlow() {
        try (var context = context(GreetUsecase.class, ShoutFlow.class)) {
            var response = dispatcher(context).dispatch(CANONICAL, Map.of("name", "Ada"));

            assertThat(response).isEqualTo(new GreetUsecase.Response("Hello Ada"));
        }
    }

    @Test
    void failsStartupWhenFlowAndUsecaseShareInputType() {
        try (var context = unrefreshedContext(GreetUsecase.class, GreetFlow.class)) {
            assertThatThrownBy(context::refresh)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(GreetUsecase.class.getName())
                    .hasMessageContaining(GreetFlow.class.getName());
        }
    }

    @Test
    void serializesToJson() {
        try (var context = context(GreetUsecase.class)) {
            var json = dispatcher(context).toJson(new GreetUsecase.Response("Hello Ada"));

            assertThat(json).isEqualTo("{\"greeting\":\"Hello Ada\"}");
        }
    }

    private static AnnotationConfigApplicationContext context(Class<?>... beans) {
        var context = unrefreshedContext(beans);
        context.refresh();
        return context;
    }

    private static AnnotationConfigApplicationContext unrefreshedContext(Class<?>... beans) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(JsonMapper.class, () -> JsonMapper.builder().build());
        for (var bean : beans) {
            context.registerBean(bean);
        }
        context.registerBean(DefaultProcessDispatcher.class);
        return context;
    }

    private static MifosProcessDispatcher dispatcher(AnnotationConfigApplicationContext context) {
        return context.getBean(MifosProcessDispatcher.class);
    }

    static final class GreetUsecase implements MifosUsecase<GreetUsecase.Request, GreetUsecase.Response> {
        @Override
        public Response execute(Request request) {
            return new Response("Hello " + request.name());
        }

        // also a flow context, so GreetFlow can claim the same input type
        record Request(String name) implements MifosUsecase.MifosRequest, MifosFlow.MifosFlowContext {}

        record Response(String greeting) implements MifosUsecase.MifosResponse {}
    }

    static final class AnotherGreetUsecase implements MifosUsecase<GreetUsecase.Request, GreetUsecase.Response> {
        @Override
        public GreetUsecase.Response execute(GreetUsecase.Request request) {
            return new GreetUsecase.Response("Hi " + request.name());
        }
    }

    static final class ShoutFlow implements MifosFlow<ShoutFlow.Ctx> {
        @Override
        public Ctx run(Ctx context) {
            return context.result(context.text().toUpperCase(Locale.ROOT));
        }

        record Ctx(String text, String result) implements MifosFlow.MifosFlowContext {
            Ctx result(String result) {
                return new Ctx(text, result);
            }
        }
    }

    static final class GreetFlow implements MifosFlow<GreetUsecase.Request> {
        @Override
        public GreetUsecase.Request run(GreetUsecase.Request context) {
            return new GreetUsecase.Request("Hey " + context.name());
        }
    }
}
