/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class FormatUtil {

    public static long convertTimeToMilli(String timestamp) {
        String[] splittedTimestamp = timestamp.split(":");
        int count = splittedTimestamp.length;

        if (count == 1)
            return TimeUnit.SECONDS.toMillis(Long.parseLong(splittedTimestamp[0]));
        else if (count >= 2)
            return TimeUnit.MINUTES.toMillis(Long.parseLong(splittedTimestamp[0])) + TimeUnit.SECONDS.toMillis(Long.parseLong(splittedTimestamp[1]));
        else
            return 0L;
    }

    public static String formatTimestamp(long millis) {
        long seconds = millis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    public static String formatTime(long duration) {
        if(duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration/1000.0);
        long hours = seconds/(60*60);
        seconds %= 60*60;
        long minutes = seconds/60;
        seconds %= 60;
        return (hours>0 ? hours+":" : "") + (minutes<10 ? "0"+minutes : minutes) + ":" + (seconds<10 ? "0"+seconds : seconds);
    }
        
    public static String progressBar(double percent)
    {
        String str = "";
        for(int i=0; i<12; i++)
            if(i == (int)(percent*12))
                str+="\uD83D\uDD18"; // 🔘
            else
                str+="▬";
        return str;
    }
    
    public static String volumeIcon(int volume)
    {
        if(volume == 0)
            return "\uD83D\uDD07"; // 🔇
        if(volume < 30)
            return "\uD83D\uDD08"; // 🔈
        if(volume < 70)
            return "\uD83D\uDD09"; // 🔉
        return "\uD83D\uDD0A";     // 🔊
    }
    
    public static String listOfTChannels(List<TextChannel> list, String query)
    {
        String out = " Multiple text channels found matching \""+query+"\":";
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (<#"+list.get(i).getId()+">)";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfVChannels(List<VoiceChannel> list, String query)
    {
        String out = " Multiple voice channels found matching \""+query+"\":";
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfRoles(List<Role> list, String query)
    {
        String out = " Multiple text channels found matching \""+query+"\":";
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String filter(String input)
    {
        return input.replace("\u202E","")
                .replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }
}
