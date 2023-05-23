package org.hse.learninglanguages.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import org.hse.learninglanguages.databinding.ActivityRoleSelectionBinding;

public class RoleSelection extends AppCompatActivity {
    private ActivityRoleSelectionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.buttonStudentSignUp.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),LanguageSelectionActivity.class).putExtra("role", "learner")));
        binding.buttonTeacherSignUp.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),LanguageSelectionActivity.class).putExtra("role", "teacher")));
    }
}