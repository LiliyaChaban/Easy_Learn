package org.hse.learninglanguages.listeners;


import org.hse.learninglanguages.models.Language;

public interface LanguageListener {
    void onDeleteClicked(Language language);
    void onLevelClicked(Language language);
}
