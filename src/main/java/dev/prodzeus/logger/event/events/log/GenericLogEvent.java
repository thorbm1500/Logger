package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.Marker;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public abstract class GenericLogEvent extends Event {

    protected final Level level;
    protected final Throwable exception;
    protected final String rawLog;
    protected final String log;
    protected final Collection<Object> args;
    protected final Collection<Marker> markers;

    protected GenericLogEvent(@NotNull final Logger logger, @NotNull final Level level, @NotNull final Collection<Marker> marker,
                              @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger);
        this.level = level;
        this.exception = null;
        this.rawLog = log;
        this.log = formatLogMessage(true);
        this.args = args;
        this.markers = marker;
    }

    protected GenericLogEvent(@NotNull final Throwable exception) {
        super(SLF4JProvider.getSystem());
        this.level = Level.EXCEPTION;
        this.exception = exception;
        this.rawLog = formatException();
        this.log = formatLogMessage(true);
        this.args = Collections.emptySet();
        this.markers = Collections.emptySet();
    }

    private @NotNull String formatException() {
        if (exception == null) return "Exception thrown with no cause!";

        StringBuilder log = new StringBuilder();

        if (exception.getStackTrace().length != 0) {
            final StackTraceElement trace = exception.getStackTrace()[0];
            log.append(exception.getCause().getClass().getSimpleName())
                    .append(" thrown in ")
                    .append(trace.getFileName())
                    .append(" at line ")
                    .append(trace.getLineNumber())
                    .append(exception.getMessage() != null && !exception.getMessage().isEmpty() ? ", with error:\n"+exception.getMessage()+"\n" : "\n");

            for (final StackTraceElement line : exception.getStackTrace()) {
                log.append("@offwhite@bold[@redLine ")
                        .append(line.getLineNumber())
                        .append("@offwhite] ")
                        .append(level.getColor())
                        .append(line.getClassName())
                        .append("#")
                        .append(line.getMethodName())
                        .append("\n");
            }
        } else {
            log.append("Exception thrown! ").append(exception);
        }
        return log.toString().stripTrailing();
    }

    private @NotNull String formatLogMessage(final boolean color) {
        final StringBuilder log = new StringBuilder();

        for (final String line : rawLog.concat(color?"":removeLogColors(rawLog)).split("\n")) {
            log.append(color ? level.getPrefix() : level.getRawPrefix());

            if (markers != null && !markers.isEmpty()) {
                for (final Marker marker : markers) {
                    log.append(" [").append(marker.getName()).append("]");
                }
            }

            log.append(" ");

            if (color) {
                log.append("@gray@bold[@reset@white")
                        .append(logger.getName())
                        .append("@gray@bold] ")
                        .append(level.getColor());
            } else {
                log.append("[").append(logger.getName()).append("] ");
            }
            log.append(line).append("\n");
        }
        return color ? formatLogColors(log.toString().stripTrailing().concat("@reset")) : log.toString().stripTrailing();
    }

    private @NotNull String formatLogColors(@NotNull final String log) {
        String formatted = log;
        if (formatted.contains("@black")) formatted = formatted.replace("@black","\u001b[30m");
        if (formatted.contains("@gray")) formatted = formatted.replace("@gray","\u001b[38;5;240m");
        if (formatted.contains("@lightgray")) formatted = formatted.replace("@lightgray","\u001b[38;5;250m");
        if (formatted.contains("@white")) formatted = formatted.replace("@white","\u001b[38;5;231m");
        if (formatted.contains("@offwhite")) formatted = formatted.replace("@offwhite","\u001b[38;5;255m");
        if (formatted.contains("@red")) formatted = formatted.replace("@red","\u001b[38;5;196m");
        if (formatted.contains("@orange")) formatted = formatted.replace("@orange","\u001b[38;5;208m");
        if (formatted.contains("@yellow")) formatted = formatted.replace("@yellow","\u001b[38;5;226m");
        if (formatted.contains("@green")) formatted = formatted.replace("@green","\u001b[38;5;46m");
        if (formatted.contains("@blue")) formatted = formatted.replace("@blue","\u001b[38;5;33m");
        if (formatted.contains("@magenta")) formatted = formatted.replace("@magenta","\u001b[38;5;93m");
        if (formatted.contains("@cyan")) formatted = formatted.replace("@cyan","\u001b[38;5;51m");
        if (formatted.contains("@bold")) formatted = formatted.replace("@bold","\u001b[1m");
        if (formatted.contains("@underline")) formatted = formatted.replace("@underline","\u001b[4m");
        if (formatted.contains("@reversed")) formatted = formatted.replace("@reversed","\u001b[7m");
        if (formatted.contains("@reset")) formatted = formatted.replace("@reset","\u001b[0m");
        return formatted;
    }

    private @NotNull String removeLogColors(@NotNull final String log) {
        String formatted = log;
        if (formatted.contains("@black")) formatted = formatted.replace("@black","");
        if (formatted.contains("@gray")) formatted = formatted.replace("@gray","");
        if (formatted.contains("@lightgray")) formatted = formatted.replace("@lightgray","");
        if (formatted.contains("@white")) formatted = formatted.replace("@white","");
        if (formatted.contains("@offwhite")) formatted = formatted.replace("@offwhite","");
        if (formatted.contains("@red")) formatted = formatted.replace("@red","");
        if (formatted.contains("@orange")) formatted = formatted.replace("@orange","");
        if (formatted.contains("@yellow")) formatted = formatted.replace("@yellow","");
        if (formatted.contains("@green")) formatted = formatted.replace("@green","");
        if (formatted.contains("@blue")) formatted = formatted.replace("@blue","");
        if (formatted.contains("@magenta")) formatted = formatted.replace("@magenta","");
        if (formatted.contains("@cyan")) formatted = formatted.replace("@cyan","");
        if (formatted.contains("@bold")) formatted = formatted.replace("@bold","");
        if (formatted.contains("@underline")) formatted = formatted.replace("@underline","");
        if (formatted.contains("@reversed")) formatted = formatted.replace("@reversed","");
        if (formatted.contains("@reset")) formatted = formatted.replace("@reset","");
        return formatted;
    }

    public boolean isException() {
        return exception != null;
    }

    public @Nullable Throwable getException() {
        return exception;
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
