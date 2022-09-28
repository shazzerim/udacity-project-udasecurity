package com.udacity.catpoint.security.application;

import java.util.prefs.BackingStoreException;

/**
 * This is the main class that launches the application.
 */
public class CatpointApp {
    public static void main(String[] args) throws BackingStoreException {
        CatpointGui gui = new CatpointGui();
        gui.setVisible(true);
    }
}
