package dev.subscripted;

import dev.subscripted.enums.LogType;
import dev.subscripted.filter.WordFilter;
import dev.subscripted.utils.CommandRegistery;
import dev.subscripted.utils.JsonFile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * Die Hauptklasse des Bots, die die JDA-Instanz initialisiert und den Bot startet.
 * Diese Klasse ist verantwortlich für das Laden des Tokens, die Konfiguration des JDA-Clients
 * und das Hinzufügen von Event-Listenern wie dem `WordFilter`.
 */
public class Main {

    // Ein Objekt von JsonFile zum Laden des Tokens aus einer Konfigurationsdatei
    static JsonFile jsonFile = new JsonFile();

    // Die JDA-Instanz, die den Bot darstellt
    @Getter
    private static JDA jda;

    // Der Token des Bots, geladen aus der Konfigurationsdatei
    @Getter
    private static String token;

    /**
     * Der Einstiegspunkt des Programms. Initialisiert den Bot und startet ihn.
     *
     * @param args Die Argumente der Befehlszeile (werden nicht verwendet).
     */
    @SneakyThrows
    public static void main(String[] args) {
        // Token aus der Konfigurationsdatei laden
        loadToken();

        // Erstellen und Konfigurieren der JDA-Instanz
        jda = JDABuilder.createDefault(getToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setStatus(OnlineStatus.ONLINE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.VOICE_STATE).build().awaitReady();

        // Hinzufügen des WordFilters als Event-Listener
        CommandRegistery.registerCommands();
        jda.addEventListener(new WordFilter(LogType.LOG_MEDIUM));

    }

    /**
     * Lädt den Bot-Token aus der Konfigurationsdatei.
     */
    private static void loadToken() {
        token = jsonFile.loadTokenFile();
    }
}
