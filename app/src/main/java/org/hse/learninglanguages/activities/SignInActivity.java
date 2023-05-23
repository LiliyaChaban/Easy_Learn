package org.hse.learninglanguages.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hse.learninglanguages.databinding.ActivitySignInBinding;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
//        preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)



        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null ){
            if(currentUser.isEmailVerified()){
                Intent intent;
                if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)){
                    intent = new Intent(getApplicationContext(), TutorsSearchActivity.class);
                }else{
                    intent = new Intent(getApplicationContext(), TutorChatActivity.class);
                }
                startActivity(intent);
                finish();
            }

        }

    }

    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),RoleSelection.class)));
        binding.buttonSignIn.setOnClickListener(v -> {

                if (isValidSignInDetails()) {


                        signIn();

                }

        });
    }

    private void signIn(){
        loading(true);
        mAuth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user!=null;
                        if(user.isEmailVerified()) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection(Constants.KEY_COLLECTION_STUDENTS)
                                    .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                                    .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful() && task.getResult() != null
                                                && task.getResult().getDocuments().size() > 0){

                                            //user.reload();
                                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putBoolean(Constants.KEY_STUDENT_LOGIN, true);
                                            preferenceManager.putString(Constants.KEY_STUDENT_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_TUTOR_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                            Intent intent = new Intent(getApplicationContext(), TutorsSearchActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        }else{

                                            database.collection(Constants.KEY_COLLECTION_TUTORS)
                                                    .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                                                    .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if(task1.isSuccessful() && task1.getResult() != null
                                                                && task1.getResult().getDocuments().size() > 0){
                                                            DocumentSnapshot documentSnapshot = task1.getResult().getDocuments().get(0);
                                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                                            preferenceManager.putBoolean(Constants.KEY_STUDENT_LOGIN, false);
                                                            preferenceManager.putString(Constants.KEY_TUTOR_ID, documentSnapshot.getId());
                                                            preferenceManager.putString(Constants.KEY_STUDENT_ID, documentSnapshot.getId());
                                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                                            Intent intent = new Intent(getApplicationContext(), TutorChatActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        }else{
                                                            loading(false);
                                                            showToast("Unable to sign in");
                                                        }

                                                    });

                                        }


                                    });
                        }else{
                            showToast("Verify your email");
                            loading(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Write correct data");
                        loading(false);
                    }
                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        } else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }
}