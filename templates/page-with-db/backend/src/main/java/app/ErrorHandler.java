package app;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@ControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleException(
            HttpServletRequest request,
            Exception ex
    ) {
        UUID instance = UUID.randomUUID();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail details = ProblemDetail.forStatusAndDetail(
                status,
                scrapeError(ex)
        );

        details.setType(URI.create("java:" + ex.getClass().getName()));
        details.setTitle(status.name());
        details.setInstance(URI.create("java:" + ex.getClass().getName() + "#" + instance));

        log.debug("Http handling error {}", instance, ex);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(details);
    }

    public static String scrapeError(Throwable ex) {
        if (ex == null) {
            return "Unknown error";
        }
        List<String> causes = findCauses(ex);

        causes = causes.stream()
                .filter(it -> !it.isBlank())
                .toList();

        if (causes.isEmpty()) {
            return "Unknown error";
        }

        if (causes.size() == 1) {
            return causes.getFirst();
        }

        return causes.stream()
                .map(it -> "(" + it + ")")
                .collect(Collectors.joining(" due to "));
    }

    private static List<String> findCauses(Throwable ex) {
        List<String> causes = new ArrayList<>();

        List<List<String>> redundantEndings = new ArrayList<>();

        Throwable t = ex;
        while (t != null && t != t.getCause()) {
            String msg = t.getMessage();
            if (msg == null || msg.isBlank()) {
                causes.add(t.getClass().getName());
                redundantEndings.add(Collections.singletonList(t.getClass().getName()));
            } else {
                causes.add(t.getClass().getSimpleName() + ": " + msg);
                redundantEndings.add(asList(
                        ": " + t.getClass().getName() + ": " + msg,
                        ": " + t.getClass().getSimpleName() + ": " + msg,
                        "; nexted exception is " + t.getClass().getSimpleName() + ": " + msg,
                        ": " + msg
                ));
            }
            t = t.getCause();
        }
        return removeEndings(causes, redundantEndings);
    }

    private static List<String> removeEndings(
            List<String> causes,
            List<List<String>> redundantEndings
    ) {
        for (int i = 1; i < causes.size(); i++) {
            next:
            while (true) {
                String prev = causes.get(i - 1);
                String curr = causes.get(i);
                for (String ending : redundantEndings.get(i)) {
                    if (prev.endsWith(ending)) {
                        causes.set(i - 1, prev.substring(0, prev.length() - ending.length()));
                        continue next;
                    }
                }
                if (prev.endsWith(curr)) {
                    causes.set(i - 1, prev.substring(0, prev.length() - curr.length()));
                }
                break;
            }
        }
        return causes;
    }
}
