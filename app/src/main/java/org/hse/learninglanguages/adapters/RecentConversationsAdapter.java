package org.hse.learninglanguages.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import org.hse.learninglanguages.databinding.ItemContainerRecentConversionBinding;
import org.hse.learninglanguages.listeners.ConversionListener;
import org.hse.learninglanguages.models.ChatMessage;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> implements Filterable {

    private List<ChatMessage> chatMessages;

    private final List<ChatMessage> allChats;
    private final ConversionListener conversionListener;
    private final Boolean isStudent;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener, Boolean isStudent) {
        this.allChats = new ArrayList<>(chatMessages);
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.isStudent = isStudent;
    }

    @Override
    public ConversionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public Integer onFilterComplete;

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ChatMessage> filteredList = new ArrayList<>();
            chatMessages = allChats;
            if (charSequence.toString().isEmpty()){
                filteredList.addAll(allChats);
            }else{
                for(ChatMessage chatMessage: allChats){
                    if(chatMessage.conversionName.toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(chatMessage);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            onFilterComplete = filteredList.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            chatMessages.clear();
            filterResults.count = onFilterComplete;
            chatMessages.addAll((Collection<? extends ChatMessage>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    class ConversionViewHolder extends RecyclerView.ViewHolder{

        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
                binding.getRoot().setOnClickListener(view -> {
                    if(!isStudent){
                    Student student = new Student();
                    student.id = chatMessage.conversionId;
                    student.name = chatMessage.conversionName;
                    student.image = chatMessage.conversionImage;
                    conversionListener.onConversionTutorClicked(student);
                    }else {
                            Tutor tutor = new Tutor();
                            tutor.id = chatMessage.conversionId;
                            tutor.name = chatMessage.conversionName;
                            tutor.image = chatMessage.conversionImage;
                            conversionListener.onConversionStudentClicked(tutor);
                    }
                });

        }
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
