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

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorProjectStatusPane extends HBox implements EditorProjectListener {

    private final EditorProject project;

    Label leftLabel = new Label("Left");
    Label midLabel = new Label("Middle");
    Label rightLabel = new Label("Right");
    
    private final Pane leftSpacer = new Pane();
    private final Pane rightSpacer = new Pane();

    public EditorProjectStatusPane() {
        getChildren().addAll(
                leftLabel,
                leftSpacer,
                midLabel,
                rightSpacer, 
                rightLabel
        );
        
        setPadding(new Insets(4));
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );

        HBox.setHgrow(
                rightSpacer,
                Priority.SOMETIMES
        );
        this.project = EditorProject.getInstance();
        this.project.addListener(this);
        updateState();
    }
    
    private void updateState() {
        String filePath = project.getFilePath();
        leftLabel.setText(filePath==null?"New Project":filePath);
        midLabel.setText("Zones: " + project.getZones().size());
        String savedMessage = "*";
        if ( filePath != null && !project.isEdited() ) {
            savedMessage = "Saved";
        } else {
            if ( project.isEdited() ) {
                savedMessage = "Edited";
            }
        }
        rightLabel.setText(savedMessage);
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        updateState();
    }
}
