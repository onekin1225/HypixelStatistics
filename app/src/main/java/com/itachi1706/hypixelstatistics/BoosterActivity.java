package com.itachi1706.hypixelstatistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itachi1706.hypixelstatistics.Objects.BoosterDescription;
import com.itachi1706.hypixelstatistics.AsyncAPI.Boosters.GetBoosterHistory;
import com.itachi1706.hypixelstatistics.AsyncAPI.Boosters.GetBoosters;
import com.itachi1706.hypixelstatistics.RecyclerViewAdapters.BoosterRecyclerAdapter;
import com.itachi1706.hypixelstatistics.RecyclerViewAdapters.StringRecyclerAdapter;
import com.itachi1706.hypixelstatistics.util.MainStaticVars;
import com.itachi1706.hypixelstatistics.util.NotifyUserUtil;

import net.hypixel.api.reply.BoostersReply;
import net.hypixel.api.util.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class BoosterActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView boostList;
    ProgressBar prog;
    TextView boosterTooltip;
    SwipeRefreshLayout swipeToRefresh;

    Handler handler;

    Activity mActivity;

    final ArrayList<CharSequence> seletedFilterItems=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Theme
        MainStaticVars.setLayoutAccordingToPrefs(this);

        setContentView(R.layout.activity_booster_list);

        MainStaticVars.updateTimeout(this);
        boostList = (RecyclerView) findViewById(R.id.BoostlvBooster);
        boostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        boostList.setLayoutManager(linearLayoutManager);
        boostList.setItemAnimator(new DefaultItemAnimator());

        prog = (ProgressBar) findViewById(R.id.BoostpbProg);
        boosterTooltip = (TextView) findViewById(R.id.tvBoosterTooltip);
        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshBooster);
        mActivity = this;

        swipeToRefresh.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4);

        swipeToRefresh.setOnRefreshListener(this);

        if (!MainStaticVars.boosterUpdated){
            if (!MainStaticVars.isBriefBooster) {
                updateActiveBoosters();
            } else {
                //Parse Brief Booster
                parseBriefBoosters();
            }
        } else {
            if (MainStaticVars.boosterList.size() != 0) {
                BoosterRecyclerAdapter adapter = new BoosterRecyclerAdapter(MainStaticVars.boosterList, this, handler);
                boostList.setAdapter(adapter);
                assert BoosterActivity.this.getSupportActionBar() != null;
                this.getSupportActionBar().setTitle(this.getResources().getString(R.string.title_activity_booster_list) + " (" + MainStaticVars.boosterList.size() + ")");
            } else {
                String[] tmp = {"No Boosters Activated"};
                StringRecyclerAdapter adapter = new StringRecyclerAdapter(tmp);
                boostList.setAdapter(adapter);
            }
        }

        handler = new Handler();
    }

    private void parseBriefBoosters(){
        final String jsonString = MainStaticVars.boosterJsonString;
        if (jsonString != null && jsonString.length() > 50){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    BoostersReply reply = gson.fromJson(jsonString, BoostersReply.class);
                    MainStaticVars.boosterList.clear();
                    MainStaticVars.boosterHashMap.clear();
                    MainStaticVars.boosterUpdated = false;
                    MainStaticVars.inProg = true;
                    JsonArray records = reply.getRecords().getAsJsonArray();
                    MainStaticVars.numOfBoosters = records.size();
                    MainStaticVars.tmpBooster = 0;
                    MainStaticVars.boosterProcessCounter = 0;
                    MainStaticVars.boosterMaxProcessCounter = 0;

                    MainStaticVars.boosterRecyclerAdapter = new BoosterRecyclerAdapter(MainStaticVars.boosterList, mActivity, handler);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            prog.setVisibility(View.VISIBLE);
                            boostList.setAdapter(MainStaticVars.boosterRecyclerAdapter);
                        }
                    });

                    if (records.size() != 0) {
                        MainStaticVars.boosterMaxProcessCounter = records.size();
                        assert BoosterActivity.this.getSupportActionBar() != null;
                        BoosterActivity.this.getSupportActionBar().setTitle(BoosterActivity.this.getResources().getString(R.string.title_activity_booster_list) + " (" + MainStaticVars.boosterMaxProcessCounter + ")");
                        for (JsonElement e : records) {
                            JsonObject obj = e.getAsJsonObject();
                            String uid = obj.get("purchaserUuid").getAsString(); //Get Player UUID
                            final BoosterDescription desc;
                            if (obj.has("purchaser")) {
                                //Old Method (Back then everything 1hr, so 3600 default)
                                desc = new BoosterDescription(obj.get("amount").getAsInt(), obj.get("dateActivated").getAsLong(),
                                        obj.get("gameType").getAsInt(), obj.get("length").getAsInt(), 3600,
                                        uid, obj.get("purchaser").getAsString());
                            } else {
                                //New Method
                                desc = new BoosterDescription(obj.get("amount").getAsInt(), obj.get("dateActivated").getAsLong(),
                                        obj.get("gameType").getAsInt(), obj.get("length").getAsInt(), obj.get("originalLength").getAsInt(),
                                        uid);
                            }
                            //Move to BoosterGetHistory
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    boosterTooltip.setVisibility(View.VISIBLE);
                                    boosterTooltip.setText("Booster list obtained. Processing Players now...");
                                    new GetBoosterHistory(mActivity, boostList, false, prog, boosterTooltip, handler).execute(desc);
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String[] tmp = {"No Boosters Activated"};
                                StringRecyclerAdapter adapter = new StringRecyclerAdapter(tmp);
                                boostList.setAdapter(adapter);
                                prog.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void updateActiveBoosters(){
        ArrayList<BoosterDescription> repop = new ArrayList<>();
        BoosterRecyclerAdapter adapter = new BoosterRecyclerAdapter(repop, this, handler);
        boostList.setAdapter(adapter);
        prog.setVisibility(View.VISIBLE);
        MainStaticVars.boosterUpdated = false;
        MainStaticVars.inProg = false;
        MainStaticVars.parseRes = false;
        MainStaticVars.unfilteredBoosterList.clear();
        if (swipeToRefresh.isRefreshing()) //Manual invoke, remind async task of the case
            new GetBoosters(this, boostList, false, prog, boosterTooltip, swipeToRefresh, handler).execute();
        else //App invoked
            new GetBoosters(this, boostList, false, prog, boosterTooltip, handler).execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_booster_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(BoosterActivity.this, GeneralPrefActivity.class));
            return true;
        } else if (id == R.id.action_refresh_active_boosters){
            swipeToRefresh.setRefreshing(true);
            updateActiveBoosters();
            NotifyUserUtil.showShortDismissSnackbar(findViewById(android.R.id.content), "Updating Booster List");
            return true;
        } else if (id == R.id.action_get_detailed_boosters){
            new AlertDialog.Builder(this)
                    .setTitle("Activated Boosters per Game").setMessage(parseStats())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        } else if (id == R.id.action_filter_boosters){
            if (!MainStaticVars.inProg)
                displayFilterAlertDialog();
            else
                new AlertDialog.Builder(this)
                        .setTitle("Filter Unavailable").setMessage("Boosters are still being processed. Filter will only be " +
                        "available and applied after boosters are processed")
                        .setPositiveButton(android.R.string.ok, null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        updateActiveBoosters();
        NotifyUserUtil.showShortDismissSnackbar(findViewById(android.R.id.content), "Updating booster list");
    }

    private String parseStats(){
        ArrayList<BoosterDescription> check;
        if (MainStaticVars.unfilteredBoosterList.size() != 0) {
            check = MainStaticVars.unfilteredBoosterList;
        } else {
            check = MainStaticVars.boosterList;
        }
        HashMap<GameType, Integer> count = new HashMap<>();
        HashMap<GameType, Integer> time = new HashMap<>();
        int unknownGameCount = 0, unknownGameTime = 0;

        for (BoosterDescription desc : check){
            if (desc.get_gameType() != null) {
                //Not Null
                if (count.containsKey(desc.get_gameType()))
                    count.put(desc.get_gameType(), count.get(desc.get_gameType()) + 1);
                else
                    count.put(desc.get_gameType(), 1);
                if (time.containsKey(desc.get_gameType()))
                    time.put(desc.get_gameType(), time.get(desc.get_gameType()) + desc.get_timeRemaining());
                else
                    time.put(desc.get_gameType(), desc.get_timeRemaining());
            } else {
                unknownGameCount ++;
                unknownGameTime += desc.get_timeRemaining();
            }
        }

        StringBuilder boosterStatBuilder = new StringBuilder();
        boosterStatBuilder.append("Based on last booster query: \n\n");
        for (Map.Entry<GameType, Integer> cursor : count.entrySet()){
            if (time.containsKey(cursor.getKey())){
                //Can Continue
                boosterStatBuilder.append(cursor.getKey().getName()).append(": ").append(cursor.getValue()).append("\n").append(createTimeLeftString(time.get(cursor.getKey()))).append("\n");
            } else {
                //Error (No time)
                boosterStatBuilder.append(cursor.getKey().getName()).append(": ").append(cursor.getValue()).append("\n").append(createTimeLeftString(0)).append("\n");
            }
        }
        //Check for unknown games
        if (unknownGameCount != 0){
            boosterStatBuilder.append("Unknown Game: ").append(unknownGameCount).append("\n").append(createTimeLeftString(unknownGameTime)).append("\n");
            boosterStatBuilder.append("(Please Contact Dev of this)");
        }

        return boosterStatBuilder.toString();
    }

    private String createTimeLeftString(int timeRemaining){
        long days, hours, minutes, seconds;

        days = TimeUnit.SECONDS.toDays(timeRemaining);
        hours = TimeUnit.SECONDS.toHours(timeRemaining) - (days * 24);
        minutes = TimeUnit.SECONDS.toMinutes(timeRemaining) - (TimeUnit.SECONDS.toHours(timeRemaining)* 60);
        seconds = TimeUnit.SECONDS.toSeconds(timeRemaining) - (TimeUnit.SECONDS.toMinutes(timeRemaining) * 60);

        //Craft the time statement
        StringBuilder timeString = new StringBuilder();
        timeString.append("(");
        if (days != 0) {
            timeString.append(getResources().getQuantityString(R.plurals.days, (int) days, (int) days));
            timeString.append(" ");
        }
        if (hours != 0) {
            timeString.append(getResources().getQuantityString(R.plurals.hours, (int) hours, (int) hours));
            timeString.append(" ");
        }
        if (minutes != 0) {
            timeString.append(getResources().getQuantityString(R.plurals.minutes, (int) minutes, (int) minutes));
            timeString.append(" ");
        }
        if (seconds != 0) {
            timeString.append(getResources().getQuantityString(R.plurals.seconds, (int) seconds, (int) seconds));
        }
        timeString.append(")");
        return timeString.toString();
    }

    private HashMap<GameType, Integer> getBoosterCountsPerGameType(ArrayList<BoosterDescription> boosterList){
        HashMap<GameType, Integer> count = new HashMap<>();
        for (BoosterDescription desc : boosterList){
            if (desc.get_gameType() != null){
                if (count.containsKey(desc.get_gameType()))
                    count.put(desc.get_gameType(), count.get(desc.get_gameType()) + 1);
                else
                    count.put(desc.get_gameType(), 1);
            } else {
                if (count.containsKey(GameType.UNKNOWN))
                    count.put(GameType.UNKNOWN, count.get(GameType.UNKNOWN) + 1);
                else
                    count.put(GameType.UNKNOWN, 1);
            }
        }
        return count;
    }

    private void displayFilterAlertDialog(){
        MainStaticVars.restoreBooster();
        ArrayList<BoosterDescription> filterSel = MainStaticVars.boosterList;
        HashMap<GameType, Integer> parsedBoosterList = getBoosterCountsPerGameType(filterSel);
        ArrayList<CharSequence> tmpItems = new ArrayList<>();
        for (Map.Entry<GameType, Integer> item : parsedBoosterList.entrySet())
            tmpItems.add(item.getKey().getName() + " (" + item.getValue() + ")");

        CharSequence[] tmp = new CharSequence[tmpItems.size()];
        tmp = tmpItems.toArray(tmp);
        final CharSequence[] items = tmp;

        //Check for already filled
        AlertDialog filterDialog;
        boolean[] isCheckedAlr = new boolean[items.length];
        if (seletedFilterItems.size() == 0){
            seletedFilterItems.clear();
            for (int i = 0; i < isCheckedAlr.length; i++){
                isCheckedAlr[i] = true;
                seletedFilterItems.add(items[i].toString().split(" \\(")[0]);
            }
        } else {
            for (int i = 0; i < isCheckedAlr.length; i++) {
                CharSequence seq = items[i].toString().split(" \\(")[0];
                for (CharSequence s : seletedFilterItems) {
                    if (seq.equals(s)) {
                        isCheckedAlr[i] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select The GameTypes to filter");
        builder.setMultiChoiceItems(items, isCheckedAlr, new DialogInterface.OnMultiChoiceClickListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void onClick(DialogInterface dialog, int indexSelected,
                                    boolean isChecked) {
                    String[] tmp = items[indexSelected].toString().split(" \\(");
                    String gameType = tmp[0];
                    if (isChecked) {
                        // If the user checked the item, add it to the selected items
                        seletedFilterItems.add(gameType);
                        Log.d("BOOSTER-FILTER", gameType + " added to filter");
                    } else if (seletedFilterItems.contains(gameType)) {
                        // Else, if the item is already in the array, remove it
                        seletedFilterItems.remove(gameType);
                        Log.d("BOOSTER-FILTER", gameType + " removed from filter");
                    }
                }
            })
            // Set the action buttons
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //  Your code when user clicked on OK
                    //  You can write the code  to save the selected item here
                    MainStaticVars.boosterRecyclerAdapter.updateAdapter(MainStaticVars.boosterList);
                    if (seletedFilterItems.size() != 0) {
                        StringBuilder craftedFilterString = new StringBuilder();
                        for (int i = 0; i < seletedFilterItems.size() - 1; i++) {
                            craftedFilterString.append(seletedFilterItems.get(i));
                            craftedFilterString.append("%SPLIT%");
                        }
                        craftedFilterString.append(seletedFilterItems.get(seletedFilterItems.size() - 1));
                        String filterString = craftedFilterString.toString();
                        if (MainStaticVars.boosterRecyclerAdapter != null) {
                            MainStaticVars.boosterRecyclerAdapter.setFilteredStringForBooster(filterString);
                            MainStaticVars.boosterRecyclerAdapter.getFilter().filter(filterString);
                        }
                    } else {
                        String filterString = "";
                        if (MainStaticVars.boosterRecyclerAdapter != null) {
                            MainStaticVars.boosterRecyclerAdapter.setFilteredStringForBooster(filterString);
                            MainStaticVars.boosterRecyclerAdapter.updateAdapter(MainStaticVars.boosterList);
                            MainStaticVars.boosterRecyclerAdapter.setFilteredStringForBooster("");
                            seletedFilterItems.clear();
                        }
                    }
                }
            })
            .setNegativeButton("Cancel", null).setNeutralButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainStaticVars.boosterRecyclerAdapter.updateAdapter(MainStaticVars.boosterList);
                MainStaticVars.boosterRecyclerAdapter.setFilteredStringForBooster("");
                seletedFilterItems.clear();
            }
        });

        filterDialog = builder.create();//AlertDialog dialog; create like this outside onClick
        filterDialog.show();
    }
}
