package test;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import test.openweathermap.group.GroupResponse;
import test.openweathermap.group.List;
import test.retrofit.CurrentWeatherGroupService;

public class Main {

	public static final String ELASTICSEARCH_URL = "http://192.168.99.100:9200";
	public static final String ELASTICSEARCH_INDEX = "myweather_index";
	public static final String ELASTICSEARCH_TYPE = "myweather_type";

	public static void main(String[] args) {
		initIndex();

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(task, 1, 60 * 10, TimeUnit.SECONDS);
	}

	static Runnable task = new Runnable() {
		@Override
		public void run() {
			System.out.println("Start task");
			GroupResponse response = new Main().executeGroupService();
			response.getList().stream().forEach(list -> post2Es(list));

			System.out.println("Finish task");
		}
	};

	public static void post2Es(List list) {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(String.join("/", ELASTICSEARCH_URL, ELASTICSEARCH_INDEX, ELASTICSEARCH_TYPE))
				.post(RequestBody.create(MediaType.parse("application/json"), convert2Json(list))).build();

		try {
			client.newCall(request).execute();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initIndex() {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(String.join("/", ELASTICSEARCH_URL, ELASTICSEARCH_INDEX))
				.put(RequestBody.create(MediaType.parse("application/json"),
						"{ \"mappings\": { \"" + ELASTICSEARCH_TYPE
								+ "\": { \"_all\": { \"enabled\": false }, \"properties\": { \"dt\": { \"type\": \"date\", \"format\": \"epoch_second\" } } } } }"))
				.build();
		try {
			client.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GroupResponse executeGroupService() {
		Retrofit retrofit = new Retrofit.Builder().baseUrl("http://api.openweathermap.org")
				.addConverterFactory(JacksonConverterFactory.create()).build();
		CurrentWeatherGroupService service = retrofit.create(CurrentWeatherGroupService.class);

		Call<GroupResponse> groupApi = service.get("1850147,6058560,4915545", "metric",
				"c4b442464d185f7909e756fde8ab1bda");
		Response<GroupResponse> result = null;
		try {
			result = groupApi.execute();
			return result.body();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// result.raw().body().close();
		}

		return new GroupResponse();
	}

	public static String convert2Json(Object object) {
		String str = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			str = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return str;
	}
}
