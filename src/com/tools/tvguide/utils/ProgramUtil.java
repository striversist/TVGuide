package com.tools.tvguide.utils;

import java.util.List;

import com.tools.tvguide.data.Program;

public class ProgramUtil {

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
