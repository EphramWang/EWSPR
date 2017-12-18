package android.serialport.reader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.serialport.SerialPortFinder;
import android.serialport.reader.utils.DataConstants;
import android.serialport.reader.utils.TimePickerDialog;
import android.serialport.reader.utils.Utils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by ning on 17/9/13.
 * 需要输入密码进入的设置
 */

public class Pref2Activity extends PreferenceActivity  implements TimePickerDialog.TimePickerDialogInterface  {

    private Application mApplication;
    private SerialPortFinder mSerialPortFinder;

    TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_serialport);

        findViewById(R.id.titlebar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mApplication = (Application) getApplication();
        mSerialPortFinder = mApplication.mSerialPortFinder;

        addPreferencesFromResource(R.xml.main_preferences2);

        //数字放大增益设置
        final ListPreference SZFDZY = (ListPreference) findPreference("SZFDZY");
        SZFDZY.setSummary(SZFDZY.getValue());
        SZFDZY.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                byte content = (byte) Integer.parseInt((String) newValue);
                EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_szfdzy, content)));
                MainActivity.mSZFDZY = content;
                return true;
            }
        });

        //数字本振频率设置
        final EditTextPreference SZBZPL = (EditTextPreference) findPreference("SZBZPL");
        SZBZPL.setSummary(SZBZPL.getText());
        SZBZPL.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                byte content = (byte) Integer.parseInt((String) newValue);
                EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_szbzpl, content)));
                MainActivity.mSZBZPL = content;
                return true;
            }
        });


//        // Devices
//        final ListPreference devices = (ListPreference) findPreference("DEVICE");
//        String[] entries = mSerialPortFinder.getAllDevices();
//        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
//        devices.setEntries(entries);
//        devices.setEntryValues(entryValues);
//        devices.setSummary(devices.getValue());
//        devices.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary((String) newValue);
//                return true;
//            }
//        });


//        // Baud rates
//        final ListPreference baudrates =  (ListPreference) findPreference("BAUDRATE");
//        baudrates.setSummary(baudrates.getValue());
//        baudrates.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary((String) newValue);
//                return true;
//            }
//        });

        //二次谐波门限设置
        final EditTextPreference thbase2 = (EditTextPreference) findPreference("thbase2");
        thbase2.setSummary(MainActivity.TH_base2 + "");
        thbase2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int thbase = Integer.parseInt((String) newValue);
                if (thbase >= 15 && thbase <= 60) {
                    preference.setSummary((String) newValue);
                    MainActivity.TH_base2 = thbase;
                    return true;
                } else {
                    return false;
                }
            }
        });
        //3次谐波门限设置
        final EditTextPreference thbase3 = (EditTextPreference) findPreference("thbase3");
        thbase3.setSummary(MainActivity.TH_base3 + "");
        thbase3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int thbase = Integer.parseInt((String) newValue);
                if (thbase >= 30 && thbase <= 60) {
                    preference.setSummary((String) newValue);
                    MainActivity.TH_base3 = thbase;
                    return true;
                } else {
                    return false;
                }
            }
        });
        //二次谐波标定增益设置
        final EditTextPreference gain2 = (EditTextPreference) findPreference("gain2");
        gain2.setSummary(gain2.getText() != null ? gain2.getText() : "1.00");
        gain2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                float gain = Float.parseFloat((String) newValue);
                if (gain >= 0.5f && gain <= 1.0f) {
                    preference.setSummary((String) newValue);
                    MainActivity.Gain2 = gain;
                    return true;
                } else {
                    return false;
                }
            }
        });
        //3次谐波标定增益设置
        final EditTextPreference gain3 = (EditTextPreference) findPreference("gain3");
        gain3.setSummary(gain3.getText() != null ? gain3.getText() : "0.8");
        gain3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                float gain = Float.parseFloat((String) newValue);
                if (gain >= 0.5f && gain <= 1.0f) {
                    preference.setSummary((String) newValue);
                    MainActivity.Gain3 = gain;
                    return true;
                } else {
                    return false;
                }
            }
        });


        // checksum
        /*final ListPreference checksum =  (ListPreference) findPreference("checksum");
        checksum.setSummary(checksum.getEntry());
        checksum.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });*/

    }

    @Override
    public void positiveListener() {
//        Calendar c = Calendar.getInstance();
//        int hour = timePickerDialog.getHour();
//        int minute = timePickerDialog.getMinute();
//        c.set(timePickerDialog.getYear(), timePickerDialog.getMonth(), timePickerDialog.getDay(),hour, minute);
//
//        long when = c.getTimeInMillis();
//
//        if(when / 1000 < Integer.MAX_VALUE){
//            ((AlarmManager)PrefActivity.this.getSystemService(Context.ALARM_SERVICE)).setTime(when);
//        }

    }

    @Override
    public void negativeListener() {

    }
}
