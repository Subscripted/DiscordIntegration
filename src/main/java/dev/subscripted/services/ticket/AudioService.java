package dev.subscripted.services.ticket;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AudioService extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private Guild guild;
    private VoiceChannel targetChannel;
    private boolean isPlayingSupportTrack = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AudioService() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.player = playerManager.createPlayer();

        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.STOPPED) {
                    if (isPlayingSupportTrack) {
                        isPlayingSupportTrack = false;
                        playAudio(guild, "warteschleife.mp3");
                    } else
                        guild.getAudioManager().closeAudioConnection();
                }
            }
        });
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        VoiceChannel joinedChannel = (VoiceChannel) event.getChannelJoined();
        VoiceChannel leftChannel = (VoiceChannel) event.getChannelLeft();
        Member joiningMember = event.getEntity();

        this.guild = event.getGuild();

        if (joiningMember != null && !joiningMember.getUser().isBot() && joinedChannel != null && joinedChannel.getId().equals("1102203664820617307")) {
            guild.getAudioManager().openAudioConnection(joinedChannel);
            playAudio(guild, "support.mp3");
            isPlayingSupportTrack = true;
            this.targetChannel = joinedChannel;
        }

        if (leftChannel != null && leftChannel.equals(targetChannel) && leftChannel.getMembers().stream().noneMatch(member -> member.getUser().isBot()) && leftChannel.getMembers().isEmpty()) {
            guild.getAudioManager().closeAudioConnection();
            this.targetChannel = null;
        }
    }

    private void playAudio(Guild guild, String filePath) {
        String absolutePath = Paths.get(filePath).toAbsolutePath().toString();
        playerManager.loadItem(absolutePath, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.startTrack(track, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                player.startTrack(playlist.getTracks().get(0), false);
            }

            @Override
            public void noMatches() {
                System.out.println("No matches found for the file: " + absolutePath);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                System.err.println("Could not load the file: " + absolutePath);
                e.printStackTrace();
            }
        });
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
    }


    private static class AudioPlayerSendHandler implements AudioSendHandler {
        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        private AudioPlayerSendHandler(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean canProvide() {
            lastFrame = audioPlayer.provide();
            return lastFrame != null;
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            return ByteBuffer.wrap(lastFrame.getData());
        }

        @Override
        public boolean isOpus() {
            return true;
        }
    }
}
