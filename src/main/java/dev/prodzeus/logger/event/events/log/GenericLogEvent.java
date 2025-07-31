package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.Marker;
import dev.prodzeus.logger.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class GenericLogEvent extends Event {

    protected final Level level;
    protected final String rawLog;
    protected final String log;
    protected final Collection<Object> args;
    protected final Collection<Marker> markers;

    protected GenericLogEvent(@NotNull final Logger logger, @NotNull final Level level, @NotNull final Collection<Marker> marker,
                              @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger);
        this.level = level;
        this.rawLog = log;
        this.log = formatLogMessage(true);
        this.args = args;
        this.markers = marker;
    }

    private @NotNull String formatLogMessage(final boolean color) {
        StringBuilder log = new StringBuilder();

        for (final String line : rawLog.split("\n")) {
            log.append(color ? level.getPrefix() : level.getRawPrefix());

            if (markers != null && !markers.isEmpty()) {
                for (final Marker marker : markers) {
                    log.append(" [").append(marker.getName()).append("]");
                }
            }

            log.append(" ");

            if (color) {
                log.append("\u001b[38;5;240m[\u001b[0m")
                        .append(logger.getName())
                        .append("\u001b[38;5;240m]\u001b[0m ")
                        .append(level.getColor());
            } else {
                log.append("[").append(logger.getName()).append("] ");
            }
            if (line.contains("@")) {
                log.append(formatLogColors(line));
            } else {
                log.append(line);
            }
            log.append("\n");
        }
        return log.toString().stripTrailing();
    }

    private @NotNull String formatLogColors(@NotNull final String line) {
        String newLine = line;
        if (newLine.contains("@black")) newLine = newLine.replace("@black","\u001b[30m");
        if (newLine.contains("@red")) newLine = newLine.replace("@red","\u001b[38;5;196m");
        if (newLine.contains("@green")) newLine = newLine.replace("@green","\u001b[38;5;46m");
        if (newLine.contains("@yellow")) newLine = newLine.replace("@yellow","\u001b[38;5;227m");
        if (newLine.contains("@blue")) newLine = newLine.replace("@blue","\u001b[38;5;33m");
        if (newLine.contains("@magenta")) newLine = newLine.replace("@magenta","\u001b[38;5;93m");
        if (newLine.contains("@cyan")) newLine = newLine.replace("@cyan","\u001b[38;5;14m");
        if (newLine.contains("@white")) newLine = newLine.replace("@white","\u001b[38;5;255m");
        if (newLine.contains("@reset")) newLine = newLine.replace("@reset","\u001b[0m");
        return newLine;
    }

    public @NotNull Level getLevel() {
        return level;
    }

    public @NotNull String getRawLog() {
        return rawLog;
    }

    public @NotNull String getFormattedLog() {
        return log;
    }

    public @NotNull String getFormattedLogNoColor() {
        return formatLogMessage(false);
    }

    public @NotNull Collection<Object> getArguments() {
        return args;
    }

    public @NotNull Collection<Marker> getMarkers() {
        return markers;
    }
}
