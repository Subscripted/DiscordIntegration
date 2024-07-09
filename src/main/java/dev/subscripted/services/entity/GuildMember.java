package dev.subscripted.services.entity;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class GuildMember {

    Member member;
    User user;
    String id;
    String username;


    public static GuildMember get(Member member) {
        User user = member.getUser();
        return new GuildMember(
                member,
                user,
                user.getId(),
                user.getName());
    }


}
