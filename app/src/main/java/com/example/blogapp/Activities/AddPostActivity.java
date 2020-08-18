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
import android.widget.Toast;


import com.example.blogapp.Model.Blog;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private ImageButton mImageBtn;
    private EditText mTitle;
    private EditText mDesc;
    private Button mPostBtn;
    private StorageReference mStorage;
    private DatabaseReference mPostReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ProgressDialog mProgress;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();

        mPostReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mImageBtn = (ImageButton) findViewById(R.id.addImageBtn);
        mTitle = (EditText) findViewById(R.id.postTitleET);
        mDesc = (EditText) findViewById(R.id.descriptionET);
        mPostBtn = (Button) findViewById(R.id.postBtn);

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            mImageUri  = data.getData();
            mImageBtn.setImageURI(mImageUri);
        }
    }

    private void startPosting() {
        mProgress.setMessage("Posting in a Blog...");
        mProgress.show();

        final String titleVal = mTitle.getText().toString().trim();
        final String descVal = mDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(titleVal) && !TextUtils.isEmpty(descVal) && mImageUri!= null){
            //Uploading...
//            Blog blog = new Blog("Title","Description","imageurl",
//                    "datestamp","Userid");
//
//            mPostReference.setValue(blog).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    Toast.makeText(getApplicationContext(),"Item Added",
//                            Toast.LENGTH_SHORT).show();
//                    mProgress.dismiss();
//                }
//            });

            StorageReference filePath = mStorage.child("Blog_images")
                    .child(mImageUri.getLastPathSegment());

            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri ;
                            DatabaseReference newPost = mPostReference.push();
                            Map<String,String > dataToSave = new HashMap<>();
                            dataToSave.put("title",titleVal);
                            dataToSave.put("desc",descVal);
                            dataToSave.put("image",downloadUrl.toString());
                            dataToSave.put("dateStamp",String.valueOf(java.lang.System.currentTimeMillis()));
                            dataToSave.put("userID",mUser.getUid());

                            newPost.setValue(dataToSave);
                            mProgress.dismiss();
                            startActivity(new Intent(AddPostActivity.this,PostActivity.class));
                            finish();
                        }
                    });

                }
            });

        }

    }
}
