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

import com.maehem.deeps.model.EntityTile;
import com.maehem.deeps.model.FixtureTile;
import com.maehem.deeps.model.MapTile;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorMainView {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    
    private static final Insets INSETS = new Insets(5);
    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final Scene scene = new Scene(root);
    

    public EditorMainView( Stage stage) throws IOException {
        this.stage = stage;
        
        buildUI();
        
        stage.show();
        EditorProject project = EditorProject.getInstance();
        project.setScene(scene);
        project.notifyProjectChanged(project,
                EditorProjectListener.ChangeType.LOADED
        );

    }
    
    private void buildUI() throws IOException {
        EditorToolbar toolbar = new EditorToolbar(stage);
        EditorSheetTabPane sheetView = new EditorSheetTabPane();
        EditorProjectNavigator projNavigator = new EditorProjectNavigator(stage);
        VBox leftBar = new VBox(sheetView, projNavigator);
        leftBar.setMaxWidth(WIDTH/4.0);
        EditorProjectStatusPane statusBar = new EditorProjectStatusPane();
        
        // Right Pane
        EditorTilePropertiesPane tileProperties = new EditorTilePropertiesPane(MapTile.class);
        EditorTilePropertiesPane itemProperties = new EditorTilePropertiesPane(FixtureTile.class);
        EditorTilePropertiesPane entityProperties = new EditorTilePropertiesPane(EntityTile.class);
        VBox rightBar = new VBox(tileProperties, itemProperties, entityProperties );
        rightBar.setMaxWidth(WIDTH/5.0);
        
        tileProperties.setFillWidth(true);
        tileProperties.setMinHeight(HEIGHT/5);
        //tileProperties.setMinWidth(WIDTH/5.0);
        itemProperties.setFillWidth(true);
        itemProperties.setMinHeight(HEIGHT/5);
        //itemProperties.setMinWidth(WIDTH/5.0);
        entityProperties.setFillWidth(true);
        entityProperties.setMinHeight(HEIGHT/5);
        //entityProperties.setMinWidth(WIDTH/5.0);
        
        Node zoneTabs = new EditorZoneTabPane();
        
        VBox.setVgrow(sheetView, Priority.SOMETIMES );

        root.setTop(toolbar);
        root.setRight(rightBar);
        root.setBottom(statusBar);
        root.setLeft(leftBar);
        root.setCenter(zoneTabs);
        
        stage.setTitle("Deep Editor");
        stage.setScene(scene);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        
    }
    
    private Button createButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setMaxHeight(Double.MAX_VALUE);
        b.setMinWidth(150);
        BorderPane.setMargin(b, INSETS);
        
        return b;
    }
}
