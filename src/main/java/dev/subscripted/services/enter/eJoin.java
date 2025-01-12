package dev.subscripted.services.enter;

import dev.subscripted.Main;
import dev.subscripted.enums.MessageType;
import dev.subscripted.utils.SmartConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hyperic.sigar.Mem;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class eJoin extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        SmartConfig c = SmartConfig.load("overloaded.yml");
        EmbedBuilder embedBuilder;
        Member member = event.getMember();
        TextChannel channel = event.getGuild().getTextChannelById(c.getString("server.welcomeChannel"));
        Guild guild = event.getGuild();
        Role role = guild.getRoleById(c.getString("roles.communityRole"));
        String servername = guild.getName();
        String username = member.getUser().getName();
        Member guildMember = guild.getMember(member);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime adjustDateTime = now.plusHours(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'");
        String formattedDateTime = adjustDateTime.format(formatter);


        embedBuilder = new EmbedBuilder()
                .setTitle(c.getString("embed.welcome.title"))
                .setAuthor(c.getString("embed.welcome.author"))
                .setColor(c.getColorFromConfig(c.getString("embed.welcome.color")))
                .setDescription(
                        String.join("\n", c.getList("embed.welcome.description").stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList()))
                                .replace("%username%", username)
                                .replace("%servername%", servername)
                )
                .setFooter(c.getString("embed.welcome.footer"), Main.getJda().getSelfUser().getAvatarUrl())
                .setThumbnail(member.getEffectiveAvatarUrl());

        MessageType.PUBLIC.sendMessageEmbed(channel, embedBuilder.build());
        assert guildMember != null;
        assert role != null;
        guild.addRoleToMember(guildMember, role);

    }
}
