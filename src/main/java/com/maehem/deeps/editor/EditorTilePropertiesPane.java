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
import com.maehem.deeps.model.EntityTile;
import com.maehem.deeps.model.FixtureTile;
import com.maehem.deeps.model.IntegerTileProperty;
import com.maehem.deeps.model.MapTile;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.TileProperty;
import com.maehem.deeps.model.Zone;
import com.maehem.deeps.view.TileView;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * TODO: Properties widgets
 *
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorTilePropertiesPane extends VBox implements EditorProjectListener {

    //private final TileType tileType;
    private final Class tileClass;
    private final EditorProject project;
    private final VBox widgets = new VBox();
    private Tile currentTile = null;
    private final Label titleLabel = new Label("Label");
    private final Label descriptionLabel = new Label("Description");
    private final Group iconBox = new Group();
    private final HBox header = new HBox();
    private final Pane headerSpacer = new Pane();

    public EditorTilePropertiesPane(Class clazz) {
        this.tileClass = clazz;
        //this.tileType = type;
        project = EditorProject.getInstance();
        project.addListener(this);

        //titleLabel.setText(tileType.name());
        titleLabel.setText(tileClass.getSimpleName());
        // Scroll Pane
        widgets.setPrefWidth(240);
        ScrollPane sp = new ScrollPane(widgets);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox details = new VBox (
                new HBox(titleLabel),
                new HBox(descriptionLabel)
        );
        
        HBox.setHgrow(
            headerSpacer,
            Priority.SOMETIMES
        );
        //iconBox.setPadding(new Insets(6,16,6, 6));
        header.getChildren().addAll( details , headerSpacer, iconBox );
        header.setPadding(new Insets(10,20,10,10));
        //header.setMinWidth(30);
        VBox content = new VBox(header, sp );

        getChildren().add(content);
        updateWidgets(null);
    }

    private void updateWidgets(Tile newTile) {
        this.currentTile = newTile;
        widgets.getChildren().clear();
        
        if (currentTile == null) {
            // Place no-selected label
            titleLabel.setText(tileClass.getSimpleName() + "  (No zone tile selected)");
            descriptionLabel.setText("");
            iconBox.getChildren().clear();

            widgets.getChildren().add(new Label("Nothing focused."));
        } else {
            // title bar        
            titleLabel.setText(tileClass.getSimpleName()
                    + "  " + currentTile.getMnemonic()
                    + "  X:" + currentTile.getX()
                    + "  Y:" + currentTile.getY()
            );
            descriptionLabel.setText(currentTile.getDescription());

            Zone zone = newTile.getZone();
            Character key = zone.getKeyFor(project.getCurrentTileUID());
            //String currentTile = key + String.valueOf( project.getCurrentTileNum());

            // Update the icon
            try {
                
                Tile clone = (Tile) currentTile.clone();
                log.log(Level.INFO, "Set icon for: {0}", new Object[]{
                    key + clone.getMnemonic().substring(1)
                });
                // Get image for current selected tile.
                TileView t = new TileView(clone, zone.getSheet(key)); //, 0, 0);
                t.setScaleX(2.0);
                t.setScaleY(2.0);
                iconBox.getChildren().clear();
                iconBox.getChildren().add(t);
                header.requestLayout();
            } catch (CloneNotSupportedException ex) {
                log.log(Level.SEVERE, "Clone operation not supported!", ex);
            }

            //widgets.getChildren().add(createIntWidget(Tile.class, "blocking"));
            //widgets.getChildren().add(createIntWidget(Tile.class, "sound"));
            //widgets.getChildren().add(createIntWidget(Tile.class, "luminous"));

//            if (newTile instanceof MapTile) {
//
//            } else if (newTile instanceof FixtureTile) {
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "ablation"));
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "inventoryItem"));
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "storage"));
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "track"));
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "umbra"));
//                //widgets.getChildren().add(createIntWidget(FixtureTile.class, "weapon"));
//            } else if (newTile instanceof EntityTile) {
//                widgets.getChildren().add(createIntWidget(EntityTile.class, "enemy"));
//                widgets.getChildren().add(createIntWidget(EntityTile.class, "npc"));
//                widgets.getChildren().add(createIntWidget(FixtureTile.class, "rolling"));
//
//            }

            for (TileProperty tp : currentTile.getPropertiesUnmodifiable()) {
                widgets.getChildren().add(createWidget(tp));
            }
        }
    }

    private Node createValueWidget(String k, String v) {
        Label lbl = new Label(k + ": ");
        lbl.setPrefWidth(100);
        lbl.setAlignment(Pos.BOTTOM_RIGHT);
        lbl.setPadding(new Insets(4, 0, 0, 0));
        TextField tf = new TextField(v);
        tf.setMaxWidth(Double.POSITIVE_INFINITY);
        HBox box = new HBox(lbl, tf);
        box.setPadding(new Insets(2, 6, 2, 2));
        return box;
    }

    private Node createWidget(TileProperty tp) {
        Label lbl = new Label(tp.getLabel() + ": ");
        //lbl.setPrefWidth(130);
        lbl.setMinWidth(130);
        
        lbl.setAlignment(Pos.BOTTOM_RIGHT);
        lbl.setPadding(new Insets(5, 0, 0, 0));
        HBox editField = new HBox();
        if (tp instanceof IntegerTileProperty) {
            IntegerTileProperty itp = (IntegerTileProperty) tp;
            List<Node> hbc = editField.getChildren();
            switch (itp.getEditStyle()) {
                case TEXT_FIELD:
                    TextField tf = new TextField(String.valueOf(itp.getValue()));
                    //tf.setMinWidth(30);
                    //tf.setPrefColumnCount(6);
                    hbc.add(tf);
                    tf.textProperty().addListener((o) -> {
                        try {
                        itp.setValue(Integer.parseInt(tf.getText()));
                        } catch (NumberFormatException ex ) {} // Ignore non numbers.
                    });
                    break;
                case SLIDER:
                    Slider slider = new Slider(itp.getMinValue(), itp.getMaxValue(), itp.getValue());
                    slider.setMinWidth(50);
                    slider.setMaxWidth(100);
                    slider.setPrefWidth(100);
                    slider.setPadding(new Insets(5, 0, 2, 3));
                    Label slbl = new Label("999");
                    slbl.setPadding(new Insets(4, 3, 0, 8));
                    slbl.setMinWidth(30);
                    slbl.textProperty().bindBidirectional(slider.valueProperty(), new DecimalFormat("######"));
                    hbc.add(slbl);
                    hbc.add(slider);

                    slider.valueProperty().addListener(
                            (ObservableValue<? extends Number> obV, Number oV, Number nV) -> {
                                itp.setValue(nV.intValue());
                    });

                    break;
                case SPINNER:
                    IntegerProperty integerProperty = new SimpleIntegerProperty(itp.getValue());
                    Spinner spinner = new Spinner(itp.getMinValue(), itp.getMaxValue(), itp.getValue());
                    //spinner.setMinWidth(100);
                    //spinner.setPrefWidth(120);
                    spinner.setEditable(true);
                    spinner.getValueFactory().valueProperty().bindBidirectional(integerProperty.asObject());
                    hbc.add(spinner);
                    
                    integerProperty.addListener(
                            (ObservableValue<? extends Number> obV, Number oV, Number nV) -> {
                                log.log(Level.INFO, "Spinner Changed. {0}", nV.intValue());
                                itp.setValue(nV.intValue());
                    });
                    
                    break;
            }

        } else {
            editField.getChildren().add(new Label("???"));
        }

        HBox box = new HBox(lbl, editField);
        box.setPadding(new Insets(2, 10, 2, 0));
        return box;
    }

    private Node createIntWidget(Class clazz, String fieldName) {
        Label lbl = new Label(fieldName + ": ");
        lbl.setPrefWidth(130);
        lbl.setAlignment(Pos.BOTTOM_RIGHT);
        lbl.setPadding(new Insets(4, 0, 0, 0));
        Field field;
        TextField tf = new TextField("ERROR");
        tf.setMinWidth(100);
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            tf.setText(Integer.toString(field.getInt(currentTile)));
            tf.setOnKeyTyped((t) -> {
                log.log(Level.FINER, "Typing in textfield {0} => {1}",
                        new Object[]{fieldName, t.getCharacter()});
                try {
                    int parseInt = Integer.parseInt(tf.getText());
                    field.set(currentTile, parseInt);
                    currentTile.notifyPropertyChanged(fieldName);
                    project.setEdited(true);
                } catch (NumberFormatException ex) {
                    // Do not update value if not a number.
                } catch (IllegalArgumentException ex) {
                    log.log(Level.SEVERE, "Illegal Argument Exception", ex);
                } catch (IllegalAccessException ex) {
                    log.log(Level.SEVERE, "Illegal Access Exception", ex);
                }
            });
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
        box.setPadding(new Insets(2, 10, 2, 0));
        return box;
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        if (type == ChangeType.FOCUS) {

            Tile ct = null;
            if (tileClass == MapTile.class) {
                ct = p.getFocusedMapTile();
            } else if (tileClass == FixtureTile.class) {
                ct = p.getFocusedFixtureTile();
            } else if (tileClass == EntityTile.class) {
                ct = p.getFocusedEntityTile();
            }
            if ( /*ct != null && */ ct != currentTile) {
                log.log(Level.FINE, "Focus changed for properties tab.");
                updateWidgets(ct);
            }
        }
    }

}
