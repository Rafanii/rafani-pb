package com.example.myapplication;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextInputEditText userInputField, passwordUser;
    private CheckBox checkBoxes;
    private Button btLogin;
    private TextView forgotPass, signUp;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Cek apakah user sudah login
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User sudah login: " + currentUser.getEmail());
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // Inisialisasi UI
        userInputField = findViewById(R.id.userInput);
        passwordUser = findViewById(R.id.password);
        checkBoxes = findViewById(R.id.checkboxes);
        btLogin = findViewById(R.id.btnLogin);
        forgotPass = findViewById(R.id.forgotPassword);
        signUp = findViewById(R.id.signUp);

        // Tombol Login
        btLogin.setOnClickListener(view -> loginUser());

        // Tombol ke Halaman Registrasi
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String userInput = userInputField.getText().toString().trim();
        String password = passwordUser.getText().toString().trim();

        if (TextUtils.isEmpty(userInput) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Username/email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cek koneksi internet sebelum login
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d("InternetCheck", "Terhubung ke internet: " + isConnected);
        if (!isConnected) {
            Toast.makeText(MainActivity.this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cek apakah input adalah email atau username
        if (userInput.contains("@")) {
            // Jika mengandung '@', anggap sebagai email langsung
            authenticateUser(userInput, password);
        } else {
            // Jika tidak, cari email berdasarkan username di database
            databaseReference.orderByChild("username").equalTo(userInput).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String email = userSnapshot.child("userEmail").getValue(String.class);
                            if (email != null) {
                                authenticateUser(email, password);
                            } else {
                                Toast.makeText(MainActivity.this, "Email tidak ditemukan!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Username tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Gagal mengambil data!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void authenticateUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Cek apakah email sudah diverifikasi
                        if (user != null && !user.isEmailVerified()) {
                            Toast.makeText(MainActivity.this, "Email belum diverifikasi. Periksa inbox Anda.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Log.d(TAG, "Login berhasil: " + user.getEmail());
                        Toast.makeText(getApplicationContext(), "Login berhasil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();
                    } else {
                        Log.e("FirebaseAuthError", "Login gagal: ", task.getException());
                        Toast.makeText(MainActivity.this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
