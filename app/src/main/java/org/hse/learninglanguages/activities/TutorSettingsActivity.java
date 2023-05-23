package org.hse.learninglanguages.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.databinding.ActivityStudentSettingsBinding;
import org.hse.learninglanguages.databinding.ActivityTutorSettingsBinding;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.HashMap;

public class TutorSettingsActivity extends BaseActivity {

    ActivityTutorSettingsBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_settings);
        binding = ActivityTutorSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void signOut(){
        showToast(getResources().getString(R.string.sign_out));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_TUTORS).document(
                        preferenceManager.getString(Constants.KEY_TUTOR_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        //showToast("Unable to sign out"),
                        Log.d(TAG, e.toString())
                );
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setListeners(){
        binding.personalInformation.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),PersonalInfoTutorActivity.class)));
        binding.chat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),TutorChatActivity.class)));
        binding.languages.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SelectedLanguagesActivity.class)));
        binding.buttonSignOut.setOnClickListener(view -> signOut());
//        binding.buttonTeacherSignUp.setOnClickListener(v ->
//                startActivity(new Intent(getApplicationContext(),LanguageSelectionActivity.class)));
    }
}