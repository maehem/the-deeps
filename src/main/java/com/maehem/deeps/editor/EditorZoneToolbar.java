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
import com.maehem.deeps.model.Zone;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorZoneToolbar extends ToolBar{
    private static final double ZOOM_MIN = 0.5;
    private static final double ZOOM_MAX = 4.0;
    private static final double ZOOM_INC = 0.5;

    private final Button zoomOut;
    private final Button zoomIn;
    private final Button zoomReset;
    private final ToggleButton selectButton;
    private final ToggleButton stampBaseButton;
    private final ToggleButton stampItemButton;
    private final ToggleButton stampEntityButton;

    private final Pane leftSpacer = new Pane();

    private double zoom = 1.0;
    
    public EditorZoneToolbar( EditorZoneEditor editor, Zone model ) {
        zoomOut   = createButton("Zoom Out", "/icons/zoom-out.png");
        zoomIn    = createButton("Zoom Out", "/icons/zoom-in.png");
        zoomReset = createButton("Zoom Reset", "/icons/magnifier.png");
        selectButton = createToggleButton("Select", "/icons/plus-circle.png");
        stampBaseButton = createToggleButton("Stamp Map Base", "/icons/paw-print.png");
        stampItemButton = createToggleButton("Stamp Fixture", "/icons/poison-bottle.png");
        stampEntityButton = createToggleButton("Stamp Entity", "/icons/spider.png");

        ToggleGroup functionGroup = new ToggleGroup();
        selectButton.setToggleGroup(functionGroup);
        stampBaseButton.setToggleGroup(functionGroup);
        stampItemButton.setToggleGroup(functionGroup);
        stampEntityButton.setToggleGroup(functionGroup);
        
        HBox functionButtons = new HBox(selectButton, 
                stampBaseButton, stampItemButton, stampEntityButton
        );
        
        getItems().addAll(
                zoomOut,
                zoomIn,
                zoomReset ,       
                createSeparator(),
                functionButtons,
                leftSpacer
        );
        
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );

        zoomOut.setOnAction((t) -> {
            zoom -= ZOOM_INC;
            checkZoom();
            editor.setZoom(zoom);
        });
        zoomIn.setOnAction((t) -> {
            zoom += ZOOM_INC;
            checkZoom();
            editor.setZoom(zoom);
        });
        zoomReset.setOnAction((t) -> {
            zoom = 1.0;
            checkZoom();
            editor.setZoom(zoom);
        });
        selectButton.setOnAction((t) -> {
            editor.setFunction(EditorZoneEditor.Function.SELECT);
            log.log(Level.INFO, "Toggle Select Button");
        });
        stampBaseButton.setOnAction((t) -> {
            editor.setFunction(EditorZoneEditor.Function.STAMP_MAP);
            log.log(Level.INFO, "Toggle Stamp Base Button");
        });
        stampItemButton.setOnAction((t) -> {
            editor.setFunction(EditorZoneEditor.Function.STAMP_FIXTURE);
            log.log(Level.INFO, "Toggle Stamp Fixture Button");
        });
        stampEntityButton.setOnAction((t) -> {
            editor.setFunction(EditorZoneEditor.Function.STAMP_ENTITY);
            log.log(Level.INFO, "Toggle Stamp Entity Button");
        });
        
        
        Platform.runLater(() -> {
            selectButton.setSelected(true);
            editor.setFunction(EditorZoneEditor.Function.SELECT);
        });
    }
    
    private void checkZoom() {
        if (zoom <= ZOOM_MIN) {
            zoom = ZOOM_MIN;
        }
        if (zoom >= ZOOM_MAX) {
            zoom = ZOOM_MAX;
        }
        zoomIn.setDisable(zoom >= ZOOM_MAX);
        zoomOut.setDisable(zoom <= ZOOM_MIN);
    }
    
    private Button createButton( String name, String path ) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(24);
        view.setPreserveRatio(true);
        
        Button b = new Button();
        b.setGraphic(view);
        b.setTooltip(new Tooltip(name));
        
        return b;
    }
    
    private ToggleButton createToggleButton( String name, String path ) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(24);
        view.setPreserveRatio(true);
        
        ToggleButton b = new ToggleButton();
        b.setGraphic(view);
        b.setTooltip(new Tooltip(name));
        
        return b;
    }
    
    private Separator createSeparator() {
        Separator s = new Separator();
        s.setMinWidth(20);

        return s;        
    }
    
}
