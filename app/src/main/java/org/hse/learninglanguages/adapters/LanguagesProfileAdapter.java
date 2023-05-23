package org.hse.learninglanguages.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hse.learninglanguages.databinding.ItemContainerLanguageBinding;
import org.hse.learninglanguages.databinding.ItemContainerProfileLanguagesBinding;
import org.hse.learninglanguages.listeners.LanguageListener;
import org.hse.learninglanguages.models.Language;

import java.util.List;

public class LanguagesProfileAdapter extends RecyclerView.Adapter<LanguagesProfileAdapter.LanguagesProfileViewHolder>{
    private List<Language> languages;
    public LanguagesProfileAdapter(List<Language> languages) {
        this.languages = languages;
    }
    @NonNull
    @Override
    public LanguagesProfileAdapter.LanguagesProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerProfileLanguagesBinding itemContainerLanguageBinding= ItemContainerProfileLanguagesBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LanguagesProfileAdapter.LanguagesProfileViewHolder(itemContainerLanguageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguagesProfileAdapter.LanguagesProfileViewHolder holder, int position) {
        holder.setLanguageData(languages.get(position));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class LanguagesProfileViewHolder extends RecyclerView.ViewHolder{
        ItemContainerProfileLanguagesBinding binding;
        LanguagesProfileViewHolder(ItemContainerProfileLanguagesBinding itemContainerLanguageBinding){
            super(itemContainerLanguageBinding.getRoot());
            binding = itemContainerLanguageBinding;
        }
        void setLanguageData(Language language){
            binding.textLanguage.setText(language.language);
            binding.textLevel.setText(language.level);
        }
    }
}
