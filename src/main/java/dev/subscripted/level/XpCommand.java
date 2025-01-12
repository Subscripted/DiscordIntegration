package dev.subscripted.level;

import dev.subscripted.Main;
import dev.subscripted.database.XPSqlManager;
import dev.subscripted.enums.MessageType;
import dev.subscripted.utils.SmartConfig;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

/**
 * Die `XpCommand`-Klasse ist ein Listener, der auf Slash-Befehle reagiert und
 * Informationen über XP (Erfahrungspunkte) und Level von Benutzern im Discord-Server bereitstellt.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XpCommand extends ListenerAdapter {

    final XPSqlManager xpSqlManager; // Manager zur Verwaltung der XP-Datenbank
    EmbedBuilder embedBuilder; // Builder für Embed-Nachrichten
    SmartConfig c = SmartConfig.load("overloaded.yml");

    /**
     * Konstruktor zur Initialisierung des XP-Datenbankmanagers.
     *
     * @param xpSqlManager Der Manager für XP-Datenbankoperationen.
     */
    public XpCommand(XPSqlManager xpSqlManager) {
        this.xpSqlManager = xpSqlManager;
    }

    /**
     * Methode, die auf Slash-Befehle reagiert.
     *
     * @param event Das Event, das den Slash-Befehl repräsentiert.
     */
    @SneakyThrows
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Überprüft, ob der Befehl "level" lautet.
        if (!event.getName().equals("level")) {
            return;
        }


        // Erstellt ein Standard-Embed für die Antwort.
        embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Novibes System")
                .setColor(Color.RED)
                .setDescription("Du kannst Befehle nur im <#" + c.getString("server.commandsChannel") + "> Channel ausführen!")
                .setFooter(c.getString("embed.footer"), Main.getJda().getSelfUser().getAvatarUrl());

        // Überprüft, ob der Befehl im richtigen Kanal ausgeführt wird.
        if (!event.getChannelId().equals(c.getString("server.commandsChannel"))) {
            MessageType.REPLY.sendMessageEmbed(event, embedBuilder.build(), true);
            return;
        }

        Member user = event.getMember();
        Member target = event.getOption("nutzer").getAsMember();

        String userId = user.getId();
        String targetId = target.getId();

        // Holt die XP- und Level-Daten des Zielbenutzers.
        int xp = xpSqlManager.getXP(targetId);
        int level = xpSqlManager.getLevel(targetId);
        int requiredXP = calculateRequiredXPForLevel(level + 1);
        int xpProgress = xp - calculateRequiredXPForLevel(level);

        System.out.println(xp + " / " + XPSystem.getRequiredXP(level));

        // Erstellt einen Titel für das Embed und kürzt ihn, falls er zu lang ist.
        String title = "Overloaded.de";
        if (title.length() > 256) {
            title = title.substring(0, 253) + "..."; // Kürzen auf 256 Zeichen mit Ellipsis
        }

        // Erstellt ein neues Embed mit den XP-Informationen des Benutzers.
        embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.MAGENTA)
                .setDescription(buildXpDescription(user, target, xp, level))
                .setFooter(c.getString("embed.footer"), Main.getJda().getSelfUser().getEffectiveAvatarUrl());

        // Antwortet mit dem erstellten Embed.
        if (user == target) {
            MessageType.REPLY.sendMessageEmbed(event, embedBuilder.build(), false);
        } else {
            embedBuilder.setAuthor(target.getEffectiveName());
            MessageType.REPLY.sendMessageEmbed(event, embedBuilder.build(), false);
        }
    }

    /**
     * Berechnet die benötigte XP für das nächste Level.
     *
     * @param level Das nächste Level.
     * @return Die benötigte XP für das nächste Level.
     */
    private int calculateRequiredXPForLevel(int level) {
        return 100 * level;
    }

    /**
     * Baut die XP-Beschreibung für das Embed.
     *
     * @param user   Der aktuelle Benutzer.
     * @param target Der Zielbenutzer, dessen XP angezeigt werden.
     * @param xp     Die aktuelle XP des Zielbenutzers.
     * @param level  Das aktuelle Level des Zielbenutzers.
     * @return Eine formatierte Beschreibung der XP und des Levels.
     */
    private String buildXpDescription(Member user, Member target, int xp, int level) {
        if (user == target) {
            return String.format("> - Deine XP: %d\n"
                            + "> - Dein Level: %d\n"
                            + "> - Dein Fortschritt: %s / %d%%",
                    xp, level, printUserXPState(xp, XPSystem.getRequiredXP(level + 1)),
                    Math.round(((double) xp / XPSystem.getRequiredXP(level + 1)) * 100)
            );
        } else {
            return String.format(
                    "> - XP von %s: %d\n"
                            + "> - Level von %s: %d\n"
                            + "> - Fortschritt: %s / %d%%",
                    target.getAsMention(), xp, target.getAsMention(), level,
                    printUserXPState(xp, XPSystem.getRequiredXP(level + 1)),
                    Math.round(((double) xp / XPSystem.getRequiredXP(level + 1)) * 100)
            );
        }
    }

    /**
     * Erstellt eine visuelle Darstellung des XP-Fortschritts als String.
     *
     * @param exp    Die aktuelle XP des Benutzers.
     * @param expMax Die maximale XP, die für das nächste Level benötigt wird.
     * @return Eine visuelle Darstellung des XP-Fortschritts.
     */
    public static String printUserXPState(int exp, int expMax) {
        int maxLength = 10;

        int reachedXP = (int) Math.ceil(((double) exp / expMax) * maxLength);
        reachedXP = Math.min(reachedXP, maxLength);

        int stillRequiredXP = maxLength - reachedXP;

        StringBuilder stateBar = new StringBuilder();
        for (int i = 0; i < reachedXP; i++) {
            stateBar.append("<:xpFull:1327591763304644680>");
        }
        for (int i = 0; i < stillRequiredXP; i++) {
            stateBar.append("<:xpempty:1327591762050678794>");
        }
        return stateBar.toString();
    }
}
