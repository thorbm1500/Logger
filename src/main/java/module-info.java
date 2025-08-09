module dev.prodzeus.logger {
    requires transitive org.slf4j;
    requires net.dv8tion.jda;
    requires org.jetbrains.annotations;

    exports dev.prodzeus.logger;
    exports dev.prodzeus.logger.components;
}