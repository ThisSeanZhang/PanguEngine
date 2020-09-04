package engine.enginemod.client.gui.game;

import engine.Platform;
import engine.client.EngineClientImpl;
import engine.game.GameDataStorage;
import engine.gui.control.Button;
import engine.gui.control.Text;
import engine.gui.control.TextField;
import engine.gui.layout.FlowPane;
import engine.gui.layout.HBox;
import engine.gui.layout.VBox;
import engine.gui.misc.Background;
import engine.gui.misc.HPos;
import engine.gui.misc.Pos;
import engine.util.Color;

public class GuiCreateWorld extends FlowPane {
    public GuiCreateWorld(GameDataStorage storage) {
        setBackground(new Background(Color.fromRGB(0x666666)));
        alignment().set(Pos.CENTER);
        var vmain = new VBox();
        vmain.alignment().set(HPos.CENTER);
        getChildren().add(vmain);
        var label1 = new Text("Create New Game");

        var nameGrp = new VBox();
        var lblName = new Text("Name");
        var txtboxName = new TextField();
        txtboxName.setPrefSize(300, 23);
        var lblErrMsg = new Text("[PH]");
        lblErrMsg.setVisible(false);
        lblErrMsg.color().set(Color.RED);
        nameGrp.getChildren().addAll(lblName, txtboxName, lblErrMsg);
        var hbox = new HBox();
        hbox.spacing().set(10f);
        var butCreate = new Button("Create");
        butCreate.setDisabled(true);
        butCreate.setOnMouseClicked(e -> {
            storage.createGameData(txtboxName.text().get());
            var engine = Platform.getEngineClient();
            ((EngineClientImpl) engine).playIntegratedGame(txtboxName.text().get());
        });
        txtboxName.text().addChangeListener((observable, oldValue, newValue) -> {
            lblErrMsg.setVisible(false);
            lblErrMsg.setText("[PH]");
            if (newValue != null && storage.isAcceptableNewGameName(newValue)) {
                butCreate.setDisabled(false);
            } else {
                butCreate.setDisabled(true);
                lblErrMsg.setVisible(true);
                if (newValue != null) {
                    if (GameDataStorage.isValidFilename(newValue)) {
                        lblErrMsg.setText("Game with the same name has already exist");
                    } else {
                        lblErrMsg.setText("Invalid game name");
                    }
                }
            }
        });
        var butBack = new Button("Back");
        butBack.setOnMouseClicked(e -> {
            var guiManager = Platform.getEngineClient().getGraphicsManager().getGUIManager();
            guiManager.showLast();
        });
        hbox.getChildren().addAll(butCreate, butBack);
        vmain.getChildren().addAll(label1, nameGrp, hbox);
    }
}
