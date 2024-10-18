package app;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.github.oleksiyp.operator.OperatorController;
import io.github.oleksiyp.operator.OperatorEvent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.proxy.ProxyHandler;
import org.eclipse.jetty.server.Request;
import org.springframework.stereotype.Component;

@Component
public class UpscaleHandler extends ProxyHandler implements OperatorController<Ingress> {

    @Override
    protected HttpURI rewriteHttpURI(Request clientToProxyRequest) {
        HttpFields headers = clientToProxyRequest.getHeaders();
        System.out.println(clientToProxyRequest.getHttpURI());
        HttpURI.Mutable uri = HttpURI.build()
                .uri("https://google.com")
                .path(clientToProxyRequest.getHttpURI().getPath());
        System.out.println(uri);
        return uri;
    }

    @Override
    public void onResourceChanged(OperatorEvent<Ingress> event) {
        System.out.println(event.resource());
    }
}
