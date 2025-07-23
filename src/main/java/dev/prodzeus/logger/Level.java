package dev.prodzeus.logger;

public enum Level implements Comparable<Level> {
    OFF("Off","[OFF]",0),
    TRACE("Debug","[DEBUG]",100),
    DEBUG("Debug","[DEBUG]",200),
    INFO("Info","[INFO]",300),
    WARNING("Warning","[WARNING]",400),
    ERROR("Error","[ERROR]",500),
    ALL("All","[ALL]",1000)
    ;

    private final String name;
    private final String prefix;
    private final int weight;

    Level(final String name, final String prefix, final int weight) {
        this.name = name;
        this.prefix = prefix;
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

    public int compare(final Level o) {
        return this.weight >= o.getWeight() ? (this.weight > o.getWeight() ? -1 : 0) : 1;
    }
}