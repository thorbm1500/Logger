package dev.prodzeus;

public enum Level {
    OFF("Off","[OFF]",0),
    DEBUG("Debug","[DEBUG]",100),
    INFO("Info","[INFO]",300),
    WARNING("Warning","[WARNING]",400),
    ERROR("Error","[ERROR]",500),
    SEVERE("Severe","[SEVERE]", 800),
    FATAL("Fatal","[FATAL]", 900),
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
}