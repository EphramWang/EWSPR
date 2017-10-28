package android.serialport.reader.views;

import android.content.Context;
import android.serialport.reader.MainActivity;
import android.serialport.reader.R;
import android.serialport.reader.utils.DataConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ning on 17/8/31.
 */

public class ViewSettings extends LinearLayout implements View.OnClickListener{

    public TextView tvBottomLeft;
    public TextView tvBottomRight;

    public ViewSettings(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.view_settings, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        tvBottomLeft = (TextView) view.findViewById(R.id.tv_bottom_left);
        tvBottomLeft.setOnClickListener(this);
        tvBottomRight = (TextView) view.findViewById(R.id.tv_bottom_right);
        tvBottomRight.setOnClickListener(this);
        view.findViewById(R.id.button_top).setOnClickListener(this);
        view.findViewById(R.id.button_center).setOnClickListener(this);
        view.findViewById(R.id.button_right).setOnClickListener(this);
        view.findViewById(R.id.button_left).setOnClickListener(this);
        view.findViewById(R.id.button_bottom).setOnClickListener(this);


        addView(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_left:
                //vol decrease
                ((MainActivity)getContext()).setVolumeDown();
                break;
            case R.id.button_right:
                //vol inc
                ((MainActivity)getContext()).setVolumeUp();
                break;
            case R.id.button_top:
                //power+
                String currentPower = tvBottomLeft.getText().toString();

                if (currentPower.equals("")) {
                    tvBottomLeft.setText(MainActivity.mPower + 1 + "");
                } else {
                    int p = Integer.parseInt(currentPower) + 1;
                    if (p > 10)
                        p = 10;
                    tvBottomLeft.setText(p + "");
                }
                break;
            case R.id.button_bottom:
                //power-
                String currentPower2 = tvBottomLeft.getText().toString();

                if (currentPower2.equals("")) {
                    tvBottomLeft.setText(MainActivity.mPower + 1 + "");
                } else {
                    int p = Integer.parseInt(currentPower2) - 1;
                    if (p < 1)
                        p = 1;
                    tvBottomLeft.setText(p + "");
                }
                break;
            case R.id.button_center:
                //set power
                String currentPower3 = tvBottomLeft.getText().toString();
                if (currentPower3.length() > 0) {
                    byte content = (byte) Integer.parseInt(currentPower3);
                    ((MainActivity)getContext()).sendBytes(DataConstants.getControlCommandBytes(DataConstants.command_send_power, content));
                }
                break;
        }

    }
}
