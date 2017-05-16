package test.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import test.openweathermap.group.GroupResponse;

public interface CurrentWeatherGroupService {

	// http://api.openweathermap.org/data/2.5/group?id=524901,703448,2643743&units=metric
	@GET("/data/2.5/group")
	Call<GroupResponse> get(@Query("id") String id, @Query("units") String units, @Query("appid") String appid);

}
