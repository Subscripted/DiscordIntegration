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
        Guild guild = Main.getJda().getGuildById("1102188267513843782");
        assert guild != null;
        guild.updateCommands().addCommands(
                Commands.slash("level", "Zeigt dir dein Level an!").addOption(OptionType.USER, "nutzer", "Nutzer von dem du die XP ansehen willst", true)).queue();
    }
}
