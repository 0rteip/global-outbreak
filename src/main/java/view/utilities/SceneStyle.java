package view.utilities;

/**
 * Enumeration of scenes.
 */
public enum SceneStyle {

    /**
     * General graphs scene.
     */
    GENERALGRAPHS("General Graphs", "layouts/generalGraphs.fxml"),
    /**
     * Initial menu scene.
     */
    INITIALMENU("Initial Menu", "layouts/MenuGui.fxml"),
    /**
     * Choose disease scene.
     */
    CHOOSEDISEASE("Choose Disease", "layouts/ChooseDisease.fxml"),
    /**
     * Choose disease name scene.
     */
    DISEASENAME("Choose disease name", "layouts/DiseaseNameGui.fxml"),
    /**
     * Tutorial scene.
     */
    TUTORIAL("Tutorial", "layouts/TutorialGui.fxml");
    

    private final String fxmlFile;
    private final String title;

    SceneStyle(final String title, final String fxmlFile) {
        this.title = title;
        this.fxmlFile = fxmlFile;
    }

    /**
     * @return the fxml file of the scene
     */
    public String getFxmlFile() {
        return this.fxmlFile;
    }

    /**
     * @return the fxml path of the scene
     */
    public String getTitle() {
        return this.title;
    }
}
