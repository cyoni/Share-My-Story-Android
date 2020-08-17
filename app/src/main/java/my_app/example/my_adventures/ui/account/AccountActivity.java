package my_app.example.my_adventures.ui.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import my_app.example.my_adventures.R;


public class AccountActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (User.isSignIn())
            loadMyAccountFragment();
        else
            loadConnectFragment();
    }

    private void loadMyAccountFragment() {
       // switchFragment(new MyAccount_Fragment());
    }

    private void loadConnectFragment() {
        switchFragment(new LoginFragment());
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
