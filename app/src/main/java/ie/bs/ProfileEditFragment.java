package ie.bs;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import ie.bs.UserObject;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ProfileEditFragment extends Fragment implements View.OnClickListener{

    ImageView   mBack,
            mConfirm;

    Button deactivateButton;

    Button mLogout;

    EditText mName;

    ImageView mProfileImage;

    FirebaseAuth mAuth;
    DatabaseReference mUserDatabase;

    String      userId = "",
            name = "--",
            image="--";


    Uri resultUri;

    private static final String TAG = "ProfileEditFragment";


    public static ProfileEditFragment newInstance(){
        ProfileEditFragment fragment = new ProfileEditFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        setHasOptionsMenu(false);


        Log.v(TAG, "ProfileEditFragment");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects(view);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mUserDatabase.keepSynced(true);
        getUserInfo();
        deactivateButton = (Button) view.findViewById(R.id.deactivate);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        deactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivate(v);
            }
        });

        return view;
    }

    public void deactivate(final View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intentDeactivate = new Intent(getActivity(), LoginActivity.class);
                        getActivity().finish();
                        startActivity(intentDeactivate);
                        Toast.makeText(getActivity(), "You have deactivated your account :(", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getActivity(), "Sorry we were unable to deactivate your account", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

        private void getUserInfo() {
        name = ((MainActivity)getActivity()).getUser().getName();
        image = ((MainActivity)getActivity()).getUser().getImage();

        if(((MainActivity)getActivity()).getUser().getName() != null)
            mName.setText(((MainActivity)getActivity()).getUser().getName());

        if(((MainActivity)getActivity()).getUser().getImage() != null && getActivity()!=null)
            Glide.with(getActivity())
                    .load(image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImage);
    }

    private void saveUserInformation() {
        ((MainActivity)getActivity()).showProgressDialog("Saving Data...");

        if(!mName.getText().toString().isEmpty())
            name = mName.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);

        if(image != null)
            userInfo.put("image", image);

        mUserDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_image").child(userId);

            UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ((MainActivity)getActivity()).clearBackStack();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("image", uri.toString());
                            mUserDatabase.updateChildren(newImage);

                            ((MainActivity)getActivity()).clearBackStack();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            ((MainActivity)getActivity()).clearBackStack();
                            return;
                        }
                    });
                }
            });
        }else{
            ((MainActivity)getActivity()).dismissProgressDialog();
            getActivity().onBackPressed();
        }
    }
    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                Glide.with(getActivity())
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mProfileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                ((MainActivity)getActivity()).clearBackStack();
                break;
            case R.id.confirm:
                saveUserInformation();
                break;
            case R.id.logout:
                LogOut();
                break;
        }
    }

    private void initializeObjects(View view) {
        mName = view.findViewById(R.id.name);
        mProfileImage = view.findViewById(R.id.profileImage);
        mBack = view.findViewById(R.id.back);
        mConfirm = view.findViewById(R.id.confirm);
        mLogout = view.findViewById(R.id.logout);
        mBack.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mLogout.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}
