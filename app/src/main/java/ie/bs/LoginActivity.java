package ie.bs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

// Class to sign a user in, it is the first activity the user will see after the splash screen.
// Connecting to firebase and checking if the user is signed up. As well a login, I have added a google sign in.
// This will connect to the google sign in pop up, via the google sign in client API.
// Once connected the user will be added to the list of users within the users tab of firebase, it will show what method the user took to sign in.

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private SignInButton googleBtn;


    private TextInputLayout loginEmail, loginPassword;

    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;
    private TextView linkRegBtn, forgotbutton;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mLoginProgress = new ProgressDialog(this);

        loginButton = (Button) findViewById(R.id.login_button);
        loginEmail = (TextInputLayout) findViewById(R.id.emailLogin);
        loginPassword = (TextInputLayout) findViewById(R.id.login_password);

        googleBtn = (SignInButton) findViewById(R.id.googleButton);

        linkRegBtn = (TextView) findViewById(R.id.createUserLink);
        forgotbutton = (TextView) findViewById(R.id.forgotButton);

        linkRegBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // the application will still run even with this error and it is connected to the parsing of the .json file created by google.
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String eml = loginEmail.getEditText().getText().toString();
                String pwd = loginPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(eml) && !TextUtils.isEmpty(pwd)) {

                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(eml, pwd);
                }
            }
        });
        if (loginButton != null) {
            Log.v("Login", "Really got the login button.");
        }

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

        forgotbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loginEmail.getEditText().getText().toString().trim().length() > 0)
                    FirebaseAuth.getInstance().sendPasswordResetEmail(loginEmail.getEditText().getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(findViewById(R.id.login_layout), "Email Sent", Snackbar.LENGTH_LONG).show();
                                    } else
                                        Snackbar.make(findViewById(R.id.login_layout), "Something went wrong", Snackbar.LENGTH_LONG).show();
                                }
                            });
            }
        });

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                            startActivity(i);
                            finish();
                            Toast.makeText(LoginActivity.this, "User logged in sucessfully", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(LoginActivity.this, "User could not log in", Toast.LENGTH_SHORT).show();
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void loginUser(String email1, String password1){
        mAuth.signInWithEmailAndPassword(email1, password1).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override // check if user is signed in or not
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            mLoginProgress.dismiss();
                            Intent mainIntent = new Intent(LoginActivity.this, SearchActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }  else {

                            mLoginProgress.hide();
                            Toast.makeText(LoginActivity.this, "Wrong email or password. Please Try again.", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

}
