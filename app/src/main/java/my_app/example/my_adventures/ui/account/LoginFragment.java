package my_app.example.my_adventures.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.agrawalsuneet.dotsloader.loaders.LazyLoader;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.MyPreferences;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.toast;
import my_app.example.my_adventures.ui.home.Home;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private View root;
    private LazyLoader loader;
    private Button sign_in_button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_login, container, false);
        sign_in_button = root.findViewById(R.id.sign_in_button);

        sign_in_button.setOnClickListener(this);

        initGoogleOptions();
        loader = root.findViewById(R.id.loader);
        return root;
    }  

    private void initGoogleOptions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(INSERT HERE YOUR TOKEN)
                .requestEmail()
                .build();

        MainActivity.mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(root.getContext(), gso);
    }

    private void startLoadingAnimation() {
        loader.setVisibility(View.VISIBLE);
        sign_in_button.setVisibility(View.INVISIBLE);
    }

    private void stopLoadingAnimation() {
        loader.setVisibility(View.INVISIBLE);
        sign_in_button.setVisibility(View.VISIBLE);
    }

  /*  public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                System.out.println(hashKey + "$$");
            }
        } catch (NoSuchAlgorithmException e) {

        } catch (Exception e) {

        }
    }*/

    private void signIn() {
        startLoadingAnimation();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    throw new Exception("No account was found");
                } else
                    firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                // Google Sign In failed, update UI appropriately
                toast.showMsg(getContext(), "Could not sign in. Please try later or contact us. (error: " + e.getMessage() + ")" );
                System.out.println("error " + e.getMessage() );

                stopLoadingAnimation();
                System.out.println("GOOGLE AUTH FAILED " + e.getLocalizedMessage());
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        MainActivity.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            MainActivity.mAuth.getCurrentUser();
                            checkAccount();
                        } else {
                            stopLoadingAnimation();
                            toast.showMsg(getContext(), "Could not sign in.");
                            System.out.println("ERROR CONNECT " + task.getException());
                        }
                    }
                });
    }

    private void goHome() {
        Home.dontLoadMsgs = false;
        getActivity().finish();
    }

    private void checkAccount() {
        MainActivity.mDatabase.child("users").child(MainActivity.mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String publicKey = String.valueOf(dataMap.get("user_public"));
                    String accountStatus = String.valueOf(dataMap.get("accountStatus"));

                    if (accountStatus.equals("X")) {
                        toast.showMsg(getContext(), "Could not log in");}
                    else if (accountStatus.equals("A")) {
                               savePublicKey(publicKey);
                               User.setPublicKey(publicKey);
                               if (hasGuestBlockedHimselfFromHisMessages(publicKey)) {
                                   BlockList.unBlockUser(getActivity(), publicKey);
                               }
                           getAndSaveNickname();
                         }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERROR CONNECT " + databaseError.getDetails());
            }
        });

    }

    private boolean hasGuestBlockedHimselfFromHisMessages(String publicKey) {
        return (BlockList.isUserBlocked(publicKey));
    }

    private void getAndSaveNickname() {
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("profile").child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String nickname = (String) dataSnapshot.getValue();
                    User.setNickname(nickname);
                    MyPreferences.setSharedPreference(getActivity(), MyPreferences.USER_FOLDER, "nickname", nickname);
                    goHome();
                    toast.showMsg(getContext(), "Hi " + nickname);
                } else {
                    changeFragment_choose_nickname();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void savePublicKey(String userPublicKey) {
        System.out.println("user public key " + userPublicKey);
        MyPreferences.savePublicKey(getActivity(), userPublicKey);
    }

    private void changeFragment_choose_nickname() {
        Intent intent = new Intent(getActivity(), NicknameManagerActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}