package org.hse.learninglanguages.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.rpc.context.AttributeContext;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.databinding.ActivityLanguageSelectionBinding;
import org.hse.learninglanguages.databinding.ActivitySignUpStudentBinding;
import org.hse.learninglanguages.utilities.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
@SuppressLint("SetTextI18n")
public class LanguageSelectionActivity extends AppCompatActivity {
    private ActivityLanguageSelectionBinding binding;
    List<String> languages;
    String selectedLanguage;
    String selectedLevel;
    List<String> levels;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        String language = Locale.getDefault().getLanguage();
//        Locale locale = new Locale(language);
        //Resources resources = this.getResources();
//        Configuration configuration = resources.getConfiguration();
//        configuration.setLocale(locale);
//        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        String role = getIntent().getStringExtra("role");
        if(role.equals("teacher"))
        {
            binding.topic.setText(getResources().getString(R.string.choose_language_to_teach));
        }else{
            binding.topic.setText(getResources().getString(R.string.choose_language_to_learn));
        }
        languages = new ArrayList<>();
        levels = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Languages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String language = (String) childSnapshot.child("name").getValue();
                    languages.add(language);
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
                    String level = (String) childSnapshot.child("code").getValue();
                    levels.add(level);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


                ArrayAdapter<String> adapter = new ArrayAdapter<>(LanguageSelectionActivity.this, android.R.layout.simple_list_item_activated_1, languages);

                binding.listView.setAdapter(adapter);

                binding.editText.addTextChangedListener(new TextWatcher() {
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
                binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedLanguage = adapter.getItem(position);
                        binding.editText.setVisibility(View.GONE);
                        binding.topic.setText(R.string.level_of_knowledge);

                        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(LanguageSelectionActivity.this, android.R.layout.simple_list_item_activated_1, levels);

                        binding.listView.setAdapter(levelAdapter);

                        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                selectedLevel = levelAdapter.getItem(position);
                                Intent intent;
                                if(role.equals("teacher")){
                                   intent = new Intent(getApplicationContext(), SignUpTutorActivity.class);
                                }else {
                                    intent = new Intent(getApplicationContext(), SignUpStudentActivity.class);
                                }
                                intent.putExtra("language", selectedLanguage);
                                intent.putExtra("level", selectedLevel);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }


}