package org.hse.learninglanguages.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.hse.learninglanguages.databinding.ItemContainerRecievedMessageBinding;
import org.hse.learninglanguages.databinding.ItemContainerSentMessageBinding;
import org.hse.learninglanguages.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private Bitmap profileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setProfileImage(Bitmap bitmap){
        profileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap profileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.profileImage = profileImage;
        this.senderId = senderId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                    ItemContainerRecievedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super((itemContainerSentMessageBinding.getRoot()));
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerRecievedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding){
            super((itemContainerRecievedMessageBinding.getRoot()));
            binding = itemContainerRecievedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap profileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(profileImage != null){
                binding.imageProfile.setImageBitmap(profileImage);
            }
        }
    }
}
