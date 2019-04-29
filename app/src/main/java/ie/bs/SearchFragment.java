package ie.bs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import ie.bs.Util.PostListAdapter;
import ie.bs.model.Post;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

public class SearchFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private static final String TAG = "SearchFragment";
    private static final int NUM_GRID_COLUMNS = 1;
    private static final int GRID_ITEM_MARGIN = 1;

    private DatabaseReference mReference;

    private String mPostId;

    Post mPost;

    public ArrayList<Post> results = new ArrayList<>();

    String post = "";

    View view;

    SwipeRefreshLayout mRefresh;
    String currentUid, userId;
    Boolean started = false;

    //widgets
    private ImageView mFilters;
    private EditText mSearchText;
    private FrameLayout mFrameLayout;
    private RecyclerView.LayoutManager mLayoutManager;

    private String mPrefCity;
    private String mPrefStateProv;
    private String mPrefCountry;

    UserObject mUser = new UserObject();


    private RecyclerView.Adapter mAdapter;

    public static SearchFragment newInstance(String post){
        SearchFragment fragment = new SearchFragment();

        Bundle args = new Bundle();
        args.putString("post", post);
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mFilters = (ImageView) view.findViewById(R.id.ic_search);
        mSearchText = (EditText) view.findViewById(R.id.input_search);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.container);
        init();
        setupPostsList();


        return view;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!post.isEmpty())
            return;
    }

    private void setupPostsList(){

        currentUid = FirebaseAuth.getInstance().getUid();
        started = true;


        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setHasFixedSize(true);


        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new PostListAdapter(results, post, this ,getContext());
        mRecyclerView.setAdapter(mAdapter);

        getData();


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: got the post id: " + mPostId);
    }

    private void init(){


        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to filters activity.");
                Intent intent = new Intent(getActivity(), FilterActivity.class);
                startActivity(intent);
            }
        });

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        ||actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {


                    String searchString = "";

                    if (!mSearchText.equals("")) {
                        searchString = searchString + mSearchText.getText().toString() + "*";
                    }
                    if (!mPrefCity.equals("")) {
                        searchString = searchString + " city:" + mPrefCity;
                    }
                    if (!mPrefStateProv.equals("")) {
                        searchString = searchString + " state_province:" + mPrefStateProv;
                    }
                    if (!mPrefCountry.equals("")) {
                        searchString = searchString + " country:" + mPrefCountry;
                    }

                }
                return false;
            }
        });
    }

    private void addItemToWatchList(){
// method to add items

        getPostInfo();
        Log.d(TAG, "addItemToWatchList: adding item to watch list.");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("watch_list")
                .child(userId)
                .child(mPostId)
                .child("post_id")
                .setValue(mPostId);

        Toast.makeText(getActivity(), "Added to watch list", Toast.LENGTH_SHORT).show();

    }

    private void getPostInfo(){
        Log.d(TAG, "getPostInfo: getting the post information.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(mPostId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                if(singleSnapshot != null){
                    mPost = singleSnapshot.getValue(Post.class);
                    if (mPost != null) {
                        mPost.getPost_id();
                    }
                    Log.d(TAG, "onDataChange: found the post: " + mPost.getTitle());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPosts(){

        DatabaseReference receivedDb = FirebaseDatabase.getInstance().getReference().child("posts");
        receivedDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        getUserInfo(snapshot.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void getUserInfo(String chatUid) {
        getUserInfo();
        DatabaseReference postsDb = FirebaseDatabase.getInstance().getReference().child("posts").child(chatUid);
        DatabaseReference userId = FirebaseDatabase.getInstance().getReference().child("Users").child(chatUid);

        postsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Post post = new Post();

                    if(dataSnapshot.child("title").getValue()!=null)
                        post.setTitle(dataSnapshot.child("title").getValue().toString());

                    if(dataSnapshot.child("description").getValue()!=null)
                        post.setDescription(dataSnapshot.child("description").getValue().toString());

                    if(dataSnapshot.child("country").getValue()!=null)
                        post.setCountry(dataSnapshot.child("country").getValue().toString());

                    if(dataSnapshot.child("state_province").getValue()!=null)
                        post.setState_province(dataSnapshot.child("state_province").getValue().toString());

                    if(dataSnapshot.child("city").getValue()!=null)
                        post.setCity(dataSnapshot.child("city").getValue().toString());

                    if(dataSnapshot.child("image").getValue()!=null) {
                        post.setThumbnail(dataSnapshot.child("image").getValue().toString());
                    }
                    if(dataSnapshot.child("price").getValue()!=null)
                        post.setPriceOfItems(dataSnapshot.child("price").getValue().toString());

                    if(dataSnapshot.child("contact_email").getValue()!=null)
                        post.setContact_email(dataSnapshot.child("contact_email").getValue().toString());

                    if(!results.contains(post)){
                        results.add(post);
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(post.equals("post"))
            getData();
    }

    public void getData(){
        clear();
        getPosts();
    }

    private void clear() {
        this.results.clear();
        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();
    }


}