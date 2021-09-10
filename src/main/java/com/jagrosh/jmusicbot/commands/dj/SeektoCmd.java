package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;

import java.util.concurrent.TimeUnit;

import static com.jagrosh.jmusicbot.utils.FormatUtil.convertTimeToMilli;

public class SeektoCmd extends DJCommand {
    public SeektoCmd(Bot bot) {
        super(bot);
        this.name = "seekto";
        this.help = "seeks the song to the specified position";
        this.arguments = "<time in seconds>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        String duration = event.getArgs().startsWith("<") && event.getArgs().endsWith(">") ? "0" : event.getArgs();
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.getPlayer().getPlayingTrack().setPosition(handler.getPlayer().getPlayingTrack().getPosition() + convertTimeToMilli(duration));
    }
}
