package vn.iotstar.UploadFile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iotstar.R;

public class MainActivity extends AppCompatActivity {

    Button btnChoose, btnUpload;
    ImageView imageViewChoose, imageViewUpload;
    EditText editTextUserName;
    TextView textViewUsername;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    public static final int MY_REQUEST_CODE = 100;
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gọi hàm ánh xạ
        AnhXa();

        // Khởi tạo ProgressDialog
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Please wait upload .... ");

        // Bắt sự kiện nút chọn ảnh
        btnChoose.setOnClickListener(v -> CheckPermission());

        // Bắt sự kiện upload ảnh
        btnUpload.setOnClickListener(v -> {
            if (mUri != null) {
                UploadImage1();
            } else {
                Toast.makeText(MainActivity.this, "Vui lòng chọn ảnh trước!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(this, permissions(), MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private String[] permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.e(TAG, "onActivityResult");

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        return;
                    }

                    Uri uri = data.getData();
                    mUri = uri;

                    Log.e(TAG, "Received URI: " + uri.toString());
                    String path = RealPathUtil.getRealPath(this, uri);
                    Log.e(TAG, "Real file path: " + path);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        imageViewChoose.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewUpload = findViewById(R.id.imgMultipart);
        editTextUserName = findViewById(R.id.editUserName);
        textViewUsername = findViewById(R.id.tvUsername);
        imageViewChoose = findViewById(R.id.imgChoose);
    }

    public void UploadImage1() {
        mProgressDialog.show();

        // Khai báo biến và lấy dữ liệu từ EditText
        String username = editTextUserName.getText().toString().trim();
        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);

        // Lấy đường dẫn thực của ảnh
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        Log.e(TAG, "IMAGE_PATH: " + IMAGE_PATH);

        if (IMAGE_PATH != null && !IMAGE_PATH.isEmpty()) {
            File file = new File(IMAGE_PATH);

            // Kiểm tra nếu file tồn tại
            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part partbodyavatar = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

                // Gọi Retrofit API
                ServiceAPI.serviceapi.upload(requestUsername, partbodyavatar).enqueue(new Callback<List<ImageUpload>>() {
                    @Override
                    public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                        mProgressDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<ImageUpload> imageUpload = response.body();

                            for (ImageUpload img : imageUpload) {
                                textViewUsername.setText(img.getUsername());

                                Glide.with(MainActivity.this)
                                        .load(img.getAvartar())
                                        .into(imageViewUpload);
                            }

                            Toast.makeText(MainActivity.this, "Thành công", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                        mProgressDialog.dismiss();
                        Log.e(TAG, "Upload Error: " + t.getMessage());
                        Toast.makeText(MainActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Log.e(TAG, "File does not exist at path: " + IMAGE_PATH);
                mProgressDialog.dismiss();
                Toast.makeText(this, "File không tồn tại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "IMAGE_PATH is null or empty");
            mProgressDialog.dismiss();
            Toast.makeText(this, "Không lấy được đường dẫn file!", Toast.LENGTH_SHORT).show();
        }
    }
}
