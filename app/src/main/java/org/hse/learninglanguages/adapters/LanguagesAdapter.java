package org.hse.learninglanguages.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hse.learninglanguages.databinding.ItemContainerLanguageBinding;
import org.hse.learninglanguages.databinding.ItemContainerTutorBinding;
import org.hse.learninglanguages.listeners.LanguageListener;
import org.hse.learninglanguages.listeners.TutorListener;
import org.hse.learninglanguages.models.Language;
import org.hse.learninglanguages.models.Tutor;

import java.util.ArrayList;
import java.util.List;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.LanguagesViewHolder>{

    private List<Language> languages;
    private final LanguageListener languageListener;
    public LanguagesAdapter(List<Language> languages, LanguageListener languageListener) {
        this.languages = languages;
        this.languageListener = languageListener;
    }
    @NonNull
    @Override
    public LanguagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerLanguageBinding itemContainerLanguageBinding= ItemContainerLanguageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LanguagesViewHolder(itemContainerLanguageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguagesAdapter.LanguagesViewHolder holder, int position) {
        holder.setLanguageData(languages.get(position));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class LanguagesViewHolder extends RecyclerView.ViewHolder{
        ItemContainerLanguageBinding binding;
        LanguagesViewHolder(ItemContainerLanguageBinding itemContainerLanguageBinding){
            super(itemContainerLanguageBinding.getRoot());
            binding = itemContainerLanguageBinding;
        }
        void setLanguageData(Language language){
            binding.editLanguage.setText(language.language);
            binding.editLevel.setText(language.level);
            binding.delete.setOnClickListener(view -> languageListener.onDeleteClicked(language));
            binding.editLevel.setOnClickListener(view -> languageListener.onLevelClicked(language));
        }
    }
}
