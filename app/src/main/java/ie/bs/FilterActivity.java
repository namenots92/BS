package ie.bs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "FilterActivity";

    private Button save;
    private ImageView back;
    private EditText city, provience, county;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        save = (Button) findViewById(R.id.btnSave);
        city = (EditText) findViewById(R.id.input_city);
        provience = (EditText) findViewById(R.id.input_state_province);
        county = (EditText) findViewById(R.id.input_country);
        back = (ImageView) findViewById(R.id.backArrow);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back.");
                finish();
            }
        });
    }

    private void init(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: saving...");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FilterActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                Log.d(TAG, "onClick: city: " + city.getText().toString());
                editor.putString(getString(R.string.preferences_city), city.getText().toString());
                editor.commit();

                Log.d(TAG, "onClick: state/province: " + provience.getText().toString());
                editor.putString(getString(R.string.preferences_state_province), provience.getText().toString());
                editor.commit();

                Log.d(TAG, "onClick: country: " + county.getText().toString());
                editor.putString(getString(R.string.preferences_country), county.getText().toString());
                editor.commit();
            }
        });
    }

    private void getFilterPreferences(){
        Log.d(TAG, "getFilterPreferences: retrieving saved preferences.");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String mCounty = preferences.getString(getString(R.string.preferences_country), "");
        String mProvience = preferences.getString(getString(R.string.preferences_state_province), "");
        String mCity = preferences.getString(getString(R.string.preferences_city), "");

        county.setText(mCounty);
        provience.setText(mProvience);
        city.setText(mCity);
    }
}
