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
import androidx.annotation.RequiresApi;
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
    ImageView imageViewChoose;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    public static final int MY_REQUEST_CODE = 100;
    public static final String TAG = MainActivity.class.getName();

    public static String[] storge_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageViewChoose.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

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

    private String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(permissions(), MY_REQUEST_CODE);
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


    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewChoose = findViewById(R.id.imgAvatar);
    }

    public void UploadImage1() {
        mProgressDialog.show();

        // Lấy đường dẫn thực của ảnh
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        Log.e(TAG, "IMAGE_PATH: " + IMAGE_PATH);

        if (IMAGE_PATH != null && !IMAGE_PATH.isEmpty()) {
            File file = new File(IMAGE_PATH);

            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part partbodyavatar = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

                ServiceAPI.serviceapi.upload(partbodyavatar).enqueue(new Callback<List<ImageUpload>>() {
                    @Override
                    public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                        mProgressDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<ImageUpload> imageUpload = response.body();
                            String avatarUrl = imageUpload.get(0).getAvatar(); // Lấy link ảnh từ kết quả

                            // Truyền link ảnh về lại ProfileActivity
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            intent.putExtra("avatarUrl", avatarUrl);
                            startActivity(intent);
                            finish(); // kết thúc MainActivity để quay lại

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
