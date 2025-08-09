package dev.prodzeus.logger.internal;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

import static dev.prodzeus.logger.components.Level.OFF;
import static dev.prodzeus.logger.components.Level.EXCEPTION;

/**
 * @apiNote <b>For internal use only.</b>
 */
public final class Formatter {

    @Contract(pure = true)
    public static @NotNull String constructLogMessage(@NotNull final Logger logger, @NotNull final Level level, @NotNull final String message) {
        return constructLogMessage(logger, level, null, message, true);
    }

    @Contract(pure = true)
    public static @NotNull String constructLogMessage(@NotNull final Logger logger, @NotNull final Level level, @NotNull final String message, final boolean color) {
        return constructLogMessage(logger, level, null, message, color);
    }

    @Contract(pure = true)
    public static @NotNull String constructLogMessage(@NotNull final Logger logger, @NotNull final Level level, @Nullable final Marker marker, @NotNull final String message, final boolean color) {
        final StringBuilder log = new StringBuilder();
        String markersPrefix = "";
        if (color) {
            final String levelPrefix = level.getPrefix();
            final String logName = " @gray@bold[@reset@white" + logger.getName() + "@gray@bold] " + level.getColor();
            final String markerPrefix = (marker != null ? "@gray@bold[@reset" + marker.getName() + "@gray@bold]@reset " : "") + level.getColor();

            for (final String line : message.split("\n")) {
                log.append(levelPrefix)
                        .append(logName)
                        .append(markerPrefix)
                        .append(line)
                        .append("\n");
            }
            return formatLogColors(log.toString().stripTrailing().concat("@reset"));
        } else {
            final String levelPrefix = level.getRawPrefix();
            final String logName = " [" + logger.getName() + "] ";
            final String markerPrefix = marker != null ? "[" + marker.getName() + "] " : "";

            for (final String line : message.split("\n")) {
                log.append(levelPrefix)
                        .append(logName)
                        .append(markerPrefix)
                        .append(line)
                        .append("\n");
            }
            return removeLogColors(log.toString().stripTrailing());
        }
    }

    @Contract(pure = true)
    public static @NotNull String ensurePlaceholderFormat(String message) {
        if (message.isEmpty()) return "{}";
        for (var i = 1; i < (message.length() - 1); i++) {
            if (message.charAt(i) == '{'
                && message.charAt(i + 1) == '}'
                && message.charAt(i - 1) == '\n') {
                return message;
            }
        }
        return message.concat(" \n{}");
    }

    @Contract(pure = true)
    public static @NotNull Pair<String, Level> formatPlaceholders(@NotNull final String log, final Collection<Object> args) {
        String message = !log.contains("%") ? log : log.replaceAll("%[s|d]", Matcher.quoteReplacement("{}"))
                .replace("%d", Matcher.quoteReplacement("{}"))
                .replaceAll("(%(\\.\\d)?f)", Matcher.quoteReplacement("{}"));
        Level level = OFF;
        for (Object arg : args) {
            switch (arg) {
                case null -> message = message.replaceFirst("\\{}", "null");
                case String s -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(s));
                case ErrorResponseException error -> {
                    level = EXCEPTION;
                    message = ensurePlaceholderFormat(message);
                    final StringBuilder builder = new StringBuilder();
                    builder.append("\n").append(EXCEPTION.getColor()).append("[")
                            .append(error.getErrorCode()).append("]:")
                            .append(error.getErrorResponse()).append(" - ")
                            .append(error.getMeaning()).append("\n");
                    for (final StackTraceElement st : error.getStackTrace()) {
                        builder.append(Level.EXCEPTION.getColor()).append(st.toString()).append("\n");
                    }
                    if (!message.contains("\\{}")) message += builder.toString();
                }
                case Throwable t -> {
                    level = EXCEPTION;
                    message = ensurePlaceholderFormat(message);
                    final StringBuilder builder = new StringBuilder();
                    builder.append(Level.EXCEPTION.getColor()).append(t.getMessage()).append("\n");
                    for (final StackTraceElement st : t.getStackTrace()) {
                        builder.append(Level.EXCEPTION.getColor()).append(st.toString()).append("\n");
                    }
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString().stripTrailing()));
                }
                case Collection<?> c -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("[ ");
                    final Iterator<?> iterator = c.iterator();
                    while (iterator.hasNext()) {
                        builder.append(iterator.next());
                        if (iterator.hasNext()) builder.append(", ");
                    }
                    builder.append("]");
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Map<?, ?> m -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("{");
                    for (final var index : m.entrySet()) {
                        builder.append(" [ %s , %s ] ".formatted(java.lang.String.valueOf(index.getKey()), java.lang.String.valueOf(index.getValue())));
                    }
                    builder.append("}");
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                default -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(arg.toString()));
            }
        }
        return Pair.of(message, level);
    }

    @Contract(pure = true)
    public static @NotNull String formatLogColors(@NotNull final String log) {
        String formatted = log;
        if (formatted.contains("@black")) formatted = formatted.replace("@black", "\u001b[30m");
        if (formatted.contains("@gray")) formatted = formatted.replace("@gray", "\u001b[38;5;240m");
        if (formatted.contains("@lightgray")) formatted = formatted.replace("@lightgray", "\u001b[38;5;250m");
        if (formatted.contains("@white")) formatted = formatted.replace("@white", "\u001b[38;5;231m");
        if (formatted.contains("@offwhite")) formatted = formatted.replace("@offwhite", "\u001b[38;5;255m");
        if (formatted.contains("@red")) formatted = formatted.replace("@red", "\u001b[38;5;196m");
        if (formatted.contains("@orange")) formatted = formatted.replace("@orange", "\u001b[38;5;208m");
        if (formatted.contains("@yellow")) formatted = formatted.replace("@yellow", "\u001b[38;5;226m");
        if (formatted.contains("@green")) formatted = formatted.replace("@green", "\u001b[38;5;46m");
        if (formatted.contains("@blue")) formatted = formatted.replace("@blue", "\u001b[38;5;33m");
        if (formatted.contains("@magenta")) formatted = formatted.replace("@magenta", "\u001b[38;5;93m");
        if (formatted.contains("@cyan")) formatted = formatted.replace("@cyan", "\u001b[38;5;51m");
        if (formatted.contains("@bold")) formatted = formatted.replace("@bold", "\u001b[1m");
        if (formatted.contains("@underline")) formatted = formatted.replace("@underline", "\u001b[4m");
        if (formatted.contains("@reversed")) formatted = formatted.replace("@reversed", "\u001b[7m");
        if (formatted.contains("@reset")) formatted = formatted.replace("@reset", "\u001b[0m");
        return formatted;
    }

    @Contract(pure = true)
    public static @NotNull String removeLogColors(@NotNull final String log) {
        String formatted = log;
        if (formatted.contains("@black")) formatted = formatted.replace("@black", "");
        if (formatted.contains("@gray")) formatted = formatted.replace("@gray", "");
        if (formatted.contains("@lightgray")) formatted = formatted.replace("@lightgray", "");
        if (formatted.contains("@white")) formatted = formatted.replace("@white", "");
        if (formatted.contains("@offwhite")) formatted = formatted.replace("@offwhite", "");
        if (formatted.contains("@red")) formatted = formatted.replace("@red", "");
        if (formatted.contains("@orange")) formatted = formatted.replace("@orange", "");
        if (formatted.contains("@yellow")) formatted = formatted.replace("@yellow", "");
        if (formatted.contains("@green")) formatted = formatted.replace("@green", "");
        if (formatted.contains("@blue")) formatted = formatted.replace("@blue", "");
        if (formatted.contains("@magenta")) formatted = formatted.replace("@magenta", "");
        if (formatted.contains("@cyan")) formatted = formatted.replace("@cyan", "");
        if (formatted.contains("@bold")) formatted = formatted.replace("@bold", "");
        if (formatted.contains("@underline")) formatted = formatted.replace("@underline", "");
        if (formatted.contains("@reversed")) formatted = formatted.replace("@reversed", "");
        if (formatted.contains("@reset")) formatted = formatted.replace("@reset", "");
        return formatted;
    }

}
