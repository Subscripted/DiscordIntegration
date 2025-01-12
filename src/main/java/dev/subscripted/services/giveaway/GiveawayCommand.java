package dev.subscripted.services.giveaway;

import dev.subscripted.Main;
import dev.subscripted.enums.EmbedType;
import dev.subscripted.enums.MessageType;
import dev.subscripted.utils.PermissionContainer;
import dev.subscripted.utils.SmartConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Clock;
import java.time.OffsetDateTime;

public class GiveawayCommand extends ListenerAdapter {

    final GiveawayRunnable runnable;
    final GiveawayManager manager;

    public GiveawayCommand(GiveawayRunnable runnable, GiveawayManager manager) {
        this.runnable = runnable;
        this.manager = manager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("giveaway"))
            return;

        Member member = event.getMember();
        assert member != null;
        SmartConfig c = SmartConfig.load("overloaded.yml");
        SmartConfig p = SmartConfig.load("permissions.yml");
        PermissionContainer container = new PermissionContainer(p);

        if (!container.hasPermission(member, "giveaway")) {
            MessageType.REPLY.sendMessageEmbed(event, EmbedType.NO_PERMISSION.getEmbedBuilder().build(), true);
            return;
        }

        Guild guild = event.getGuild();
        assert guild != null;

        OptionMapping optionMappingPrize = event.getOption("preis");
        OptionMapping optionMappingWinners = event.getOption("gewinner");
        OptionMapping optionMappingDuration = event.getOption("dauer");
        assert optionMappingPrize != null;
        assert optionMappingWinners != null;
        assert optionMappingDuration != null;

        String prize = optionMappingPrize.getAsString();
        int winners = optionMappingWinners.getAsInt();
        int duration = (int) parseDuration(optionMappingDuration.getAsString());

        long endTime = (System.currentTimeMillis() / 1000 + duration);
        event.reply(guild.getRoleById(c.getString("roles.communityRole")).getAsMention()).addEmbeds(
                        new EmbedBuilder()
                                .setTitle("<:event:1260135620420833310> Giveaway <:event:1260135620420833310>")
                                .setFooter("Novibes Giveway | Update 2023 ©", Main.getJda().getSelfUser().getEffectiveAvatarUrl())
                                .setColor(Color.MAGENTA)
                                .setThumbnail(Main.getJda().getSelfUser().getAvatarUrl())
                                .setDescription("- Gestartet von: " + member.getAsMention() + "\n" + "- Was wird verlost: **" + prize + "** \n" + "- Restzeit: <t:" + (endTime) + ":R>\n" + "- Teilnehmer: -")
                                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                                .build())
                .addActionRow(Button.primary("giveaway_join", Emoji.fromFormatted("<:event:1260135620420833310>")))
                .complete().retrieveOriginal().queue(message -> {
                    manager.create(member, prize, winners, endTime, message.getChannel().getId(), message.getId());
                    runnable.updateRunnable();
                });
    }

    private long parseDuration(String durationInput) {
        long durationInSeconds = 0;
        String[] parts = durationInput.split("\\s+");
        for (String part : parts) {
            part = part.trim();
            char unit = part.charAt(part.length() - 1);
            int value = Integer.parseInt(part.substring(0, part.length() - 1));
            switch (unit) {
                case 'd':
                    durationInSeconds += value * 24 * 60 * 60;
                    break;
                case 'h':
                    durationInSeconds += value * 60 * 60;
                    break;
                case 'm':
                    durationInSeconds += value * 60;
                    break;
                case 's':
                    durationInSeconds += value;
                    break;
                default:
                    throw new IllegalArgumentException("Ungültige Einheit: " + unit);
            }
        }
        return durationInSeconds;
    }
}
