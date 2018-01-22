# Notes_App
Note app for android

Android app that allows for taking notes and adding a reminder to be notified in the future.
Notes can be marked as favorite, archived, unarchived, and sent to the trash bin.
Notes in the trash will automatically be deleted in X days based on user selection.

The database was created using Room. Queries are accesed off the main thread using RxJava2

Dagger2 is used for some dependency injection and for facilitating unit tests.

Wrote and made layout for Reminder dialog which allows for the selection of the time and date.
Also created a frequency dialog which allows for different types of reminders to be selected

For example
1. Repeats daily
2. Repeats weekly
3. Repeats monthly
4. Repeats yearly
5. And more complex ones like -> repeats every Monday, Wednesday, and Saturday every two weeks for 2 times
