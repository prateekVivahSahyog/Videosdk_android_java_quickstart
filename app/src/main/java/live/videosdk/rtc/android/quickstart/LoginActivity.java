package live.videosdk.rtc.android.quickstart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import live.videosdk.rtc.android.quickstart.repository.MainRepository;

public class LoginActivity extends AppCompatActivity {

    private MainRepository mainRepository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        mainRepository = MainRepository.getInstance();

        final Button btnLogin = findViewById(R.id.login);
        final EditText userName = findViewById(R.id.username);

        btnLogin.setOnClickListener(v->{
            mainRepository.login(userName.getText() .toString(),()->{
                startActivity(new Intent(LoginActivity.this , JoinActivity.class));
            });
        });

    }
}
