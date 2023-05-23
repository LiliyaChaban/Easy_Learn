package org.hse.learninglanguages.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_STUDENTS = "students";
    public static final String KEY_COLLECTION_TUTORS = "tutors";
    public static final String KEY_STUDENT_LOGIN = "isStudentLogin";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_STUDENT_ID = "studentId";
    public static final String KEY_TUTOR_ID = "tutorId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    public static final String KEY_ABOUT_YOURSELF = "aboutYourself";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_PURPOSE_OF_STUDY = "purposeOfStudy";
    public static final String KEY_WORK_EXPERIENCE = "workExperience";
    public static final String KEY_EDUCATION = "education";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_TUTOR = "tutor";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_STUDENT = "student";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAg2tB4Hc:APA91bH2fCx_qk9XuKGprXKWY00sfLGEB9wFsp7IPf5U_Ki8XMUdAmpVQHO3EheT8zmalIXRslSYSrafI8-ZJNsw9ir9pmqdISpb1B6Y5IZF_hh4tUaPJ4vqw0zE5K5uGTxkZWuITsYs"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }



    public static final String KEY_COLLECTION_LANGUAGES = "languages";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_USER_ID = "userId";
}
