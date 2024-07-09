package dev.subscripted.filter;

import dev.subscripted.enums.LogType;
import dev.subscripted.enums.MessageType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MemberAction;

import java.awt.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Die Klasse `WordFilter` ist ein Listener, der Nachrichten auf unerwünschte Wörter überprüft.
 * Wenn eine Nachricht ein unerwünschtes Wort enthält, werden Benachrichtigungen an den Benutzer und
 * an einen spezifischen Textkanal gesendet.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WordFilter extends ListenerAdapter {

    /**
     * Der Log-Typ bestimmt, an welchen Kanal die Nachrichtenprotokolle gesendet werden.
     */
    final LogType logType;

    /**
     * Eine Liste von unerwünschten Wörtern, die in Nachrichten gefiltert werden.
     */
    final List<String> BAD_WORDS = Arrays.asList(
            "Assi", "NIGGER", "nigger", "Nigger", "Neger", "NEGER", "neger", "Heil Hitler",
            "Arschloch", "arschloch", "Arsch", "arsch", "Bastard", "bastard", "Bummsfehler",
            "bummsfehler", "Ballsack", "ballsack", "Dildo", "dildo", "Fuck You", "fuck You",
            "FuckYou", "fuckYou", "Giganigga", "giganigga", "Geplatzteskondom", "geplatzteskondom",
            "Hure", "hure", "Hurensohn", "Mongolischeraffenzchtverein", "mongolischeraffenzchtverein",
            "nigger", "NIGGER", "Nigger", "Niger", "NIGER", "dreckssau", "für AFD", "Karbonaterol",
            "hurensohn", "Karbonat erol", "karbonat erol", "Leck mich", "leck mich", "Opfer",
            "opfer", "Titten", "titten", "Wixe", "wixe", "Wixer", "wixer", "wixxer", "Wixxer",
            "WIXXER", "sex", "SEX", "Sex", "✡", "☭", "✯", "☮", "Ⓐ", "卐", "卍", "✙", "ᛋᛋ", "ꖦ"
    );

    /**
     * Konstruktor für die `WordFilter`-Klasse.
     *
     * @param logType Der Log-Typ, der bestimmt, wohin die Logs gesendet werden.
     */
    public WordFilter(LogType logType) {
        this.logType = logType;
    }

    /**
     * Wird aufgerufen, wenn eine Nachricht empfangen wird.
     * Überprüft die Nachricht auf unerwünschte Wörter und sendet entsprechende Warnungen.
     *
     * @param event Das Ereignis, das die empfangene Nachricht enthält.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Überprüfen, ob der Autor der Nachricht null ist
        if (event.getAuthor() == null)
            return;

        // Überprüfen, ob das Mitglied null ist
        Member member = event.getMember();
        if (member == null)
            return;

        // Überprüfen, ob das Mitglied Administratorrechte hat
        if (member.hasPermission(Permission.ADMINISTRATOR))
            return;

        // Nachrichtentext in Kleinbuchstaben umwandeln
        String messageContent = event.getMessage().getContentRaw().toLowerCase();

        // Überprüfen, ob die Nachricht eines der schlechten Wörter enthält
        List<String> triggeredBadWords = BAD_WORDS.stream()
                .filter(badWord -> {
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(badWord) + "\\b");
                    return pattern.matcher(messageContent).find();
                })
                .collect(Collectors.toList());

        // Wenn ein schlechtes Wort gefunden wurde
        if (!triggeredBadWords.isEmpty()) {
            // Erstellen einer Embed-Nachricht für die private Benachrichtigung des Benutzers
            EmbedBuilder dmembed = new EmbedBuilder()
                    .setTitle("Novibes Warnung!")
                    .setColor(Color.RED)
                    .setDescription("Deine letzte Nachricht auf **" + event.getGuild().getName() + "** enthielt dieses ungewollte Wort: **" + triggeredBadWords.get(0) + "**")
                    .addField("Gesamte Nachricht: ", " " + messageContent, false)
                    .addField("Weiteres: ", "Diese Nachricht wird geloggt und kann später gegen dich verwendet werden!", false)
                    .setFooter("Novibes Safety Feature | Update 2023 © ", null);

            // Erstellen einer Embed-Nachricht für die öffentliche Benachrichtigung im Log-Kanal
            EmbedBuilder channelem = new EmbedBuilder()
                    .setTitle("Novibes Warnung!")
                    .setColor(Color.RED)
                    .setDescription(event.getMember().getAsMention() + " letzte Nachricht enthielt dieses ungewollte Wort: **" + triggeredBadWords.get(0) + "**")
                    .addField("Gesamte Nachricht: ", " " + messageContent, false)
                    .setFooter("Novibes Safety Feature | Update 2023 © ", null);

            // Löschen der Nachricht
            event.getMessage().delete().queue();

            // Senden der Embed-Nachricht an den Benutzer im privaten Kanal
            MessageType.PRIVATE.sendMessageEmbed(event.getAuthor(), dmembed.build());

            // Holen des Log-Kanals basierend auf dem Log-Typ
            TextChannel channel = logType.getLogChannelByType(LogType.LOG_MEDIUM);

            // Wenn der Log-Kanal vorhanden ist, senden der Embed-Nachricht an den Kanal
            if (channel != null) {
                MessageType.PUBLIC.sendMessageEmbed(channel, channelem.build(), null);
            }

            // Timeout des Benutzers für eine bestimmte Dauer (z.B. 10 Minuten)
            event.getGuild().timeoutFor(member, Duration.ofMinutes(10)).queue(
                    success -> System.out.println("User " + member.getEffectiveName() + " has been timed out."),
                    error -> System.err.println("Failed to timeout user " + member.getEffectiveName() + ": " + error.getMessage())
            );
        }
    }
}
