package org.hse.learninglanguages.listeners;

import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;

public interface ConversionListener {
    void onConversionStudentClicked(Tutor tutor);
    void onConversionTutorClicked(Student student);
}
