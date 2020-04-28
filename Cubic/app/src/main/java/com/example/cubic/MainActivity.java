package com.example.cubic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextView your_points;
    TextView robot_points;
    TextView first_cube_points;
    TextView second_cube_points;
    Button btn;
    Random random = new Random();

    int common_your = 0;
    int common_robots = 0;
    int first_rand;
    int second_rand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        your_points = findViewById(R.id.your_points);
        robot_points = findViewById(R.id.robot_points);
        first_cube_points = findViewById(R.id.first_cube_points);
        second_cube_points = findViewById(R.id.second_cube_points);
        btn = findViewById(R.id.button);
    }
    public void onClick(View view) {
        if (!addUserPoints()) {
           btn.setText("Ход компьютера");
           btn.setEnabled(false);
           btn.setBackgroundColor(Color.parseColor("#A1FF9800"));
           Handler handler = new Handler();
                 handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = true;
                        while (b) {
                            b = addRobotPoints();
                        }
                        btn.setBackgroundColor(Color.parseColor("#FF9800"));
                        btn.setText("Сделать ход");
                        btn.setEnabled(true);
                    }
                 }, 1200);
                 if (common_robots >= 100) {
                      Intent i2 = new Intent(this, LooseActivity.class);
                      your_points.setText("0");
                      robot_points.setText("0");
                      first_cube_points.setText("0");
                      second_cube_points.setText("0");
                      common_your = 0;
                      common_robots = 0;
                      first_rand = 0;
                      second_rand = 0;
                      startActivity(i2);
                 }
        }
        if (common_your >= 100 && common_robots >= 100) {
            Intent i3 = new Intent(this, NobodyActivity.class);
            your_points.setText("0");
            robot_points.setText("0");
            first_cube_points.setText("0");
            second_cube_points.setText("0");
            common_your = 0;
            common_robots = 0;
            first_rand = 0;
            second_rand = 0;
            startActivity(i3);
        } else  if (common_your >= 100) {
            Intent i = new Intent(this, WinActivity.class);
            your_points.setText("0");
            robot_points.setText("0");
            first_cube_points.setText("0");
            second_cube_points.setText("0");
            common_your = 0;
            common_robots = 0;
            first_rand = 0;
            second_rand = 0;
            startActivity(i);
        }

    }
    public boolean addUserPoints() {
        first_rand = 1+random.nextInt(6);
        first_cube_points.setText(Integer.toString(first_rand));
        common_your += first_rand;
        second_rand = 1+random.nextInt(6);
        second_cube_points.setText(Integer.toString(second_rand));
        common_your += second_rand;
        your_points.setText(Integer.toString(common_your));
        return first_rand == second_rand;
    }
    public boolean addRobotPoints() {
       first_rand = 1+random.nextInt(6);
       first_cube_points.setText(Integer.toString(first_rand));
       common_robots += first_rand;
       second_rand = 1+random.nextInt(6);
       second_cube_points.setText(Integer.toString(second_rand));
       common_robots += second_rand;
       robot_points.setText(Integer.toString(common_robots));
       return first_rand == second_rand;
    }
}