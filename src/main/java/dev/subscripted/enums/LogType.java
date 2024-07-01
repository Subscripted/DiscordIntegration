package dev.subscripted.enums;

import dev.subscripted.Main;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Das `LogType`-Enum definiert verschiedene Typen von Log-Kanälen.
 * Es enthält Methoden zum Abrufen eines Textkanals basierend auf dem Log-Typ.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum LogType {

    /**
     * Log-Typ für niedrige Priorität (z.B. Debugging-Informationen).
     */
    LOG_LOW("123456789012345678"),

    /**
     * Log-Typ für mittlere Priorität (z.B. allgemeine Warnungen).
     */
    LOG_MEDIUM("1257416818809896980"),

    /**
     * Log-Typ für hohe Priorität (z.B. kritische Fehler).
     */
    LOG_HIGH("987654321098765432");

    @Getter
    final String channelID;

    /**
     * Konstruktor für das `LogType`-Enum.
     * @param channelID Die ID des Textkanals, der diesem Log-Typ zugeordnet ist.
     */
    LogType(String channelID) {
        this.channelID = channelID;
    }

    /**
     * Holt den Log-Kanal basierend auf dem Log-Typ.
     * @param logType Der Log-Typ, für den der Kanal abgerufen werden soll.
     * @return Der Textkanal, der dem Log-Typ entspricht.
     */
    public TextChannel getLogChannelByType(LogType logType) {
        return Main.getJda().getTextChannelById(logType.getChannelID());
    }
}
