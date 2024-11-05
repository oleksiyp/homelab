package app;

import app.WalkHistoryService.Quarter;
import app.eventslib.SubscribeEventPattern;
import codegen.openapi.earth_meter.api.GetWalkHistory200Response;
import codegen.openapi.earth_meter.api.GetWalkHistory200ResponseYearsInner;
import codegen.openapi.earth_meter.api.WalkHistoryApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/earth-meter/v1")
@RequiredArgsConstructor
public class WalkHistoryController implements WalkHistoryApi {
    private final WalkHistoryService service;

    @Override
    @SubscribeEventPattern(WalkHistoryService.DISTANCE_WALKING_HISTORY_EVENT_PATTERNS)
    public ResponseEntity<GetWalkHistory200Response> getWalkHistory(String user) {
        Map<Quarter, Integer> distanceMap = service.distance();

        long allDistance = distanceMap
                .values()
                .stream()
                .mapToLong(Integer::longValue)
                .sum();

        return ResponseEntity.ok(new GetWalkHistory200Response()
                .overallDistance(allDistance)
                .years(
                        distanceMap.entrySet()
                                .stream()
                                .collect(Collectors.groupingBy(it -> it.getKey().year()))
                                .entrySet()
                                .stream()
                                .sorted(Comparator.comparing(Map.Entry::getKey))
                                .map(it -> new GetWalkHistory200ResponseYearsInner()
                                        .year(BigDecimal.valueOf(it.getKey()))
                                        .quarters(it.getValue().stream().collect(
                                                Collectors.toMap(jt -> "Q" + jt.getKey().quarter(),
                                                        jt -> Long.valueOf(jt.getValue())))))
                                .toList()
                ));
    }
}
