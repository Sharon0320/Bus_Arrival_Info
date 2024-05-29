package kr.co.company.bus_arrival_info.controller;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import kr.co.company.bus_arrival_info.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityIntroBinding binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // api 33 이상이면 POST_NOTIFICATIONS 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한 체크
            checkPermission();
        } else {
            // 알람 권한 설정 api 31 이상
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // 알람 권한 설정 확인
                checkAlarmPermission(0);
            } else {
                // 메인으로 이동
                goMain();
            }
        }
    }

    /* 권한 체크 */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermission() {
        // 권한 체크
        Dexter.withContext(this)
                .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        // 권한 허용

                        // 알람 권한 설정 api 31 이상
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // 알람 권한 설정 확인
                            checkAlarmPermission(0);
                        } else {
                            // 메인으로 이동
                            goMain();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // 권한 거부
                        Toast.makeText(IntroActivity.this, "앱을 이용하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        // 권한 거부시 설정 다이얼로그 보여주기
                        showPermissionRationale(permissionToken);
                    }
                })
                .withErrorListener(dexterError -> {
                    // 권한설정 오류
                    Toast.makeText(this, "오류", Toast.LENGTH_SHORT).show();
                }).check();
    }

    /* 만약 권한을 거절했을 경우, 다이얼로그 보여주기 */
    private void showPermissionRationale(PermissionToken token) {
        new AlertDialog.Builder(this)
                .setPositiveButton("수락", (dialog, which) -> {
                    // 권한 요청 허용
                    token.continuePermissionRequest();
                })
                .setNegativeButton("거부", (dialog, which) -> {
                    // 권한 요청 취소
                    // 다시 권한요청 후 거부했을 경우 (onPermissionRationaleShouldBeShown) 메서드가 다시 실행 안됨 (권한 설정 못함) -> 앱 설정에서 직접 권한설정을 해야함
                    token.cancelPermissionRequest();
                })
                .setCancelable(false)
                .setMessage("앱을 이용하기 위해 권한이 필요합니다.")
                .show();
    }

    /* 알람 권한 설정 api 31 이상 */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkAlarmPermission(int flag) {
        // 알람 권한 허용 여부
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()) {
            // 권한 허용이면 메인으로 이동
            goMain();
        } else {
            // 권한이 거부됨
            if (flag == 0) {
                // (알람 권한 요청)
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                this.activityLauncher.launch(intent);
            } else {
                Toast.makeText(this, "앱을 이용하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* 메인으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, NotificationMain.class);
        startActivity(intent);
        finish();
    }

    /* ActivityForResult 알람 권한 요청 후 결과 */
    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // api 31 이상
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // 알람 권한 설정 확인
                    checkAlarmPermission(1);
                }
            });
}