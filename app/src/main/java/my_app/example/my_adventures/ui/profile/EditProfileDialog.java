package my_app.example.my_adventures.ui.profile;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import my_app.example.my_adventures.MainActivity;
import my_app.example.my_adventures.R;
import my_app.example.my_adventures.ui.account.User;
import my_app.example.my_adventures.toast;
import my_app.example.my_adventures.ui.account.NicknameManagerActivity;

public class EditProfileDialog extends Dialog {
    final String[] COUNTRIES = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"};
    Button edit_nickname;
    public ProfileActivity profileActivity;

    private Button submit;
    private TextInputEditText  age, status;
    private AutoCompleteTextView country;
    //private Spinner gender_list_box;
    private Integer gender;
    private AutoCompleteTextView gender_list_box;

    public EditProfileDialog(ProfileActivity activity) {
        super(activity);
        this.profileActivity = activity;
    }

    private void setCountryAutoComplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout, R.id.textView, COUNTRIES);
        AutoCompleteTextView textView = findViewById(R.id.country);
        textView.setAdapter(adapter);
    }

    public void setValues(String age, String gender, String country, String status) {
         this.age.setText(age);
         this.country.setText(country);
         this.status.setText(status);
         setGender(gender);
/*
         gender_list_box.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 gender_list_box.setThreshold(0);
                    System.out.println("XXXXXXXXXX");
                 gender_list_box.showDropDown();
             }
         });
*/
    }

    private void setGender(String gender) {
        switch (gender) {
            case "Male":
                this.gender = 0;
                break;
            case "Female":
                this.gender = 1;
                break;
            default:
                this.gender = null;
                break;
        }
        gender_list_box.setText(gender);

     /*   if (this.gender == null || this.gender != 0 && this.gender != 1)
            gender_list_box.setSelection(2);
        else
            gender_list_box.setSelection(this.gender);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_profile);

        setObjects();
        setCountryAutoComplete();
        submitSetOnClick();


       /* String[] ___COUNTRIES = new String[] {"Item 1", "Item 2", "Item 3", "Item 4"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.dropdown_menu_popup_item,
                        ___COUNTRIES);

        AutoCompleteTextView editTextFilledExposedDropdown =
                findViewById(R.id.gender_box);
        editTextFilledExposedDropdown.setAdapter(adapter);*/
    }


    private void setObjects() {
        submit = findViewById(R.id.submit);
        edit_nickname = findViewById(R.id.edit_nickname);
        age = findViewById(R.id.age);
        country = findViewById(R.id.country);
        status = findViewById(R.id.status);
        gender_list_box = findViewById(R.id.gender_box);
        final String[] items = {"Male", "Female", "Not specified"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout, R.id.textView, items);
        gender_list_box.setAdapter(adapter);
        gender_list_box.setThreshold(10);



       // gender_list_box.setOnItemSelectedListener(this);
    }

    private void submitSetOnClick() {

        edit_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNicknameManager();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (areValuesValid()) {
                    submit.setEnabled(false);
                    updateAccount();
                }
            }
        });

    }

    public static boolean isNumericAndLessThan100(String str) {
        try {
            int age = Integer.parseInt(str);
            return age < 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean areValuesValid() {
        boolean flag = false;
        boolean isCountryOk = isCountryValid();

        if (!country.getText().toString().isEmpty() && !isCountryOk)
            toast.showMsg(getContext(), "Enter a valid country.");
        else if (!age.getText().toString().isEmpty() && !isNumericAndLessThan100(age.getText().toString())) {
            toast.showMsg(getContext(), "Enter a valid age.");
        } else if ((status.getText().length() > 100))
            toast.showMsg(getContext(), "Status cannot exceed 100 letters.");
        else flag = true;

        return flag;
    }

    private boolean isCountryValid() {
        String input = country.getText().toString().toLowerCase();
        for (String currentCountry : COUNTRIES) {
            if (input.equals(currentCountry.toLowerCase())) {
                country.setText(currentCountry);
                return true;
            }
        }
        return false;
    }

    private void updateAccount() {

        String str_age = age.getText().toString().trim();
        String str_country = country.getText().toString().trim();
        String str_status = status.getText().toString().trim();

        if (str_status.isEmpty()) str_status = null;
        if (str_country.isEmpty()) str_country = null;

        Integer num_age = null;

        try {
            num_age = Integer.parseInt(str_age);
            //     num_gender = Integer.parseInt(str_gender);
        } catch (Exception e) {
            System.out.println("not a number");
        }

        initGender();

        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("profile").child("age").setValue(num_age);
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("profile").child("gender").setValue(gender);
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("profile").child("country").setValue(str_country);
        MainActivity.mDatabase.child("user_public").child(User.getPublicKey()).child("profile").child("status").setValue(str_status)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        cancel(); // close dialog
                        profileActivity.getProfileInfo();
                    }
                });
    }

    private void initGender() {
        switch (gender_list_box.getText().toString()) {
            case "Male":
                gender = 0;
                break;
            case "Female":
                gender = 1;
                break;
            case "Not specified":
                gender = null;
                break;
        }
    }

    private void closeDialog() {
        cancel();
        profileActivity.setProfile(age.getText().toString(), status.getText().toString(), country.getText().toString(), gender_list_box.getText().toString());
    }

    private void openNicknameManager() {
        Intent intent = new Intent(getContext(), NicknameManagerActivity.class);
        getContext().startActivity(intent);
        closeDialog();
    }


    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
