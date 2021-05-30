package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Signup extends AppCompatActivity {
    private final int REQ = 1 ;
    private ImageView profileimage;
    private EditText Name,Email,Password,Confirmpassword;
    private Button signupbtn;
    String name,email,pwd,cpwd,downloadurl="";
    FirebaseAuth auth;
    private Bitmap bitmap = null;
    private DatabaseReference reference,dbref;
    private StorageReference storageReference,stref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        profileimage = findViewById(R.id.profileimage);
        Name = findViewById(R.id.Name);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        Confirmpassword = findViewById(R.id.Confirmpassword);
        signupbtn = findViewById(R.id.signupbtn);
        reference = FirebaseDatabase.getInstance().getReference().child("User");
        storageReference = FirebaseStorage.getInstance().getReference().child("Upload");

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opengallery();
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkvalidation();
            }
        });

    }

    private void opengallery() {
        Intent picimage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(picimage,REQ);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            profileimage.setImageBitmap(bitmap);
        }
    }

    private void checkvalidation() {
        name = Name.getText().toString();
        email = Email.getText().toString();
        pwd = Password.getText().toString();
        cpwd = Confirmpassword.getText().toString();

        if(name.isEmpty()){
            Name.setError("Empty");
            Name.requestFocus();
        }else if (email.isEmpty()){
            Email.setError("Empty");
            Email.requestFocus();
        }else if(pwd.isEmpty()){
            Password.setError("Empty");
            Password.requestFocus();
        }else if (pwd.length()<6){
            Password.setError("Password have at least 6 letters");
            Password.requestFocus();
        }else if(cpwd.isEmpty()){
            Confirmpassword.setError("Empty");
            Confirmpassword.requestFocus();
        }else if (!pwd.equals(cpwd)){
            Password.setText("");
            Confirmpassword.setText("");
            Toast.makeText(this, "Both password field must be same.", Toast.LENGTH_SHORT).show();
        }
        else {
            signuphere();
        }


    }

    private void signuphere() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50, baos);
        byte[] finalimg = baos.toByteArray();
        final StorageReference filepath;
        filepath = storageReference.child("Teacher").child(finalimg+"jpg");
        final UploadTask uploadTask = filepath.putBytes(finalimg);
        uploadTask.addOnCompleteListener(Signup.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadurl = String.valueOf(uri);
                                    uploadData();
                                }
                            });
                        }
                    });
                }else{

                    Toast.makeText(Signup.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void uploadData() {
        dbref = reference.child("data");
       // stref = storageReference.child(auth.getUid());
        final String uniquekey = dbref.push().getKey();

        Userdata userdata = new Userdata(name,email,pwd,downloadurl,uniquekey);
        dbref.child(uniquekey).setValue(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Signup.this, "Teacher Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Signup.this, "Error while saving data.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}