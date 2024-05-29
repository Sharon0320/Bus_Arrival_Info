package kr.co.company.bus_arrival_info.controller;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.company.bus_arrival_info.R;
import kr.co.company.bus_arrival_info.model.BusInfo;
import kr.co.company.bus_arrival_info.model.Station;

public class NotificationMain extends AppCompatActivity {

    private static final int REQUEST_EXACT_ALARM_PERMISSION = 1;

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
    private String finalBusNum;

    private Button alarmListButton;

    public static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARM_NO_KEY = "alarm_no";
    public static final String ALARM_LIST_KEY = "alarm_list";
    private int alarmNo;

    private boolean isTimerTaskOn = false;
    TimerTask timerTask;
    Timer timer;

    private ArrayList<Station> stations = new ArrayList<Station>();
    private ArrayList<String> stationNames = new ArrayList<String>();
    private ArrayList<BusInfo> busInfos = new ArrayList<BusInfo>();
    private ArrayList<String> busInfoStrings = new ArrayList<String>();
    private ArrayList<String> alarmListData = new ArrayList<>();
    private String selectedBusInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editStation = findViewById(R.id.editStation);
        editBusNum = findViewById(R.id.editBusNum);
        listView = findViewById(R.id.listview);
        listView2 = findViewById(R.id.listview2);
        alarmtext = findViewById(R.id.alarmtext);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        Button alarmSetButton = findViewById(R.id.alarm_set_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stationNames);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busInfoStrings);
        listView.setAdapter(adapter);
        listView2.setAdapter(adapter2);

        listView2.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        alarmNo = prefs.getInt(ALARM_NO_KEY, 0);

        loadAlarmListData();

        alarmSetButton.setOnClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            setAlarm(hour, minute, selectedBusInfo);
            addAlarmToList(stationData, selectedBusInfo, hour, minute);
            saveAlarmListData();
        });

        alarmListButton = findViewById(R.id.alarm_list_button);
        alarmListButton.setOnClickListener(view -> openAlarmListActivity());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            stationData = (String) parent.getItemAtPosition(position);
            String stationId = "";
            for (Station station : stations) {
                if (stationData.equals(station.getName())) {
                    stationId = station.getArsId();
                }
            }
            Toast.makeText(NotificationMain.this, stationData + " " + stationId, Toast.LENGTH_SHORT).show();

            busNum = editBusNum.getText().toString();
            if (busNum.isEmpty()) {
                busNum = "None";
            }

            String finalStationId = stationId;
            finalBusNum = busNum;

            RequestBusInfos(finalStationId, finalBusNum);

            stations.clear();
            stationNames.clear();
            adapter.notifyDataSetChanged();

            runOnUiThread(() -> {
                listView.setVisibility(View.GONE);
                listView2.setVisibility(View.VISIBLE);
            });
        });

        listView2.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedBusInfo = busInfoStrings.get(i);
            Toast.makeText(NotificationMain.this, selectedBusInfo, Toast.LENGTH_SHORT).show();
            alarmtext.setText(selectedBusInfo);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(ALARM_NO_KEY, alarmNo);
        saveAlarmListData(editor);
        editor.apply();
    }

    public void search(View view) throws IOException {
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

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();
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

                runOnUiThread(() -> adapter2.notifyDataSetChanged());
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(int hour, int minute, String busInfo) {
        if (busInfo == null || busInfo.isEmpty()) {
            Toast.makeText(this, "Please select a bus", Toast.LENGTH_SHORT).show();
            return;
        }

        this.alarmNo++;

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("alarm_no", alarmNo);
        intent.putExtra("title", busInfo);
        intent.putExtra("message", "Alarm set for " + hour + ":" + String.format("%02d", minute));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.alarmNo, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Alarm set for " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
    }


    private void addAlarmToList(String stationData, String busNum, int hour, int minute) {
        if (stationData == null) {
            Toast.makeText(this, "Please select a bus", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeString = String.format("%02d:%02d", hour, minute);
        String alarmInfo = "Station: " + stationData + "\nBus: " + busNum + "\nTime: " + timeString;
        alarmListData.add(0,alarmInfo);
    }

    private void openAlarmListActivity() {
        loadAlarmListData(); // SharedPreferences에서 최신 데이터 로드
        Intent intent = new Intent(NotificationMain.this, AlarmListActivity.class);
        intent.putStringArrayListExtra("alarmListData", alarmListData);
        startActivity(intent);
    }

    private void loadAlarmListData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> alarmSet = prefs.getStringSet(ALARM_LIST_KEY, new HashSet<>());
        alarmListData.clear();
        alarmListData.addAll(alarmSet);
    }

    private void saveAlarmListData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        saveAlarmListData(editor);
        editor.apply();
    }

    private void saveAlarmListData(SharedPreferences.Editor editor) {
        Set<String> alarmSet = new HashSet<>(alarmListData);
        editor.putStringSet(ALARM_LIST_KEY, alarmSet);
    }
}
