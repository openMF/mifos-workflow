///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.service.dispatch.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/// Dispatches to `MifosFlow` and `MifosUsecase` beans by input type.
///
/// Both kinds are one input in, one output out, so they share one registry keyed by input class name. The registry
/// is built once all singletons exist, so beans that themselves depend on the workflow engine do not form a cycle
/// with the engine adapters that depend on this dispatcher. Two beans sharing an input type is a configuration
/// error and fails startup, whether they are flows, usecases or one of each.
@Slf4j
@Component
final class DefaultProcessDispatcher implements MifosProcessDispatcher, SmartInitializingSingleton {
    private final ObjectProvider<MifosFlow<? extends MifosFlow.MifosFlowContext>> flows;
    private final ObjectProvider<
                    MifosUsecase<? extends MifosUsecase.MifosRequest, ? extends MifosUsecase.MifosResponse>>
            usecases;
    private final JsonMapper jsonMapper;
    private volatile Map<String, Registration> registrations = Map.of();

    DefaultProcessDispatcher(
            ObjectProvider<MifosFlow<? extends MifosFlow.MifosFlowContext>> flows,
            ObjectProvider<MifosUsecase<? extends MifosUsecase.MifosRequest, ? extends MifosUsecase.MifosResponse>>
                    usecases,
            JsonMapper jsonMapper) {
        this.flows = flows;
        this.usecases = usecases;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void afterSingletonsInstantiated() {
        registrations = register(Stream.concat(
                        flows.orderedStream().map(DefaultProcessDispatcher::of),
                        usecases.orderedStream().map(DefaultProcessDispatcher::of))
                .toList());

        log.debug(
                "registered {} flows and usecases for process dispatch",
                Set.copyOf(registrations.values()).size());
    }

    @Override
    public @Nullable Object dispatch(String type, @Nullable Object input) {
        var registration = registrations.get(type);

        if (registration == null) {
            throw new IllegalArgumentException("No flow or usecase registered for input type '" + type + "'");
        }

        var converted = toInput(registration.inputType(), input);

        log.debug("dispatching {} to {}", type, registration.target().getClass().getName());

        return registration.call().apply(converted);
    }

    @Override
    public String toJson(Object value) {
        return jsonMapper.writeValueAsString(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Registration of(MifosFlow<? extends MifosFlow.MifosFlowContext> flow) {
        return registration(flow, MifosFlow.class, input -> ((MifosFlow) flow).run((MifosFlow.MifosFlowContext) input));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Registration of(
            MifosUsecase<? extends MifosUsecase.MifosRequest, ? extends MifosUsecase.MifosResponse> usecase) {
        return registration(
                usecase,
                MifosUsecase.class,
                input -> ((MifosUsecase) usecase).execute((MifosUsecase.MifosRequest) input));
    }

    static Map<String, Registration> register(List<@Nullable Registration> candidates) {
        var result = new HashMap<String, Registration>();

        for (var registration : candidates) {
            if (registration == null) {
                continue;
            }

            for (var name : names(registration.inputType())) {
                var existing = result.putIfAbsent(name, registration);

                if (existing != null && existing.target() != registration.target()) {
                    throw new IllegalStateException("Input type " + name + " is handled by both "
                            + existing.target().getClass().getName() + " and "
                            + registration.target().getClass().getName());
                }
            }
        }

        return Map.copyOf(result);
    }

    private static @Nullable Registration registration(
            Object target, Class<?> contract, Function<Object, @Nullable Object> call) {
        var inputType = ResolvableType.forClass(AopUtils.getTargetClass(target))
                .as(contract)
                .getGeneric(0)
                .resolve();

        if (inputType == null) {
            log.warn(
                    "cannot determine input type of {}, skipping",
                    target.getClass().getName());
            return null;
        }

        return new Registration(inputType, target, call);
    }

    private static Set<String> names(Class<?> inputType) {
        var name = inputType.getName();
        var canonical = inputType.getCanonicalName();
        // top-level classes have identical binary and canonical names; local and anonymous ones have no canonical name
        return canonical == null || canonical.equals(name) ? Set.of(name) : Set.of(name, canonical);
    }

    private Object toInput(Class<?> inputType, @Nullable Object input) {
        if (input == null) {
            return jsonMapper.convertValue(Map.of(), inputType);
        }

        if (inputType.isInstance(input)) {
            return input;
        }

        if (input instanceof String json) {
            return jsonMapper.readValue(json, inputType);
        }

        return jsonMapper.convertValue(input, inputType);
    }

    record Registration(Class<?> inputType, Object target, Function<Object, @Nullable Object> call) {}
}
