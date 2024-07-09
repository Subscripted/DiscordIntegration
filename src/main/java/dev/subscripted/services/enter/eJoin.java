package dev.subscripted.services.enter;

import dev.subscripted.Main;
import dev.subscripted.enums.MessageType;
import dev.subscripted.services.entity.GuildMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class eJoin extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        EmbedBuilder embedBuilder;

        Member member = event.getMember();
        if (member != null) {

            TextChannel channel = event.getGuild().getTextChannelById("1102195680665153596");

            GuildMember guildMember = GuildMember.get(member);
            String userId = guildMember.getId();
            String username = guildMember.getUsername();
            Guild guild = event.getGuild();
            Role role = guild.getRoleById("1102191280106246214");
            String servername = guild.getName();
            System.out.println("Neues Mitglied: " + username + " (ID: " + userId + ")");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime adjustDateTime = now.plusHours(2);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'");
            String formattedDateTime = adjustDateTime.format(formatter);


            embedBuilder = new EmbedBuilder()
                    .setAuthor("Novibes | Bot")
                    .setDescription(("Hey **%username%**, wilkommen auf \n " +
                            "**%servername%**\n").replace("%username%", username).replace("%servername%", servername) +
                            " \n" +
                            "Ich bitte dich das Regelwerk durchzulesen,\n" +
                            "damit keine Unannehmlichkeiten entstehen.\n" +
                            "Ich danke dir und hab viel Spaß auf unserem Server! \n")
                    .setFooter("Gesendet am " + formattedDateTime, Main.getJda().getSelfUser().getAvatarUrl())
                    .setThumbnail(member.getEffectiveAvatarUrl());

            MessageType.PUBLIC.sendMessageEmbed(channel, embedBuilder.build());
            guild.addRoleToMember(guildMember.getMember(), role);

        }
    }
}
