package app.eventslib;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublishEventsInterceptor implements HandlerInterceptor {
    private final EventsService eventsService;

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) throws Exception {
        try {
            if (!(handler instanceof HandlerMethod)) {
                return;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            PublishEventPattern eventPattern = method.getAnnotation(PublishEventPattern.class);
            if (eventPattern != null) {
                eventsService.publish(Set.of(eventPattern.value()));
            }
        } catch (Exception e) {
            log.warn("Failed to publish event notification", e);
        }
    }
}
