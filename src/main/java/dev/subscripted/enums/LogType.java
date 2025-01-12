package dev.subscripted.enums;

import dev.subscripted.Main;
import dev.subscripted.utils.SmartConfig;
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
    LOG_LOW("admin.logchannel.low"),

    /**
     * Log-Typ für mittlere Priorität (z.B. allgemeine Warnungen).
     */
    LOG_MEDIUM("admin.logchannel.medium"),

    /**
     * Log-Typ für hohe Priorität (z.B. kritische Fehler).
     */
    LOG_HIGH("admin.logchannel.high");

    @Getter
    final String channelID;

    /**
     * Konstruktor für das `LogType`-Enum.
     * Liest die Kanal-ID aus der Konfiguration.
     *
     * @param configPath Der Pfad in der Konfigurationsdatei, der diesem Log-Typ zugeordnet ist.
     */
    LogType(String configPath) {
        SmartConfig c = SmartConfig.load("overloaded.yml");

        // Lade die Kanal-ID aus der Konfiguration
        String loadedChannelID = c.getString(configPath);

        // Fallback auf eine Standard-Kanal-ID, falls die Konfiguration fehlt
        this.channelID = (loadedChannelID != null && !loadedChannelID.isEmpty())
                ? loadedChannelID
                : "default-channel-id"; // Ersetze durch eine sinnvolle Standard-ID
    }

    /**
     * Holt den Log-Kanal basierend auf dem Log-Typ.
     *
     * @param logType Der Log-Typ, für den der Kanal abgerufen werden soll.
     * @return Der Textkanal, der dem Log-Typ entspricht.
     */
    public TextChannel getLogChannelByType(LogType logType) {
        return Main.getJda().getTextChannelById(logType.getChannelID());
    }
}
