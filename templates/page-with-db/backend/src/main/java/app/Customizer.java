package app;

import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.stereotype.Component;

@Component
public class Customizer implements DefaultConfigurationCustomizer  {
    @Override
    public void customize(DefaultConfiguration configuration) {
        configuration.settings()
                .withRenderMapping(new RenderMapping()
                        .withSchemata(new MappedSchema()
                                .withInput("public")
                                .withOutput("backend")));
    }
}
