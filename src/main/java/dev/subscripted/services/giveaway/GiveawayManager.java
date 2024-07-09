package dev.subscripted.services.giveaway;

import dev.subscripted.Main;
import dev.subscripted.utils.TimestampMaker;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.sql.*;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GiveawayManager {

    private static final String DB_URL = "jdbc:sqlite:giveaway.db";

    @SneakyThrows
    public void createTable() {
        String createGiveawaysTable = "CREATE TABLE IF NOT EXISTS giveaways (" +
                "messageId TEXT PRIMARY KEY," +
                "channelId TEXT NOT NULL," +
                "prize TEXT NOT NULL," +
                "creatorId TEXT NOT NULL," +
                "winners INTEGER NOT NULL," +
                "endTime INTEGER NOT NULL" +
                ")";
        String createGiveawayUsersTable = "CREATE TABLE IF NOT EXISTS giveaway_users (" +
                "messageId TEXT NOT NULL," +
                "userId TEXT NOT NULL," +
                "FOREIGN KEY (messageId) REFERENCES giveaways(messageId) ON DELETE CASCADE" +
                ")";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.execute(createGiveawaysTable);
            statement.execute(createGiveawayUsersTable);
        }
    }

    @SneakyThrows
    public void create(Member member, String prize, int winners, long endTime, String channelId, String messageId) {
        String query = "INSERT INTO giveaways (messageId, channelId, prize, creatorId, winners, endTime) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, messageId);
            statement.setString(2, channelId);
            statement.setString(3, prize);
            statement.setString(4, member.getId());
            statement.setInt(5, winners);
            statement.setLong(6, endTime);
            statement.executeUpdate();
        }
    }

    @SneakyThrows
    public Giveaway getGiveaway(String id) {
        String query = "SELECT * FROM giveaways WHERE messageId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = Main.getJda().getUserById(resultSet.getString("creatorId"));
                String prize = resultSet.getString("prize");
                String channelId = resultSet.getString("channelId");
                int winners = resultSet.getInt("winners");
                long endTime = resultSet.getLong("endTime");

                List<String> users = getJoinedUsers(id);
                return new Giveaway(user, prize, channelId, id, winners, endTime, users);
            }
        }
        return null;
    }

    @SneakyThrows
    public List<Giveaway> getGiveaways() {
        List<Giveaway> giveawayList = new ArrayList<>();
        String query = "SELECT * FROM giveaways";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = Main.getJda().retrieveUserById(resultSet.getString("creatorId")).complete();
                String prize = resultSet.getString("prize");
                String channelId = resultSet.getString("channelId");
                String messageId = resultSet.getString("messageId");
                int winners = resultSet.getInt("winners");
                long endTime = resultSet.getLong("endTime");

                List<String> users = getJoinedUsers(messageId);
                giveawayList.add(new Giveaway(user, prize, channelId, messageId, winners, endTime, users));
            }
        }
        return giveawayList;
    }

    @SneakyThrows
    public void updateGiveaway(Giveaway giveaway) {
        String query = "UPDATE giveaways SET channelId = ?, prize = ?, creatorId = ?, winners = ?, endTime = ? WHERE messageId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, giveaway.getChannelId());
            statement.setString(2, giveaway.getPrize());
            statement.setString(3, giveaway.getCreator().getId());
            statement.setInt(4, giveaway.getWinners());
            statement.setLong(5, giveaway.getEndTime()); // Endzeit als long
            statement.setString(6, giveaway.getMessageId());
            statement.executeUpdate();
        }
        updateJoinedUsers(giveaway);
    }

    public void endGiveaway(Giveaway giveaway) {
        GiveawayRunnable runnable = Main.getGiveawayRunnable();
        String prize = giveaway.getPrize();
        User user = giveaway.getCreator();
        List<String> memberIds = giveaway.getUsers();
        int winners = giveaway.getWinners();

        List<String> giveawayWinners = getWinners(memberIds, winners);
        StringBuilder winner = new StringBuilder();
        if (giveawayWinners.isEmpty()) {
            winner = new StringBuilder("Niemand");
        } else {
            for (String giveawayWinner : giveawayWinners) {
                winner.append("<@").append(giveawayWinner).append(">").append(" ");
            }
        }

        StringBuilder finalWinner = winner;

        TextChannel textChannel = Main.getJda().getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            return;
        }
        textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> message.editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("<:event:1260135620420833310> Giveaway <:event:1260135620420833310>")
                                .setFooter("Novibes Giveway | Update 2023 ©", Main.getJda().getSelfUser().getEffectiveAvatarUrl())
                                .setColor(Color.DARK_GRAY)
                                .setThumbnail(Main.getJda().getSelfUser().getAvatarUrl())
                                .setDescription("- Gestartet von: " + user.getAsMention() + "\n" + "- Was wurde verlost: **" + prize + "** \n" + "- Gewinner: " + finalWinner + "\n- Restzeit: **ABGELAUFEN**" + "\n" + "- Wurde beendet am: <t:" + TimestampMaker.getTime(0) + ":R>\n" + "- Teilnehmer: " + giveaway.getUsers().size())
                                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                                .build())
                .setComponents(message.getActionRows().get(0).asDisabled())
                .queue());

        deleteGiveaway(giveaway.getMessageId());
        runnable.updateRunnable();
    }

    private List<String> getWinners(List<String> users, int winners) {
        List<String> winner = new ArrayList<>();
        if (users.isEmpty()) {
            return winner;
        }
        Random random = new Random();
        for (int x = 0; x < winners; x++) {
            int randomInt = random.nextInt(users.size());
            if (!winner.contains(users.get(randomInt))) {
                winner.add(users.get(randomInt));
            }
        }
        return winner;
    }

    public boolean isInGiveaway(Giveaway giveaway, String userId) {
        List<String> users = giveaway.getUsers();
        return users.contains(userId);
    }

    public void updateEmbed(Giveaway giveaway) {
        Main.getJda().getTextChannelById(giveaway.getChannelId()).retrieveMessageById(giveaway.getMessageId()).queue(message -> {
            message.editMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("<:event:1260135620420833310> Giveaway <:event:1260135620420833310>")
                            .setFooter("Novibes Giveway | Update 2023 ©", Main.getJda().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.MAGENTA)
                            .setThumbnail(Main.getJda().getSelfUser().getAvatarUrl())
                            .setDescription("- Gestartet von: " + giveaway.getCreator().getAsMention() + "\n" + "- Was wird verlost: **" + giveaway.getPrize() + "** \n" + "- Restzeit: <t:" + giveaway.getEndTime() + ":R> \n" + "- Teilnehmer: " + giveaway.getUsers().size())
                            .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                            .build()).queue();
        });
    }

    @SneakyThrows
    private List<String> getJoinedUsers(String messageId) {
        List<String> users = new ArrayList<>();
        String query = "SELECT userId FROM giveaway_users WHERE messageId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, messageId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(resultSet.getString("userId"));
            }
        }
        return users;
    }

    @SneakyThrows
    private void updateJoinedUsers(Giveaway giveaway) {
        String deleteQuery = "DELETE FROM giveaway_users WHERE messageId = ?";
        String insertQuery = "INSERT INTO giveaway_users (messageId, userId) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            deleteStatement.setString(1, giveaway.getMessageId());
            deleteStatement.executeUpdate();

            for (String userId : giveaway.getUsers()) {
                insertStatement.setString(1, giveaway.getMessageId());
                insertStatement.setString(2, userId);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    @SneakyThrows
    private void deleteGiveaway(String messageId) {
        String query = "DELETE FROM giveaways WHERE messageId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, messageId);
            statement.executeUpdate();
        }
    }
}
