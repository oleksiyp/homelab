package app;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static codegen.jooq.earth_meter.backend.tables.WalkingHistory.WALKING_HISTORY;
import static org.jooq.impl.DSL.*;

@Service
@RequiredArgsConstructor
public class WalkHistoryService {
    private final DSLContext dsl;
    public static final String UPDATE_WALKING_HISTORY_EVENT_PATTERNS = "walking_history::rows()";
    public static final String DISTANCE_WALKING_HISTORY_EVENT_PATTERNS = "walking_history::rows()";


    public void updateWalkingHistory(Map<Day, Long> distances) {
        for (Day day : distances.keySet()) {
            dsl.insertInto(WALKING_HISTORY)
                    .set(WALKING_HISTORY.YEAR, day.year)
                    .set(WALKING_HISTORY.MONTH, day.month)
                    .set(WALKING_HISTORY.DAY, day.day)
                    .set(WALKING_HISTORY.DISTANCE, distances.get(day).intValue())
                    .onDuplicateKeyUpdate()
                    .set(WALKING_HISTORY.DISTANCE, distances.get(day).intValue())
                    .execute();
        }
    }

    public Map<Quarter, Integer> distance() {
        return dsl.select(
                        WALKING_HISTORY.YEAR,
                        when(WALKING_HISTORY.MONTH.between(1, 3), 1)
                                .when(WALKING_HISTORY.MONTH.between(4, 6), 2)
                                .when(WALKING_HISTORY.MONTH.between(7, 9), 3)
                                .when(WALKING_HISTORY.MONTH.between(10, 12), 4).as("quarter"),
                        sum(WALKING_HISTORY.DISTANCE).as("distance")
                )
                .from(WALKING_HISTORY)
                .groupBy(WALKING_HISTORY.YEAR, field(name("quarter")))
                .fetch()
                .collect(Collectors.toMap(e -> new Quarter(e.component1(), e.component2()), e -> e.component3().intValue()));
    }

    public record Day(int year, int month, int day) {

    }

    public record Quarter(int year, int quarter) {

    }
}
