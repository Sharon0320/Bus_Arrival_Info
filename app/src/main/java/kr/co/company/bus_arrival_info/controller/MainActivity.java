package kr.co.company.bus_arrival_info.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.company.bus_arrival_info.R;
import kr.co.company.bus_arrival_info.model.BusInfo;
import kr.co.company.bus_arrival_info.model.Station;

public class MainActivity extends AppCompatActivity {

    private String getData;
    private EditText editStation;
    private EditText editBusNum;
    private ListView listView;
    private ListView listView2;
    private TextView alarmtext;
    private String stationData;
    private String busNum;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    private TimePicker timePicker;
    private Button alarmSetButton;

    private boolean isTimerTaskOn = false;
    TimerTask timerTask;
    Timer timer;

    private ArrayList<Station> stations = new ArrayList<Station>();
    private ArrayList<String> stationNames = new ArrayList<String>();
    private ArrayList<BusInfo> busInfos = new ArrayList<BusInfo>();
    private ArrayList<String> busInfoStrings = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editStation = (EditText) findViewById(R.id.editStation);
        editBusNum = (EditText) findViewById(R.id.editBusNum);
        listView = (ListView) findViewById(R.id.listview);
        listView2 = (ListView) findViewById(R.id.listview2);
        alarmtext = (TextView) findViewById(R.id.alarmtext);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmSetButton = (Button) findViewById(R.id.alarm_set_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stationNames);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busInfoStrings);
        listView.setAdapter(adapter);
        listView2.setAdapter(adapter2);

        // Initially hide listView2
        listView2.setVisibility(View.GONE);

        // Set alarm button click listener
        alarmSetButton.setOnClickListener(v -> setAlarm());
    }

    public void search(View view) throws IOException {
        // Reset visibility of the list views
        listView.setVisibility(View.VISIBLE);
        listView2.setVisibility(View.GONE);

        String stationName = editStation.getText().toString();
        busInfoStrings.clear();
        adapter2.notifyDataSetChanged();

        if (isTimerTaskOn) {
            timerTask.cancel();
            isTimerTaskOn = false;
        }

        new Thread(() -> {
            stations.clear();
            stationNames.clear();

            try {
                getData = DataLoader.apiRequest(stationName);
                Log.d("data", getData);
                stations = DataLoader.ParseStationJson(getData);

                for (Station station : stations) {
                    stationNames.add(station.getName());
                }

                Log.d("data", stationNames.toString());

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            stationData = (String) parent.getItemAtPosition(position);
            String stationId = "";
            for (Station station : stations) {
                if (stationData.equals(station.getName())) {
                    stationId = station.getArsId();
                }
            }
            Toast.makeText(MainActivity.this, stationData + " " + stationId, Toast.LENGTH_SHORT).show();

            busNum = editBusNum.getText().toString();
            if (busNum.isEmpty()) {
                busNum = "None";
            }

            String finalStationId = stationId;
            String finalBusNum = busNum;

            RequestBusInfos(finalStationId, finalBusNum);

            stations.clear();
            stationNames.clear();
            adapter.notifyDataSetChanged();

            // Hide listView and show listView2
            runOnUiThread(() -> {
                listView.setVisibility(View.GONE);
                listView2.setVisibility(View.VISIBLE);
            });
        });

        listView2.setOnItemClickListener((adapterView, view12, i, l) -> {
            String selectedBusInfo = busInfoStrings.get(i);
            Toast.makeText(MainActivity.this, selectedBusInfo, Toast.LENGTH_SHORT).show();
            alarmtext.setText(selectedBusInfo);
        });
    }

    public void renew(View view) {
        adapter2.notifyDataSetChanged();
    }

    private void RequestBusInfos(String stationId, String busNum) {
        busInfos.clear();
        busInfoStrings.clear();
        new Thread(() -> {
            try {
                String jsonData = DataLoader.apiRequest(stationId, busNum);
                busInfos = DataLoader.ParseBusInfoJson(jsonData, busNum);

                for (BusInfo busInfo : busInfos) {
                    String busInfoString = busInfo.getBusRouteAbrv() + " " + busInfo.getAdirection() + "\n" + busInfo.getArrmsg1() + "\n" + busInfo.getArrmsg2();
                    busInfoStrings.add(busInfoString);
                }

                Log.d("data", busInfoStrings.toString());

                runOnUiThread(() -> {
                    adapter2.notifyDataSetChanged();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    private void setAlarm() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Alarm set for " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
        }
    }
}
