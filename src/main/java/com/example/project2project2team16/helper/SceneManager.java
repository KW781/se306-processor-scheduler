package com.example.project2project2team16.helper;

import javafx.scene.Parent;

import java.util.HashMap;

public class SceneManager {
    public enum AppScene {
        MAIN_VISUALISATION
    }

    private static HashMap<AppScene, Parent> sceneMap = new HashMap<>();

    /**
     * This method adds a specified scene to the scene map
     *
     * @param appScene the enum of the specificed scene
     * @param sceneRoot the Parent specified scene
     */
    public static void addUi(AppScene appScene, Parent sceneRoot) {
        sceneMap.put(appScene, sceneRoot);
    }

    /**
     * This method returns the Parent of the specified scene
     *
     * @param appScene the enum of the specified scene
     * @return the Parent specified scene
     */
    public static Parent getUiRoot(AppScene appScene) {
        return sceneMap.get(appScene);
    }
}
