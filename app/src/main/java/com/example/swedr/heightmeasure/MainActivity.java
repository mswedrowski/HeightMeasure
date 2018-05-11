package com.example.swedr.heightmeasure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.buttonMeasure)
    Button buttonMeasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.buttonMeasure)
    public void onViewClicked()
    {
        Intent goToMeasure = new Intent(this,MeasureActivity.class);
        startActivity(goToMeasure);
    }
}
