package comb.example.administrator.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import comb.example.administrator.coolweather.gson.Forecast;
import comb.example.administrator.coolweather.gson.Weather;
import comb.example.administrator.coolweather.util.HttpUtil;
import comb.example.administrator.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/6 0006.
 */

public class WeatherActivity extends AppCompatActivity{

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView weatherInfoText;
    private TextView degreeText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ScrollView weatherLayout;
    private LinearLayout forecastLayout;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;  //swipeRefresh、mWeatherId  手动更新天气
    private String mWeatherId;
    public DrawerLayout drawerLayout;   //切换城市
    private Button navButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //背景图和状态栏融合效果(setSystemUiVisibility系统可见行，STABLE 稳定，FULLSCREEN 全屏）
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);  //setStatusBarColor色阶，TRANSPARENT透明
        }
        setContentView(R.layout.activity_weather);

        //初始化五个.xml
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        degreeText = (TextView) findViewById(R.id.degree_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        bingPicImg =(ImageView )findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);  //手动更新天气
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); //切换城市
        navButton = (Button) findViewById(R.id.nav_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            //缓存时解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;   //手动更新天气
            showWeatherInfo(weather);
        }else {
            //无缓存数据返回服务器查询天气
           mWeatherId= getIntent().getStringExtra("weather_id");  //手动更新天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);   //手动更新天气
        }
        //手动更新天气
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        //切换城市
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);  //openDrawer此方法打开滑动菜单
            }
        });


    }

    //根据天气id 请求城市天气信息
    void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=e394c88dbbe64b35809168e49aa9efbc";  //API key
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {     //调用方法向该地址发送请求
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);//swipeRefresh 旋转刷新
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);  //调用U方法将Json 转换成weather
                runOnUiThread(new Runnable() {   //将当前线程切回主线程，用if判断
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){      //status 是ok，则请求天气成功，再将数据储存到 SharedPreferences
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText); //添加数据，静态方法：PreferenceManager.getDefaultSharedPreferences
                            editor.apply();   //调用apply()提交数据
                            mWeatherId = weather.basic.weatherId;  //变量mWeatherId
                            showWeatherInfo(weather);   //用此方法来显示内容
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    //处理并展示Weather 实体类数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ") [1];
        System.out.println("updateTime=========="+updateTime);
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
