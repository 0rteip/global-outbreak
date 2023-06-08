package globaloutbreak.view.scenemanager;

import globaloutbreak.model.message.Message;
import globaloutbreak.view.View;
import globaloutbreak.view.sceneloader.SceneLoader;
import globaloutbreak.view.sceneloader.SceneLoaderImpl;
import javafx.stage.Stage;
import view.utilities.SceneStyle;

/**
 * Class that manage the scene changing.
 */
public final class SceneManagerImpl implements SceneManager {

    private final SceneLoader sceneLoader;
    private final Stage stage;

    /**
     * Constructor that load menu scenes.
     * 
     * @param stage
     * @param view
     * 
     */
    public SceneManagerImpl(final Stage stage, final View view) {
        this.stage = stage;
        this.sceneLoader = new SceneLoaderImpl(view);
    }

    @Override
    public void openInitialMenu() {
        this.openScene(SceneStyle.INITIALMENU);
    }

    @Override
    public void openTutorial() {
        this.openScene(SceneStyle.TUTORIAL);
    }

    @Override
    public void openDiseaseChoice() {
        this.openScene(SceneStyle.CHOOSEDISEASE);
    }

    @Override
    public void openDiseaseName() {
        this.openScene(SceneStyle.DISEASENAME);
    }

    @Override
    public void openBackScene() {
        this.sceneLoader.loadBackScene(this.stage);
    }

    private void openScene(final SceneStyle sceneStyle) {
        this.sceneLoader.loadScene(sceneStyle, this.stage);
    }

    @Override
    public SceneLoader getSceneLoader() {
        return this.sceneLoader;
    }

    @Override
    public void openSettings() {
        this.openScene(SceneStyle.SETTINGS);
    }

    @Override
    public void openMessage(Message message) {
        this.sceneLoader.openDialog(this.stage, message);
    }
}