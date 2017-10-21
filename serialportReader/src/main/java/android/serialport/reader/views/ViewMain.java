package android.serialport.reader.views;

import android.content.Context;
import android.serialport.reader.R;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by ning on 17/8/31.
 */

public class ViewMain extends LinearLayout {
    public ViewMain(Context context, View.OnClickListener listener) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.view_main, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        ImageView img1 = (ImageView) view.findViewById(R.id.img1);
        img1.setOnClickListener(listener);
        ImageView img2 = (ImageView) view.findViewById(R.id.img2);
        img2.setOnClickListener(listener);
        ImageView img3 = (ImageView) view.findViewById(R.id.img3);
        img3.setOnClickListener(listener);
        ImageView img4 = (ImageView) view.findViewById(R.id.img4);
        img4.setOnClickListener(listener);

        addView(view);
    }
}
