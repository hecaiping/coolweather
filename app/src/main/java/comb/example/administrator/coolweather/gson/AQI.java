package comb.example.administrator.coolweather.gson;

/**
 * Created by Administrator on 2018/12/5 0005.
 */
  //空气质量
public class AQI {
    public AQICity city;

    public  class AQICity{
        public String aqi;
        public String pm25;
    }
}
