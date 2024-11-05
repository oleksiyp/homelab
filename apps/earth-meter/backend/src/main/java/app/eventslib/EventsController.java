package app.eventslib;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/earth-meter/v1")
@RequiredArgsConstructor
@Slf4j
public class EventsController {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final EventsService eventsService;

    @GetMapping("/events")
    public SseEmitter subscribeWalkHistory(
            @RequestParam MultiValueMap<String, String> params
    ) {
        Set<String> eventPatternsToSubscribe = findEventPatternsToSubscribe("/api/earth-meter/v1", params);
        SseEmitter emitter = new SseEmitter();
        if (eventPatternsToSubscribe.isEmpty()) {
            return emitter;
        }
        //noinspection resource
        AutoCloseable subscribedObj = eventsService.subscribe(eventPatternsToSubscribe, () -> {
            try {
                emitter.send("");
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        emitter.onCompletion(() -> {
            try {
                subscribedObj.close();
            } catch (Exception e) {
                log.debug("Failed to unsubscribe", e);
            }
        });
        emitter.onError(e -> log.debug("Emitter failed", e));
        return emitter;
    }

    private Set<String> findEventPatternsToSubscribe(String prefix, MultiValueMap<String, String> params) {
        Set<String> eventPatternsToSubscribe = new LinkedHashSet<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            for (String path : params.keySet()) {
                PathPatternsRequestCondition condition = mappingInfo.getPathPatternsCondition();
                if (condition == null || condition
                        .getPatterns()
                        .stream()
                        .noneMatch(it -> it.getPatternString().equals(prefix + "/" + path))) {
                    continue;
                }

                Set<String> httpMehods = params.get(path)
                        .stream()
                        .flatMap(it -> Arrays.stream(it.split(",")))
                        .collect(Collectors.toSet());

                if (!httpMehods.contains("*")) {
                    boolean ok = false;
                    for (String httpMethod : httpMehods) {
                        RequestMethodsRequestCondition methodsCondition = mappingInfo.getMethodsCondition();
                        RequestMethod httpMethodObj = RequestMethod.resolve(httpMethod);
                        if (httpMethodObj == null) {
                            continue;
                        }
                        if (methodsCondition.getMethods().contains(httpMethodObj)) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        continue;
                    }
                }

                SubscribeEventPattern pattern = entry.getValue().getMethodAnnotation(SubscribeEventPattern.class);
                if (pattern == null) {
                    continue;
                }

                eventPatternsToSubscribe.addAll(Arrays.asList(pattern.value()));
            }
        }
        return eventPatternsToSubscribe;
    }
}
