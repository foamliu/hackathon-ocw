package org.hackathon_ocw.androidclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by dianyang on 2016/3/12.
 */
public class TabDescription extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tab_description_detail, container, false);

        Bundle b = getActivity().getIntent().getExtras();
        String description = b.getString("description");

        TextView t = (TextView)inflatedView.findViewById(R.id.tabDescription);
        t.setText(description);

        return inflatedView;
    }
}
