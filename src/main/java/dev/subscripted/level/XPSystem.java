package dev.subscripted.level;

import dev.subscripted.Main;
import dev.subscripted.database.XPSqlManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class XPSystem extends ListenerAdapter {

    final XPSqlManager xpSqlManager;
    static final Map<Integer, Integer> levelXPRequirements = new HashMap<>();
    final double XP_MIN = 0.5;
    final double XP_MAX = 1.5;


    // Initialisiere XP-Anforderungen beim Laden der Klasse
    static {
        int baseXP = 100;
        int multiplier = 150; // Wachstumskurs für XP-Anforderungen
        for (int level = 1; level <= 50; level++) {
            levelXPRequirements.put(level, baseXP);
            baseXP = (int) (baseXP * (multiplier / 100.0));
        }
    }

    public XPSystem(XPSqlManager xpSqlManager) {
        this.xpSqlManager = xpSqlManager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        User user = event.getAuthor();
        String userId = user.getId();
        Message message = event.getMessage();

        int currentXP = xpSqlManager.getXP(userId);
        int currentLevel = xpSqlManager.getLevel(userId);

        // XP basierend auf der Nachricht berechnen
        int xpGained = calculateMessageXP(message.getContentRaw());
        currentXP += xpGained;

        // Bestimme das neue Level
        int newLevel = calculateLevel(currentXP);

        // Überprüfe, ob ein Level-Up stattgefunden hat
        if (newLevel > currentLevel) {
            sendLevelUpMessage(user, newLevel);
            xpSqlManager.setLevel(userId, newLevel);
        }

        // Aktualisiere den XP-Wert in der Datenbank
        xpSqlManager.setXP(userId, currentXP);
    }

    private int calculateMessageXP(String messageContent) {
        // Berechne XP basierend auf der Nachricht
        int wordCount = messageContent.split("\\s+").length;
        double xp = wordCount * (Math.random() * (XP_MAX - XP_MIN) + XP_MIN);
        return (int) Math.round(xp);
    }

    private int calculateLevel(int xp) {
        // Bestimme das Level basierend auf der XP-Anforderung
        int level = 1;
        for (Map.Entry<Integer, Integer> entry : levelXPRequirements.entrySet()) {
            if (xp >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }
        return level;
    }

    private void sendLevelUpMessage(User user, int newLevel) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Level Up!")
                .setDescription("Herzlichen Glückwunsch! Du bist auf Level " + newLevel + " aufgestiegen!")
                .setColor(Color.MAGENTA)
                .setFooter("XP-System | Update 2024", Main.getJda().getSelfUser().getEffectiveAvatarUrl());

        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embedBuilder.build()).queue());
    }

    public static int getRequiredXP(int level) {
        return levelXPRequirements.getOrDefault(level, 0);
    }
}
