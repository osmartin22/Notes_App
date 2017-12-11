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
public class ReminderReceiver extends BroadcastReceiver {

    private Context mContext;
    private String title, content;
    private int notificationId;
    private PendingResult mPendingResult;
    private AppDatabase db;


    @Override
    public void onReceive(@NonNull Context context, Intent intent) {

        mContext = context;
        notificationId = intent.getIntExtra(context.getString(R.string.notificationId), 0);
        title = intent.getStringExtra(context.getString(R.string.notificationTitle));
        content = intent.getStringExtra(context.getString(R.string.notificationContent));

        mPendingResult = goAsync();
        if (AppDatabase.getAppDatabase() == null) {
            AppDatabase.setUpAppDatabase(context);
        }
        db = AppDatabase.getAppDatabase();

        NotificationManager nManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nManager != null) {
            nManager.notify(notificationId, NotificationHelper.buildNotification(context, title,
                    content));
        }

        getReminder(notificationId);
    }

    private void getReminder(int reminderId) {
        Single.fromCallable(() -> db.remindersDao().getReminder(reminderId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processNextReminder);
    }

    private void processNextReminder(@NonNull Reminder reminder) {

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

                ReminderManager.createReminder(mContext, notificationId, title, content, nextReminderTime);
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
