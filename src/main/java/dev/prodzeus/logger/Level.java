package dev.prodzeus.logger;

public enum Level implements Comparable<Level> {
    OFF("Off","[OFF]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;7mOFF\u001b[38;5;240m]\u001b[0m","\u001b[38;5;15m",0),
    TRACE("Trace","[TRACE]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;165mTRACE\u001b[38;5;240m]\u001b[0m","\u001b[64;64;64m",100),
    DEBUG("Debug","[DEBUG]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;87mDEBUG\u001b[38;5;240m]\u001b[0m","\u001b[64;64;64m",200),
    INFO("Info","[INFO]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;226mINFO\u001b[38;5;240m]\u001b[0m","\u001b[38;5;231m",300),
    WARNING("Warning","[WARNING]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;202mWARNING\u001b[38;5;240m]\u001b[0m","\u001b[38;5;208m",400),
    ERROR("Error","[ERROR]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;196mERROR\u001b[38;5;240m]\u001b[0m","\u001b[1m\u001b[38;5;196m",500),
    ALL("All","[ALL]","\u001b[1m\u001b[38;5;240m[\u001b[38;5;15mALL\u001b[38;5;240m]\u001b[0m","\u001b[38;5;15m",1000)
    ;

    private final String name;
    private final String rawPrefix;
    private final String prefix;
    private final String color;
    private final int weight;

    Level(final String name, final String rawPrefix, final String prefix, final String color, final int weight) {
        this.name = name;
        this.rawPrefix = rawPrefix;
        this.prefix = prefix;
        this.color = color;
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
        return rawPrefix;
    }

    public String getColor() {
        return color;
    }
}