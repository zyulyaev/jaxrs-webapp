package io.github.zyulyaev.jaxrswebapp.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link Clock} implementation which does not move until requested
 */
public class ControllableClock extends Clock {
    private final ZoneId zoneId;
    private final AtomicReference<Instant> now;

    public ControllableClock(ZoneId zoneId, Instant initial) {
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.now = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    }

    public void advance(Duration duration) {
        if (duration.isNegative()) { // intentional NPE
            throw new IllegalArgumentException("Cannot move to past");
        }
        now.updateAndGet(instant -> instant.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
