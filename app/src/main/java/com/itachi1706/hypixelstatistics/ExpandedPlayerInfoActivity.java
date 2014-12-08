package com.itachi1706.hypixelstatistics;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itachi1706.hypixelstatistics.AsyncAPI.GetPlayerByName;
import com.itachi1706.hypixelstatistics.AsyncAPI.GetPlayerByNameExpanded;
import com.itachi1706.hypixelstatistics.util.CharHistory;
import com.itachi1706.hypixelstatistics.util.HistoryObject;
import com.itachi1706.hypixelstatistics.util.ResultDescription;

import java.util.ArrayList;


public class ExpandedPlayerInfoActivity extends ActionBarActivity {

    AutoCompleteTextView playerName;
    TextView debug, result;
    ExpandableListView generalDetails;
    Button checkPlayer;
    ImageView pHead;
    ProgressDialog checkProgress;
    ProgressBar headBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);

        playerName = (AutoCompleteTextView) findViewById(R.id.PlayersetName);
        debug = (TextView) findViewById(R.id.players_tvDebug);
        result = (TextView) findViewById(R.id.players_lblResult);
        checkPlayer = (Button) findViewById(R.id.PlayersBtnChk);
        playerName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pHead = (ImageView) findViewById(R.id.playersIvPlayerHead);
        debug.setMovementMethod(new ScrollingMovementMethod());
        generalDetails = (ExpandableListView) findViewById(R.id.players_lvGeneral);
        headBar = (ProgressBar) findViewById(R.id.PlayerspbHead);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getHistory());
        playerName.setAdapter(adapter);

        if (this.getIntent().hasExtra("player")){
            String intentPlayer = this.getIntent().getStringExtra("player");
            playerName.setText(intentPlayer);
            checkProgress = new ProgressDialog(ExpandedPlayerInfoActivity.this);
            checkProgress.setCancelable(false);
            checkProgress.setIndeterminate(true);
            checkProgress.setTitle("Querying Server...");
            checkProgress.setMessage("Getting Player Statistics from the Hypixel API");
            checkProgress.show();
            new GetPlayerByName(result, debug, generalDetails, pHead, checkProgress, headBar, getApplicationContext()).execute(intentPlayer);

        }

        //Check if we should hide the debug window
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!(myPref.getBoolean("debugMode", true))){
            debug.setVisibility(View.INVISIBLE);
        } else {
            debug.setVisibility(View.VISIBLE);
        }

        checkPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerName.getText().toString().equals("") || playerName.getText().toString() == null){
                    Toast.makeText(getApplicationContext(), "Please enter a name!", Toast.LENGTH_SHORT).show();
                } else {
                    String name = playerName.getText().toString();
                    checkProgress = new ProgressDialog(ExpandedPlayerInfoActivity.this);
                    checkProgress.setCancelable(false);
                    checkProgress.setIndeterminate(true);
                    checkProgress.setTitle("Querying Server...");
                    checkProgress.setMessage("Getting Player Statistics from the Hypixel API");
                    checkProgress.show();
                    new GetPlayerByNameExpanded(result, debug, generalDetails, pHead, checkProgress, headBar, ExpandedPlayerInfoActivity.this).execute(name);
                }
            }
        });

        generalDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (generalDetails.getItemAtPosition(position) instanceof ResultDescription) {
                    ResultDescription desc = (ResultDescription) generalDetails.getItemAtPosition(position);
                    if (desc.get_alert() != null){
                        new AlertDialog.Builder(ExpandedPlayerInfoActivity.this).setTitle(desc.get_title())
                                .setMessage(Html.fromHtml(desc.get_alert())).show();
                    }
                }
            }
        });
    }

    private String[] getHistory(){
        String hist = CharHistory.getListOfHistory(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        ArrayList<String> tmp = new ArrayList<>();
        if (hist != null) {
            Gson gson = new Gson();
            HistoryObject check = gson.fromJson(hist, HistoryObject.class);
            JsonArray histCheck = check.getHistory();
            for (JsonElement el : histCheck) {
                JsonObject histCheckName = el.getAsJsonObject();
                tmp.add(histCheckName.get("displayname").getAsString());
            }
        }

        String[] results = new String[tmp.size()];
        for (int i = 0; i < results.length; i++){
            results[i] = tmp.get(i);
        }
        return results;
    }

    @Override
    public void onResume(){
        super.onResume();
        //Check if we should hide the debug window
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getHistory());
        playerName.setAdapter(adapter);
        if (!(myPref.getBoolean("debugMode", true))){
            debug.setVisibility(View.INVISIBLE);
        } else {
            debug.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player_info, menu);
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
            startActivity(new Intent(ExpandedPlayerInfoActivity.this, GeneralPrefActivity.class));
            return true;
        } else if (id == R.id.view_debug) {
            TextView jsonDebug = new TextView(ExpandedPlayerInfoActivity.this);
            jsonDebug.setText(debug.getText());
            jsonDebug.setGravity(Gravity.CENTER);
            LinearLayout dialogLayout = new LinearLayout(ExpandedPlayerInfoActivity.this);

            dialogLayout.addView(jsonDebug);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            AlertDialog.Builder debugAlert = new AlertDialog.Builder(ExpandedPlayerInfoActivity.this);
            ScrollView scrollPane = new ScrollView(this);
            scrollPane.addView(dialogLayout);
            debugAlert.setView(scrollPane);
            debugAlert.setTitle("JSON Information");
            debugAlert.setPositiveButton(android.R.string.ok, null);
            debugAlert.show();
            return true;
        } else if (id == R.id.view_legacy){
            new AlertDialog.Builder(ExpandedPlayerInfoActivity.this)
                    .setTitle("WARNING")
                    .setMessage("You are about to use a no longer worked upon version of player statistics" +
                            " that is incomplete in statistic data. This is still here" +
                            " for people to see the development progression and will" +
                            " be removed in the future. \n\n Are you sure you want to proceed?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton("Proceed!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(ExpandedPlayerInfoActivity.this, OldPlayerInfoActivity.class));
                        }
                    }).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
