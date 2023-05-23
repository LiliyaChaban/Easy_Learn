package org.hse.learninglanguages.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.databinding.ActivitySignUpStudentBinding;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SignUpStudentActivity extends AppCompatActivity {

    private ActivitySignUpStudentBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    private String encodedImage;
    TextView country;
    TextView birthday;
    List<String> names;
    Dialog dialog;
    //Spinner purposeOfStudy;
    List<String> purposes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        names = new ArrayList<>();
        purposes = new ArrayList<>();
        country = findViewById(R.id.country);
        birthday = findViewById(R.id.dateOfBirth);
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
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
        databaseReference.child("PurposeOfStudy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                    String purpose = (String) childSnapshot.child("item").getValue();
                    purposes.add(purpose);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> startActivity(
                new Intent(getApplicationContext(), SignInActivity.class)));
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(SignUpStudentActivity.this);
                dialog.setContentView(R.layout.dialog_searchable_spinner);
                dialog.getWindow().setLayout(1300, 2500);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpStudentActivity.this, android.R.layout.simple_list_item_1, names);
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
                        country.setText(adapter.getItem(position));
                        dialog.dismiss();
                    }
                });
            }
        });

        binding.purposeOfStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(SignUpStudentActivity.this);
                dialog.setContentView(R.layout.my_spinner);
                TextView title = dialog.findViewById(R.id.title);
                title.setText(getResources().getString(R.string.purpose_of_study));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpStudentActivity.this, android.R.layout.simple_list_item_1, purposes);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        binding.purposeOfStudy.setText(adapter.getItem(position));
                        dialog.dismiss();
                    }
                });
            }
        });

        birthday.setOnClickListener(new View.OnClickListener() {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(SignUpStudentActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                        String dateFormat = dateformat.format(new Date(year - 1900, month, dayOfMonth));
                        birthday.setText(dateFormat);

                    }
                }, year, month, day);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        mAuth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        HashMap<String, Object> student = new HashMap<>();
                        student.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                        student.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                        student.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                        student.put(Constants.KEY_PHONE, binding.phoneNumber.getText().toString());
                        student.put(Constants.KEY_DATE_OF_BIRTH, binding.dateOfBirth.getText().toString());
                        student.put(Constants.KEY_ABOUT_YOURSELF, binding.aboutYourself.getText().toString());
                        if(encodedImage == null) {
                            encodedImage = "";
                        }
                        student.put(Constants.KEY_IMAGE, encodedImage);
                        student.put(Constants.KEY_GENDER, binding.gender.getSelectedItem().toString());
                        student.put(Constants.KEY_COUNTRY, binding.country.getText().toString());
                        student.put(Constants.KEY_PURPOSE_OF_STUDY, binding.purposeOfStudy.getText().toString());
                        database.collection(Constants.KEY_COLLECTION_STUDENTS)
                                .add(student)
                                .addOnSuccessListener(documentReference -> {
                                    loading(false);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putBoolean(Constants.KEY_STUDENT_LOGIN, true);
                                    preferenceManager.putString(Constants.KEY_STUDENT_ID, documentReference.getId());
                                    preferenceManager.putString(Constants.KEY_TUTOR_ID, documentReference.getId());
                                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                                    HashMap<String, Object> language = new HashMap<>();
                                    language.put(Constants.KEY_LANGUAGE, getIntent().getStringExtra("language"));
                                    language.put(Constants.KEY_LEVEL, getIntent().getStringExtra("level"));
                                    language.put(Constants.KEY_USER_ID, documentReference.getId());
                                    database.collection(Constants.KEY_COLLECTION_LANGUAGES)
                                            .add(language)
                                            .addOnSuccessListener(document -> {
                                                sendEmailVer();
//                                                FirebaseUser user = mAuth.getCurrentUser();
//
//                                                assert user!=null;
//                                                if(user.isEmailVerified()){
//                                                    Intent intent = new Intent(getApplicationContext(), TutorsSearchActivity.class);
//                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                    startActivity(intent);
//                                                }else{
                                                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
//                                                }
                                            });
                                })
                                .addOnFailureListener(exception -> {
                                    loading(false);
                                    showToast(exception.getMessage());
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getMessage());
                    }
                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUrl = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUrl);
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private  Boolean isValidSignUpDetails(){
        if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password and confirm password must be same");
            return false;
        } else if (!binding.phoneNumber.getText().toString().trim().equals("") && !Patterns.PHONE.matcher(binding.phoneNumber.getText().toString()).matches()){
            showToast("Enter correct phone number");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }

    }

    private void sendEmailVer(){
        FirebaseUser user = mAuth.getCurrentUser();
        assert user !=null;
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast(getResources().getString(R.string.checking_email));
                }else{
                    showToast(getResources().getString(R.string.sending_email_failed));
                }
            }
        });
    }
}