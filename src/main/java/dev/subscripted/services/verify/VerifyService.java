package dev.subscripted.services.verify;

import dev.subscripted.Main;
import dev.subscripted.enums.EmbedType;
import dev.subscripted.enums.MessageType;
import dev.subscripted.services.entity.GuildMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class VerifyService extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        EmbedBuilder embedBuilder;
        Message message = event.getMessage();
        Member member = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();
        GuildMember guildMember = GuildMember.get(member);

        if (!message.getContentRaw().equals("4dkW7dwa74iko48/222u37Jws_")) {
            return;
        }

        if (!guildMember.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            MessageType.PUBLIC.sendMessageEmbed(channel, EmbedType.NO_PERMISSION.getEmbedBuilder().build());
            return;
        }

        Button button = Button.danger("verify", "Verify");
        MessageType.PUBLIC.sendMessageEmbed(channel, EmbedType.VERIFY.getEmbedBuilder().build(), button);
    }
}
