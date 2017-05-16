package test.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import test.openweathermap.CurrentWeather;

public interface CurrentWeatherService {

	// api.openweathermap.org/data/2.5/weather?q=London,uk
	@GET("/data/2.5/weather")
	Call<CurrentWeather> getCurrentWeather(@Query("q") String q, @Query("units") String units, @Query("appid") String appid);

}
