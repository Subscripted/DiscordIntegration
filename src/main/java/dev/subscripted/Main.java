package dev.subscripted;

import dev.subscripted.database.XPSqlManager;
import dev.subscripted.enums.LogType;
import dev.subscripted.filter.WordFilter;
import dev.subscripted.services.enter.eJoin;
import dev.subscripted.services.giveaway.GiveawayCommand;
import dev.subscripted.services.giveaway.GiveawayManager;
import dev.subscripted.services.giveaway.GiveawayRunnable;
import dev.subscripted.interaction.ButtonIA;
import dev.subscripted.level.XPSystem;
import dev.subscripted.level.XpCommand;
import dev.subscripted.services.ticket.AudioService;
import dev.subscripted.services.verify.VerifyService;
import dev.subscripted.utils.CommandRegistery;
import dev.subscripted.utils.JsonFile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
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


    @Getter
    private static XPSqlManager xpSqlManager;

    @Getter
    private static GiveawayManager giveawayManager;

    @Getter
    private static GiveawayRunnable giveawayRunnable;
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
        xpSqlManager = new XPSqlManager();
        giveawayManager = new GiveawayManager();
        giveawayRunnable = new GiveawayRunnable();
        giveawayManager.createTable();


        LogType logTypeLow = LogType.LOG_LOW;
        LogType logTypeMedium = LogType.LOG_MEDIUM;
        LogType logTypeHigh = LogType.LOG_HIGH;

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
        jda.addEventListener(new XpCommand(xpSqlManager));
        jda.addEventListener(new XPSystem());
        jda.addEventListener(new GiveawayCommand(giveawayRunnable, giveawayManager));
        jda.addEventListener(new ButtonIA(giveawayManager, giveawayRunnable));
        jda.addEventListener(new eJoin());
        jda.addEventListener(new VerifyService());
        jda.addEventListener(new AudioService());


        startGiveawayRunnable();
    }

    /**
     * Lädt den Bot-Token aus der Konfigurationsdatei.
     */
    private static void loadToken() {
        token = jsonFile.loadTokenFile();
    }

    private static void startGiveawayRunnable() {
        GiveawayRunnable giveawayRunnable = new GiveawayRunnable();
        giveawayRunnable.run();
    }
}
