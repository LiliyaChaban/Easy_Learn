package org.hse.learninglanguages.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import org.hse.learninglanguages.adapters.LanguagesProfileAdapter;
import org.hse.learninglanguages.databinding.ActivityStudentProfileBinding;
import org.hse.learninglanguages.databinding.ActivityTutorProfileBinding;
import org.hse.learninglanguages.models.Language;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudentProfileActivity extends AppCompatActivity {

    private ActivityStudentProfileBinding binding;
    private Student student;
    private FirebaseFirestore database;
    DocumentReference reference;
    private PreferenceManager preferenceManager;
    List<Language> selectedLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        student = new Student();
        selectedLanguages = new ArrayList<>();
        getStudent();
        setListeners();
    }

    private void setListeners(){
        binding.buttonSignOut.setOnClickListener(view -> signOut());
        binding.messageBtn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ChatActivity.class).putExtra(Constants.KEY_STUDENT, student))
        );

    }

    private void getStudent(){
        student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);
        student.country = "";
        student.aboutYourself = "";
        student.token = "";
        student.dateOfBirth = "";
        student.purposeOfStudy = "";

        reference = database.collection(Constants.KEY_COLLECTION_STUDENTS).document(student.id);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        student.country = document.getString(Constants.KEY_COUNTRY);
                        student.aboutYourself = document.getString(Constants.KEY_ABOUT_YOURSELF);
                        student.token = document.getString(Constants.KEY_FCM_TOKEN);
                        student.dateOfBirth = document.getString(Constants.KEY_DATE_OF_BIRTH);
                        student.purposeOfStudy = document.getString(Constants.KEY_PURPOSE_OF_STUDY);
                        loadStudentDetails();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void loadStudentDetails(){
        binding.name.setText(student.name);
        binding.imageProfile.setImageBitmap(getProfileImage(student.image));
        database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                .whereEqualTo(Constants.KEY_USER_ID, student.id)
                .get()
                .addOnCompleteListener(task -> {
                    String currentLanguageId = preferenceManager.getString(Constants.KEY_STUDENT_ID);
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

        if(!student.country.equals("")){
            binding.country.setText(student.country);
            binding.country.setVisibility(View.VISIBLE);
        }
        if(!student.dateOfBirth.equals("")){
            LocalDate currentDate = LocalDate.now();
            LocalDate birthDate = LocalDate.parse(student.dateOfBirth);
            binding.age.setText(Period.between(birthDate, currentDate).getYears() + " " + getResources().getString(R.string.years_old));
            binding.age.setVisibility(View.VISIBLE);
        }
        if(!student.purposeOfStudy.equals("")){
            binding.purposeOfStudyEdit.setText(student.purposeOfStudy);
            binding.purposeOfStudy.setVisibility(View.VISIBLE);
            binding.purposeOfStudyEdit.setVisibility(View.VISIBLE);
        }
        if(!student.aboutYourself.equals("")){
            binding.aboutYourselfEdit.setText(student.aboutYourself);
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
}