package com.mega.carmaintenance.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.mega.carmaintenance.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mExitApp;
    private TextView mRecordsTab;
    private TextView mBookingTab;
    private BookingFragment mBookingFragment;
    private RecordsFragment mRecordsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        mExitApp = findViewById(R.id.exit_app);
        mBookingTab = findViewById(R.id.maintenance_appointment);
        mRecordsTab = findViewById(R.id.maintenance_records);
        mExitApp.setOnClickListener(this);
        mBookingTab.setOnClickListener(this);
        mRecordsTab.setOnClickListener(this);
        mBookingTab.setSelected(true);
        mBookingFragment = new BookingFragment();
        mRecordsFragment = new RecordsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mBookingFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mRecordsFragment).commit();
        getSupportFragmentManager().beginTransaction().hide(mRecordsFragment).commit();
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.exit_app:
               finish();
               break;
           case R.id.maintenance_appointment:
               getSupportFragmentManager().beginTransaction().show(mBookingFragment).commit();
               getSupportFragmentManager().beginTransaction().hide(mRecordsFragment).commit();
               mBookingTab.setSelected(true);
               mRecordsTab.setSelected(false);
               break;
           case R.id.maintenance_records:
               getSupportFragmentManager().beginTransaction().hide(mBookingFragment).commit();
               getSupportFragmentManager().beginTransaction().show(mRecordsFragment).commit();
               mBookingTab.setSelected(false);
               mRecordsTab.setSelected(true);
               break;
           default:
               break;
       }
    }
}