package com.itachi1706.hypixelstatistics.RevampedDesign.RecyclerViewAdapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itachi1706.hypixelstatistics.AsyncAPI.Friends.GetFriendsHead;
import com.itachi1706.hypixelstatistics.AsyncAPI.Players.GetLastOnlineInfoFriends;
import com.itachi1706.hypixelstatistics.AsyncAPI.Session.GetSessionInfoFriends;
import com.itachi1706.hypixelstatistics.Objects.FriendsObject;
import com.itachi1706.hypixelstatistics.R;
import com.itachi1706.hypixelstatistics.RevampedDesign.PlayerInfoActivity;
import com.itachi1706.hypixelstatistics.util.GeneratePlaceholderDrawables;
import com.itachi1706.hypixelstatistics.util.HistoryHandling.HeadHistory;
import com.itachi1706.hypixelstatistics.util.MainStaticVars;
import com.itachi1706.hypixelstatistics.util.MinecraftColorCodes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Kenneth on 10/10/2015.
 * for HypixelStatistics in package com.itachi1706.hypixelstatistics.RevampedDesign.RecyclerViewAdapters
 */
public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.FriendsViewHolder> {

    private List<FriendsObject> items;
    private Activity activity;

    public FriendsRecyclerAdapter(List<FriendsObject> friendsObjects, Activity activity){
        this.items = friendsObjects;
        this.activity = activity;
    }

    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View friendView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_guild_desc, parent, false);
        return new FriendsViewHolder(friendView);
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        FriendsObject object = items.get(position);

        if (object.is_done()) {
            holder.playerName.setText(Html.fromHtml(object.get_mcNameWithRank()));
            if (MainStaticVars.friends_session_data.containsKey(object.getFriendUUID())){
                holder.session.setText(Html.fromHtml(MinecraftColorCodes.parseColors(MainStaticVars.friends_session_data.get(object.getFriendUUID()))));
            } else {
                holder.session.setText("Getting Session Information");
                new GetSessionInfoFriends(holder.session).execute(object.getFriendUUID());
            }
            String timeStamp = new SimpleDateFormat("dd-MMM-yyyy hh:mm a zz").format(new Date(object.getDate()));
            holder.joined.setText("Friends From: " + timeStamp);
            holder.prog.setVisibility(View.VISIBLE);
            holder.head.setImageDrawable(GeneratePlaceholderDrawables.generateFromMcNameWithInitialsConversion(object.get_mcName()));
            if (HeadHistory.checkIfHeadExists(activity.getApplicationContext(), object.get_mcName())){
                holder.head.setImageDrawable(HeadHistory.getHead(activity.getApplicationContext(), object.get_mcName()));
                holder.prog.setVisibility(View.GONE);
                Log.d("HEAD RETRIEVAL", "Retrieved " + object.get_mcName() + "'s Head from device");
            } else {
                new GetFriendsHead(activity.getApplicationContext(), holder.head, holder.prog).execute(object);
            }
            if (MainStaticVars.friends_last_online_data.containsKey(object.getFriendUUID())){
                holder.lastOnline.setText(Html.fromHtml(MinecraftColorCodes.parseColors(MainStaticVars.friends_last_online_data.get(object.getFriendUUID()))));
            } else {
                holder.lastOnline.setText("Getting Last Online Information...");
                new GetLastOnlineInfoFriends(holder.lastOnline).execute(object.getFriendUUID());
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView playerName, session, joined, lastOnline;
        protected ImageView head;
        protected ProgressBar prog;

        public FriendsViewHolder(View v){
            super(v);
            playerName = (TextView) v.findViewById(R.id.tvPlayerName);
            session = (TextView) v.findViewById(R.id.tvPlayerStatus);
            joined = (TextView) v.findViewById(R.id.tvPlayerJoined);
            lastOnline = (TextView) v.findViewById(R.id.tvTimeStatus);
            head = (ImageView) v.findViewById(R.id.ivHead);
            prog = (ProgressBar) v.findViewById(R.id.pbPlayerHeadProg);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            final FriendsObject item = items.get(position);

            String message;
            if (item.isSendFromOwner()){
                message = "Friend Request sent by " + MainStaticVars.friendOwner + "<br />";
            } else {
                message = "Sent Friend Request to " + MainStaticVars.friendOwner + "<br />";
            }
            String timeStamp = new SimpleDateFormat("dd-MMM-yyyy hh:mm a zz").format(new Date(item.getDate()));
            message += "Friends From: " + timeStamp;
            new AlertDialog.Builder(v.getContext()).setTitle(Html.fromHtml(item.get_mcNameWithRank()))
                    .setMessage(Html.fromHtml(message))
                    .setPositiveButton("View Player Info", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intentE = new Intent(activity, PlayerInfoActivity.class);
                            intentE.putExtra("player", item.get_mcName());
                            activity.startActivity(intentE);
                        }
                    }).setNegativeButton("Close", null).show();
        }
    }

}
