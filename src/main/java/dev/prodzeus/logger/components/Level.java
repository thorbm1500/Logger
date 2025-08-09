package dev.prodzeus.logger.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Log levels are used to differentiate logs based on importance
 * and purpose. Each log level has an Integer weight, which ranges from {@code 0-1000}.
 */
public enum Level {
    ALL("All","@white",null,0),
    TRACE("Trace","@magenta",null,100),
    DEBUG("Debug","@cyan",null,200),
    INFO("Info","@yellow",null,300),
    WARNING("Warning","@orange","@orange",400),
    ERROR("Error","@red","@red",500),
    EXCEPTION("Exception","@red","@red",800),
    OFF("Off","@gray",null,1000)
    ;

    private final String name;
    private final String prefixRaw;
    private final String prefix;
    private final String color;
    private final int weight;

    Level(final String name, @NotNull final String prefixColor, @Nullable final String color, final int weight) {
        this.name = name;
        this.prefixRaw = "["+name.toUpperCase()+"]";
        this.prefix = "@bold@gray["+prefixColor+name.toUpperCase()+"@gray]@reset";
        this.color = "@reset" + (color == null ? "@offwhite" : color);
        this.weight = weight;
    }

    /**
     * Gets the weight of the level.
     * <p>
     *     The weight is what decides the order of the levels, and is used
     *     when checking if a log is logged or ignored.
     *     Only logs which level's weight equals or is higher than the current
     *     level's weight will be logged.
     * </p>
     * <b>Default:</b> {@link Level#INFO}
     * @return An Integer value.
     */
    @Contract(pure = true)
    public int getWeight() {
        return weight;
    }

    /**
     * Gets the name of the level.
     * The name defined is the same as the level.
     * @return The name of the level in Sentence case; The first letter capitalized, and the remaining letters in lowercase.
     */
    @Override @Contract(pure = true)
    public @NotNull String toString() {
        return name;
    }

    /**
     * Gets a formatted version of the prefix.<br>
     * <b>Note: The prefix returned will contain color codes
     * that aren't converted to ASCII format yet.</b><br>
     * <b>Example:</b> {@code [@colorLEVEL@reset]}
     * @return The prefix.
     */
    @Contract(pure = true)
    public @NotNull String getPrefix() {
        return prefix;
    }

    /**
     * Gets a clean version of the prefix without any formatting added.<br>
     * <b>Example:</b> {@code [LEVEL]}
     * @return The raw prefix.
     */
    @Contract(pure = true)
    public @NotNull String getRawPrefix() {
        return prefixRaw;
    }

    /**
     * Gets the color of the level.
     * This is used in logs like Error logs and Exception logs,
     * to color the whole log red, to highlight its importance.
     * @return The color as a String.
     */
    @Contract(pure = true)
    public @NotNull String getColor() {
        return color;
    }

    /**
     * Checks if the level of this instance is loggable.
     * @param other The logger's current Log Level.
     * @return True, if the weight of the level provided is higher than this instance's weight,
     * otherwise false.
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Level other) {
        return other.getWeight() <= weight;
    }
}