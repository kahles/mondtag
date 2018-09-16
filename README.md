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
A [gradle](https://gradle.org) build script is included to manage dependencies and automate testing and building. The plugin [gradle-android-git-version](https://github.com/gladed/gradle-android-git-version) is used to automate versioning based on git and also works well in a non-Android java project.

#### libZodiac
This app is based on [libZodiac](https://github.com/kahles/libZodiac), which is the library I created to calculate and manage calendar data.
libZodiac uses libnova/novaforjava for astronomical calculations - see libZodiac project page for further details.

#### ThreeTenABP
[ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) is a JSR-310 backport for Android. It enables support of [java.time framework](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) introduced in Java 8.

### Further references
- Book: [Vom richtigen Zeitpunkt](http://www.paungger-poppe.com/index.php/de/publikationen/unsere-buecher/vom-richtigen-zeitpunkt) This book also is available in other languages, the English version is called "The Power of Timing".

### License
This project is licensed under the GNU General Public License v3. See [LICENSE](LICENSE) for details.

### Contact
Feel free to contact me if you have wishes, proposals or if want to contribute.
