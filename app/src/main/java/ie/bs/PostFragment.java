package ie.bs;

import android.graphics.Bitmap;
import android.icu.util.UniversalTimeScale;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ie.bs.Util.UniversalImageLoader;
import ie.bs.model.Post;

import static android.text.TextUtils.isEmpty;


public class PostFragment extends Fragment implements SelectPhotoText.OnPhotoSelectedListener {

    private static final String TAG = "PostFragment";

    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mPostImage.setImageBitmap(bitmap);
        //assign to a global variable
        mSelectedBitmap = bitmap;
    }

    //widgets
    private ImageView mPostImage;
    private EditText title, desc, price, county, province, city, email, location;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars
    private Bitmap mSelectedBitmap;
    private byte[] mUploadBytes;
    private double mProgress = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        title = view.findViewById(R.id.input_title);
        desc = view.findViewById(R.id.input_description);
        mPostImage = view.findViewById(R.id.post_image);

        price = view.findViewById(R.id.input_price);
        county = view.findViewById(R.id.input_county);

        mPost = view.findViewById(R.id.btn_post);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        province = view.findViewById(R.id.input_state_province);
        city = view.findViewById(R.id.input_city);
        email = view.findViewById(R.id.input_email);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

        init();

        return view;
    }

    private void init(){

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                SelectPhotoText dialog = new SelectPhotoText();
                dialog.show(getFragmentManager(), getString(R.string.text_select_photo));
                dialog.setTargetFragment(PostFragment.this, 1);
            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to post...");
                if(!isEmpty(title.getText().toString())
                        && !isEmpty(county.getText().toString())
                        && !isEmpty(province.getText().toString())
                        && !isEmpty(city.getText().toString())
                        && !isEmpty(desc.getText().toString())
                        && !isEmpty(price.getText().toString())
                        && !isEmpty(email.getText().toString())) {
                    //we have a bitmap and no Uri
                    if (mSelectedBitmap != null) {
                        uploadNewPhoto(mSelectedBitmap);
                    } else {
                        Toast.makeText(getActivity(), "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]>{

        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if(bitmap != null){
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){
                try{
                    mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), params[0]);
                }catch (IOException e){
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            byte[] bytes = null;
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount() / 1000000 );
            bytes = getBytesFromBitmap(mBitmap, 100);
            Log.d(TAG, "doInBackground: megabytes before compression: " + bytes.length / 1000000 );
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            hideProgressBar();
            //execute the upload task
            executeUploadTask();
        }
    }

    private void executeUploadTask() {
        Toast.makeText(getActivity(), "uploading image", Toast.LENGTH_SHORT).show();

        final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                        "/" + postId + "/post_image");

        final UploadTask uploadTask = storageReference.putBytes(mUploadBytes);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        Toast.makeText(getActivity(), "Post Success", Toast.LENGTH_SHORT).show();

                        //insert the download url into the firebase database


                        String uriS = uri.toString();

                        Log.d(TAG, "onSuccess: firebase download url: " + uriS);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                        Post post = new Post();
                        post.setImage(uriS);
                        post.setCity(city.getText().toString());
                        post.setDescription(desc.getText().toString());
                        post.setPost_id(postId);
                        post.setState_province(province.getText().toString());
                        post.setTitle(title.getText().toString());
                        post.setContact_email(email.getText().toString());
                        post.setCountry(county.getText().toString());
                        post.setPrice(price.getText().toString());
                        post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(getString(R.string.node_posts))
                                .child(postId)
                                .setValue(post);

                        resetFields();

                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "could not upload photo", Toast.LENGTH_SHORT).show();
                    }
                });
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        if (currentProgress > (mProgress + 15)) {
                            mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                            Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

            public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
                return stream.toByteArray();
            }


            private void resetFields() {
                UniversalImageLoader.setImage("", mPostImage);
                title.setText("");
                desc.setText("");
                price.setText("");
                county.setText("");
                province.setText("");
                city.setText("");
                email.setText("");
            }

            private void showProgressBar() {
                mProgressBar.setVisibility(View.VISIBLE);

            }

            private void hideProgressBar() {
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            /**
             * Return true if the @param is null
             *
             * @param string
             * @return
             */
            private boolean isEmpty(String string) {
                return string.equals("");
            }
        }
