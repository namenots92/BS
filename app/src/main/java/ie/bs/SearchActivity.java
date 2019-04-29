package ie.bs;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import ie.bs.model.Post;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int REQUEST_CODE = 1;

    FragmentManager fm =getSupportFragmentManager();

    //widgets
    private TabLayout mTabLayout;
    public ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    UserObject mUser = new UserObject();

    //vars
    public SectionsPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager  = (ViewPager) findViewById(R.id.viewpager_container);

        verifyPermissions();
        mAuth = FirebaseAuth.getInstance(); // gets the current instance of the app,
        // and make sure a user is signed in and get whom is signed in
        getUserInfo();



        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            userRef.keepSynced(true); // keep the user signed in by referencing the current user signed in, from the collection "Users" in the database.
        }
    }

    @Override
    public void onStart() { // start check the current user
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            sendToStart();
        } else {
            userRef.child("online").setValue("true"); // this adds a child to the database and sets the value of who is signed in.
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

        }
        userRef.child("offline").setValue(ServerValue.TIMESTAMP); // when a user closes the app this will show in the collection, stating the user is offline

    }

    private void sendToStart(){
        Intent startIntent = new Intent(SearchActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    public void openProfileEditFragment(){
        fm.beginTransaction()
                .replace(R.id.container, ProfileEditFragment.newInstance(), "ChooseReceiverTextFragment")
                .addToBackStack(null)
                .commit();
    }

    public UserObject getUser() {
        return mUser;
    }

    private void setupViewPager(){
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new SearchFragment());
        mPagerAdapter.addFragment(new WatchListFragment());
        mPagerAdapter.addFragment(new PostFragment());
        mPagerAdapter.addFragment(new AccountFragment());

        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setText(getString(R.string.fragment_search));
        mTabLayout.getTabAt(1).setText(getString(R.string.fragment_watchlist));
        mTabLayout.getTabAt(2).setText(getString(R.string.fragment_post));
        mTabLayout.getTabAt(3).setText(getString(R.string.fragment_account));

    }

    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            setupViewPager();
        }else{
            ActivityCompat.requestPermissions(SearchActivity.this,
                    permissions,
                    REQUEST_CODE);
        }
    }

    private void getUserInfo() { // Gathering the information from firebase from the Users collection. Both their name and image.
        mUser.setId(FirebaseAuth.getInstance().getUid());
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            if(dataSnapshot.child("name").getValue()!=null)
                                mUser.setName(dataSnapshot.child("name").getValue().toString());
                            if(dataSnapshot.child("image").getValue()!=null)
                                mUser.setImage(dataSnapshot.child("image").getValue().toString());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void clearBackStack(){ // kills an activity if a fragment is called
        dismissProgressDialog();
        while(fm.getBackStackEntryCount()>0)
            onBackPressed();
    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(SearchActivity.this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }


}