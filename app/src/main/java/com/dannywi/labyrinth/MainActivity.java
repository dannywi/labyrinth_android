package com.dannywi.labyrinth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private LabyrinthView labyrinthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        labyrinthView = new LabyrinthView(this);
        labyrinthView.startSensor();
        setContentView(labyrinthView);
    }
}
