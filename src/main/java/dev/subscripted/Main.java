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
import dev.subscripted.utils.PermissionContainer;
import dev.subscripted.utils.SmartConfig;
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

import java.util.*;

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
        SmartConfig config = new SmartConfig("overloaded.yml");
        conf(config);
        loadToken();
        xpSqlManager = new XPSqlManager();
        giveawayManager = new GiveawayManager();
        giveawayRunnable = new GiveawayRunnable();
        giveawayManager.createTable();


        LogType logTypeLow = LogType.LOG_LOW;
        LogType logTypeMedium = LogType.LOG_MEDIUM;
        LogType logTypeHigh = LogType.LOG_HIGH;

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

    private static void conf(SmartConfig config) {
        String key;
        List<String> placeholderForID = new ArrayList<>();
        List<String> bannedWords = new ArrayList<>();
        List<String> description = new ArrayList<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> field1 = new HashMap<>();
        field1.put("title", "Gesamte Nachricht:");
        field1.put("content", "%messageContent%");
        field1.put("inline", false);
        fields.add(field1);


        placeholderForID.add("<id>");

        bannedWords.add("wixxer");
        bannedWords.add("nigger");
        bannedWords.add("Heil Hitler");

        description.add("line1");
        description.add("line2");
        description.add("line3");
        description.add("line4");


        config = SmartConfig.load("permissions.yml");
        key = "role";
        config.addList("permissions.all." + key, placeholderForID.stream().toList());
        config.addList("permissions.giveaway." + key, placeholderForID.stream().toList());
        config.addList("permissions.ByPassSwears." + key, placeholderForID.stream().toList());

        key = "user";
        config.addList("permissions.all." + key, placeholderForID.stream().toList());
        config.addList("permissions.giveaway." + key, placeholderForID.stream().toList());
        config.addList("permissions.ByPassSwears." + key, placeholderForID.stream().toList());


        config = SmartConfig.load("overloaded.yml");
        config.setString("server.guildID", "0123456789");
        config.setString("server.welcomeChannel", "0123456789");
        config.setString("server.supportWarteraumchannel", "0123456789");
        config.setString("server.commandsChannel", "0123456789");
        config.setString("server.verifyChannel", "0123456789");
        config.addList("server.bannedWords", bannedWords);
        config.setInt("server.swearTimeoutTimeInMin", 30);

        config.setString("roles.communityRole", "0123456789");
        config.setString("roles.verified", "0123456789");


        config.setString("embed.footer", "footer");
        config.setString("embed.welcome.author", "AuthorString");
        config.setString("embed.welcome.title", "Title");
        config.addList("embed.welcome.description", description);
        config.setString("embed.welcome.footer", "footer");
        config.setString("embed.welcome.color", "white #You can use the Java.Color Names or HexColor-Codes");

        config.setString("embed.bannedWords.userDirect.title", "title");
        config.addList("embed.bannedWords.userDirect.description", description);
        config.setString("embed.bannedWords.userDirect.color", "Red");
        config.addList("embed.bannedWords.userDirect.fields", fields);
        config.setString("embed.bannedWords.userDirect.footer", "footer");

        config.setString("embed.bannedWords.serverDirect.title", "title");
        config.addList("embed.bannedWords.serverDirect.description", description);
        config.setString("embed.bannedWords.serverDirect.color", "Red");
        config.addList("embed.bannedWords.serverDirect.fields", fields);
        config.setString("embed.bannedWords.serverDirect.footer", "footer");


        config.setString("admin.logchannel.low", "0123456789");
        config.setString("admin.logchannel.medium", "0123456789");
        config.setString("admin.logchannel.high", "0123456789");
        config.setString("admin.logchannel.middle", "0123456789");

    }
}
