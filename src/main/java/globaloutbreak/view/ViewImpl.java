package globaloutbreak.view;

import java.util.ArrayList;
import java.util.List;

import globaloutbreak.controller.api.Controller;
import globaloutbreak.model.api.Infodata;
import globaloutbreak.model.api.Message;
import globaloutbreak.model.api.Voyage;
import globaloutbreak.view.scenemanager.SceneManager;
import globaloutbreak.view.scenemanager.SceneManagerImpl;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import settings.WindowSettings;
import settings.WindowSettingsImpl;

public class ViewImpl implements View {

    private final WindowSettings settings = new WindowSettingsImpl();
    private final SceneManager manager;
    private List<Button> diseasesButtons = new ArrayList<Button>();
    private Controller controller;

    /**
     * 
     */
    public ViewImpl() {
        this.manager = new SceneManagerImpl(this);
    }

    /**
     * @param controller
     *                   the current controller.
     * @param stage
     *                   the current stage.
     */
    @Override
    public void start(final Controller controller, final Stage stage) {
        this.controller = controller;
        this.manager.getSceneLoader().loadFiles();
        this.manager.openInitialMenu(stage);
    }

    /**
     * 
     */
    @Override
    public void displayInfo(final Infodata info) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displayInfo'");
    }

    /**
     * 
     */
    @Override
    public void displayMessage(final Message message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displayMessage'");
    }

    /**
     * 
     */
    @Override
    public void displayVoyage(final Voyage voyage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displayVoyage'");
    }

    /**
     * 
     */
    @Override
    public Controller getController() {
        return this.controller;
    }

    /**
     * 
     */
    @Override
    public WindowSettings getWindowSettings() {
        return this.settings;
    }

    /**
     * @return
     *         the scene manager.
     */
    @Override
    public SceneManager getSceneManager() {
        return this.manager;
    }

    /**
     * @return
     *         a list with diseases buttons.
     */
    public List<Button> getDiseasesButtons() {
        return List.copyOf(diseasesButtons);
    }

    /**
     * @param diseasesNames
     *                      a list of diseases names.
     */
    public void setDiseasesNames(List<String> diseasesNames) {
        diseasesNames.stream().forEach(name -> diseasesButtons.add(new Button(name)));
    }

}
