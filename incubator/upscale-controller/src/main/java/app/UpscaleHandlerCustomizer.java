package app;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class UpscaleHandlerCustomizer implements JettyServerCustomizer {
    private final UpscaleHandler upscaleHandler;

    public UpscaleHandlerCustomizer(UpscaleHandler upscaleHandler) {
        this.upscaleHandler = upscaleHandler;
    }

    @Override
    public void customize(Server server) {
        server.setHandler(new Handler.Sequence(upscaleHandler, server.getHandler()));
    }
}
