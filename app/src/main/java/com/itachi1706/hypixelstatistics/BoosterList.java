package com.itachi1706.hypixelstatistics;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.hypixelstatistics.AsyncAPI.Boosters.BoosterGet;
import com.itachi1706.hypixelstatistics.util.ListViewAdapters.BoosterDescListAdapter;
import com.itachi1706.hypixelstatistics.util.Objects.BoosterDescription;
import com.itachi1706.hypixelstatistics.util.MainStaticVars;

import net.hypixel.api.util.GameType;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class BoosterList extends ActionBarActivity {

    ListView boostList;
    ProgressBar prog;
    TextView boosterTooltip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booster_list);

        boostList = (ListView) findViewById(R.id.BoostlvBooster);
        prog = (ProgressBar) findViewById(R.id.BoostpbProg);
        boosterTooltip = (TextView) findViewById(R.id.tvBoosterTooltip);

        boostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BoosterDescription sel = (BoosterDescription) boostList.getItemAtPosition(position);
                Intent intentE = new Intent(BoosterList.this, ExpandedPlayerInfoActivity.class);
                intentE.putExtra("player", sel.get_mcName());
                startActivity(intentE);
            }
        });

        if (!MainStaticVars.boosterUpdated){
            updateActiveBoosters();
        } else {
            if (MainStaticVars.boosterList.size() != 0) {
                BoosterDescListAdapter adapter = new BoosterDescListAdapter(getApplicationContext(), R.layout.listview_booster_desc, MainStaticVars.boosterList);
                boostList.setAdapter(adapter);
                this.getSupportActionBar().setTitle(this.getResources().getString(R.string.title_activity_booster_list) + " (" + MainStaticVars.boosterList.size() + ")");
            } else {
                String[] tmp = {"No Boosters Activated"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, tmp);
                boostList.setAdapter(adapter);
            }
        }
    }

    private void updateActiveBoosters(){
        ArrayList<BoosterDescription> repop = new ArrayList<>();
        BoosterDescListAdapter adapter = new BoosterDescListAdapter(getApplicationContext(), R.layout.listview_booster_desc, repop);
        boostList.setAdapter(adapter);
        prog.setVisibility(View.VISIBLE);
        new BoosterGet(this.getApplicationContext(), boostList, false, prog, boosterTooltip).execute();
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
            startActivity(new Intent(BoosterList.this, GeneralPrefActivity.class));
            return true;
        } else if (id == R.id.action_refresh_active_boosters){
            updateActiveBoosters();
            Toast.makeText(this.getApplicationContext(), "Updating Booster List", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_get_detailed_boosters){
            new AlertDialog.Builder(this)
                    .setTitle("Activated Boosters per Game").setMessage(parseStats(null))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String parseStats(ArrayList<BoosterDescription> incomplete){
        ArrayList<BoosterDescription> check;
        if (incomplete != null){
            check = incomplete;
        } else {
             check = MainStaticVars.boosterList;
        }
        int quake = 0,walls = 0,pb = 0,bsg = 0,tnt = 0,vz = 0,mw = 0,arcade = 0,arena = 0,cac = 0,unknown = 0, uhc = 0, war = 0;
        int quakeSec = 0,wallsSec = 0,pbSec = 0,bsgSec = 0,tntSec = 0,
                vzSec = 0,mwSec = 0,arcadeSec = 0,arenaSec = 0,cacSec = 0,unknownSec = 0, uhcSec = 0, warSec = 0;
        for (BoosterDescription desc : check){
            switch (desc.get_gameType().getId()){
                case 2: quake++; quakeSec += desc.get_timeRemaining(); break;
                case 3: walls++; wallsSec += desc.get_timeRemaining(); break;
                case 4: pb++; pbSec += desc.get_timeRemaining(); break;
                case 5: bsg++; bsgSec += desc.get_timeRemaining(); break;
                case 6: tnt++; tntSec += desc.get_timeRemaining(); break;
                case 7: vz++; vzSec += desc.get_timeRemaining(); break;
                case 13: mw++; mwSec += desc.get_timeRemaining(); break;
                case 14: arcade++; arcadeSec += desc.get_timeRemaining(); break;
                case 17: arena++; arenaSec += desc.get_timeRemaining(); break;
                case 21: cac++; cacSec += desc.get_timeRemaining(); break;
                case 20: uhc++; uhcSec += desc.get_timeRemaining(); break;
                case 23: war++; warSec += desc.get_timeRemaining(); break;
                default: unknown++; unknownSec += desc.get_timeRemaining(); break;
            }
        }
        //Check if present then parse
        StringBuilder bu = new StringBuilder();
        bu.append("Based on last booster query: \n\n");
        if (quake != 0){
            bu.append(GameType.QUAKECRAFT.getName()).append(": ").append(quake).append("\n").append(createTimeLeftString(quakeSec)).append("\n");
        }
        if (walls != 0){
            bu.append(GameType.WALLS.getName()).append(": ").append(walls).append("\n").append(createTimeLeftString(wallsSec)).append("\n");
        }
        if (pb != 0){
            bu.append(GameType.PAINTBALL.getName()).append(": ").append(pb).append("\n").append(createTimeLeftString(pbSec)).append("\n");
        }
        if (bsg != 0){
            bu.append(GameType.SURVIVAL_GAMES.getName()).append(": ").append(bsg).append("\n").append(createTimeLeftString(bsgSec)).append("\n");
        }
        if (tnt != 0){
            bu.append(GameType.TNTGAMES.getName()).append(": ").append(tnt).append("\n").append(createTimeLeftString(tntSec)).append("\n");
        }
        if (vz != 0){
            bu.append(GameType.VAMPIREZ.getName()).append(": ").append(vz).append("\n").append(createTimeLeftString(vzSec)).append("\n");
        }
        if (mw != 0){
            bu.append(GameType.WALLS3.getName()).append(": ").append(mw).append("\n").append(createTimeLeftString(mwSec)).append("\n");
        }
        if (arcade != 0){
            bu.append(GameType.ARCADE.getName()).append(": ").append(arcade).append("\n").append(createTimeLeftString(arcadeSec)).append("\n");
        }
        if (arena != 0){
            bu.append(GameType.ARENA.getName()).append(": ").append(arena).append("\n").append(createTimeLeftString(arenaSec)).append("\n");
        }
        if (cac != 0){
            bu.append(GameType.MCGO.getName()).append(": ").append(cac).append("\n").append(createTimeLeftString(cacSec)).append("\n");
        }
        if (uhc != 0){
            bu.append(GameType.UHC.getName()).append(": ").append(uhc).append("\n").append(createTimeLeftString(uhcSec)).append("\n");
        }
        if (war != 0){
            bu.append(GameType.WARLORDS.getName()).append(": ").append(war).append("\n").append(createTimeLeftString(warSec)).append("\n");
        }
        if (unknown != 0){
            bu.append("Unknown Game: ").append(unknown).append("\n").append(createTimeLeftString(unknownSec)).append("\n");
            bu.append("(Please Contact Dev of this)");
        }
        return bu.toString();
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
            if (days == 1)
                timeString.append(days).append(" day ");
            else
                timeString.append(days).append(" days ");
        }
        if (hours != 0) {
            if (hours == 1)
                timeString.append(hours).append(" hour ");
            else
                timeString.append(hours).append(" hours ");
        }
        if (minutes != 0) {
            if (minutes == 1)
                timeString.append(minutes).append(" minute ");
            else
                timeString.append(minutes).append(" minutes ");
        }
        if (seconds != 0) {
            if (seconds == 1)
                timeString.append(seconds).append(" second ");
            else
                timeString.append(seconds).append(" seconds ");
        }
        timeString.append(")");
        return timeString.toString();
    }
}
