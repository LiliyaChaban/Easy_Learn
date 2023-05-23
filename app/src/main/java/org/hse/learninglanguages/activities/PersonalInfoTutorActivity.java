package org.hse.learninglanguages.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.databinding.ActivityPersonalInfoTutorBinding;
import org.hse.learninglanguages.models.Tutor;
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

@SuppressLint("SetTextI18n")
public class PersonalInfoTutorActivity extends BaseActivity {

    private ActivityPersonalInfoTutorBinding binding;
    private Tutor tutor;
    private String encodedImage;
    private FirebaseFirestore database;
    Dialog dialog;
    DatabaseReference databaseReference;
    DocumentReference reference;
    private PreferenceManager preferenceManager;
    List<String> names;
    List<String> education;
    List<String> workExperience;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityPersonalInfoTutorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        names = new ArrayList<>();
        education = new ArrayList<>();
        workExperience = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        tutor = new Tutor();
        getTutor();
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
                    String experience = (String) childSnapshot.child("item").getValue();
                    workExperience.add(experience);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        setListeners();
    }
    private void setListeners(){
        binding.buttonSave.setOnClickListener(v -> {
            if(isValidSaveDetails()){
                updateTutor();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            createDialog();
        });
        binding.country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(PersonalInfoTutorActivity.this);
                dialog.setContentView(R.layout.dialog_searchable_spinner);
                dialog.getWindow().setLayout(1300, 2500);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PersonalInfoTutorActivity.this, android.R.layout.simple_list_item_1, names);
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
                        binding.country.setText(adapter.getItem(position));
                        dialog.dismiss();
                    }
                });
            }
        });

        binding.education.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(PersonalInfoTutorActivity.this);
                dialog.setContentView(R.layout.my_spinner);
                TextView title = dialog.findViewById(R.id.title);
                title.setText(getResources().getString(R.string.education_level));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PersonalInfoTutorActivity.this, android.R.layout.simple_list_item_1, education);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        binding.education.setText(adapter.getItem(position));
                        dialog.dismiss();
                    }
                });
            }
        });

        binding.workExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(PersonalInfoTutorActivity.this);
                dialog.setContentView(R.layout.my_spinner);
                TextView title = dialog.findViewById(R.id.title);
                title.setText(getResources().getString(R.string.work_experience));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PersonalInfoTutorActivity.this, android.R.layout.simple_list_item_1, workExperience);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        binding.workExperience.setText(adapter.getItem(position));
                        dialog.dismiss();
                    }
                });
            }
        });

        binding.dateOfBirth.setOnClickListener(new View.OnClickListener() {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(PersonalInfoTutorActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                        String dateFormat = dateformat.format(new Date(year - 1900, month, dayOfMonth));
                        binding.dateOfBirth.setText(dateFormat);

                    }
                }, year, month, day);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_photo);
        builder.setMessage(R.string.how_edit_photo)
                .setCancelable(true)
                .setPositiveButton(R.string.edit_photo,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                pickImage.launch(intent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(R.string.delete_photo,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                encodedImage = "";
                                binding.imageProfile.setImageBitmap(getProfileImage(encodedImage));
                                binding.textAddImage.setVisibility(View.VISIBLE);
                                dialog.cancel();
                            }
                        })
                .setNeutralButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        builder.show();
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

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private  Boolean isValidSaveDetails(){
        if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        } else if (!binding.phoneNumber.getText().toString().trim().equals("") && !Patterns.PHONE.matcher(binding.phoneNumber.getText().toString()).matches()){
            showToast("Enter correct phone number");
            return false;
        } else {
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSave.setVisibility(View.VISIBLE);
        }

    }

    private void getTutor(){
        tutor.name = "";
        tutor.image = "";
        tutor.country = "";
        tutor.aboutYourself = "";
        tutor.phone = "";
        tutor.token = "";
        tutor.dateOfBirth = "";
        tutor.education = "";
        tutor.workExperience = "";
        tutor.gender = "";

        reference = database.collection(Constants.KEY_COLLECTION_TUTORS).document(preferenceManager.getString(Constants.KEY_TUTOR_ID));
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        tutor.name = document.getString(Constants.KEY_NAME);
                        tutor.image = document.getString(Constants.KEY_IMAGE);
                        tutor.country = document.getString(Constants.KEY_COUNTRY);
                        tutor.aboutYourself = document.getString(Constants.KEY_ABOUT_YOURSELF);
                        tutor.token = document.getString(Constants.KEY_FCM_TOKEN);
                        tutor.dateOfBirth = document.getString(Constants.KEY_DATE_OF_BIRTH);
                        tutor.education = document.getString(Constants.KEY_EDUCATION);
                        tutor.workExperience = document.getString(Constants.KEY_WORK_EXPERIENCE);
                        tutor.phone = document.getString(Constants.KEY_PHONE);
                        tutor.gender = document.getString(Constants.KEY_GENDER);
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

    private void updateTutor(){
        loading(true);
        HashMap<String, Object> tutor = new HashMap<>();
        tutor.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        tutor.put(Constants.KEY_PHONE, binding.phoneNumber.getText().toString());
        tutor.put(Constants.KEY_DATE_OF_BIRTH, binding.dateOfBirth.getText().toString());
        tutor.put(Constants.KEY_ABOUT_YOURSELF, binding.aboutYourself.getText().toString());
        if(encodedImage == null) {
            encodedImage = "";
        }
        tutor.put(Constants.KEY_IMAGE, encodedImage);
        tutor.put(Constants.KEY_GENDER, binding.gender.getSelectedItem().toString());
        tutor.put(Constants.KEY_COUNTRY, binding.country.getText().toString());
        tutor.put(Constants.KEY_EDUCATION, binding.education.getText().toString());
        tutor.put(Constants.KEY_WORK_EXPERIENCE, binding.workExperience.getText().toString());
        reference = database.collection(Constants.KEY_COLLECTION_TUTORS).document(preferenceManager.getString(Constants.KEY_TUTOR_ID));
        reference.update(tutor).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loading(false);
                showToast(getResources().getString(R.string.update));
            }
        });
    }

    private void loadTutorDetails(){
        binding.inputName.setText(tutor.name);
        if(tutor.gender.equals(getResources().getString(R.string.female))){
            binding.gender.setSelection(0);
        }else{
            binding.gender.setSelection(1);
        }

        if(!tutor.image.equals("")){
            binding.textAddImage.setVisibility(View.GONE);
            binding.imageProfile.setImageBitmap(getProfileImage(tutor.image));
        }
        if(!tutor.country.equals("")){
            binding.country.setText(tutor.country);
        }
        if(!tutor.phone.equals("")){
            binding.phoneNumber.setText(tutor.phone);
        }
        if(!tutor.dateOfBirth.equals("")){
            binding.dateOfBirth.setText(tutor.dateOfBirth);
        }
        if(!tutor.education.equals("")){
            binding.education.setText(tutor.education);
        }
        if(!tutor.workExperience.equals("")){
            binding.workExperience.setText(tutor.workExperience);
        }
        if(!tutor.aboutYourself.equals("")){
            binding.aboutYourself.setText(tutor.aboutYourself);
        }
    }

    private Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}