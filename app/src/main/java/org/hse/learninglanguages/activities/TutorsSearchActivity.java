package org.hse.learninglanguages.activities;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.adapters.TutorsAdapter;
import org.hse.learninglanguages.databinding.ActivityTutorsSearchBinding;
import org.hse.learninglanguages.listeners.TutorListener;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TutorsSearchActivity extends BaseActivity implements TutorListener {

    private ActivityTutorsSearchBinding binding;
    private PreferenceManager preferenceManager;
    List<Tutor> tutors;
    List<String> names, education, workExperience;
    DatabaseReference databaseReference;
    List<String> selectedLanguages;
    Integer selectedLanguage;
    Map<Integer, String> selectedEducation, selectedCountries, selectedWorkExperience;
    FirebaseFirestore database;
    Dialog dialogFilter, dialogCountry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorsSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        tutors = new ArrayList<>();
        names = new ArrayList<>();
        education = new ArrayList<>();
        workExperience = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
        selectedCountries = new HashMap<>();
        selectedEducation = new HashMap<>();
        selectedWorkExperience = new HashMap<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        database = FirebaseFirestore.getInstance();
        getFiltersInfo();
        getTutors();
        setListeners();

    }

    private void getFiltersInfo() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Countries").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String countryName = (String) childSnapshot.child("Name").getValue();
                    names.add(countryName);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        databaseReference.child("Education").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String edu = (String) childSnapshot.child("item").getValue();
                    education.add(edu);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        databaseReference.child("WorkExperience").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String work = (String) childSnapshot.child("item").getValue();
                    workExperience.add(work);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void setListeners(){
        binding.filters.setOnClickListener(v -> {
            if(!binding.startSearch.getText().toString().equals("")){
                binding.startSearch.setText("");
            }

            dialogFilter = new Dialog(TutorsSearchActivity.this);
            dialogFilter.setContentView(R.layout.activity_filters);
            dialogFilter.getWindow().setLayout(1300, 2600);
            dialogFilter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogFilter.show();
            Spinner language = dialogFilter.findViewById(R.id.language);
            selectedLanguages.clear();
            TextView country = dialogFilter.findViewById(R.id.country);
            ListView educationList = dialogFilter.findViewById(R.id.educationList);
            ListView workExperienceList = dialogFilter.findViewById(R.id.workExperienceList);
            Button buttonApplyFilters = dialogFilter.findViewById(R.id.buttonApply);
            Button buttonClear = dialogFilter.findViewById(R.id.buttonClear);
            buttonClear.setOnClickListener(v2 -> {
                selectedLanguage = null;
                selectedCountries.clear();
                selectedEducation.clear();
                selectedWorkExperience.clear();
                tutors.clear();
                getTutors();
                dialogFilter.dismiss();
            });

            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        String currentLanguageId = preferenceManager.getString(Constants.KEY_STUDENT_ID);
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (currentLanguageId.equals(queryDocumentSnapshot.getId())) {
                                    continue;
                                }
                                selectedLanguages.add(queryDocumentSnapshot.getString(Constants.KEY_LANGUAGE));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_dropdown_item, selectedLanguages);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            language.setAdapter(adapter);
                            if(selectedLanguage!=null){
                                language.setSelection(selectedLanguage);
                            }
                        }
                    });

            country.setText(selectedCountries.values().stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));
            country.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogCountry = new Dialog(TutorsSearchActivity.this);
                    dialogCountry.setContentView(R.layout.dialog_filter_spinner);
                    dialogCountry.getWindow().setLayout(1300, 2500);
                    dialogCountry.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogCountry.show();
                    ListView listView = dialogCountry.findViewById(R.id.list_view);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(TutorsSearchActivity.this, android.R.layout.simple_list_item_multiple_choice, names);
                    listView.setAdapter(adapter);
                    if(selectedCountries!=null){
                        for (Integer position: selectedCountries.keySet()) {
                            listView.setItemChecked(position, true);
                        }
                    }
                    Button buttonApply = dialogCountry.findViewById(R.id.buttonApply);
                    buttonApply.setOnClickListener(task -> {
                                selectedCountries.clear();
                                SparseBooleanArray array = listView.getCheckedItemPositions();
                                for (int i = 0; i < array.size(); i++) {
                                    int key = array.keyAt(i);
                                    if (array.get(key)){
                                        selectedCountries.put(key, names.get(key));
                                    }

                                }
                                if(!selectedCountries.isEmpty()){
                                    country.setText(selectedCountries.values().stream().map(Object::toString)
                                            .collect(Collectors.joining(", ")));
                                }
                                dialogCountry.dismiss();
                    });
                }
            });
            educationList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(TutorsSearchActivity.this, android.R.layout.simple_list_item_multiple_choice, education);
            educationList.setAdapter(adapter);
            if(educationList!=null){
                for (Integer position: selectedEducation.keySet()) {
                    educationList.setItemChecked(position, true);
                }
            }

            workExperienceList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            ArrayAdapter<String> workAdapter = new ArrayAdapter<>(TutorsSearchActivity.this, android.R.layout.simple_list_item_multiple_choice, workExperience);
            workExperienceList.setAdapter(workAdapter);
            if(workExperienceList!=null){
                for (Integer position: selectedWorkExperience.keySet()) {
                    workExperienceList.setItemChecked(position, true);
                }
            }

            buttonApplyFilters.setOnClickListener(v1 -> {
                selectedLanguage = language.getSelectedItemPosition();

                selectedEducation.clear();
                SparseBooleanArray array = educationList.getCheckedItemPositions();
                for (int i = 0; i < array.size(); i++) {
                    int key = array.keyAt(i);
                    if (array.get(key)){
                        selectedEducation.put(key, education.get(key));
                    }
                }

                selectedWorkExperience.clear();
                SparseBooleanArray array1 = workExperienceList.getCheckedItemPositions();
                for (int i = 0; i < array1.size(); i++) {
                    int key = array1.keyAt(i);
                    if (array1.get(key)){
                        selectedWorkExperience.put(key, workExperience.get(key));
                    }
                }


                List<Tutor> filteredList = new ArrayList<>();
                for (Tutor tutor : tutors) {
                    database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                            .whereEqualTo(Constants.KEY_LANGUAGE, language.getSelectedItem().toString())
                            .whereEqualTo(Constants.KEY_USER_ID, tutor.id)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (!task.getResult().isEmpty()) {
                                    if(!selectedCountries.isEmpty())
                                    {
                                        for (String selectedCountry: selectedCountries.values()) {
                                            database.collection(Constants.KEY_COLLECTION_TUTORS).document(tutor.id)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                                if (task1.getResult().getString(Constants.KEY_COUNTRY).equals(selectedCountry)) {
                                                                    if(!selectedEducation.isEmpty()){
                                                                        for (String education: selectedEducation.values()) {
                                                                            if (task1.getResult().getString(Constants.KEY_EDUCATION).equals(education)) {
                                                                                if(!selectedWorkExperience.isEmpty()){
                                                                                    for (String experience: selectedWorkExperience.values()) {
                                                                                        if (task1.getResult().getString(Constants.KEY_WORK_EXPERIENCE).equals(experience)) {
                                                                                            filteredList.add(tutor);
                                                                                            TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                                            binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                                            binding.textErrorMessage.setVisibility(View.GONE);
                                                                                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                                        }
                                                                                    }
                                                                                }else{
                                                                                    filteredList.add(tutor);
                                                                                    TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                                    binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                                    binding.textErrorMessage.setVisibility(View.GONE);
                                                                                    binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                                }

                                                                            }
                                                                        }
                                                                    }else if(!selectedWorkExperience.isEmpty()){
                                                                        for (String experience: selectedWorkExperience.values()) {
                                                                            if (task1.getResult().getString(Constants.KEY_WORK_EXPERIENCE).equals(experience)) {
                                                                                    filteredList.add(tutor);
                                                                                    TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                                    binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                                    binding.textErrorMessage.setVisibility(View.GONE);
                                                                                    binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                            }
                                                                        }
                                                                    }else{
                                                                        filteredList.add(tutor);
                                                                        TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                        binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                        binding.textErrorMessage.setVisibility(View.GONE);
                                                                        binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                    }
                                                                }
                                                    });
                                        }
                                    }else if(!selectedEducation.isEmpty()){
                                            database.collection(Constants.KEY_COLLECTION_TUTORS).document(tutor.id)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        for (String education: selectedEducation.values()) {
                                                            if (task1.getResult().getString(Constants.KEY_EDUCATION).equals(education)) {
                                                                if(!selectedWorkExperience.isEmpty()){
                                                                    for (String experience: selectedWorkExperience.values()) {
                                                                        if (task1.getResult().getString(Constants.KEY_WORK_EXPERIENCE).equals(experience)) {
                                                                            filteredList.add(tutor);
                                                                            TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                            binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                            binding.textErrorMessage.setVisibility(View.GONE);
                                                                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                        }
                                                                    }
                                                                }else{
                                                                    filteredList.add(tutor);
                                                                    TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                    binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                    binding.textErrorMessage.setVisibility(View.GONE);
                                                                    binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                }

                                                            }
                                                        }
                                                    });
                                        }else if(!selectedWorkExperience.isEmpty()){
                                            database.collection(Constants.KEY_COLLECTION_TUTORS).document(tutor.id)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                                if(!selectedWorkExperience.isEmpty()){
                                                                    for (String experience: selectedWorkExperience.values()) {
                                                                        if (task1.getResult().getString(Constants.KEY_WORK_EXPERIENCE).equals(experience)) {
                                                                            filteredList.add(tutor);
                                                                            TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                            binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                            binding.textErrorMessage.setVisibility(View.GONE);
                                                                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                        }
                                                                    }
                                                                }else{
                                                                    filteredList.add(tutor);
                                                                    TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                                                    binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                                                    binding.textErrorMessage.setVisibility(View.GONE);
                                                                    binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                                                }
                                                    });
                                        }else {
                                            filteredList.add(tutor);
                                            TutorsAdapter tutorsAdapter = new TutorsAdapter(filteredList, TutorsSearchActivity.this);
                                            binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                                            binding.textErrorMessage.setVisibility(View.GONE);
                                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                                        }
                                    }
                            });
                    //int test = binding.tutorsRecyclerView.getAdapter().getItemCount();
                    if(filteredList.isEmpty()){
                        binding.tutorsRecyclerView.setVisibility(View.GONE);
                        binding.textErrorMessage.setText(getResources().getString(R.string.no_such_tutors));
                        binding.textErrorMessage.setVisibility(View.VISIBLE);
                    }
                    //TODO исправить no such tutors in filters
                }

                dialogFilter.dismiss();
            });
            }
        );

        binding.startSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                TutorsAdapter tutorsAdapter = new TutorsAdapter(tutors, TutorsSearchActivity.this);
                binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                tutorsAdapter.getFilter().filter(s, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        if(count == 0){
                            binding.tutorsRecyclerView.setVisibility(View.GONE);
                            binding.textErrorMessage.setVisibility(View.VISIBLE);
                            binding.textErrorMessage.setText(getResources().getString(R.string.no_such_tutor));
                        } else{
                            binding.textErrorMessage.setVisibility(View.GONE);
                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                selectedLanguage = null;
                selectedCountries.clear();
                selectedEducation.clear();
                selectedWorkExperience.clear();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.tutorSearch.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),TutorsSearchActivity.class)));
        binding.chat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),StudentChatActivity.class)));
        binding.buttonSignOut.setOnClickListener(view -> signOut());
        binding.settings.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),StudentSettingsActivity.class)));
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

    private void getTutors(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_TUTORS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentTutorId = preferenceManager.getString(Constants.KEY_TUTOR_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(currentTutorId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            Tutor tutor = new Tutor();
                            tutor.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            tutor.country = queryDocumentSnapshot.getString(Constants.KEY_COUNTRY);
                            tutor.aboutYourself = queryDocumentSnapshot.getString(Constants.KEY_ABOUT_YOURSELF);
                            tutor.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            tutor.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            tutor.dateOfBirth = queryDocumentSnapshot.getString(Constants.KEY_DATE_OF_BIRTH);
                            tutor.education = queryDocumentSnapshot.getString(Constants.KEY_EDUCATION);
                            tutor.workExperience = queryDocumentSnapshot.getString(Constants.KEY_WORK_EXPERIENCE);
                            tutor.id = queryDocumentSnapshot.getId();
                            tutors.add(tutor);
                        }
                        if(tutors.size() > 0){
                            TutorsAdapter tutorsAdapter = new TutorsAdapter(tutors, this);
                            binding.tutorsRecyclerView.setAdapter(tutorsAdapter);
                            binding.tutorsRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage("No tutor available");
                        }
                    }else{
                        showErrorMessage("No tutor available");
                    }
                });
    }

    private void showErrorMessage(String string){
        binding.textErrorMessage.setText(String.format("%s", string));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onTutorClicked(Tutor tutor) {
        Intent intent = new Intent(getApplicationContext(), TutorProfileActivity.class);
        intent.putExtra(Constants.KEY_TUTOR, tutor);
        startActivity(intent);
    }
}