package com.ozmar.notes.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.utils.ReminderUtils;

import org.joda.time.DateTime;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


// TODO: Check if repeatEvents, or repeatToDate has not passed
// If not, calculate next reminder
// Check again if repeatToDate will be violated with the new reminder time

// TODO: Possible optimization is to not immediately set an alarm manager when a new reminder is created
// A reminder 2 months away does not need to be created.
// Can have a separate alarm that runs every week that sets up any reminders that
// will occur in that week
public class ReminderNotificationReceiver extends BroadcastReceiver {

    private String title, content;
    private PendingResult mPendingResult;

    @Inject
    AppDatabase db;


    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        int notificationId = intent.getIntExtra(context.getString(R.string.notificationId), 0);
        title = intent.getStringExtra(context.getString(R.string.notificationTitle));
        content = intent.getStringExtra(context.getString(R.string.notificationContent));

        mPendingResult = goAsync();

        NotificationManager nManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {
            nManager.notify(notificationId, NotificationHelper.buildNotification(context, title,
                    content));
        }

        getReminder(context, notificationId);
    }

    private void getReminder(Context context, int reminderId) {
        Single.fromCallable(() -> db.remindersDao().getReminder(reminderId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reminder -> processNextReminder(context, reminder));
    }

    private void processNextReminder(@NonNull Context context, @NonNull Reminder reminder) {

        if (reminder.getFrequencyChoices() != null) {

            int reminderId = reminder.getId();
            boolean onEventsSatisfied = false;
            boolean onDesiredEndDateSatisfied = false;
            FrequencyChoices choices = reminder.getFrequencyChoices();

            if (choices.getRepeatEvents() > 0) {
                onEventsSatisfied = updateEventsOccurred(choices, reminderId);

            } else if (choices.getRepeatToDate() > 0) {
                onDesiredEndDateSatisfied = choices.getRepeatToDate() < System.currentTimeMillis();
            }

            if (!(onEventsSatisfied || onDesiredEndDateSatisfied)) {
                long currentReminderTime = reminder.getDateTime().getMillis();
                long nextReminderTime = ReminderUtils.getNextRepeatReminder(choices, currentReminderTime);

                if (nextReminderTime > currentReminderTime) {
                    reminder.setDateTime(new DateTime(nextReminderTime));
                    Completable.fromAction(() -> db.remindersDao().updateReminderTime(reminderId,
                            nextReminderTime))
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }

                ReminderNotificationManager.createReminderAlarm(context, reminderId, title, content, nextReminderTime);
            }
        }

        mPendingResult.finish();
    }

    private boolean updateEventsOccurred(@NonNull FrequencyChoices choices, int reminderId) {
        int eventsOccurred = choices.getRepeatEventsOccurred() + 1;
        Completable.fromAction(() -> db.remindersDao().updateEventsOccurred(reminderId, eventsOccurred))
                .subscribeOn(Schedulers.io())
                .subscribe();

        return eventsOccurred == choices.getRepeatEvents();
    }
}
