package app.eventslib;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class PublishEventsInterceptorConfig implements WebMvcConfigurer {
    private final PublishEventsInterceptor eventsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(eventsInterceptor)
                .addPathPatterns("/**");
    }
}
