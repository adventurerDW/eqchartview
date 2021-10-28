package com.wenx.simple;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wenx.eqviews.EqChartView;

/**
 * Created By WenXiong on 2021/10/21.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private EqChartView mChartView;

    private ImageView imageView;

    private EditText etX, etY;
    private Button sendBtn;
    private Button setDelTypeBtn;
    private Button setNewPointBtn;
    private Button setPointBtn;
    private Button setRandomDataBtn;
    private Button setBgColorBtn;
    private Button setBgCustomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        etX = findViewById(R.id.et_axisx_value);
        etY = findViewById(R.id.et_axisy_value);
        sendBtn = findViewById(R.id.btn_set_axis_value);
        setDelTypeBtn = findViewById(R.id.btn_set_bond_type);
        setNewPointBtn = findViewById(R.id.btn_set_new_point);
        setPointBtn = findViewById(R.id.btn_set_point);
        setRandomDataBtn = findViewById(R.id.btn_set_random_data);
        setBgColorBtn = findViewById(R.id.btn_set_bg_color);
        setBgCustomBtn = findViewById(R.id.btn_set_bg_custom);
        setListener(sendBtn, setDelTypeBtn, setNewPointBtn, setPointBtn, setRandomDataBtn, setBgColorBtn, setBgCustomBtn);

        imageView = findViewById(R.id.iv_test);
        imageView.setImageLevel(level);

        mChartView = findViewById(R.id.eq_view);
        mChartView.setOnSlideListener(new EqChartView.OnSlideListener() {
            @Override
            public void onDataBack(float[] freqs, float[] gains) {
                Log.e(TAG, "抬手时EQ如下:\nfreqs: " + new Gson().toJson(freqs).replace(",", " ,  ")
                        + "\ngains: " + new Gson().toJson(gains).replace(",", " ,  "));

                Toast.makeText(MainActivity.this, new Gson().toJson(freqs).replace(",", " ,  ")
                        + "\n" + new Gson().toJson(gains).replace(",", " ,  "), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean needDel = true;
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_axis_value:
                String xStr = etX.getText().toString();
                String yStr = etY.getText().toString();
                if (!xStr.contains(",") || !yStr.contains(",")) {
                    Toast.makeText(MainActivity.this, "数据需,隔开", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] strX = xStr.split(",");
                String[] strY = yStr.split(",");
                mChartView.setAxisText(strX, strY);
                break;
            case R.id.btn_set_bond_type:
                mChartView.setBorderDelete(needDel);
                setDelTypeBtn.setText(needDel ? "关闭边距删除" : "设置边距删除");
                needDel = !needDel;
                break;
            case R.id.btn_set_new_point:
                mChartView.setNewPointsBackGround(R.drawable.bg_test_shape, false);
                break;
            case R.id.btn_set_point:
                mChartView.setPointsBackGround(R.drawable.bg_chartview_64, false);
                break;
            case R.id.btn_set_random_data:
                float[] f1 = new float[]{800.f, 1111.f, 2828.f, 4848.f, 6464.f, 7878.f, 9191.f, 13333.f};
                float[] f2 = new float[]{1.f  , 0.f   , 2.5f  , 3.16f , -3.15f, -2.1f , 4.6f  , 6.f};
                mChartView.refreshData(f1, f2);
                break;
            case R.id.btn_set_bg_color:
                mChartView.setCanvasBg(R.color.testblue1);
                break;
            case R.id.btn_set_bg_custom:
                mChartView.setCanvasCustom(R.mipmap.ic_launcher);
                break;
        }
    }

    private void setListener(View... views){
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }






    private int level = 0;
    private boolean isStart = false;
    public void click1027(View view){
        isStart = !isStart;
        mThread.start();
    }
    private HThread mThread = new HThread();
    class HThread extends Thread {
        @Override
        public void run() {
            while (isStart) {
                level = level >= 11 ? 0 : level++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageLevel(level);
                    }
                });

                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}