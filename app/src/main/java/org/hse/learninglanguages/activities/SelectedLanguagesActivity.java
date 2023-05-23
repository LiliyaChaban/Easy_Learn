package org.hse.learninglanguages.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.adapters.LanguagesAdapter;
import org.hse.learninglanguages.adapters.TutorsAdapter;
import org.hse.learninglanguages.databinding.ActivityLanguageSelectionBinding;
import org.hse.learninglanguages.databinding.ActivitySelectedLanguagesBinding;
import org.hse.learninglanguages.databinding.ActivityTutorsSearchBinding;
import org.hse.learninglanguages.listeners.LanguageListener;
import org.hse.learninglanguages.models.Language;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SelectedLanguagesActivity extends BaseActivity implements LanguageListener {
    private ActivitySelectedLanguagesBinding binding;
    private PreferenceManager preferenceManager;
    FirebaseFirestore database;
    DatabaseReference databaseReference;
    DocumentReference reference;
    List<Language> selectedLanguages;
    Dialog dialog;
    String selectedLanguage, selectedLevel;
    List<String>  allLanguages, allLevels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectedLanguagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        databaseReference = FirebaseDatabase.getInstance().getReference();
        database = FirebaseFirestore.getInstance();
        selectedLanguages = new ArrayList<>();
        allLanguages = new ArrayList<>();
        allLevels = new ArrayList<>();
        getAllLanguages();
        getLanguages();
        setListeners();
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setListeners() {
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLanguages();
            }
        });
                binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog = new Dialog(SelectedLanguagesActivity.this);
                        dialog.setContentView(R.layout.dialog_searchable_spinner);
                        TextView title = dialog.findViewById(R.id.title);
                        title.setText(getResources().getString(R.string.choose_new_language));
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                        EditText editText = dialog.findViewById(R.id.edit_text);
                        ListView listView = dialog.findViewById(R.id.list_view);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectedLanguagesActivity.this, android.R.layout.simple_list_item_1, allLanguages);
                        listView.setAdapter(adapter);
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapter.getFilter().filter(s);
                            }
                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                selectedLanguage = adapter.getItem(position);
                                dialog.dismiss();
                                dialog = new Dialog(SelectedLanguagesActivity.this);
                                dialog.setContentView(R.layout.my_spinner);
                                TextView title = dialog.findViewById(R.id.title);
                                title.setText(getResources().getString(R.string.level_of_knowledge));
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();
                                ListView listView = dialog.findViewById(R.id.list_view);
                                ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(SelectedLanguagesActivity.this, android.R.layout.simple_list_item_activated_1, allLevels);

                                listView.setAdapter(levelAdapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        selectedLevel = levelAdapter.getItem(position);
                                        saveLanguage();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                    }
                });


    }

    private void updateLanguages() {
        for (Language selectedLanguage: selectedLanguages) {
            HashMap<String, Object> language = new HashMap<>();
            language.put(Constants.KEY_LEVEL, selectedLanguage.level);
            reference = database.collection(Constants.KEY_COLLECTION_LANGUAGES).document(selectedLanguage.id);
            reference.update(language);
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void unused) {
//
//                }
//            });
        }
        showToast(getResources().getString(R.string.update));
        binding.buttonSave.setVisibility(View.GONE);
    }

    private void getAllLanguages() {
        databaseReference.child("Languages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String language = (String) childSnapshot.child("name").getValue();
                    allLanguages.add(language);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        databaseReference.child("Levels").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String language = (String) childSnapshot.child("code").getValue();
                    allLevels.add(language);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void saveLanguage() {
        HashMap<String, Object> language = new HashMap<>();
        language.put(Constants.KEY_LANGUAGE, selectedLanguage);
        language.put(Constants.KEY_LEVEL, selectedLevel);
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)){
            language.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                    .whereEqualTo(Constants.KEY_LANGUAGE, selectedLanguage)
                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if(!task.getResult().isEmpty()){
                            showToast(getResources().getString(R.string.language_repetition));
                        }else{
                            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                                    .add(language)
                                    .addOnSuccessListener(document -> {
                                        selectedLanguages.clear();
                                        getLanguages();
                                        showToast(getResources().getString(R.string.update));
                                    });
                        }
                    });
        }else{
            language.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID));
            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                    .whereEqualTo(Constants.KEY_LANGUAGE, selectedLanguage)
                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if(!task.getResult().isEmpty()){
                            showToast(getResources().getString(R.string.language_repetition));
                        }else{
                            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                                    .add(language)
                                    .addOnSuccessListener(document -> {
                                        selectedLanguages.clear();
                                        getLanguages();
                                        showToast(getResources().getString(R.string.update));
                                    });
                        }
                    });
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getLanguages(){
        loading(true);
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)) {
            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
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
                                Collections.sort(selectedLanguages, (a, b) -> a.language.compareTo(b.language));
                                LanguagesAdapter languagesAdapter = new LanguagesAdapter(selectedLanguages, this);
                                binding.languagesRecyclerView.setAdapter(languagesAdapter);
                                binding.languagesRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            //showErrorMessage("No tutor available");
                        }
                    });
        }else{
            database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
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
                            Collections.sort(selectedLanguages, (a, b) -> a.language.compareTo(b.language));
                            LanguagesAdapter languagesAdapter = new LanguagesAdapter(selectedLanguages, this);
                            binding.languagesRecyclerView.setAdapter(languagesAdapter);
                            binding.languagesRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            //showErrorMessage("No tutor available");
                        }
                    });
        }
    }

    @Override
    public void onDeleteClicked(Language language) {
        if(selectedLanguages.size()>1){
            reference = database.collection(Constants.KEY_COLLECTION_LANGUAGES).document(language.id);
            reference.delete().addOnSuccessListener(v -> {
                selectedLanguages.clear();
                getLanguages();
                showToast(getResources().getString(R.string.deleted));
            });
        }else{
            showToast(getResources().getString(R.string.stop_delete));
        }

    }

    @Override
    public void onLevelClicked(Language language) {
        dialog = new Dialog(SelectedLanguagesActivity.this);
        dialog.setContentView(R.layout.my_spinner);
        TextView title = dialog.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.level_of_knowledge));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        ListView listView = dialog.findViewById(R.id.list_view);
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(SelectedLanguagesActivity.this, android.R.layout.simple_list_item_activated_1, allLevels);

        listView.setAdapter(levelAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Language changedLanguage = new Language();
                changedLanguage.id = language.id;
                changedLanguage.language = language.language;
                changedLanguage.level = levelAdapter.getItem(position);
                changedLanguage.userId = language.userId;
                selectedLanguages.remove(language);
                selectedLanguages.add(changedLanguage);
                Collections.sort(selectedLanguages, (a, b) -> a.language.compareTo(b.language));
                LanguagesAdapter languagesAdapter = new LanguagesAdapter(selectedLanguages, SelectedLanguagesActivity.this);
                binding.languagesRecyclerView.setAdapter(languagesAdapter);
                binding.buttonSave.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
    }
}