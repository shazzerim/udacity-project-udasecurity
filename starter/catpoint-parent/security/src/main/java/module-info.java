module security {
    requires miglayout;
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires image;

    opens com.udacity.catpoint.security.data to com.google.gson;

}