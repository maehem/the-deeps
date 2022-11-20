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
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.Zone.TileType;
import java.lang.reflect.Field;
import java.util.logging.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *  TODO: Properties widgets
 * 
 * 
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorTilePropertiesPane extends VBox implements EditorProjectListener {

    private final TileType tileType;
    private final EditorProject project;
    private final VBox widgets = new VBox();
    private Tile currentTile = null;
    private final Label titleLabel = new Label("Label");
    private final Label descriptionLabel = new Label("Description");
    

    public EditorTilePropertiesPane( TileType type) {
        this.tileType = type;
        project = EditorProject.getInstance();
        project.addListener(this);
                
        titleLabel.setText(tileType.name());
        VBox content = new VBox(
                new HBox(titleLabel) ,
                new HBox( descriptionLabel),
                widgets);
        
        // Scroll Pane
        ScrollPane sp = new ScrollPane(content);
        getChildren().add(sp);
        updateWidgets(null);
    }

    private void updateWidgets( Tile newTile ) {
        this.currentTile = newTile;
        widgets.getChildren().clear();
        
        
        if ( currentTile == null ) {
            // Place no-selected label
            titleLabel.setText(tileType.name() + "  (No zone tile selected)" );
            descriptionLabel.setText("");
            
            widgets.getChildren().add(new Label("Nothing focused."));
        } else {
            // title bar        
            titleLabel.setText(tileType.name()  +
                    "  " + currentTile.getMnemonic() + 
                    "  X:" + currentTile.getX() + 
                    "  Y:" + currentTile.getY() 
            );
            descriptionLabel.setText(currentTile.getDescription());
            
            widgets.getChildren().add(createIntWidget("inventoryItem"));
            widgets.getChildren().add(createIntWidget("wear"));
            widgets.getChildren().add(createIntWidget("weapon"));
                    
        }
    }
    
    private Node createValueWidget( String k, String v) {
        Label lbl = new Label(k + ": ");
        lbl.setPrefWidth(100);
        lbl.setAlignment(Pos.BOTTOM_RIGHT);
        lbl.setPadding(new Insets(4, 0, 0, 0));
        TextField tf = new TextField(v);
        tf.setMaxWidth(Double.POSITIVE_INFINITY);
        HBox box = new HBox(lbl, tf);
        box.setPadding(new Insets(2,6,2,2));
        return box;
    }
    
    private Node createIntWidget( String fieldName ) {
        Label lbl = new Label(fieldName + ": ");
        lbl.setPrefWidth(100);
        lbl.setAlignment(Pos.BOTTOM_RIGHT);
        lbl.setPadding(new Insets(4, 0, 0, 0));
        Field field;
        TextField tf = new TextField("ERROR");
        try {
            field = Tile.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            tf.setText(Integer.toString(field.getInt(currentTile)));
        } catch (NoSuchFieldException ex) {
            log.log(Level.SEVERE, "No such field " + fieldName, ex);
        } catch (SecurityException ex) {
            log.log(Level.SEVERE, "SecurityException for field: " + fieldName, ex);
        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, "IllegalArgument for field: " + fieldName, ex);
        } catch (IllegalAccessException ex) {
            log.log(Level.SEVERE, "IllegalAccess for field: " + fieldName, ex);
        }
        HBox box = new HBox(lbl, tf);
        box.setPadding(new Insets(2,6,2,2));
        return box;
    }
            
            
    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        if ( type == ChangeType.FOCUS ) {
            Tile ct = p.getFocusedTile(tileType);
            if ( ct != currentTile ) {
                log.log(Level.INFO, "Focus changed for properties tab.");
                updateWidgets( ct );
            }
        }
    }
    
    
    
}
