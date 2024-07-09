package dev.subscripted.interaction;

import dev.subscripted.Main;
import dev.subscripted.enums.EmbedType;
import dev.subscripted.enums.MessageType;
import dev.subscripted.services.giveaway.Giveaway;
import dev.subscripted.services.giveaway.GiveawayManager;
import dev.subscripted.services.giveaway.GiveawayRunnable;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ButtonIA extends ListenerAdapter {

    final GiveawayManager giveawayManager;
    final GiveawayRunnable runnable;

    public ButtonIA(GiveawayManager giveawayManager, GiveawayRunnable runnable) {
        this.giveawayManager = giveawayManager;
        this.runnable = runnable;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        Role verified = guild.getRoleById("1102191280106246214");
        Member member = event.getMember();
        Button button = event.getButton();
        TextChannel channel = event.getChannel().asTextChannel();
        String userId = event.getUser().getId();
        EmbedBuilder embedBuilder;

        switch (button.getId()) {
            case "giveaway_join":
                if (member == null) {
                    return;
                }
                String messageId = event.getMessageId();
                Giveaway giveaway = giveawayManager.getGiveaway(messageId);

                if (!giveawayManager.isInGiveaway(giveaway, member.getId())) {
                    giveaway.getUsers().add(member.getId());
                    giveawayManager.updateGiveaway(giveaway);
                    runnable.updateRunnable();
                    giveawayManager.updateEmbed(giveaway);
                    event.reply("Du nimmst nun an diesem Giveaway teil!").setEphemeral(true).queue();
                } else {
                    embedBuilder = new EmbedBuilder()
                            .setTitle("Varilx Giveaway")
                            .setColor(Color.MAGENTA)
                            .setDescription("Du bist bereits ein Teilnehmer bei diesem Giveaway")
                            .setFooter("Novibes Giveaway Feature | Update 2023 ©", Main.getJda().getSelfUser().getAvatarUrl());
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                }
                break;

            case "verify":
                if (!isVerified(member)) {
                    guild.addRoleToMember(member, verified);
                    MessageType.REPLY.sendMessageEmbed(event, EmbedType.SUCCESSFULL_VERIFIED.getEmbedBuilder().build(), true);
                } else {
                    MessageType.REPLY.sendMessageEmbed(event, EmbedType.IS_VERIFIED.getEmbedBuilder().build(), true);
                }
                break;
        }
    }


    private static boolean isVerified(Member user) {
        Guild guild = Main.getJda().getGuildById("1102188267513843782");
        Role role = guild.getRoleById("1102191280106246214");
        return user.getRoles().contains(role);
    }
}
