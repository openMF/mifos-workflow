package org.mifos.workflow.fineract.application.backend.implementation;

import static org.springframework.modulith.core.DependencyDepth.IMMEDIATE;
import static org.springframework.modulith.docs.Documenter.DiagramOptions.DiagramStyle.C4;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@Slf4j
class FineractApplicationTest {
    static final ApplicationModules modules = ApplicationModules.of(Main.class);

    @Test
    void verify() {
        modules.verify();

        log.warn("Modules: {}", modules.stream().count());
    }

    @Test
    @SuppressWarnings("java:S2699")
    void document() {
        new Documenter(modules).writeModulesAsPlantUml(
                    Documenter.DiagramOptions.defaults()
                            .withStyle(C4)
                            .withDependencyDepth(IMMEDIATE)
                            .withColorSelector(_ -> Optional.of("#E8F4F8"))
                )
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases()
                .writeAggregatingDocument();
    }
}
