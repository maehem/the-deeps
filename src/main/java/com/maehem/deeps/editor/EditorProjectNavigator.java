/*
    Licensed to the Apache Software Foundation (ASF) under one or more 
    contributor license agreements.  See the NOTICE file distributed with this
    work for additional information regarding copyright ownership.  The ASF 
    licenses this file to you under the Apache License, Version 2.0 
    (the "License"); you may not use this file except in compliance with the 
    License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the 
    License for the specific language governing permissions and limitations 
    under the License.
 */
package com.maehem.deeps.editor;

import static com.maehem.deeps.Deeps.log;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Zone;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorProjectNavigator extends VBox implements EditorProjectListener {

    private final Button projectSettingsButton;
    private final HBox controls = new HBox();
    private final EditorProject project = EditorProject.getInstance();
    private final TitledPane zonesPane;
    private final VBox zonesListNode = new VBox();
    private final TitledPane sheetsPane;
    private final HBox messagePane = new HBox();
    private final VBox sheetsListNode = new VBox();
    private final Label projectLabel;

    public EditorProjectNavigator(Stage stage) {
        projectSettingsButton = createButton("Project Settings", "/icons/cogwheel.png");
        projectSettingsButton.setOnAction((t) -> {
            EditorDialogs.projectSettingsDialog(project, stage);
        });
        Pane spacerPane = new Pane();
        HBox.setHgrow(spacerPane, Priority.SOMETIMES);

        projectLabel = new Label("Project Name Label");
        projectLabel.setPadding(new Insets(4, 0, 0, 4));

        controls.setPadding(new Insets(4));
        controls.setBackground(new Background(new BackgroundFill(
                new Color(0, 0, 0, 0.2),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        controls.getChildren().addAll(projectLabel, spacerPane, projectSettingsButton);

        // Zones Toolbar
        Button newZoneButton = createSizedButton("New Zone", -4);
        newZoneButton.setOnAction((t) -> {
            // Create a new Zone
            actionCreateNewZone();
            updateTree();
            //collapseZones();
        });

        ArrayList<Node> zoneBtnList = new ArrayList<>();
        zoneBtnList.add(newZoneButton);

        // Sheets Toolbar
        Button newZSheetButton = createSizedButton("New Sheet", -4);
        newZSheetButton.setOnAction((t) -> {
            // Create a new Zone
            actionCreateNewSheet(stage);
            updateTree();
        });

        ArrayList<Node> sheetBtnList = new ArrayList<>();
        sheetBtnList.add(newZSheetButton);

        zonesPane = createFancyTitlePane("Zones", zoneBtnList);
        zonesPane.setContent(zonesListNode);
        zonesListNode.setPadding(new Insets(0, 4, 0, 16));

        sheetsPane = createFancyTitlePane("Sheets", sheetBtnList);
        sheetsPane.setContent(sheetsListNode);
        sheetsListNode.setPadding(new Insets(0, 4, 0, 16));

        VBox spContent = new VBox(messagePane, sheetsPane, zonesPane);
        ScrollPane sp = new ScrollPane(spContent);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        getChildren().addAll(controls, sp);
        setMinHeight(340);

        updateTree();

        project.addListener(this);
    }

    private void updateTree() {
        projectLabel.setText("Project:  " + project.getName());

        messagePane.getChildren().clear();
        zonesListNode.getChildren().clear();
        sheetsListNode.getChildren().clear();

        if (project.getFilePath().equals("")) {
            Label msg = new Label("Project must be saved before creating Zones or Sheets.");
            msg.setFont(new Font(msg.getFont().getSize() - 2));
            messagePane.getChildren().add(msg);

            zonesPane.setDisable(true);
            sheetsPane.setDisable(true);
        } else {
            zonesPane.setDisable(false);
            sheetsPane.setDisable(false);
        }

        log.log(Level.CONFIG, "EditorProjectNavigator: Do sheets update... ");
        for (SheetModel sheet : project.getSheets()) {
            sheetsListNode.getChildren().add(createSheetItem(sheet));
            log.log(Level.FINE,
                    "EditorProjectNavigator: Added sheet pane: {0}",
                    sheet.getName()
            );
        }

        log.log(Level.CONFIG, "EditorProjectNavigator: Do zones update... ");
        for (Zone zone : project.getZones()) {
            zonesListNode.getChildren().add(createZoneItem(zone));
            log.log(Level.FINE,
                    "EditorProjectNavigator: Added zone pane: {0}",
                    zone.getName()
            );
        }
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        if (    type == ChangeType.LOADED ||
                type == ChangeType.CLEARED ||
                type == ChangeType.SHEET_NAME || 
                type == ChangeType.ZONE_NAME
            ) {
            updateTree();
        }
    }

    private Button createSizedButton(String label, double sizeDiff) {
        Button btn = new Button(label);
        btn.setFont(new Font(btn.getFont().getSize() + sizeDiff));

        return btn;
    }

    private Node createSheetItem(SheetModel sm) {
        // Name label
        Label nameLabel = new Label(sm.getName());
        // spacer
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        Button propsButton = createButton("Sheet Settings", "/icons/cogwheel.png");
        propsButton.setOnAction((t) -> {
            // Sheet settings dialog
            EditorDialogs.sheetSettingsDialog(project, sm);
        });
        HBox itemArea = new HBox(nameLabel, spacer, propsButton);
        nameLabel.setTooltip(new Tooltip(sm.getWidth() + "x" + sm.getHeight() + " tiles.\ntile size: " + sm.getSize()));
        itemArea.setPadding(new Insets(0, 0, 0, 20));
        return itemArea;
    }

    private Node createZoneItem(Zone zm) {
        // Name label
        Label nameLabel = new Label(zm.getName());
        // spacer
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        Button propsButton = createButton("Zone Settings", "/icons/cogwheel.png");
        propsButton.setOnAction((t) -> {
            EditorDialogs.zoneSettingsDialog(project, zm);
        });
        HBox itemArea = new HBox(nameLabel, spacer, propsButton);
        itemArea.setPadding(new Insets(0, 0, 0, 20));
        return itemArea;
    }

    private void actionCreateNewZone() {
        // Ask Project to prompt for new zone.
        EditorDialogs.newZoneDialog(project);
    }

    private void actionCreateNewSheet(Stage stage) {
        // Ask Project to prompt for new zone.
        EditorDialogs.newSheetDialog(project, stage);
    }

    private TitledPane createFancyTitlePane(String label, ArrayList<Node> nodes) {
        // Create the TitlePane
        TitledPane titledPane = new TitledPane();
        titledPane.setAlignment(Pos.CENTER);
        titledPane.animatedProperty().set(false);

        // Create HBox to hold our Title and button
        HBox contentPane = new HBox();
        contentPane.setAlignment(Pos.CENTER);

        // Set padding on the left side to avoid overlapping the TitlePane's 
        // expand arrow.  We will also pad the right side
        contentPane.setPadding(new Insets(0, 10, 0, 35));

        // Now, since the TitlePane's graphic node generally has a fixed size,
        // we need to bind our content pane's width to match the width of the 
        // TitledPane. This will account for resizing as well
        contentPane.minWidthProperty().bind(titledPane.widthProperty());

        // Create a Region to act as a separator for the title and button
        HBox region = new HBox();
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);

        // Add our nodes to the contentPane
        contentPane.getChildren().addAll(
                new Label(label),
                region
        );
        contentPane.getChildren().addAll(nodes);

        // Add the contentPane as the graphic for the TitledPane
        titledPane.setGraphic(contentPane);

        return titledPane;
    }

//    private void collapseZones() {
//        for (Node n : zonesListNode.getChildren()) {
//            if (n instanceof TitledPane) {
//                ((TitledPane) (n)).setExpanded(false);
//            }
//        }
//        //accordion.setExpandedPane(null);
//
//    }
//    
    private Button createButton(String name, String path) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(16);
        view.setPreserveRatio(true);
        //Setting the size of the button
        //button.setPrefSize(80, 80);
        //Setting a graphic to the button

        Button b = new Button();
        b.setPadding(new Insets(2));
        b.setGraphic(view);
        b.setTooltip(new Tooltip(name));

        return b;
    }
}
