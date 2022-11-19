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

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
    
    
    //private final Button stampButton; // = new Button("Stamp");
    
    //private final Button zoomOut; //= new Button("-");
    //private final Button zoomIn; //= new Button("+");
    //private final Button zoomReset; // = new Button("1:1");

    private final Button midButton = new Button("XXX");
    private final Button infoButton = new Button("?");
    
    private final Pane leftSpacer = new Pane();
    private final Pane rightSpacer = new Pane();
    
    public EditorToolbar(Stage stage) {
        newFileButton = createButton("New File", "/icons/plus-circle.png");
        openButton = createButton("Open", "/icons/folder-open.png");
        saveButton = createButton("Save", "/icons/floppy-disk.png");
        //stampButton = createButton("Stamp", "/icons/paw-print.png");
        
        //zoomOut   = createButton("Zoom Out", "/icons/zoom-out.png");
        //zoomIn    = createButton("Zoom Out", "/icons/zoom-in.png");
        //zoomReset = createButton("Zoom Reset", "/icons/magnifier.png");
        
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );

        HBox.setHgrow(
                rightSpacer,
                Priority.SOMETIMES
        );
        
        getItems().addAll(newFileButton,
                openButton,
                saveButton,
                createSeparator(),
                //stampButton,
                //createSeparator(),
                //zoomOut,
                //zoomIn,
                //zoomReset ,       
                leftSpacer,
                midButton,
                rightSpacer,
                infoButton
        );
        
        this.project = EditorProject.getInstance();
        this.project.addListener(this);
        updateState();
        
        saveButton.setOnAction((t) -> {
            if ( project.getFilePath().equals("") ) {
                EditorDialogs.projectSettingsDialog(project, stage);
            } else {
                project.doSave();
            }
        });
    }
    
    private void updateState() {
        saveButton.setDisable(!project.getFilePath().equals("") && !project.isEdited());
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
