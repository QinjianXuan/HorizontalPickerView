package com.example.xuanq.horizontalpickerview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 为了美好假期 我要写一个炫酷吊炸天的demo
 */
public class MainActivity extends AppCompatActivity {
    HorizontalPickerView minute_pv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minute_pv = (HorizontalPickerView) findViewById(R.id.minute_pv);
        List<DataModel> data = new ArrayList<DataModel>();

        DataModel dataModel = new DataModel();
        dataModel.setRate("6.5");
        dataModel.setColor(Color.parseColor("#19c8b7"));
        dataModel.setMonth("无锁定期");
        dataModel.setValue("0");
        data.add(dataModel);

        dataModel = new DataModel();
        dataModel.setRate("8.5");
        dataModel.setColor(Color.parseColor("#f9364d"));
        dataModel.setMonth("1个月-3个月");
        dataModel.setValue("4");
        data.add(dataModel);

        dataModel = new DataModel();
        dataModel.setRate("9.5");
        dataModel.setColor(Color.parseColor("#fd611d"));
        dataModel.setMonth("3个月以上-6个月");
        dataModel.setValue("5");
        data.add(dataModel);

        dataModel = new DataModel();
        dataModel.setRate("10.5");
        dataModel.setColor(Color.parseColor("#5cc5f0"));
        dataModel.setMonth("6个月以上-12个月");
        dataModel.setValue("6");
        data.add(dataModel);


        minute_pv.setData(data);
        minute_pv.setOnSelectListener(new HorizontalPickerView.onSelectListener() {

            @Override
            public void onSelect(String text) {
//                Toast.makeText(MainActivity.this, "选择了 " + text + " 分",
//                        Toast.LENGTH_SHORT).show();

            }
        });
    }
}
