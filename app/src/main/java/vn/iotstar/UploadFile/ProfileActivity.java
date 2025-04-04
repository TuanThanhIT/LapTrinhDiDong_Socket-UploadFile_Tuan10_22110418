package vn.iotstar.UploadFile;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import vn.iotstar.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.imgAvatar);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Nhận dữ liệu từ MainActivity
        Intent intent = getIntent();
        String avatarUrl = intent.getStringExtra("avatarUrl");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(ProfileActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_user)
                    .into(imgAvatar);
        }


    }
}
