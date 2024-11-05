package app;

import app.WalkHistoryService.Day;
import app.eventslib.PublishEventPattern;
import app.eventslib.SubscribeEventPattern;
import codegen.openapi.earth_meter.api.TakeoutApi;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/earth-meter/v1")
@RequiredArgsConstructor
public class TakeoutController implements TakeoutApi {

    public static final String ACTIVITY_TYPE = "WALKING";
    public static final String CONFIDENCE_LEVEL = "HIGH";

    private final WalkHistoryService walkHistoryService;

    @Override
    @PublishEventPattern(WalkHistoryService.UPDATE_WALKING_HISTORY_EVENT_PATTERNS)
    public ResponseEntity<Void> uploadTakeout(Resource body) {
        try {
            if (body == null) {
                throw new RuntimeException("missing body");
            }
            Map<Day, Long> distances = new LinkedHashMap<>();
            parseGoogleTakeout(body, distances);
            walkHistoryService.updateWalkingHistory(distances);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new RuntimeException("failed to open uploaded file", e);
        }
    }

    private static void parseGoogleTakeout(Resource body, Map<Day, Long> distances) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(body.getInputStream())) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                try {
                    if (!entry.getName().matches(".+/Semantic Location History/[^/]+/[^/]+.json")) {
                        continue;
                    }

                    parseGoogleTakeoutLocationHistoryJson(distances, zin);
                } finally {
                    zin.closeEntry();
                }
            }
        }
    }

    private static void parseGoogleTakeoutLocationHistoryJson(
            Map<Day, Long> distances,
            ZipInputStream zin
    ) throws IOException {
        JsonNode node = new ObjectMapper().reader().without(JsonParser.Feature.AUTO_CLOSE_SOURCE).readTree(zin);
        for (JsonNode timelineObject : node.path("timelineObjects")) {
            JsonNode activitySegment = timelineObject.path("activitySegment");
            JsonNode activityType = activitySegment.path("activityType");
            JsonNode confidence = activitySegment.path("confidence");
            JsonNode duration = activitySegment.path("duration");
            if (!ACTIVITY_TYPE.equals(activityType.asText()) ||
                    !CONFIDENCE_LEVEL.equals(confidence.asText())) {
                continue;
            }

            long distance = activitySegment.path("waypointPath").path("distanceMeters").asLong(0);
            if (distance == 0L) {
                continue;
            }
            String []dayArr = duration.path("startTimestamp").asText()
                    .replaceAll("T.*$", "").split("-");
            Day day = new Day(
                    Integer.parseInt(dayArr[0]),
                    Integer.parseInt(dayArr[1]),
                    Integer.parseInt(dayArr[2])
            );
            distances.put(day, distances.getOrDefault(day, 0L) + distance);
        }
    }
}
