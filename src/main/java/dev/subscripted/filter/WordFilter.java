package dev.subscripted.filter;

import dev.subscripted.Main;
import dev.subscripted.enums.LogType;
import dev.subscripted.enums.MessageType;
import dev.subscripted.utils.PermissionContainer;
import dev.subscripted.utils.SmartConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MemberAction;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WordFilter extends ListenerAdapter {


    final LogType logType;

    private SmartConfig c = SmartConfig.load("overloaded.yml");
    private SmartConfig p = SmartConfig.load("permissions.yml");
    PermissionContainer container = new PermissionContainer(p);


    List<String> BAD_WORDS = (List<String>) c.getList("server.bannedWords");


    public WordFilter(LogType logType) {
        this.logType = logType;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor() == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        if (container.hasPermission(member, "ByPassSwears")) {
            return;
        }

        String messageContent = event.getMessage().getContentRaw().toLowerCase();

        List<Map<String, Object>> fields = (List<Map<String, Object>>) c.getList("embed.bannedWords.userDirect.fields");
        List<String> triggeredBadWords = BAD_WORDS.stream()
                .filter(badWord -> {
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(badWord) + "\\b");
                    return pattern.matcher(messageContent).find();
                })
                .toList();

        if (!triggeredBadWords.isEmpty()) {


            Guild guild = Main.getJda().getGuildById(c.getString("server.guildID"));

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%NickName%", member.getNickname());
            placeholders.put("%name%", member.getEffectiveName());
            placeholders.put("%NamePing%", member.getAsMention());
            placeholders.put("%messageContent%", messageContent);
            placeholders.put("%word%", triggeredBadWords.get(0));
            placeholders.put("%server%", event.getGuild().getName());

            List<String> descriptionLines;

            descriptionLines = c.getList("embed.bannedWords.userDirect.description").stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            String description;

            description = c.makeDescription(descriptionLines, placeholders);
            EmbedBuilder dmembed = new EmbedBuilder()
                    .setTitle(c.getString("embed.bannedWords.userDirect.title").replace("%server%", event.getGuild().getName()))
                    .setColor(c.getColorFromConfig(c.getString("embed.bannedWords.userDirect.color")))
                    .setDescription(description)
                    .setFooter(c.getString("embed.bannedWords.userDirect.footer").replace("%server%", event.getGuild().getName()), member.getEffectiveAvatarUrl());

            c.addFieldsToEmbed(dmembed, fields, placeholders);

            descriptionLines = c.getList("embed.bannedWords.serverDirect.description").stream()
                    .map(Objects::toString)
                    .collect(Collectors.toList());

            description = c.makeDescription(descriptionLines, placeholders);

            EmbedBuilder channelem = new EmbedBuilder()
                    .setTitle(c.getString("embed.bannedWords.serverDirect.title").replace("%server%", event.getGuild().getName()))
                    .setColor(c.getColorFromConfig(c.getString("embed.bannedWords.serverDirect.color")))
                    .setDescription(description)
                    .setFooter(c.makeFooter(c.getString("embed.bannedWords.serverDirect.footer"), placeholders), member.getEffectiveAvatarUrl());

            c.addFieldsToEmbed(channelem, fields, placeholders);

            event.getMessage().delete().queue();

            MessageType.PRIVATE.sendMessageEmbed(event.getAuthor(), dmembed.build());

            TextChannel channel = logType.getLogChannelByType(LogType.LOG_MEDIUM);

            if (channel != null) {
                MessageType.PUBLIC.sendMessageEmbed(channel, channelem.build());
            }
            event.getGuild().timeoutFor(member, Duration.ofMinutes(c.getInt("server.swearTimeoutTimeInMin"))).queue(
                    success -> System.out.println("User " + member.getEffectiveName() + " has been timed out."),
                    error -> System.err.println("Failed to timeout user " + member.getEffectiveName() + ": " + error.getMessage())
            );
        }
    }
}
