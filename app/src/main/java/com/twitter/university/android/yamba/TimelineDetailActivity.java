package com.twitter.university.android.yamba;

import android.os.Bundle;


public class TimelineDetailActivity extends YambaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_detail);

        ((TimelineDetailFragment) getFragmentManager()
            .findFragmentById(R.id.fragment_timeline_detail))
            .setDetails(getIntent().getExtras());
    }
}
