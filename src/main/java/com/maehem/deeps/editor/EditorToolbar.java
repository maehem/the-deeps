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

import java.io.File;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *  See this video:  https://www.youtube.com/watch?v=z8yd-duChIk
 * 
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorToolbar extends ToolBar implements EditorProjectListener {

    private final EditorProject project;
    
    private final Button newFileButton; // = new Button("New");
    private final Button openButton; // = new Button("Open");
    private final Button saveButton;

    private final Button midButton = new Button("XXX");
    private final Button infoButton = new Button("?");
    
    private final Pane leftSpacer = new Pane();
    private final Pane rightSpacer = new Pane();
    
    public EditorToolbar(Stage stage) {
        newFileButton = createButton("New File", "/icons/plus-circle.png");
        openButton = createButton("Open", "/icons/folder-open.png");
        saveButton = createButton("Save", "/icons/floppy-disk.png");
        
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );

        HBox.setHgrow(
                rightSpacer,
                Priority.SOMETIMES
        );
        
        getItems().addAll(
                newFileButton,
                openButton,
                saveButton,
                createSeparator(),
                leftSpacer,
                midButton,
                rightSpacer,
                infoButton
        );
        
        this.project = EditorProject.getInstance();
        this.project.addListener(this);
        updateState();
        
        newFileButton.setOnAction((t) -> {
            project.clear();
        });
        
        openButton.setOnAction((t) -> {
            
            // TODO:  Open list of previous open project paths and allow
            //      selecting one or a new path as below:
            
            DirectoryChooser dc =  new DirectoryChooser();
            dc.setTitle("Choose Existing Project Directory");
            dc.setInitialDirectory(project.getProjectsDir());
            File selectedDir = dc.showDialog(stage);
            
            if ( selectedDir != null ) {
                File projectSettings = new File(selectedDir, EditorProject.PROJECTS_FILE );
                if ( projectSettings.exists() ) {
                    // load the project
                    project.clear();
                    project.loadProject(selectedDir);
                } else {
                    // Alert that folder is not a valid project.
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Selected Folder is not a project directory!");
                    alert.setTitle("Not a Project Folder");
                    alert.show();
                }
            } // else nothing happens.
        });
        
        saveButton.setOnAction((t) -> {
            if ( project.getFilePath().equals("") ) {
                EditorDialogs.projectSettingsDialog(project, stage);
            } else {
                project.doSave();
            }
        });
    }
    
    private void updateState() {
        //saveButton.setDisable(!project.getFilePath().equals("") && !project.isEdited());
        saveButton.setDisable(!project.isEdited());
    }
    
    private Separator createSeparator() {
        Separator s = new Separator();
        s.setMinWidth(20);

        return s;        
    }
    
    private Button createButton( String name, String path ) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(32);
        view.setPreserveRatio(true);
        
        Button b = new Button();
        b.setGraphic(view);
        b.setTooltip(new Tooltip(name));
        
        return b;
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        updateState();
    }
}
