package com.itachi1706.hypixelstatistics.Fragments.PlayerInfo;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.itachi1706.hypixelstatistics.R;
import com.itachi1706.hypixelstatistics.Fragments.BaseFragmentCompat;
import com.itachi1706.hypixelstatistics.Objects.PlayerInfoBase;
import com.itachi1706.hypixelstatistics.Objects.PlayerInfoHeader;
import com.itachi1706.hypixelstatistics.Objects.PlayerInfoStatistics;
import com.itachi1706.hypixelstatistics.PlayerStatistics.GameStatisticsHandler;
import com.itachi1706.hypixelstatistics.PlayerStatistics.StatisticsHelper;
import com.itachi1706.hypixelstatistics.RecyclerViewAdapters.PlayerInfoExpandableRecyclerAdapter;
import com.itachi1706.hypixelstatistics.RecyclerViewAdapters.StringRecyclerAdapter;
import com.itachi1706.hypixelstatistics.util.MinecraftColorCodes;

import net.hypixel.api.reply.PlayerReply;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class GameStatisticsFragment extends BaseFragmentCompat {

    public GameStatisticsFragment() {
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_player_info_recycler;
    }

    //Fragment Elements
    private RecyclerView recyclerView;


    static String[] noStatistics = {"To start, press the Search icon!"};
    private StringRecyclerAdapter noStatAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(getFragmentLayout(), container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.player_info_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        noStatAdapter = new StringRecyclerAdapter(noStatistics);

        processPlayerJson(null);
        return v;
    }

    @Override
    public void processPlayerJson(String json){
        Log.i("HypixelStatistics", "Switched to GeneralStatisticsFragment");
        if (json == null || json.equals("")) { recyclerView.setAdapter(noStatAdapter); return; }
        Gson gson = new Gson();
        PlayerReply reply = gson.fromJson(json, PlayerReply.class);
        process(reply);
    }

    @Override
    public void processPlayerObject(PlayerReply object){
        process(object);
    }

    // PROCESS RESULT METHODS (GRABBLED FROM ASYNC TASK)

    private void process(PlayerReply reply){
        recyclerView.setVisibility(View.VISIBLE);

        //Get Local Player Name
        String localPlayerName;
        if (MinecraftColorCodes.checkDisplayName(reply))
            localPlayerName = reply.getPlayer().get("displayname").getAsString();
        else
            localPlayerName = reply.getPlayer().get("playername").getAsString();

        parse(reply, localPlayerName);
    }

    private void parse(PlayerReply reply, String localPlayerName){
        ArrayList<PlayerInfoBase> resultArray = new ArrayList<>();

        if (reply.getPlayer().has("stats")){
            ArrayList<PlayerInfoHeader> tmp = GameStatisticsHandler.parseStats(reply, localPlayerName);
            for (PlayerInfoHeader t : tmp){
                resultArray.add(t);
            }
        }

        for (PlayerInfoBase base : resultArray) {
            if (!(base instanceof PlayerInfoHeader)) {
                PlayerInfoStatistics statistics = (PlayerInfoStatistics) base;
                if (statistics.getMessage() != null) statistics.setMessage(StatisticsHelper.parseColorInPlayerStats(statistics.getMessage()));
                if (statistics.getTitle() != null) statistics.setTitle(StatisticsHelper.parseColorInPlayerStats(statistics.getTitle()));
                continue;
            }

            PlayerInfoHeader e = (PlayerInfoHeader) base;
            e.setTitle(StatisticsHelper.parseColorInPlayerStats(e.getTitle()));
            if (!e.hasChild()) continue;

            List<PlayerInfoStatistics> array = e.getChild();
            for (PlayerInfoStatistics child : array){
                if (child.getMessage() != null) child.setMessage(StatisticsHelper.parseColorInPlayerStats(child.getMessage()));
                if (child.getTitle() != null) child.setTitle(StatisticsHelper.parseColorInPlayerStats(child.getTitle()));
            }
        }

        PlayerInfoExpandableRecyclerAdapter adapter = new PlayerInfoExpandableRecyclerAdapter(resultArray, getActivity());
        recyclerView.setAdapter(adapter);
    }
}
