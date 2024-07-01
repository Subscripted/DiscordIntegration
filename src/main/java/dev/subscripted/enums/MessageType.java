package dev.subscripted.enums;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Das `MessageType`-Enum definiert verschiedene Typen von Nachrichtenempfängern
 * und bietet Methoden zum Senden von Nachrichten und Embeds an diese Empfänger.
 */
public enum MessageType {

    /**
     * Der PRIVATE-Typ wird verwendet, um Nachrichten und Embeds an Benutzer per privatem Kanal zu senden.
     */
    PRIVATE {
        @Override
        public void sendMessage(User user, String message) {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(message).queue();
            });
        }

        @Override
        public void sendMessageEmbed(User user, MessageEmbed embed) {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(embed).queue();
            });
        }

        @Override
        public void sendMessage(TextChannel channel, String message) {
            throw new UnsupportedOperationException("Cannot send messages to TextChannels using PRIVATE type.");
        }

        @Override
        public void sendMessageEmbed(TextChannel channel, MessageEmbed embed) {
            throw new UnsupportedOperationException("Cannot send embeds to TextChannels using PRIVATE type.");
        }
    },

    /**
     * Der PUBLIC-Typ wird verwendet, um Nachrichten und Embeds an öffentliche Textkanäle zu senden.
     */
    PUBLIC {
        @Override
        public void sendMessage(User user, String message) {
            throw new UnsupportedOperationException("Cannot send messages to Users using PUBLIC type.");
        }

        @Override
        public void sendMessageEmbed(User user, MessageEmbed embed) {
            throw new UnsupportedOperationException("Cannot send embeds to Users using PUBLIC type.");
        }

        @Override
        public void sendMessage(TextChannel channel, String message) {
            channel.sendMessage(message).queue();
        }

        @Override
        public void sendMessageEmbed(TextChannel channel, MessageEmbed embed) {
            channel.sendMessageEmbeds(embed).queue();
        }
    };

    /**
     * Sendet eine Nachricht an einen Benutzer.
     * @param user Der Benutzer, an den die Nachricht gesendet wird.
     * @param message Die Nachricht, die gesendet wird.
     */
    public abstract void sendMessage(User user, String message);

    /**
     * Sendet ein Embed an einen Benutzer.
     * @param user Der Benutzer, an den das Embed gesendet wird.
     * @param embed Das Embed, das gesendet wird.
     */
    public abstract void sendMessageEmbed(User user, MessageEmbed embed);

    /**
     * Sendet eine Nachricht an einen Textkanal.
     * @param channel Der Textkanal, an den die Nachricht gesendet wird.
     * @param message Die Nachricht, die gesendet wird.
     */
    public abstract void sendMessage(TextChannel channel, String message);

    /**
     * Sendet ein Embed an einen Textkanal.
     * @param channel Der Textkanal, an den das Embed gesendet wird.
     * @param embed Das Embed, das gesendet wird.
     */
    public abstract void sendMessageEmbed(TextChannel channel, MessageEmbed embed);
}
