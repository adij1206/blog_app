package com.example.blogapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.blogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText name,lname,email,password;
    private Button createAccount;
    private DatabaseReference databaseReference;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private ImageButton profilePic;
    private StorageReference mStorage;
    private static final int GALLERY_CODE = 1;
    private Uri resultUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account2);

        mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance().getReference().child("Blog_Profile_Pics");

        mProgress = new ProgressDialog(this);

        name = (EditText) findViewById(R.id.firstNameET);
        lname =(EditText) findViewById(R.id.lastNameET);
        email = (EditText) findViewById(R.id.emailET);
        password = (EditText) findViewById(R.id.pwdET);
        profilePic = (ImageButton) findViewById(R.id.profilePic);
        createAccount = (Button) findViewById(R.id.createAccountBTN);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
            }
        });
    }

    private void createNewAccount()
    {
        final String fname = name.getText().toString().trim();
        final String laname= lname.getText().toString().trim();
        String em = email.getText().toString().trim();
        String pwd= password.getText().toString().trim();

        if(!TextUtils.isEmpty(fname)&&!TextUtils.isEmpty(laname)
              && !TextUtils.isEmpty(em)&&!TextUtils.isEmpty(pwd)){
            mProgress.setMessage("Creating Account...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(em,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
                public void onSuccess(AuthResult authResult) {
                    if(authResult!=null){
                        StorageReference imagePath = mStorage.child("Blog_Profile_Pics")
                                .child(resultUri.getLastPathSegment());
                        imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String userid = mAuth.getUid();
                                DatabaseReference currentUserDb = databaseReference.child(userid);
                                currentUserDb.child("firstName").setValue(fname);
                                currentUserDb.child("lastName").setValue(laname);
                                currentUserDb.child("image").setValue(resultUri.toString());

                                mProgress.dismiss();
                                Intent intent = new Intent(CreateAccountActivity.this,PostActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        });

//                        String userid = mAuth.getUid();
//                        DatabaseReference currentUserDb = databaseReference.child(userid);
//                        currentUserDb.child("firstName").setValue(name);
//                        currentUserDb.child("lastName").setValue(laname);
//                        currentUserDb.child("image").setValue("none");
//
//                        mProgress.dismiss();
//                        Intent intent = new Intent(CreateAccountActivity.this,PostActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode== RESULT_OK){
            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profilePic.setImageURI(resultUri);
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
