﻿• Classes
There is a total of 6 classes implemented for this project.

MainActivity.kt
This class generates a welcome screen where two possible game modes might be selected by clicking appropriate buttons. Those clicks will open the app the Google Map activity which is handled by GameMode.kt class.

GameMode.kt
This class sets up the map interface and prints it to the screen. It also listens to a current location every 2 seconds and updates the view. On each update, the code scans for markers that are in range (max 25m far from player's location) and partly handles their collection.
It is also responsible for the generation of random lyrics and random placement of markers, containing those lyrics on a map.
Also, the app keeps track of connectivity. If either GPS or cellular is off the alert will pop up, so players might quickly fix the problem.
GameMode.kt controls both Classic and Current game modes and makes sure different songs are used in these two modes.

SqlAdapter.kt
This class consists of a large number of methods that are responsible for getting the content from the MySQL database or editing that content. There are methods for establishing the connection, getting current contents of a backpack in each game mode, checking the correctness of a guessing attempt, utilizing the giving up the algorithm , deleting guessed songs from a DB, addition of a newly collected lyrics to a backpack, getting and setting the points in each game mode. Also, it controls the calculation of points using the special formula, the ability to upgrade the backpack and last but not least updating the ListView when data is added or deleted. All main activities use SqlAdapter's methods because the database is an essential part of this application which handles most of the data required for the app to function properly.

Backpack.kt
This class instantiates the backpack activity, listens to button presses and performs some essential checks but the whole its functionality is fully controlled by the SqlAdapter class.

CustomAdapter.kt
This class was created to perform some tweaks to a standard ListView adapter which made it much more useful for this project. The idea of a header and an item was implemented. Basically, different songs are separated with thin black headers which makes the UI intuitive and easy to use.

LyricPart.kt
A tiny class that is used to compare two pieces of lyrics. A custom compareTo() is implemented which returns 0 if lyric parts are the same and 1 or -1 if they differ.

• Android Components Used
- Activities
The welcome screen, map view, and a backpack are 3 main activities used in this app.
- GPS, Cellular data
GPS is an essential part of this game because the interaction is based on the player's current location. Celluar is used to enhance the precision of a GPS.
-Alert Dialogs
Those appear when guessing the song, upgrading the backpack or facing connectivity issues.
-ListView
The idea of a backpack is implemented using a ListView with a custom adapter because lists are a clever and convenient way to store a large number of repetitive-looking data.

• Database
This application uses JDBC driver and MySQL connector to connect Kotlin with a relational database. It stores all the data on a server and thus cannot function when this server is not properly set up or hasn't been set up at all. Now, I will explain how to set up the application to work on an emulator.

• How to run the app? * IMPORTANT! *
0. Connect to a WiFi network and ensure the connection is stable
1. Install and run Icode-Go and Icode-Web Server programs from Play Store (They will create a localhost on the phone)
2. Launch Icode-Web Server and install all pre-requisities. After that hit the install button at the lower part of the screen
3. Launch I code-go, press on the hamburger menu and select the third tab on a horisontal menu.
4. Press start and wait till Apache and MySQL servers will turn on
5. Navigate down to you will see an Sql Buddy button. Press on it
6. If Username/Password will be asked, username: root, password = null
7. Navigate to query button, press it and paste in the contents of guess_lyrics.sql found in Additional Resources folder
8. Click submit

After that install the apk file and launch the app. It should hopefully work. 
After the location permisiion is given, reload the app and it will work as expected

In case the above steps didn't help to install the app:
1. Make sure the MySQL-connector-java-8.0.13.jar is visible and used by Android studio. It gives access to a library that utilizes the communication between the app and a database. You will find the .jar file in an Additional Resources folder in the submission archive. The instructions on how to add a .jar might be found here: https://www.youtube.com/watch?v=M5CHOynHm30
1.5 Add the following line to the build.gradle:

implementation files('libs/mysql-connector-java-3.0.17-ga-bin.jar')

2. Use XAMPP to start an Apache localhost server and MySQL localhost server
3. Import guess_lyrics.sql file using phpMyAdmin and copy-paste the contents of a file to the SQL section. guess_lyrics.sql may be found again in Additional Resources folder with this submission 
4. Make sure to run the app with the emulator on the same machine where the localhost is created because this is how the connection is set up in this application.
5. If the above steps were successfully implemented the app should work. The proofs of a working code might be seen in a video demo. 

*NOTE!* This application will not work by simply installing the .apk file. It needs working localhost on 10.0.2.2:3306 or 127.0.0.1:3306
In case something goes wrong, I provided the whole Android Studio Project zip in an Additional Resources folder. Try building the app from there with running XAMPP
