package dev.subscripted.services.giveaway;

import dev.subscripted.Main;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GiveawayRunnable {

    static GiveawayManager manager = Main.getGiveawayManager();
    static List<Giveaway> giveawayList;

    public void run() {

        giveawayList = manager.getGiveaways();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                long currentTime = (int) (System.currentTimeMillis() / 1000);
                for (Giveaway giveaway : giveawayList) {
                    if (currentTime > giveaway.getEndTime()) {
                        manager.endGiveaway(giveaway);
                    }
                }
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    public void updateRunnable() {
        giveawayList = manager.getGiveaways();
    }
}
