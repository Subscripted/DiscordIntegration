package dev.subscripted.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EmbedType {

    NO_PERMISSION(new EmbedBuilder()
            .setTitle("Fehlende Berechtigung")
            .setDescription("Du hast keine Berechtigung, diese Aktion auszuführen.")
            .setColor(Color.RED)
            .setFooter("Novibes System | Update 2023 ©")
            .setTimestamp(OffsetDateTime.now())),

    VERIFY(new EmbedBuilder()
            .setTitle("Verify | Discord")
            .setDescription("Click the button below to verify your account. As part of our security measures, we may retain some personal data related to your account.")
            .setColor(Color.BLACK)),
    IS_VERIFIED(new EmbedBuilder()
            .setTitle("Verify | Discord")
            .setColor(Color.YELLOW)
            .setDescription("You have already verified yourself, further verification is not necessary.")),
    SUCCESSFULL_VERIFIED(new EmbedBuilder()
            .setTitle("Verify | Discord")
            .setColor(Color.GREEN)
            .setDescription("You have successfully completed verification. Only the essential data related to your account will be retained.")),
    NO_INTERACTION(new EmbedBuilder()
            .setColor(Color.RED)
            .setDescription("Your given Interaction was declared as null!"));


    @Getter
    final EmbedBuilder embedBuilder;

    EmbedType(EmbedBuilder embedBuilder) {
        this.embedBuilder = embedBuilder;
    }

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }
}
