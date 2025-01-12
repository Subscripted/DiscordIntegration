package dev.subscripted.utils;

import dev.subscripted.Main;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandRegistery {

    @SneakyThrows
    public static void registerCommands() {

        SmartConfig c = SmartConfig.load("overloaded.yml");
        Guild guild = Main.getJda().getGuildById(c.getString("server.guildID"));
        System.out.println(guild);
        assert guild != null;
        guild.updateCommands().addCommands(
                Commands.slash("level", "Zeigt dir dein Level an!").addOption(OptionType.USER, "nutzer", "Nutzer von dem du die XP ansehen willst", true),
                Commands.slash("giveaway", "Startet eine Verlosung").addOption(OptionType.STRING, "preis", "Gib hier den Preis ein.", true).addOption(OptionType.INTEGER, "gewinner", "Gib die Anzahl der Gewinner ein.", true).addOption(OptionType.STRING, "dauer", "Gib die Dauer des Giveaways in Sekunden ein.", true)).queue();

    }
}
