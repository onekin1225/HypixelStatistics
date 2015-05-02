package com.itachi1706.hypixelstatistics.util.Warlords;

/**
 * Created by Kenneth on 2/5/2015
 * for HypixelStatistics in package com.itachi1706.hypixelstatistics.util.Warlords
 */
//TODO Get actual color for WarHorse and BattleBeast
public enum WarlordsMounts {
    NOBLE_STEED("noble_steed", "§7Noble Steed§r"),
    UNDYING_MARE("undying_mare", "§aUndying Mare§r"),
    CORPSE_MARE("corpse_mare", "§aCorpse Mare§r"),
    WAR_HORSE("war_horse", "§aWar Horse§r"),
    BATTLE_BEAST("battle_beast", "§aBattle Beast§r"),
    RAGING_STALLION("raging_stallion", "§9Raging Stallion§r"),
    UNKNOWN("unknown", "Unknown");

    private final String id, name;

    WarlordsMounts(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static WarlordsMounts fromDatabase(String id){
        for (WarlordsMounts m : WarlordsMounts.values()){
            if (m.getId().equals(id))
                return m;
        }
        return UNKNOWN;
    }
}
