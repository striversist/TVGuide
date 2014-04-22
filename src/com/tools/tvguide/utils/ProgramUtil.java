package com.tools.tvguide.utils;

import java.util.Calendar;
import java.util.List;

import com.tools.tvguide.data.Program;

public class ProgramUtil {

    /**
     * 根据date，从programList中获取正在播放的program
     * @param programList
     * @param date
     * @return
     */
    public static Program getOnplayingProgramByTime(List<Program> programList, long date) {
        if (programList == null)
            return null;
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        String time = hour + ":" + minute;
        return getOnplayingProgramByTime(programList, time);
    }
    
    /**
     * 根据指定time，从programList中获取正在播放的program
     * @param programList
     * @param time: 格式为：HH:mm
     * @return
     */
    public static Program getOnplayingProgramByTime(List<Program> programList,
            String time) {
        if (programList == null || time == null)
            return null;

        Program onPlayingProgram = new Program();
        for (int i = 0; i < programList.size(); ++i) {
            String programTime = programList.get(i).time;
            if (programTime == null)
                continue;

            if (i == 0 && Utility.compareTime(time, programTime) < 0) {    // 播放的还是昨晚的最后一个节目
                break;
            }

            if (i < programList.size() - 1) {    // 除最后一个节目外，中间正在播放的节目
                String nextProgramTime = programList.get(i + 1).time;
                if (nextProgramTime == null)
                    continue;
                if (Utility.compareTime(time, programTime) >= 0
                        && Utility.compareTime(time, nextProgramTime) < 0) {
                    onPlayingProgram.copy(programList.get(i));
                    break;
                }
            } else {    // 当天的最后一个节目
                onPlayingProgram.copy(programList.get(i));
                break;
            }
        }
        return onPlayingProgram;
    }
}
