package com.itachi1706.hypixelstatistics.AsyncAPI.Boosters;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.itachi1706.hypixelstatistics.ListViewAdapters.BoosterDescListAdapter;
import com.itachi1706.hypixelstatistics.Objects.BoosterDescription;
import com.itachi1706.hypixelstatistics.Objects.HistoryArrayObject;
import com.itachi1706.hypixelstatistics.Objects.HistoryObject;
import com.itachi1706.hypixelstatistics.R;
import com.itachi1706.hypixelstatistics.util.HistoryHandling.CharHistory;
import com.itachi1706.hypixelstatistics.util.MainStaticVars;
import com.itachi1706.hypixelstatistics.util.MinecraftColorCodes;
import com.itachi1706.hypixelstatistics.util.NotifyUserUtil;

import net.hypixel.api.reply.PlayerReply;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Kenneth on 18/11/2014, 9:12 PM
 * for Hypixel Statistics in package com.itachi1706.hypixelstatistics.AsyncAPI
 */
@Deprecated
public class BoosterGetPlayerName extends AsyncTask<BoosterDescription, Void, String> {

    Exception except = null;
    Context mContext;
    BoosterDescription playerName;
    ListView list;
    boolean isActive;
    ProgressBar bar;
    TextView tooltip;
    int retry = 0;

    public BoosterGetPlayerName(Context context, ListView listView, boolean isActiveOnly, ProgressBar bars, TextView tooltips){
        mContext = context;
        list = listView;
        isActive = isActiveOnly;
        bar = bars;
        tooltip = tooltips;
    }

    public BoosterGetPlayerName(Context context, ListView listView, boolean isActiveOnly, ProgressBar bars, TextView tooltips, int retry){
        mContext = context;
        list = listView;
        isActive = isActiveOnly;
        bar = bars;
        tooltip = tooltips;
        this.retry = retry;
    }

    @Override
    protected String doInBackground(BoosterDescription... playerData) {
        playerName = playerData[0];
        String url = MainStaticVars.API_BASE_URL + "?type=player&uuid=" + playerName.get_purchaseruuid();
        url = MainStaticVars.updateURLWithApiKeyIfExists(url);
        String tmp = "";
        //Get Statistics
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(MainStaticVars.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(MainStaticVars.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            tmp = str.toString();


        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            except = e;
        }

        return tmp;
    }

    protected void onPostExecute(String json) {
        if (except != null) {
            if (except instanceof SocketTimeoutException){
                if (retry > 10)
                    NotifyUserUtil.createShortToast(mContext, "Connection Timed Out. Try again later");
                else {
                    Log.d("RESOLVE", "Retrying");
                    new BoosterGetPlayerName(mContext, list, isActive, bar, tooltip, retry + 1).execute(playerName);
                }
            } else
                NotifyUserUtil.createShortToast(mContext.getApplicationContext(), "An Exception Occured (" + except.getMessage() + ")");
        } else {
            Gson gson = new Gson();
            if (!MainStaticVars.checkIfYouGotJsonString(json)){
                Log.d("Invalid JSON", json + " is invalid");
                Log.d("RESOLVE", "Retrying");
                new BoosterGetPlayerName(mContext, list, isActive, bar, tooltip).execute(playerName);
            } else {
                PlayerReply reply = gson.fromJson(json, PlayerReply.class);
                if (reply.isThrottle()) {
                    //Throttled (API Exceeded Limit)
                    //NotifyUserUtil.createShortToast(mContext, "The Hypixel Public API only allows 60 queries per minute. Please try again later");
                    Log.d("THROTTLED", "BOOSTER API NAME GET: " + playerName.get_purchaseruuid());
                    Log.d("RESOLVE", "Retrying");
                    new BoosterGetPlayerName(mContext, list, isActive, bar, tooltip).execute(playerName);
                } else if (!reply.isSuccess()) {
                    //Not Successful
                    NotifyUserUtil.createShortToast(mContext.getApplicationContext(), "Unsuccessful Query!\n Reason: " + reply.getCause());
                    Log.d("UNSUCCESSFUL", "BOOSTER API NAME GET: " + playerName.get_purchaseruuid());
                    Log.d("RESOLVE", "Retrying");
                    new BoosterGetPlayerName(mContext, list, isActive, bar, tooltip).execute(playerName);
                } else if (reply.getPlayer() == null) {
                    NotifyUserUtil.createShortToast(mContext.getApplicationContext(), "Invalid Player " + playerName.get_purchaseruuid());
                } else {
                    //Succeeded
                    if (!MinecraftColorCodes.checkDisplayName(reply)) {
                        playerName.set_mcName(reply.getPlayer().get("playername").getAsString());
                        playerName.set_mcNameWithRank(reply.getPlayer().get("playername").getAsString());
                    } else {
                        playerName.set_mcName(reply.getPlayer().get("displayname").getAsString());
                        playerName.set_mcNameWithRank(MinecraftColorCodes.parseHypixelRanks(reply));
                    }
                    playerName.set_done(true);
                    if (!checkHistory(reply)) {
                        CharHistory.addHistory(reply, PreferenceManager.getDefaultSharedPreferences(mContext));
                        Log.d("Player", "Added history for player " + reply.getPlayer().get("playername").getAsString());
                    }
                    MainStaticVars.boosterList.add(playerName);
                    MainStaticVars.tmpBooster++;
                    MainStaticVars.boosterProcessCounter++;
                    checkIfComplete();
                    //new BoosterGetPlayerHead(mContext, list, isActive, bar).execute(playerName);
                }
            }
        }
    }

    private boolean checkHistory(PlayerReply reply){
        String hist = CharHistory.getListOfHistory(PreferenceManager.getDefaultSharedPreferences(mContext));
        if (hist != null) {
            Gson gson = new Gson();
            HistoryObject check = gson.fromJson(hist, HistoryObject.class);

            List<HistoryArrayObject> histCheck = CharHistory.convertHistoryArrayToList(check.getHistory());

            for (HistoryArrayObject histCheckName : histCheck) {
                if (histCheckName.getPlayername().equals(reply.getPlayer().get("playername").getAsString())) return true;
            }
        }
        return false;
    }

    private void checkIfComplete(){
        boolean done = true;
        for (BoosterDescription desc : MainStaticVars.boosterList){
            if (!desc.is_done()) {
                done = false;
                break;
            }
        }

        if (done){
            if (!isActive) {
                if (MainStaticVars.boosterList != null && MainStaticVars.boosterList.size() != 0) {
                    MainStaticVars.boosterListAdapter.updateAdapter(MainStaticVars.boosterList);
                    MainStaticVars.boosterListAdapter.notifyDataSetChanged();
                }
            }
        }

        if (MainStaticVars.boosterList.size() >= MainStaticVars.numOfBoosters && !MainStaticVars.parseRes){
            tooltip.setVisibility(View.INVISIBLE);
            bar.setVisibility(View.INVISIBLE);
            MainStaticVars.inProg = false;
            MainStaticVars.parseRes = true;
            MainStaticVars.boosterUpdated = true;

            if (isActive){
                ArrayList<BoosterDescription> tmp = new ArrayList<>();
                for (BoosterDescription desc : MainStaticVars.boosterList) {
                    tmp.add(desc);
                }
                Iterator<BoosterDescription> iter = tmp.iterator();
                while (iter.hasNext()) {
                    BoosterDescription desc = iter.next();
                    if (!desc.checkIfBoosterActive())
                        iter.remove();
                }
                BoosterDescListAdapter adapter = new BoosterDescListAdapter(mContext, R.layout.listview_booster_desc, tmp);
                list.setAdapter(adapter);
            } else {
                //Filter based on filter
                String filterString = MainStaticVars.boosterListAdapter.getFilteredStringForBooster();
                MainStaticVars.backupBooster();
                if (!filterString.equals(""))
                    MainStaticVars.boosterListAdapter.getFilter().filter(filterString);
            }
            MainStaticVars.parseRes = false;
        }

        if (MainStaticVars.inProg) {
            tooltip.setVisibility(View.VISIBLE);
            tooltip.setText("Processed Player " + MainStaticVars.boosterProcessCounter + "/" + MainStaticVars.boosterMaxProcessCounter);
        }
    }
}
