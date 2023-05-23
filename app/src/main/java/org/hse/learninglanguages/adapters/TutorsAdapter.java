package org.hse.learninglanguages.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hse.learninglanguages.R;
import org.hse.learninglanguages.databinding.ItemContainerTutorBinding;
import org.hse.learninglanguages.listeners.TutorListener;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public class TutorsAdapter extends RecyclerView.Adapter<TutorsAdapter.TutorViewHolder> implements Filterable {

    private List<Tutor> tutors;
    private final List<Tutor> allTutors;
    private final TutorListener tutorListener;

    public TutorsAdapter(List<Tutor> tutors, TutorListener tutorListener) {
        this.allTutors = tutors;
        this.tutors = new ArrayList<>(allTutors);
        this.tutorListener = tutorListener;
    }

    @Nonnull
    @Override
    public TutorViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        ItemContainerTutorBinding itemContainerTutorBinding = ItemContainerTutorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TutorViewHolder(itemContainerTutorBinding);
    }

    @Override
    public void onBindViewHolder(@Nonnull TutorViewHolder holder, int position) {
        holder.setTutorData(tutors.get(position));
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public Integer onFilterComplete;

    @Override
    public Filter getFilter() {
        return filter;
    }
        Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Tutor> filteredList = new ArrayList<>();

            //tutors = allTutors;
            if (charSequence.toString().isEmpty()){
                filteredList.addAll(allTutors);
            }else{
                for(Tutor tutor: allTutors){
                    if(tutor.name.toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(tutor);
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
                tutors.clear();
                filterResults.count = onFilterComplete;
                tutors.addAll((Collection<? extends Tutor>) filterResults.values);
                notifyDataSetChanged();
        }
    };

    class TutorViewHolder extends RecyclerView.ViewHolder{
        ItemContainerTutorBinding binding;
        TutorViewHolder(ItemContainerTutorBinding itemContainerTutorBinding){
            super(itemContainerTutorBinding.getRoot());
            binding = itemContainerTutorBinding;
        }
        void setTutorData(Tutor tutor){
            binding.textName.setText(tutor.name);
            binding.textCountry.setText(tutor.country);
            binding.textAboutYorself.setText(tutor.aboutYourself);
            binding.imageProfile.setImageBitmap(getTutorImage(tutor.image));
            binding.getRoot().setOnClickListener(view -> tutorListener.onTutorClicked(tutor));
        }
    }

    private Bitmap getTutorImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
