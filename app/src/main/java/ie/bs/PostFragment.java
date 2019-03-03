package ie.bs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

public class PostFragment extends Fragment {

    private static final String TAG = "PostFragment";

    private ImageView imageView;
    private EditText title, desc, price, county, province, city, contactEmail;
    private Button post;
    private ProgressBar progressBar;

    public static PostFragment newInstance(){
        PostFragment fragment = new PostFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);

        desc = view.findViewById(R.id.input_description);
        city = view.findViewById(R.id.input_city);
        contactEmail = view.findViewById(R.id.input_email);
        imageView = view.findViewById(R.id.post_image);
        title = view.findViewById(R.id.input_title);


        post = view.findViewById(R.id.btn_post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Posting items is coming soon.", Toast.LENGTH_LONG).show();
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        price = view.findViewById(R.id.input_price);
        county = view.findViewById(R.id.input_county);

        // used to move the edit text up and scroll
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return view;
    }

    private void resetFields(){
        //UniversalImageLoader.setImage("", post);
        title.setText("");
        desc.setText("");
        price.setText("");
        county.setText("");
        province.setText("");
        city.setText("");
        contactEmail.setText("");
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        if(progressBar.getVisibility() == View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
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
