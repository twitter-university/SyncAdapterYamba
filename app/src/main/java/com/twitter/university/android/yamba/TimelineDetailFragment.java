package com.twitter.university.android.yamba;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TimelineDetailFragment extends Fragment {

    public static Intent marshallDetails(Context ctxt, long ts, String handle, String tweet) {
        Intent i = new Intent(ctxt, TimelineDetailActivity.class);
        i.putExtra(YambaContract.Timeline.Columns.TIMESTAMP, ts);
        i.putExtra(YambaContract.Timeline.Columns.HANDLE, handle);
        i.putExtra(YambaContract.Timeline.Columns.TWEET, tweet);
        return i;
    }

    public static Fragment newInstance(Bundle args) {
        Fragment details = new TimelineDetailFragment();
        details.setArguments(args);
        return details;
    }


    private View details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle state) {
        View v = inflater.inflate(R.layout.fragment_timeline_detail, root, false);
        details = v.findViewById(R.id.timeline_details);

        setDetails(getArguments());

        return v;
    }

    public void setDetails(Bundle args) {
        if ((null == args) || (null == details)) { return; }

        ((TextView) details.findViewById(R.id.timeline_detail_timestamp))
            .setText(DateUtils.getRelativeTimeSpanString(
                args.getLong(YambaContract.Timeline.Columns.TIMESTAMP, 0L)));
        ((TextView) details.findViewById(R.id.timeline_detail_handle)).setText(
            args.getString(YambaContract.Timeline.Columns.HANDLE));
        ((TextView) details.findViewById(R.id.timeline_detail_tweet)).setText(
            args.getString(YambaContract.Timeline.Columns.TWEET));
    }
}
