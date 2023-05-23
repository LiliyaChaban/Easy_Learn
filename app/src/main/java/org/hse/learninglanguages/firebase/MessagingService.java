package org.hse.learninglanguages.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hse.learninglanguages.R;
import org.hse.learninglanguages.activities.ChatActivity;
import org.hse.learninglanguages.models.Student;
import org.hse.learninglanguages.models.Tutor;
import org.hse.learninglanguages.utilities.Constants;
import org.hse.learninglanguages.utilities.PreferenceManager;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String channelId = "chat_message";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        int notificationId = new Random().nextInt();
        PendingIntent pendingIntent = null;

        if(preferenceManager.getBoolean(Constants.KEY_STUDENT_LOGIN)){
            Tutor tutor = new Tutor();
            tutor.id = remoteMessage.getData().get(Constants.KEY_TUTOR_ID);
            tutor.name = remoteMessage.getData().get(Constants.KEY_NAME);
            tutor.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);


            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.KEY_TUTOR, tutor);
             pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


            builder.setSmallIcon(R.drawable.ic_notification);
            builder.setContentTitle(tutor.name);
        }else{
            Student student = new Student();
            student.id = remoteMessage.getData().get(Constants.KEY_STUDENT_ID);
            student.name = remoteMessage.getData().get(Constants.KEY_NAME);
            student.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);


            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.KEY_STUDENT, student);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


            builder.setSmallIcon(R.drawable.ic_notification);
            builder.setContentTitle(student.name);
        }
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
