package dev.subscripted.services.giveaway;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Giveaway {
    User creator;
    String prize;
    String channelId;
    String messageId;
    int winners;
    long endTime;
    List<String> users;
}
