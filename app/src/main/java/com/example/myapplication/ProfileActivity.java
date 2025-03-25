package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtUsername, txtEmail, txtNIM;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Inisialisasi UI
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        txtNIM = findViewById(R.id.txtNIM);
        btnLogout = findViewById(R.id.btnLogout);

        if (user != null) {
            String userId = user.getUid();

            // Mengambil data user dari Firebase Database
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("userEmail").getValue(String.class);
                        String nim = snapshot.child("userNIM").getValue(String.class);

                        txtUsername.setText("Nama: " + (username != null ? username : "Tidak tersedia"));
                        txtEmail.setText("Email: " + (email != null ? email : "Tidak tersedia"));
                        txtNIM.setText("NIM: " + (nim != null ? nim : "Tidak tersedia"));
                    } else {
                        Toast.makeText(ProfileActivity.this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Gagal mengambil data!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Tombol Logout
        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
    }
}
