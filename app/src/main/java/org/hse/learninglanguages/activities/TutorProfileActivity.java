package org.hse.learninglanguages.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.adapters.LanguagesAdapter;
import org.hse.learninglanguages.adapters.LanguagesProfileAdapter;
import org.hse.learninglanguages.databinding.ActivityTutorProfileBinding;
import org.hse.learninglanguages.models.Language;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@SuppressLint("SetTextI18n")
public class TutorProfileActivity extends BaseActivity {

    private ActivityTutorProfileBinding binding;
    private Tutor tutor;
    private FirebaseFirestore database;
    DocumentReference reference;
    private PreferenceManager preferenceManager;
    List<Language> selectedLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityTutorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        selectedLanguages = new ArrayList<>();
        tutor = new Tutor();
        getTutor();
        setListeners();
    }

    private void setListeners(){
        binding.buttonSignOut.setOnClickListener(view -> signOut());
        binding.messageBtn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ChatActivity.class).putExtra(Constants.KEY_TUTOR, tutor))
                );

    }

    private void getTutor(){

//        reference = FirebaseDatabase.getInstance().getReference(Constants.KEY_COLLECTION_TUTORS);
//        reference.child(documentId).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DataSnapshot document = task.getResult();
//                if (document.exists()) {
//                    tutor.name = String.valueOf(document.child(Constants.KEY_NAME).getValue());
//                    tutor.country = String.valueOf(document.child(Constants.KEY_COUNTRY).getValue());
//                    tutor.aboutYourself = String.valueOf(document.child(Constants.KEY_ABOUT_YOURSELF).getValue());
//                    tutor.image = String.valueOf(document.child(Constants.KEY_IMAGE).getValue());
//                    tutor.token = String.valueOf(document.child(Constants.KEY_FCM_TOKEN).getValue());
//                    tutor.dateOfBirth = String.valueOf(document.child(Constants.KEY_DATE_OF_BIRTH).getValue());
//                    tutor.education = String.valueOf(document.child(Constants.KEY_EDUCATION).getValue());
//                    tutor.workExperience = String.valueOf(document.child(Constants.KEY_WORK_EXPERIENCE).getValue());
//                }
//                else {
//                    Log.d(TAG, "No such document");
//                }
//            }
//            else {
//                Log.d(TAG, "get failed with ", task.getException());
//            }
//        });
        tutor = (Tutor) getIntent().getSerializableExtra(Constants.KEY_TUTOR);
        tutor.country = "";
        tutor.aboutYourself = "";
        tutor.token = "";
        tutor.dateOfBirth = "";
        tutor.education = "";
        tutor.workExperience = "";
//        database.collection(Constants.KEY_COLLECTION_TUTORS).document(
//                tutor.id
//        ).addSnapshotListener(TutorProfileActivity.this, (value, error) -> {
//            if (error != null) {
//                return;
//            }
//            if (value != null) {
//                tutor.name = value.getString(Constants.KEY_NAME);
//                tutor.country = value.getString(Constants.KEY_COUNTRY);
//                tutor.aboutYourself = value.getString(Constants.KEY_ABOUT_YOURSELF);
//                tutor.image = value.getString(Constants.KEY_IMAGE);
//                tutor.token = value.getString(Constants.KEY_FCM_TOKEN);
//                tutor.dateOfBirth = value.getString(Constants.KEY_DATE_OF_BIRTH);
//                tutor.education = value.getString(Constants.KEY_EDUCATION);
//                tutor.workExperience = value.getString(Constants.KEY_WORK_EXPERIENCE);
//                loadTutorDetails();
//            }
//        });

        reference = database.collection(Constants.KEY_COLLECTION_TUTORS).document(tutor.id);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tutor.country = document.getString(Constants.KEY_COUNTRY);
                        tutor.aboutYourself = document.getString(Constants.KEY_ABOUT_YOURSELF);
                        tutor.token = document.getString(Constants.KEY_FCM_TOKEN);
                        tutor.dateOfBirth = document.getString(Constants.KEY_DATE_OF_BIRTH);
                        tutor.education = document.getString(Constants.KEY_EDUCATION);
                        tutor.workExperience = document.getString(Constants.KEY_WORK_EXPERIENCE);
                        loadTutorDetails();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void loadTutorDetails(){

        binding.name.setText(tutor.name);
        binding.imageProfile.setImageBitmap(getProfileImage(tutor.image));
        database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                .whereEqualTo(Constants.KEY_USER_ID, tutor.id)
                .get()
                .addOnCompleteListener(task -> {
                    String currentLanguageId = preferenceManager.getString(Constants.KEY_TUTOR_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentLanguageId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            Language language = new Language();
                            language.language = queryDocumentSnapshot.getString(Constants.KEY_LANGUAGE);
                            language.level = queryDocumentSnapshot.getString(Constants.KEY_LEVEL);
                            language.userId = queryDocumentSnapshot.getString(Constants.KEY_USER_ID);
                            language.id = queryDocumentSnapshot.getId();
                            selectedLanguages.add(language);
                        }
                        LanguagesProfileAdapter languagesAdapter = new LanguagesProfileAdapter(selectedLanguages);
                        binding.languagesRecyclerView.setAdapter(languagesAdapter);
                        binding.languages.setVisibility(View.VISIBLE);
                        binding.languagesRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        //showErrorMessage("No tutor available");
                    }
                });

        if(!tutor.country.equals("")){
            binding.country.setText(tutor.country);
            binding.country.setVisibility(View.VISIBLE);
        }
        if(!tutor.dateOfBirth.equals("")){
            LocalDate currentDate = LocalDate.now();
            LocalDate birthDate = LocalDate.parse(tutor.dateOfBirth);
            binding.age.setText(Period.between(birthDate, currentDate).getYears() + " " + getResources().getString(R.string.years_old));
            binding.age.setVisibility(View.VISIBLE);
        }
        if(!tutor.education.equals("")){
            binding.educationEdit.setText(tutor.education);
            binding.education.setVisibility(View.VISIBLE);
            binding.educationEdit.setVisibility(View.VISIBLE);
        }
        if(!tutor.workExperience.equals("")){
            binding.workExperienceEdit.setText(tutor.workExperience);
            binding.workExperience.setVisibility(View.VISIBLE);
            binding.workExperienceEdit.setVisibility(View.VISIBLE);
        }
        if(!tutor.aboutYourself.equals("")){
            binding.aboutYourselfEdit.setText(tutor.aboutYourself);
            binding.aboutYourself.setVisibility(View.VISIBLE);
            binding.aboutYourselfEdit.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private void signOut(){
        showToast(getResources().getString(R.string.sign_out));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_STUDENTS).document(
                        preferenceManager.getString(Constants.KEY_STUDENT_ID)
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
}