package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Models.UserDetails;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;  // Tambahkan ini untuk debugging

public class MainActivity2 extends AppCompatActivity {

    Button signUpBtn;
    TextInputEditText usernameSignUp, passwordSignUp, nimPengguna, emailPengguna;
    TextView signInText;
    FirebaseAuth mAuth;
    private static final String TAG = "MainActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi UI
        signUpBtn = findViewById(R.id.signUpBtn);
        usernameSignUp = findViewById(R.id.usernameSignUp);
        emailPengguna = findViewById(R.id.emailPengguna);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        nimPengguna = findViewById(R.id.nimPengguna);
        signInText = findViewById(R.id.signIn);

        // Inisialisasi FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(view -> {
            String username = usernameSignUp.getText().toString().trim();
            String email = emailPengguna.getText().toString().trim();
            String password = passwordSignUp.getText().toString().trim();
            String NIM = nimPengguna.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameSignUp.setError("Masukkan Username");
                usernameSignUp.requestFocus();
            } else if (TextUtils.isEmpty(email)) {
                emailPengguna.setError("Masukkan Email");
                emailPengguna.requestFocus();
            } else if (TextUtils.isEmpty(password) || password.length() < 6) {
                passwordSignUp.setError("Password harus minimal 6 karakter");
                passwordSignUp.requestFocus();
            } else if (TextUtils.isEmpty(NIM) || !NIM.matches("\\d+")) {
                nimPengguna.setError("Masukkan NIM yang valid (hanya angka)");
                nimPengguna.requestFocus();
            } else {
                registerUser(username, email, password, NIM);
            }
        });

        signInText.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser(String username, String email, String password, String NIM) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser fUser = mAuth.getCurrentUser();
                if (fUser != null) {
                    String uid = fUser.getUid();

                    UserDetails userDetails = new UserDetails(uid, username, email, password, NIM);

                    // Debugging: cek data sebelum masuk ke database
                    Log.d(TAG, "User Details: " + new Gson().toJson(userDetails));

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                    reference.child(uid).setValue(userDetails).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            fUser.sendEmailVerification();
                            Toast.makeText(MainActivity2.this, "Akun berhasil dibuat. Verifikasi email Anda!", Toast.LENGTH_LONG).show();

                            // Debugging: konfirmasi penyimpanan berhasil
                            Log.d(TAG, "Data pengguna berhasil disimpan di Firebase");

                            // Pindah ke halaman utama setelah sukses
                            Intent intent = new Intent(MainActivity2.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Exception e = task1.getException();
                            Toast.makeText(MainActivity2.this, "Gagal menyimpan data pengguna: " + e.getMessage(), Toast.LENGTH_LONG).show();

                            // Debugging: lihat error saat menyimpan data
                            Log.e(TAG, "Database Error: ", e);
                        }
                    });
                }
            } else {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(MainActivity2.this, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity2.this, "Pendaftaran gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Debugging: lihat error saat registrasi akun
                Log.e(TAG, "Register Error: ", e);
            }
        });
    }
}
