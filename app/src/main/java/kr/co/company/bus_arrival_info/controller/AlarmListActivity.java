package kr.co.company.bus_arrival_info.controller;

import static kr.co.company.bus_arrival_info.controller.NotificationMain.ALARM_LIST_KEY;
import static kr.co.company.bus_arrival_info.controller.NotificationMain.PREFS_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import kr.co.company.bus_arrival_info.R;

public class AlarmListActivity extends AppCompatActivity {

    private ListView alarmListView;
    private ArrayAdapter<String> alarmAdapter;
    private ArrayList<String> alarmListData;
    private Button deleteAllButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        alarmListView = findViewById(R.id.alarm_list_view);
        deleteAllButton = findViewById(R.id.delete_all);
        backButton = findViewById(R.id.back_button);

        alarmListData = getIntent().getStringArrayListExtra("alarmListData");

        alarmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alarmListData);
        alarmListView.setAdapter(alarmAdapter);

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 리스트뷰의 모든 항목 제거
                alarmListData.clear();
                // 어댑터에 변경된 데이터를 알림
                alarmAdapter.notifyDataSetChanged();

                // SharedPreferences에도 변경된 데이터를 저장
                saveAlarmListData();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로 가기 버튼 클릭 시 현재 액티비티 종료
                finish();
            }
        });
    }

    private void saveAlarmListData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        Set<String> alarmSet = new HashSet<>(alarmListData);
        editor.putStringSet(ALARM_LIST_KEY, alarmSet);
        editor.apply();
    }
}
