package dev.prodzeus.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Level {
    OFF("Off","OFF","@gray",null,0),
    TRACE("Trace","TRACE","@magenta",null,100),
    DEBUG("Debug","DEBUG","@cyan",null,200),
    INFO("Info","INFO","@yellow",null,300),
    WARNING("Warning","WARNING","@orange","@orange",400),
    ERROR("Error","ERROR","@red","@red",500),
    EXCEPTION("Exception","EXCEPTION","@red","@red",800),
    ALL("All","ALL","@white",null,1000)
    ;

    private final String name;
    private final String prefixRaw;
    private final String prefix;
    private final String color;
    private final int weight;

    Level(final String name, final String prefix, @NotNull final String prefixColor, @Nullable final String color, final int weight) {
        this.name = name;
        this.prefixRaw = prefix;
        this.prefix = "@bold@gray["+prefixColor+prefix+"@gray]@reset";
        this.color = "@reset" + (color == null ? "@offwhite" : color);
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getRawPrefix() {
        return prefixRaw;
    }

    public String getColor() {
        return color;
    }

    /**
     * Checks if the level of this instance is loggable.
     * @param o The logger's current Log Level.
     * @return True, if the weight of the level provided is higher than this instance's weight,
     * otherwise false.
     */
    public boolean isLoggable(@NotNull final Level o) {
        return o.getWeight() <= weight;
    }
}