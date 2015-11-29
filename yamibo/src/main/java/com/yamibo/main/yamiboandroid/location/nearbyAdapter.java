package com.yamibo.main.yamiboandroid.location;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yamibo.main.yamiboandroid.R;

import java.util.List;


/**
 * Created by Clover on 2015-11-29.
 */
public class NearbyAdapter extends ArrayAdapter<PersonItem> {
    public NearbyAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public NearbyAdapter(Context context, int resource, List<PersonItem> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.person_item, null);
        }

        //update selcted item
        PersonItem p = getItem(position);


        if (p != null) {
            ImageView logo = (ImageView) v.findViewById(R.id.near_logo);

            TextView tt1 = (TextView) v.findViewById(R.id.near_id);
            TextView tt2 = (TextView) v.findViewById(R.id.near_distance);
            TextView tt3 = (TextView) v.findViewById(R.id.near_intro);

            if (logo != null) {
                if (p.getLogo() != null)
                    logo.setImageDrawable((Drawable) p.getLogo());
                else
                    logo.setImageResource(R.drawable.noavatar_small);
            }

            if (tt1 != null) {
                tt1.setText(p.getId());
            }

            if (tt2 != null) {
                tt2.setText(p.getDistance());
            }

            if (tt3 != null) {
                tt3.setText(p.getIntro());
            }
        }

        return v;
    }

}
