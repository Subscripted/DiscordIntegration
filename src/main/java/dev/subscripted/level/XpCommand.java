package dev.subscripted.level;


import dev.subscripted.Main;
import dev.subscripted.database.XPSqlManager;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class XpCommand extends ListenerAdapter {

    final XPSqlManager xpSqlManager;

    public XpCommand(XPSqlManager xpSqlManager) {
        this.xpSqlManager = xpSqlManager;
    }

    @SneakyThrows
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("level"))
            return;

        Member user = event.getMember();
        Member target = event.getOption("nutzer").getAsMember();

        String userId = user.getId();
        String targetId = target.getId();

        int xp = xpSqlManager.getXP(targetId);
        int level = xpSqlManager.getLevel(targetId);
        int requiredXP = calculateRequiredXPForLevel(level + 1);
        int xpProgress = xp - calculateRequiredXPForLevel(level);

        System.out.println(xp + " / " + XPSystem.getRequiredXP(level));

        EmbedBuilder embedBuilder;
        if (user == target) {
            embedBuilder = new EmbedBuilder()
                    .setTitle("Novibes XP")
                    .setColor(Color.GREEN)
                    .setDescription("%placeholder%%placeholder%%placeholder%%placeholder%\n"
                            + "- Deine XP: " + xp + "\n"
                            + "- Dein Level: " + level + "\n"
                            + "- Dein Fortschritt: " + printUserXPState(xp, XPSystem.getRequiredXP(level + 1)) + " / " + Math.round(((double) xp / XPSystem.getRequiredXP(level + 1)) * 100) * 1 + "%")
                    .setFooter("Novibes XP Feature | Update 2023 ©", Main.getJda().getSelfUser().getEffectiveAvatarUrl());
            event.replyEmbeds(embedBuilder.build()).queue();
        } else {
            embedBuilder = new EmbedBuilder()
                    .setTitle("Novibes XP")
                    .setColor(Color.MAGENTA)
                    .setDescription("- XP von " + target.getAsMention() + ": " + xp + "\n"
                            + "- Level von " + target.getAsMention() + ": " + level + "\n"
                            + "- Vortschritt : " + printUserXPState(xp, XPSystem.getRequiredXP(level + 1)) + " / " + Math.round(((double) xp / XPSystem.getRequiredXP(level + 1)) * 100) * 1 + "%")
                    .setFooter("Novibes XP Feature | Update 2023 ©", Main.getJda().getSelfUser().getEffectiveAvatarUrl())
                    .setAuthor(target.getEffectiveName());
            event.replyEmbeds(embedBuilder.build()).queue();
        }
    }

    private int calculateRequiredXPForLevel(int level) {
        return 100 * level;
    }


    public static String printUserXPState(int exp, int expMax) {
        int maxLength = 10;

        int reachedXP = (int) Math.ceil(((double) exp / expMax) * maxLength);
        reachedXP = Math.min(reachedXP, maxLength);

        int stillRequiredXP = maxLength - reachedXP;

        StringBuilder stateBar = new StringBuilder();
        for (int i = 0; i < reachedXP; i++) {
            stateBar.append("🟩");
        }
        for (int i = 0; i < stillRequiredXP; i++) {
            stateBar.append(" ◾");
        }

        return stateBar.toString();
    }
}

