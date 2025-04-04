package vn.iotstar.UploadFile;

import android.os.Message;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface ServiceAPI {
    String BASE_URL = "http://app.iotstar.vn:8081/appfoods/";

    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();

    ServiceAPI serviceapi = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ServiceAPI.class);

    @Multipart
    @POST("upload.php")
    Call<List<ImageUpload>> upload(
            @Part(Const.MY_USERNAME) RequestBody username,
            @Part MultipartBody.Part avatar
    );

    @Multipart
    @POST("upload1.php")
    Call<Message> upload1(
            @Part(Const.MY_USERNAME) RequestBody username,
            @Part MultipartBody.Part avatar
    );
}
