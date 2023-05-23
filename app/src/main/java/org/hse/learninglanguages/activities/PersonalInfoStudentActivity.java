package org.hse.learninglanguages.activities;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

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
import android.service.controls.Control;
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
import org.hse.learninglanguages.databinding.ActivityPersonalInfoStudentBinding;
import org.hse.learninglanguages.databinding.ActivityStudentProfileBinding;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
@SuppressLint("SetTextI18n")
public class PersonalInfoStudentActivity extends BaseActivity {
    private ActivityPersonalInfoStudentBinding binding;
    private Student student;
    private String encodedImage;
    private FirebaseFirestore database;
    Dialog dialog;
    DatabaseReference databaseReference;
    DocumentReference reference;
    private PreferenceManager preferenceManager;
    List<String> names;
    List<String> purposes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityPersonalInfoStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        names = new ArrayList<>();
        purposes = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        student = new Student();
        getStudent();
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
        binding.buttonSave.setOnClickListener(v -> {
            if(isValidSaveDetails()){
                updateStudent();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            createDialog();
        });
        binding.country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(PersonalInfoStudentActivity.this);
                dialog.setContentView(R.layout.dialog_searchable_spinner);
                dialog.getWindow().setLayout(1300, 2500);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PersonalInfoStudentActivity.this,
                        android.R.layout.simple_list_item_1, names);
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

        binding.purposeOfStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(PersonalInfoStudentActivity.this);
                dialog.setContentView(R.layout.my_spinner);
                TextView title = dialog.findViewById(R.id.title);
                title.setText(getResources().getString(R.string.purpose_of_study));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                ListView listView = dialog.findViewById(R.id.list_view);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PersonalInfoStudentActivity.this, android.R.layout.simple_list_item_1, purposes);
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

        binding.dateOfBirth.setOnClickListener(new View.OnClickListener() {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(PersonalInfoStudentActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
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
        //builder.create().setFeatureDrawableResource(R.color.primary,);
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

    private void getStudent(){
        //student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);
        student.name = "";
        student.image = "";
        student.country = "";
        student.aboutYourself = "";
        student.phone = "";
        student.token = "";
        student.dateOfBirth = "";
        student.purposeOfStudy = "";
        student.gender = "";

        reference = database.collection(Constants.KEY_COLLECTION_STUDENTS).document(preferenceManager.getString(Constants.KEY_STUDENT_ID));
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        student.name = document.getString(Constants.KEY_NAME);
                        student.image = document.getString(Constants.KEY_IMAGE);
                        student.country = document.getString(Constants.KEY_COUNTRY);
                        student.aboutYourself = document.getString(Constants.KEY_ABOUT_YOURSELF);
                        student.token = document.getString(Constants.KEY_FCM_TOKEN);
                        student.dateOfBirth = document.getString(Constants.KEY_DATE_OF_BIRTH);
                        student.purposeOfStudy = document.getString(Constants.KEY_PURPOSE_OF_STUDY);
                        student.phone = document.getString(Constants.KEY_PHONE);
                        student.gender = document.getString(Constants.KEY_GENDER);
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

    private void updateStudent(){
        loading(true);
        //student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);
//        student.name = "";
//        student.image = "";
//        student.country = "";
//        student.aboutYourself = "";
//        student.phone = "";
//        student.token = "";
//        student.dateOfBirth = "";
//        student.purposeOfStudy = "";
//        student.gender = "";
        HashMap<String, Object> student = new HashMap<>();
        student.put(Constants.KEY_NAME, binding.inputName.getText().toString());
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
        reference = database.collection(Constants.KEY_COLLECTION_STUDENTS).document(preferenceManager.getString(Constants.KEY_STUDENT_ID));
        reference.update(student).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loading(false);
                showToast(getResources().getString(R.string.update));
            }
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        loading(false);
//                        student.name = document.getString(Constants.KEY_NAME);
//                        student.image = document.getString(Constants.KEY_IMAGE);
//                        student.country = document.getString(Constants.KEY_COUNTRY);
//                        student.aboutYourself = document.getString(Constants.KEY_ABOUT_YOURSELF);
//                        student.token = document.getString(Constants.KEY_FCM_TOKEN);
//                        student.dateOfBirth = document.getString(Constants.KEY_DATE_OF_BIRTH);
//                        student.purposeOfStudy = document.getString(Constants.KEY_PURPOSE_OF_STUDY);
//                        student.phone = document.getString(Constants.KEY_PHONE);
//                        student.gender = document.getString(Constants.KEY_GENDER);
//                        loadStudentDetails();
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
        });
    }

    private void loadStudentDetails(){
        binding.inputName.setText(student.name);
        if(student.gender.equals(getResources().getString(R.string.female))){
            binding.gender.setSelection(0);
        }else{
            binding.gender.setSelection(1);
        }

        if(!student.image.equals("")){
            binding.textAddImage.setVisibility(View.GONE);
            binding.imageProfile.setImageBitmap(getProfileImage(student.image));
        }
        if(!student.country.equals("")){
            binding.country.setText(student.country);
            //binding.country.setVisibility(View.VISIBLE);
        }
        if(!student.phone.equals("")){
            binding.phoneNumber.setText(student.phone);
//            binding.aboutYourself.setVisibility(View.VISIBLE);
//            binding.aboutYourselfEdit.setVisibility(View.VISIBLE);
        }
        if(!student.dateOfBirth.equals("")){
//            LocalDate currentDate = LocalDate.now();
//            LocalDate birthDate = LocalDate.parse(student.dateOfBirth);
            binding.dateOfBirth.setText(student.dateOfBirth);
            //binding.age.setVisibility(View.VISIBLE);
        }
        if(!student.purposeOfStudy.equals("")){
            binding.purposeOfStudy.setText(student.purposeOfStudy);
//            binding.purposeOfStudy.setVisibility(View.VISIBLE);
//            binding.purposeOfStudyEdit.setVisibility(View.VISIBLE);
        }
        if(!student.aboutYourself.equals("")){
            binding.aboutYourself.setText(student.aboutYourself);
//            binding.aboutYourself.setVisibility(View.VISIBLE);
//            binding.aboutYourselfEdit.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}