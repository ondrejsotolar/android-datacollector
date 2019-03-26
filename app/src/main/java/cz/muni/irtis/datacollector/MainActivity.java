package cz.muni.irtis.datacollector;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

public class MainActivity extends AppCompatActivity {
    //DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        DatabaseHelper.getInstance(this);
        SchedulerService.startMeUp(this);
        super.onStart();
    }
}
