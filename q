[33mcommit 64a199102112301d0bbc24af8bd6cad93d625872[m[33m ([m[1;36mHEAD -> [m[1;32mmaster[m[33m)[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 15:35:01 2017 -0700

    Updated UI a little bit. Pressing Save in note editor will now use an intent to go to the mainactivity a

[33mcommit b87ae1761e35672ee342c5d1203796e8fef9b279[m[33m ([m[1;31morigin/master[m[33m)[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 13:50:33 2017 -0700

    ListView now updates when adding or deleting notes

[33mcommit dd11b7eed28349d5d3184b787b1a3a4e5c9c1496[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 13:37:30 2017 -0700

    Fixed bugs when pressing save or deleting notes. Does not seem to crash on multiple add/deletes.

[33mcommit f84d660d66990a86b03229482286fab4fb25fb1a[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 12:36:53 2017 -0700

    Pressing the save button now only saves a new note just once. Before, creating a new note and pressing the save button created a new note for each press.

[33mcommit 57dcacf33098bcc2887cf328051886181763c2ee[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 00:25:57 2017 -0700

    Cleaned up code and added comments. Renamed addNote() in MainActivity to launchNoteEditor() to better describe what its purpose is

[33mcommit 00461cb2774cb8fe75c90077e0eab4fb5a23320a[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Sat Sep 23 00:14:14 2017 -0700

    Notes can be added/deleted from db but listview not updated unless app is restarted. Moved around UI.

[33mcommit d7afb90cd4ba11eda342a82b301606ee75dd1a0d[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Fri Sep 22 20:32:26 2017 -0700

    Created custom view for list view and an adapter to display it. onClick() for adapter not created yet, but the adpater displays correctly

[33mcommit 2b071864f1797c0121fa8d6c6981c3ff09c159d6[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Fri Sep 22 15:05:46 2017 -0700

    Database is working but not implemented to app yet. Only tested by manually inserting notes and using log() to display them

[33mcommit 3d9a872b0cd99271193dc02b68ef66271b4460e6[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Fri Sep 22 00:15:04 2017 -0700

    Started creating helper for database to store user notes

[33mcommit 127f7ddeda5d06a31c08ea5fde8d38f8116ea78d[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Thu Sep 21 21:22:14 2017 -0700

    Added floaterActionButtn(Plus) for adding notes instead of the menu

[33mcommit 7b6998808a574a692b609b1870599bf1ec29ce1b[m
Author: osmartin22 <oemartinez@ucdavis.edu>
Date:   Thu Sep 21 18:41:15 2017 -0700

    First Commit. Notes App does not use permanent storage yet. Can add or delete notes
