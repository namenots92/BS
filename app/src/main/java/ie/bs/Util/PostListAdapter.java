package ie.bs.Util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//import de.hdodenhof.circleimageview.CircleImageView;
import ie.bs.R;
import ie.bs.SearchActivity;
import ie.bs.SearchFragment;
import ie.bs.UserObject;
import ie.bs.WatchListFragment;
import ie.bs.model.Post;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;


public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostListViewHolders> {

    private Context context;
    private ArrayList<Post> postList = new ArrayList<>();
    private SearchFragment searchFragment;
    private String mPost, locationPut, mPostId;
    private Post post;


    public PostListAdapter (ArrayList<Post> postList, String mPost, SearchFragment searchFragment, Context context) {
        this.postList = postList;
        this.mPost = mPost;
        this.searchFragment = searchFragment;
        this.context = context;
    }

    public PostListAdapter(FragmentActivity activity, ArrayList<Post> mPosts) {
    }

    @NonNull
    @Override
    public PostListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, null);
        PostListViewHolders rcv = new PostListViewHolders(layoutView);
        return rcv;


    }

    @Override
    public void onBindViewHolder(final PostListViewHolders postListViewHolders, int position) {

        post = postList.get(position);

        final int pos = position;

        UserObject user = new UserObject();
        postListViewHolders.mTitle.setText(post.getTitle());
        postListViewHolders.price.setText(" €" + post.getPriceOfItems());
        postListViewHolders.description.setText(post.getDescription());
        postListViewHolders.location.setText(post.getLocation());
     //   postListViewHolders.username.setText(post.getUser_id());
        //postListViewHolders.mEmail.setText(post.getContact_email());
        Glide.with(context).load(user.getImage()).into(postListViewHolders.profilePhoto);
       // Glide.with(context).load(user.getImage()).into(postListViewHolders.profilePhoto);
        Glide.with(context).load(post.getThumbnail()).into(postListViewHolders.imageItem);



        postListViewHolders.menuDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(postListViewHolders.menuDot);
            }
        });




    /*    postListViewHolders.imageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view the post in more detail
                Fragment fragment = (Fragment)((SearchActivity)context).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                ((SearchActivity)context).mViewPager.getCurrentItem());
                if(fragment != null){
                    //SearchFragment (AKA #0)
                    if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){

                        SearchFragment searchFragment = (SearchFragment)((SearchActivity)context).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity)context).mViewPager.getCurrentItem());

                       // searchFragment.viewPost(postList.get(pos).getPost_id());
                    }
                    //WatchList Fragment (AKA #1)
                    else if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){

                        WatchListFragment watchListFragment = (WatchListFragment)((SearchActivity)context).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((SearchActivity)context).mViewPager.getCurrentItem());

                        watchListFragment.viewPost(postList.get(pos).getPost_id());
                    }
                }
            }
        }); */
    }

    public void contactSeller(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {post.getContact_email()});
        try {
            context.startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return this.postList.size();
    }

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }


    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_to_watch_list:
                    addItemToWatchList();
                    Toast.makeText(context, "Added to Watch List", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_contact_seller:
                    contactSeller();
                    return true;
                default:
            }
            return false;
        }
    }

   private void addItemToWatchList(){
        Log.d(TAG, "addItemToWatchList: adding item to watch list.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("posts")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPostId)
                .child("post_id")
                .setValue(mPostId);

        Toast.makeText(context, "Added to watch list", Toast.LENGTH_SHORT).show();
    }


    public class PostListViewHolders extends RecyclerView.ViewHolder {
        TextView mTitle, price, description, location, username, mEmail;
        ImageView menuDot;
        SquareImageView imageItem;
        ImageView profilePhoto;

        PostListViewHolders(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title_item);
            description = itemView.findViewById(R.id.description_item);
            location = itemView.findViewById(R.id.location_item);
            price = itemView.findViewById(R.id.price_item);
            imageItem = itemView.findViewById(R.id.post_image);
            profilePhoto = itemView.findViewById(R.id.profile_photo);
            menuDot = itemView.findViewById(R.id.menu);

        }
    }

    public void filterList(ArrayList<Post> filteredList){
        postList = filteredList;
        notifyDataSetChanged();
    }

}


