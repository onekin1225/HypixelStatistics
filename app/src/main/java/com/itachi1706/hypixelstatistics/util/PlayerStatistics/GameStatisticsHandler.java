package com.itachi1706.hypixelstatistics.util.PlayerStatistics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itachi1706.hypixelstatistics.util.MinecraftColorCodes;
import com.itachi1706.hypixelstatistics.util.Objects.ResultDescription;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.ArcadeStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.ArenaStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.BlitzSGStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.CopsAndCrimsStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.MegaWallsStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.OldSpleefStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.PaintballStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.QuakecraftStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.TNTGamesStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.UHCChampionsStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.VampireZStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.WallsStatistics;
import com.itachi1706.hypixelstatistics.util.PlayerStatistics.GameStatistics.WarlordsStatistics;

import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.util.GameType;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kenneth on 13/5/2015
 * for HypixelStatistics in package com.itachi1706.hypixelstatistics.util.PlayerStatistics
 */
public class GameStatisticsHandler {

    /**
     * Parse statistics (Split based on GameType)
     * @param reply PlayerReply object
     */
    public static ArrayList<ResultDescription> parseStats(PlayerReply reply, String localPlayerName){
        ArrayList<ResultDescription> descArray = new ArrayList<>();
        JsonObject mainStats = reply.getPlayer().getAsJsonObject("stats");
        for (Map.Entry<String, JsonElement> entry : mainStats.entrySet()){
            //Based on stat go parse it
            JsonObject statistic = entry.getValue().getAsJsonObject();
            GameType parseVariousGamemode = GameType.fromDatabase(entry.getKey());
            if (parseVariousGamemode == null) {
                switch (entry.getKey().toLowerCase()) {
                    case "spleef":
                        descArray.add(new ResultDescription("Legacy Spleef Statistics", null, false, OldSpleefStatistics.parseSpleef(statistic)));
                        break;
                    case "holiday": //descArray.remove(descArray.size() - 1);
                        break;
                    default:
                        descArray.add(new ResultDescription(entry.getKey() + " (ERROR - INFORM DEV)", MinecraftColorCodes.parseColors("§cPlease contact the dev to add this into the statistics§r")));
                        break;
                }
            } else {
                switch (parseVariousGamemode) {
                    case ARENA:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, ArenaStatistics.parseArena(statistic)));
                        break;
                    case ARCADE:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, ArcadeStatistics.parseArcade(statistic)));
                        break;
                    case SURVIVAL_GAMES:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, BlitzSGStatistics.parseHG(statistic)));
                        break;
                    case MCGO:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, CopsAndCrimsStatistics.parseMcGo(statistic)));
                        break;
                    case PAINTBALL:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, PaintballStatistics.parsePaintball(statistic)));
                        break;
                    case QUAKECRAFT:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, QuakecraftStatistics.parseQuake(statistic)));
                        break;
                    case TNTGAMES:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, TNTGamesStatistics.parseTntGames(statistic)));
                        break;
                    case VAMPIREZ:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, VampireZStatistics.parseVampZ(statistic)));
                        break;
                    case WALLS:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, WallsStatistics.parseWalls(statistic)));
                        break;
                    case WALLS3:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, MegaWallsStatistics.parseWalls3(statistic)));
                        break;
                    case UHC:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, UHCChampionsStatistics.parseUHC(statistic)));
                        break;
                    case WARLORDS:
                        descArray.add(new ResultDescription(parseVariousGamemode.getName() + " Statistics", null, false, WarlordsStatistics.parseWarlords(statistic, localPlayerName)));
                        break;
                    default:
                        descArray.add(new ResultDescription(entry.getKey() + " (ERROR - INFORM DEV)", MinecraftColorCodes.parseColors("§cPlease contact the dev to add this into the statistics§r")));
                        break;
                }
            }
        }
        return descArray;
    }
}
