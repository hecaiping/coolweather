package comb.example.administrator.coolweather.gson;

import android.text.style.UpdateAppearance;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/12/5 0005.
 */

//基本信息
public class Basic {
    @SerializedName("city")
    public  String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
