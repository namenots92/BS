package ie.bs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout regName, regEmail, regPassword;
    private Button regButton;

    private FirebaseAuth mAuth;
    private DatabaseReference fbDatabase;
    private TextView signBackBtn;

    private ProgressDialog mRegProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        regName = (TextInputLayout) findViewById(R.id.reg_name);
        regEmail = (TextInputLayout) findViewById(R.id.reg_email);
        regPassword = (TextInputLayout) findViewById(R.id.reg_password);
        regButton = (Button) findViewById(R.id.reg_btn);

        signBackBtn = (TextView) findViewById(R.id.backToSignUp);

        signBackBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intentBack = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intentBack);
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = regName.getEditText().getText().toString(); // get the text and change to string
                String email = regEmail.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we set up your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(name, email, password);

                }
                else if(TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Please fill in the form.", Toast.LENGTH_LONG).show();

                }
            }
        });

        if (regButton != null)
        {
            Log.v("Registered", "Really got the register button");
        }
    }



    private void registerUser(final String nameReg, final String emailReg, final String passwordReg) {
        // ensure all fields are entered
        mAuth.createUserWithEmailAndPassword(emailReg, passwordReg).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                mRegProgress.dismiss();
                if(task.isSuccessful()){

                    // get the current user thats logged in
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();
                    // add child instances from the database
                    fbDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    // hash map is a data type to retain data, can be in a tree form. Key and value pair set

                    Map mNewUserMap = new HashMap();
                    mNewUserMap.put("email", emailReg);
                    mNewUserMap.put("name", nameReg);
                    fbDatabase.setValue(mNewUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRegProgress.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                Toast.makeText(RegisterActivity.this, "Welcome, your registraiton was sucessful.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });

                }else{
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "That email address is already registered please try again.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}