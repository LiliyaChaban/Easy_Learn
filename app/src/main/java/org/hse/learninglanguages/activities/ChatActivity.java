package org.hse.learninglanguages.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.C;
import org.hse.learninglanguages.R;
import org.hse.learninglanguages.adapters.ChatAdapter;
import org.hse.learninglanguages.databinding.ActivityChatBinding;
import org.hse.learninglanguages.models.ChatMessage;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.network.ApiClient;
import org.hse.learninglanguages.network.ApiService;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private Tutor tutor;
    private Student student;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private  Boolean isReceiverAvailable = false;
    private String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)) {
            loadTutorDetails();
            initTutor();
        }else{
            loadStudentDetails();
            initStudent();
        }
        listenMessages();

    }

    private void initTutor(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(tutor.image),
                preferenceManager.getString(Constants.KEY_TUTOR_ID)
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void initStudent(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(student.image),
                preferenceManager.getString(Constants.KEY_STUDENT_ID)
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN))
        {
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
            message.put(Constants.KEY_RECEIVER_ID, tutor.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversionId != null){
                updateConversion(binding.inputMessage.getText().toString().trim());
            }else{
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, tutor.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, tutor.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, tutor.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString().trim());
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
            if(!isReceiverAvailable){
                try{
                    JSONArray tokens = new JSONArray();
                    JSONObject data = new JSONObject();
                    tokens.put(tutor.token);
                    data.put(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                    sendNotification(body.toString());
                }catch (Exception exception){
                    showToast(exception.getMessage());
                }
            }
            binding.inputMessage.setText(null);
        }else{
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID));
            message.put(Constants.KEY_RECEIVER_ID, student.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversionId != null){
                updateConversion(binding.inputMessage.getText().toString().trim());
            }else{
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, student.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, student.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, student.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString().trim());
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
            if(!isReceiverAvailable){
                try{
                    JSONArray tokens = new JSONArray();
                    JSONObject data = new JSONObject();
//                    if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)) {
//                        tokens.put(tutor.token);
//                        data.put(Constants.KEY_STUDENT_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID));
//                    }else {
                        tokens.put(student.token);
                        data.put(Constants.KEY_TUTOR_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID));
//                    }
                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                    sendNotification(body.toString());
                }catch (Exception exception){
                    showToast(exception.getMessage());
                }
            }
            binding.inputMessage.setText(null);
        }

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try{
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.fillInStackTrace();
                    }
                    //showToast("Notification sent successfully");
                }else{
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver(){
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN))
        {
            database.collection(Constants.KEY_COLLECTION_TUTORS).document(
                    tutor.id
            ).addSnapshotListener(ChatActivity.this, (value, error) -> {
                if(error !=null){
                    return;
                }
                if(value != null){
                    if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                        int availability = Objects.requireNonNull(
                                value.getLong(Constants.KEY_AVAILABILITY)
                        ).intValue();
                        isReceiverAvailable = availability == 1;
                    }
                    tutor.token = value.getString(Constants.KEY_FCM_TOKEN);
                    if(tutor.image == null){
                        tutor.image = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setProfileImage(getBitmapFromEncodedString(tutor.image));
                        chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                    }
                }
                if(isReceiverAvailable){
                    binding.availability.setText("Online");
                }else{
                    binding.availability.setText("Offline");
                }
            });
        }
        else{
            database.collection(Constants.KEY_COLLECTION_STUDENTS).document(
                    student.id
            ).addSnapshotListener(ChatActivity.this, (value, error) -> {
                if(error !=null){
                    return;
                }
                if(value != null){
                    if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                        int availability = Objects.requireNonNull(
                                value.getLong(Constants.KEY_AVAILABILITY)
                        ).intValue();
                        isReceiverAvailable = availability == 1;
                    }
                    student.token = value.getString(Constants.KEY_FCM_TOKEN);
                    if(student.image == null){
                        student.image = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setProfileImage(getBitmapFromEncodedString(student.image));
                        chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                    }
                }
                if(isReceiverAvailable){
                    binding.availability.setText("Online");
                }else{
                    binding.availability.setText("Offline");
                }
            });
        }
    }

    private void listenMessages() {
        if (preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)) {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, tutor.id)
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, tutor.id)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                    .addSnapshotListener(eventListener);
        }else{
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID))
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, student.id)
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, student.id)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_TUTOR_ID))
                    .addSnapshotListener(eventListener);
        }
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }

            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else{
            return null;
        }

    }

    private void loadTutorDetails(){
        tutor = (Tutor) getIntent().getSerializableExtra(Constants.KEY_TUTOR);
        //tutorId = getIntent().getStringExtra()
        binding.textName.setText(tutor.name);
        binding.imageProfile.setImageBitmap(getProfileImage(tutor.image));
    }

    private Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void loadStudentDetails(){
        student = (Student) getIntent().getSerializableExtra(Constants.KEY_STUDENT);
        binding.textName.setText(student.name);
        binding.imageProfile.setImageBitmap(getProfileImage(student.image));
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        //TODO schedule imageBack from Chat
        binding.layoutSend.setOnClickListener(view -> {
            if(!binding.inputMessage.getText().toString().trim().equals("")){
                sendMessage();
            }else {
                showToast(getResources().getString(R.string.write_your_message));
            }
        });
        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)){
            binding.imageProfile.setOnClickListener(v -> startActivity(
                    new Intent(getApplicationContext(), TutorProfileActivity.class)
                            .putExtra(Constants.KEY_TUTOR, tutor)));
//            binding.imageBack.setOnClickListener(v -> startActivity(
//                    new Intent(getApplicationContext(), StudentChatActivity.class)));
        }else{
            binding.imageProfile.setOnClickListener(v -> startActivity(
                    new Intent(getApplicationContext(), StudentProfileActivity.class)
                            .putExtra(Constants.KEY_STUDENT, student)));
//            binding.imageBack.setOnClickListener(v -> startActivity(
//                    new Intent(getApplicationContext(), TutorChatActivity.class)));
        }

    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkForConversion(){
        if(chatMessages.size() != 0){
            if (preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)) {
                checkForConversionRemotely(
                        preferenceManager.getString(Constants.KEY_STUDENT_ID), tutor.id
                );
                checkForConversionRemotely(
                        tutor.id, preferenceManager.getString(Constants.KEY_STUDENT_ID)
                );
            }else{
                checkForConversionRemotely(
                        preferenceManager.getString(Constants.KEY_TUTOR_ID), student.id
                );
                checkForConversionRemotely(
                        student.id, preferenceManager.getString(Constants.KEY_TUTOR_ID)
                );
            }
        }
    }

    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}