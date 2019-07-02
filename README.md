# Mondtag
Free, open-source lunar calendar for Android written in Java.

### Goal
This software is intended to become a small, handy assistant to plan e.g. gardening work.

### Status
All basic functionality including gardening interpretations is implemented and should work.

### Requirements

#### Android Studio
See [developer.android.com](https://developer.android.com/studio/index.html)

#### gradle
A [gradle](https://gradle.org) build script is included to manage dependencies and automate testing 
and building. The plugin 
[gradle-android-git-version](https://github.com/gladed/gradle-android-git-version) is used to 
automate versioning based on git.

#### libZodiac4A
This app is based on [libZodiac4A](https://github.com/kahles/libZodiac4A), which is an android 
fork of [libZodiac](https://github.com/kahles/libZodiac), the library I created to calculate and 
manage calendar data.
libZodiac uses libnova/novaforjava/nova4jmt for astronomical calculations - see libZodiac project 
page for further details.

#### ThreeTenABP
[ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) is a JSR-310 backport for Android. It 
enables support of 
[java.time framework](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) 
introduced in Java 8.

### Further references
- Book: [Vom richtigen Zeitpunkt](http://www.paungger-poppe.com/index.php/de/publikationen/unsere-buecher/vom-richtigen-zeitpunkt) This book also is available in other languages, the English version is called "The Power of Timing".

### License
This project is licensed under the GNU General Public License v3. See [LICENSE](LICENSE) for 
details.

### Privacy Statement
This software is open source and doesn't contain advertisements. No internet connection is needed 
and no user data gets utilised or stored outside the user's device.
Sole exception is the functionality to search for geo coordinates: Therefor an Android-service is 
requested and the search term is submitted to Google.

### Warranty
Although I implemented this software to the best of my knowledge, I am not able to guarantee the 
completion, correctness and accuracy of the algorithms.

### Contact 
Feel free to contact me if you have wishes, proposals, bug reports or if you want to contribute.
