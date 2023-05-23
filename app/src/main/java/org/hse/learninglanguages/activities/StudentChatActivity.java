package org.hse.learninglanguages.activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Filter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.adapters.RecentConversationsAdapter;
import org.hse.learninglanguages.databinding.ActivityStudentChatBinding;
import org.hse.learninglanguages.listeners.ConversionListener;
import org.hse.learninglanguages.models.ChatMessage;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class StudentChatActivity extends BaseActivity implements ConversionListener {
    private ActivityStudentChatBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        //binding.startSearch.setInputType(InputType.TYPE_NULL);
        init();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this, preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN));
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        binding.startSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RecentConversationsAdapter conversationsAdapter = new RecentConversationsAdapter(conversations, StudentChatActivity.this, preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN));
                binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
                conversationsAdapter.getFilter().filter(s, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        if(count == 0){
                            binding.conversationsRecyclerView.setVisibility(View.GONE);
                            binding.textErrorMessage.setVisibility(View.VISIBLE);
                            binding.textErrorMessage.setText(getResources().getString(R.string.no_chats_available));
                        } else{
                            binding.textErrorMessage.setVisibility(View.GONE);
                            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //binding.startSearch.setOnClickListener(v -> binding.startSearch.setInputType(InputType.TYPE_CLASS_TEXT));
        binding.buttonSignOut.setOnClickListener(view -> signOut());
        binding.tutorSearch.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),TutorsSearchActivity.class)));
        binding.chooseNewTutor.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),TutorsSearchActivity.class)));
        binding.settings.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),StudentSettingsActivity.class)));
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_STUDENT_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.KEY_STUDENT_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i = 0; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            if(conversations.size() == 0){
                binding.textErrorMessage.setText(getResources().getString(R.string.no_chats_available));
                binding.textErrorMessage.setVisibility(View.VISIBLE);
                binding.chooseNewTutor.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }else{
                binding.progressBar.setVisibility(View.GONE);
                binding.chooseNewTutor.setVisibility(View.GONE);
                binding.textErrorMessage.setVisibility(View.GONE);
                binding.conversationsRecyclerView.smoothScrollToPosition(0);
                binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            }
        }

    };


    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private  void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_STUDENTS).document(
                        preferenceManager.getString(Constants.KEY_STUDENT_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
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
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionStudentClicked(Tutor tutor) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_TUTOR, tutor);
        startActivity(intent);
        //finish();
    }

    @Override
    public void onConversionTutorClicked(Student student) {

    }
}