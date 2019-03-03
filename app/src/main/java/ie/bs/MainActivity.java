package ie.bs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemsAdapter adapter;
    private List<Items> itemsList;
    private Toolbar toolbar;
    private  EditText editText;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    UserObject mUser = new UserObject();

    FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAuth = FirebaseAuth.getInstance();

        getUserInfo();
        itemsList = new ArrayList<>();
        adapter = new ItemsAdapter(this, itemsList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        editText = findViewById(R.id.search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        prepareAlbums();

        if (mAuth.getCurrentUser() != null) {
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            userRef.keepSynced(true);
        }

    }

    private void filter(String text){
        ArrayList<Items> filteredList = new ArrayList<>();

        for (Items item : itemsList){
            if(item.getName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }

        adapter.filterList(filteredList);
    }

    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.car,
                R.drawable.cover,
                R.drawable.handbag,
                R.drawable.headphones,
                R.drawable.iphonex,
                R.drawable.sportsbag,
                R.drawable.vans,
                R.drawable.waterbottle,
                R.drawable.white};

        Items a = new Items("Car", 13000, covers[0]);
        itemsList.add(a);

        a = new Items("Adele Duvet Cover", 9, covers[1]);
        itemsList.add(a);

        a = new Items("Handbag", 110, covers[2]);
        itemsList.add(a);

        a = new Items("Headphones", 70, covers[3]);
        itemsList.add(a);

        a = new Items("Iphone X", 900, covers[4]);
        itemsList.add(a);

        a = new Items("Sportsbad", 20, covers[5]);
        itemsList.add(a);

        a = new Items("Vans shoes", 70, covers[6]);
        itemsList.add(a);

        a = new Items("Waterbottle", 14, covers[7]);
        itemsList.add(a);

        a = new Items("White Tile", 11, covers[8]);
        itemsList.add(a);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            sendToStart();
        } else {
            userRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

        }
        userRef.child("online").setValue(ServerValue.TIMESTAMP);

    }

    private void sendToStart(){
        Intent startIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    public UserObject getUser() {
        return mUser;
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void getUserInfo() {
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

    public void clearBackStack(){
        dismissProgressDialog();
        while(fm.getBackStackEntryCount()>0)
            onBackPressed();
    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(MainActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_post:
                fm.beginTransaction()
                        .replace(R.id.container, PostFragment.newInstance(), "PostFragment")
                        .addToBackStack(null)
                        .commit();
               break;
            case R.id.action_settings:
                fm.beginTransaction()
                        .replace(R.id.container, ProfileEditFragment.newInstance(), "ProfileEditFragment")
                        .addToBackStack(null)
                        .commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}



