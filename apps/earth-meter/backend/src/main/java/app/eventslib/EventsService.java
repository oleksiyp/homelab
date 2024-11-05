package app.eventslib;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsService {
    public static final int MIN_DELAY_TO_RECONNECT_MS = 3000;
    public static final int MIN_DELAY_TO_CHECK_NOTIFICATION_MS = 500;
    private final Set<EventActor> actors = new LinkedHashSet<>();
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private ScheduledExecutorService executor;
    private Thread eventListenerThread;

    public AutoCloseable subscribe(Set<String> eventPatterns, Runnable handler) {
        EventActor actor = new EventActor(eventPatterns, handler);
        synchronized (this) {
            actors.add(actor);
        }
        return actor;
    }

    @PostConstruct
    public void startListener() {
        executor = Executors.newSingleThreadScheduledExecutor(EventsService::newDispatcherThread);
        eventListenerThread = new Thread(this::runListener, "events-listener");
        eventListenerThread.setDaemon(true);
        eventListenerThread.start();
    }

    private static Thread newDispatcherThread(Runnable r) {
        Thread thread = new Thread(r, "events-listener-dispatcher");
        thread.setDaemon(true);
        return thread;
    }

    private void runListener() {
        try {
            while (!Thread.interrupted()) {
                long start = System.currentTimeMillis();
                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement()) {
                    statement.execute("LISTEN events");
                    PGConnection pgConnection = connection.unwrap(PGConnection.class);

                    while (!Thread.currentThread().isInterrupted()) {
                        long startNotifyRead = System.currentTimeMillis();
                        //noinspection SqlNoDataSourceInspection
                        statement.executeQuery("SELECT 1");
                        Set<String> events = eventsOf(pgConnection);
                        if (!events.isEmpty()) {
                            System.out.println(events);
                            Set<EventActor> copy;
                            synchronized (this) {
                                copy = new LinkedHashSet<>(actors);
                            }
                            for (EventActor actor : copy) {
                                actor.accept(events);
                            }
                        }
                        long passed = System.currentTimeMillis() - startNotifyRead;
                        //noinspection BusyWait
                        Thread.sleep(Math.max(MIN_DELAY_TO_CHECK_NOTIFICATION_MS - passed, 0));
                    }
                } catch (Exception e) {
                    log.debug("Failure in PostgreSQL listener events thread", e);
                    long passed = System.currentTimeMillis() - start;
                    //noinspection BusyWait
                    Thread.sleep(Math.max(MIN_DELAY_TO_RECONNECT_MS - passed, 0));
                }
            }
        } catch (InterruptedException e) {
            // skip
        }
    }

    private static Set<String> eventsOf(PGConnection pgConnection) throws SQLException {
        Set<String> events = new LinkedHashSet<>();

        PGNotification[] notifications = pgConnection.getNotifications();
        if (notifications == null) {
            return events;
        }

        for (var notification : notifications) {
            events.addAll(List.of(notification.getParameter().split(",")));
        }
        return events;
    }

    public void publish(Set<String> events) {
        //noinspection SqlNoDataSourceInspection
        jdbcTemplate.query("SELECT pg_notify(?, ?)", rs -> null, "events", String.join(" ", events));
    }

    @PreDestroy
    public void stopListener() throws InterruptedException {
        eventListenerThread.interrupt();
        eventListenerThread.join();
        executor.shutdownNow();
    }

    @RequiredArgsConstructor
    private class EventActor implements AutoCloseable {
        private final Set<String> eventPatterns;
        private final Runnable handler;

        @Override
        public void close() {
            synchronized (EventsService.this) {
                actors.remove(this);
            }
        }

        public void accept(Set<String> events) {
            for (String pattern : eventPatterns) {
                for (String event : events) {
                    if (event.startsWith(pattern)) {
                        executor.execute(handler);
                        return;
                    }
                }
            }
        }
    }
}
