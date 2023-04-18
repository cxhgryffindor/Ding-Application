package utar.edu.my.registerlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public class focus_page extends AppCompatActivity {

    private int timeSelected = 0;
    private CountDownTimer timeCountDown = null;
    private int timeProgress = 0;
    private long pauseOffSet = 0;
    private boolean isStart = true;

    //new adding
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_page);

        //Bottom menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomViewNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_focus);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_home:
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    return true;
                case R.id.bottom_focus:
                    return true;
                case R.id.bottom_profile:
                    startActivity(new Intent(getApplicationContext(),StatisticActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    return true;
            }
            return false;
        });

        ImageButton addBtn = findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimeFunction();
            }
        });

        Button startBtn = findViewById(R.id.btnPlayPause);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimerSetup();
            }
        });

        ImageButton resetBtn = findViewById(R.id.ib_reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime();
            }
        });

        TextView addTimeTv = findViewById(R.id.tv_addTime);
        addTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExtraTime();
            }
        });

        /*//Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Records");*/

    }

    private void addExtraTime() {

        ProgressBar progressBar = findViewById(R.id.pbTimer);
        if (timeSelected != 0) {
            timeSelected += 60;
            progressBar.setMax(timeSelected);
            timePause();
            startTimer(pauseOffSet);
            Toast.makeText(this, "60 seconds added!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer(long pauseOffSetL) {
        ProgressBar progressBar = findViewById(R.id.pbTimer);
        progressBar.setProgress(timeProgress);

        timeCountDown = new CountDownTimer((timeSelected * 1000L) - pauseOffSetL * 1000L, 1000) {
            @Override
            public void onTick(long p0) {
                timeProgress++;
                pauseOffSet = timeSelected - p0 / 1000;
                progressBar.setProgress(timeSelected - timeProgress);
                TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
                timeLeftTv.setText(String.valueOf(timeSelected - timeProgress));
            }

            @Override
            public void onFinish() {

                resetTime();
                Toast.makeText(focus_page.this, "Time Up!", Toast.LENGTH_SHORT).show();

                // soundEffect
                MediaPlayer mediaPlayer = MediaPlayer.create(focus_page.this, R.raw.alarm_time_up);
                mediaPlayer.start();

                /*//Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Records");

                // 获取 add_dialog.xml 中的 EditText 的文本
                Dialog timeDialog = new Dialog(MainActivity.this);
                timeDialog.setContentView(R.layout.add_dialog);
                EditText timeRecordEditText = timeDialog.findViewById(R.id.etGetTime);
                String timeRecordText = timeRecordEditText.getText().toString();

                // 尝试将输入的文本转换为整数，如果转换失败则设置默认值为0
                int timeRecordValue;
                try {
                    timeRecordValue = Integer.parseInt(timeRecordText);
                } catch (NumberFormatException e) {
                    timeRecordValue = 0;
                    e.printStackTrace();
                }

                // 获取 activity_main.xml 中的 EditText 的文本
                EditText etActivityName = findViewById(R.id.et_activityName);
                String activityNameText = etActivityName.getText().toString();

                // add data to firebase
                myRef.child("activityName").setValue(activityNameText);
                myRef.child("time").setValue(timeRecordValue, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Log.e(TAG, "Data could not be saved: " + error.getMessage());
                        } else {
                            Log.d(TAG, "Data saved successfully.");
                        }
                    }
                });*/

                showDialog();

            }
        }.start();
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    // show Dialog
    private void showDialog() {
        Dialog timeDialog = new Dialog(focus_page.this);
        timeDialog.setContentView(R.layout.add_dialog);
        EditText timeRecordEditText = timeDialog.findViewById(R.id.etGetTime);
        timeRecordEditText.setHint("Enter your time again");

        Button saveButton = timeDialog.findViewById(R.id.btnOK);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeRecordText = timeRecordEditText.getText().toString();
                int timeRecordValue;
                try {
                    timeRecordValue = Integer.parseInt(timeRecordText);
                } catch (NumberFormatException e) {
                    timeRecordValue = 0;
                    e.printStackTrace();
                }

                EditText etActivityName = findViewById(R.id.et_activityName);
                String activityNameText = etActivityName.getText().toString();

                //get username id

                // 生成一个新的唯一节点
                String key = myRef.child("Records").push().getKey();

                // 将 "activityName" 和 "time" 作为其子节点添加到新节点中
                Map<String, Object> recordValues = new HashMap<>();
                //recordValues.put("username", userNameText);
                recordValues.put("activityName", activityNameText);
                recordValues.put("time", timeRecordValue);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/Focus_time/" + key, recordValues);

                // add data to firebase
                myRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Log.e(TAG, "Data could not be saved: " + error.getMessage());
                        } else {
                            Log.d(TAG, "Data saved successfully.");
                        }
                    }
                });

                timeDialog.dismiss();
            }
        });

        timeDialog.show();
    }

    private void timePause() {
        if (timeCountDown != null) {

            timeCountDown.cancel();

        }
    }

    private void resetTime() {
        if (timeCountDown != null) {

            timeCountDown.cancel();
            timeProgress = 0;
            timeSelected = 0;
            pauseOffSet = 0;
            timeCountDown = null;

            Button startBtn = findViewById(R.id.btnPlayPause);
            startBtn.setText("Start");
            isStart = true;

            ProgressBar progressBar = findViewById(R.id.pbTimer);
            progressBar.setProgress(0);

            TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
            timeLeftTv.setText("0");

        }
    }

    private void startTimerSetup() {

        Button startBtn = findViewById(R.id.btnPlayPause);
        if (timeSelected > timeProgress) {

            if (isStart) {

                startBtn.setText("PAUSE");
                startTimer(pauseOffSet);
                isStart = false;

            } else {

                isStart = true;
                startBtn.setText("RESUME");
                timePause();

            }
        } else {

            Toast.makeText(this, "Enter Time", Toast.LENGTH_SHORT).show();

        }
    }

    private void setTimeFunction() {
        final Dialog timeDialog = new Dialog(this);
        timeDialog.setContentView(R.layout.add_dialog);
        final EditText timeSet = timeDialog.findViewById(R.id.etGetTime);
        final TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
        final Button btnStart = findViewById(R.id.btnPlayPause);
        final ProgressBar progressBar = findViewById(R.id.pbTimer);

        timeDialog.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeSet.getText().toString().isEmpty()) {
                    Toast.makeText(focus_page.this, "Enter Time Duration", Toast.LENGTH_SHORT).show();
                } else {
                    resetTime();
                    timeLeftTv.setText(timeSet.getText());
                    btnStart.setText("Start");
                    timeSelected = Integer.parseInt(timeSet.getText().toString());
                    progressBar.setMax(timeSelected);
                }
                timeDialog.dismiss();
            }
        });
        timeDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeCountDown != null) {
            timeCountDown.cancel();
            timeProgress = 0;
        }
    }

}