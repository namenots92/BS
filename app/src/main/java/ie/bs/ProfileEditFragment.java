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
import java.util.Set;

import static android.app.Activity.RESULT_OK;

// Profile Edit Fragment is a fragment that allows the user to update their profile image, name, logout or deactivate their account.
// A new instance is created when the fragment is clicked on from the menu drop down. Once the fragment loads, the users name and image is also loaded by
// checking the current user signed in from the Users colleciton. These are set to default when a user registers.

public class ProfileEditFragment extends Fragment implements View.OnClickListener{

    ImageView   mBack;

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


    public static ProfileEditFragment newInstance(){ // new instance of this fragment is created
        ProfileEditFragment fragment = new ProfileEditFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // a new view for a fragment
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        setHasOptionsMenu(false);


        Log.v(TAG, "ProfileEditFragment");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects(view); // initialize the objects within this method (back,confirm,profileimage,name and back)

        mAuth = FirebaseAuth.getInstance(); // make sure a user is signed in
        userId = mAuth.getCurrentUser().getUid(); // get the currend User ID , needed to get user details from the user collection

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mUserDatabase.keepSynced(true); // keep users signed in
        getUserInfo(); // initialize method to get the name and image about the user from the database
        deactivateButton = (Button) view.findViewById(R.id.deactivate);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        }); // the user clicks the image view to change their profile picture, and sets the image in the collection

        deactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivate(v);
            }
        });

        return view;
    }

    // deactivate the user account
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
        // get the users information (name and image) the method getActivity() is need when using a fragment to get the information from a previous activity
        private void getUserInfo() {
        name = ((SearchActivity)getActivity()).getUser().getName();
        image = ((SearchActivity)getActivity()).getUser().getImage();

        if(((SearchActivity)getActivity()).getUser().getName() != null)
            mName.setText(((SearchActivity)getActivity()).getUser().getName());

        if(((SearchActivity)getActivity()).getUser().getImage() != null && getActivity()!=null)
            Glide.with(getActivity())
                    .load(image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mProfileImage);
    }

    private void saveUserInformation() { // saving data to the collection within the database.
        ((SearchActivity)getActivity()).showProgressDialog("Saving Data..."); // displayed when the save (tick) is clicked

        if(!mName.getText().toString().isEmpty())
            name = mName.getText().toString();

        Map userInfo = new HashMap(); // using a map to put and get the information from the database
        userInfo.put("name", name);

        if(image != null)
            userInfo.put("image", image);

        mUserDatabase.updateChildren(userInfo);

        if(resultUri != null) { // when a user uploads a photo a child is added to the profile image collection of the storage section of firebase
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_image").child(userId);
            // the path to the collection
            UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ((SearchActivity)getActivity()).clearBackStack();
                    return;
                }
            });
            // if the activity has been successful the user is sent back to the main activity and the children have update has been successful
            // the hashmap puts the image into the database aswell and you return to the main activity
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("image", uri.toString());
                            mUserDatabase.updateChildren(newImage);

                            ((SearchActivity)getActivity()).clearBackStack();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            ((SearchActivity)getActivity()).clearBackStack();
                            return;
                        }
                    });
                }
            });
        }else{
            ((SearchActivity)getActivity()).dismissProgressDialog();
            getActivity().onBackPressed();
        }
    }
    // sign out method
    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    // putting the image in, using a circle crop, this uses a bitmap which is used for image when putting or getting an image into a specific position
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
                ((SearchActivity)getActivity()).clearBackStack();
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
        //mConfirm = view.findViewById(R.id.confirm);
        mLogout = view.findViewById(R.id.logout);
        mBack.setOnClickListener(this);
        //mConfirm.setOnClickListener(this);
        mLogout.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}
