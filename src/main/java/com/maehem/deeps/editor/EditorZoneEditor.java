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
import com.maehem.deeps.model.MapTile;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.Zone;
import com.maehem.deeps.view.TileView;
import com.maehem.deeps.view.ZoneView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Scale;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorZoneEditor extends ScrollPane implements EditorProjectListener {

    public static enum Function {
        SELECT, STAMP_MAP, STAMP_FIXTURE, STAMP_ENTITY
    }

    private final Zone zone;
    private final ZoneView zoneView;
    private final Group scaleGroup = new Group();
    private final EditorProject project;
    private final Rectangle stampHighlight;
    private final Rectangle propsHighlight;

    private Function function = Function.SELECT;

    public EditorZoneEditor(Zone model) {
        this.zone = model;
        this.zoneView = new ZoneView(zone);

        this.setFitToWidth(true);
        this.setFitToHeight(true);

        scaleGroup.getChildren().add(zoneView);
        setContent(new Group(scaleGroup));

        this.project = EditorProject.getInstance();
        project.addListener(this);

        Tile t0 = zone.getMapTile(0, 0);
        SheetModel sheet = zone.getSheet(t0.getSheet());
        int dim = sheet.getSize();

        stampHighlight = new Rectangle(0, 0, dim, dim);
        stampHighlight.setFill(Color.TRANSPARENT);
        stampHighlight.setStroke(Color.LIGHTBLUE);
        stampHighlight.setStrokeWidth(0.8);
        stampHighlight.setStrokeType(StrokeType.INSIDE);
        stampHighlight.setVisible(true);

        propsHighlight = new Rectangle(0, 0, dim, dim);
        propsHighlight.setFill(Color.TRANSPARENT);
        propsHighlight.setStroke(Color.LIGHTGREEN);
        propsHighlight.setStrokeWidth(1.2);
        propsHighlight.setStrokeType(StrokeType.INSIDE);
        propsHighlight.setVisible(true);

        scaleGroup.getChildren().addAll(stampHighlight, propsHighlight);

        setOnMouseMoved((tt) -> {
            log.log(Level.FINER,
                    "Zone View: Mouse Moved: {0}x{1}",
                    new Object[]{tt.getX(), tt.getY()}
            );

            int x = (int) (tt.getX() / dim / getZoom());
            if (x > (zone.getWidth() - 1)) {
                x = zone.getWidth() - 1;
            }
            //highlight.setLayoutX( dim*(int)(x/getZoom()) );
            stampHighlight.setLayoutX(dim * x);

            int y = (int) (tt.getY() / dim / getZoom());
            if (y > (zone.getHeight() - 1)) {
                y = zone.getHeight() - 1;
            }
            //highlight.setLayoutY( dim*(int)(y/getZoom()) );
            stampHighlight.setLayoutY(dim * y);
        });

        setOnMouseClicked((t) -> {

            switch (function) {
                case SELECT:
                    updatePropsHighlight(t, dim);
                    break;
                case STAMP_MAP:
                    doStampBase(t, MapTile.class);
                    break;
                case STAMP_FIXTURE:
                    doStampBase(t, FixtureTile.class);
                    break;
                case STAMP_ENTITY:
                    doStampBase(t, EntityTile.class);
                    break;
                default:

            }
        });
    }

    private void updatePropsHighlight(MouseEvent tt, int dim) {
        log.log(Level.FINER, "update props higlight.");
        int x = (int) (tt.getX() / dim / getZoom());
        if (x > (zone.getWidth() - 1)) {
            x = zone.getWidth() - 1;
        }
        propsHighlight.setLayoutX(dim * x);

        int y = (int) (tt.getY() / dim / getZoom());
        if (y > (zone.getHeight() - 1)) {
            y = zone.getHeight() - 1;
        }
        propsHighlight.setLayoutY(dim * y);

        zoneView.setFocusX(x);
        zoneView.setFocusY(y);
        project.setFocusedMapTile(zone.getMapTile(x, y));
        project.setFocusedFixtureTile(zone.getFixtureTile(x, y));
    }

    private void doStampBase(MouseEvent mevt, Class clazz) {
        log.log(Level.INFO, "Mouse click: {0}", mevt.getButton().name());
        double tileSize = 16;
        if (getCursor() instanceof ImageCursor) {
            ImageCursor c = (ImageCursor) getCursor();
            tileSize = c.getImage().getWidth();
            log.log(Level.FINER, "Tile Size: {0}", tileSize);
        }
        int x = (int) (mevt.getX() / tileSize);
        int y = (int) (mevt.getY() / tileSize);
        log.log(Level.INFO,
                "Stamp at: {0}x{1}",
                new Object[]{x, y});
        
        long uid = project.getCurrentTileUID();
        Character key = zone.getKeyFor(uid);
        if (mevt.getButton() == MouseButton.PRIMARY) {
            try {
                //zone.swapTile(f, x, y, key + String.valueOf(project.getCurrentTileNum() ));
                
                Tile clone = (Tile) project.getCurrentSheetTile().clone();
                if (clone.getClass() == clazz) {
                    clone.setSheet(key);
                    zone.swapTile(x, y, clone);
                    project.setEdited(true);
                } else {
                    log.log(Level.WARNING, "doStampBase():  Could not swap tiles as they are not the same class!");
                }
            } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(EditorZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException e) {
                log.log(Level.FINER, "User clicked stamp out of bounds.");
            }
        } else if ( mevt.getButton() == MouseButton.SECONDARY ) {
            // Clear the tile
            switch ( project.getFunction() ) {
                case STAMP_MAP:
                    // Set tile to <key>0
                    SheetModel sheet = project.getSheet(uid);
                    {
                        try {
                            Tile clone = (Tile) sheet.getTile(0).clone();
                            clone.setSheet(key);
                            zone.swapTile(x, y, clone);
                            project.setEdited(true);
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(EditorZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;

                case STAMP_FIXTURE:
                    // remove fixture at x,y
                    if ( zone.removeFixture(x,y) ) {
                        project.setEdited(true);
                    }
                    break;
                    
                case STAMP_ENTITY:
                    // remove entity at x,y
                    if ( zone.removeEntity(x,y) ) {
                        project.setEdited(true);
                    }
                    break;
            }
        }
    }

    protected void setZoom(double zoom) {
        scaleGroup.setScaleX(zoom);
        scaleGroup.setScaleY(zoom);
        updateCursor();
    }

    protected double getZoom() {
        return scaleGroup.getScaleX();
    }

    public void setFunction(Function f) {
        this.function = f;
        log.log(Level.FINE,
                "Zone Editor [{0}] set function to: {1}",
                new Object[]{zone.getName(), f.name()}
        );

        updateCursor(); // change cursor
        project.setFunction(this.function);
        muteTiles(this.function);
    }

    private void muteTiles(Function f) {
        for (Node n : zoneView.getChildren()) {
            if (n instanceof TileView) {
                TileView tv = (TileView) n;
                switch (f) {
                    case SELECT:
                        tv.setGrey(false);
                        break;
                    case STAMP_MAP:
                        tv.setGrey(!(tv.getTile() instanceof MapTile));
                        break;
                    case STAMP_FIXTURE:
                        tv.setGrey(!(tv.getTile() instanceof FixtureTile));
                        break;
                    case STAMP_ENTITY:
                        tv.setGrey(!(tv.getTile() instanceof EntityTile));
                        break;
                }
            }
        }
    }

    private void updateCursor() {
        switch (function) {
            case SELECT:
                setCursor(Cursor.DEFAULT);
                break;
            case STAMP_MAP:
            case STAMP_FIXTURE:
            case STAMP_ENTITY:
                setCursorStamp();
                break;
        }
    }

    private void setCursorStamp() {
        try {
            Character key = zone.getKeyFor(project.getCurrentTileUID());
            //String currentTile = key + String.valueOf( project.getCurrentTileNum());

            Tile clone = (Tile) project.getCurrentSheetTile().clone();
            clone.setSheet(key);
            log.log(Level.INFO, "Set stamp for: {0}", new Object[]{
                key + clone.getMnemonic().substring(1)
            });
            // Get image for current selected tile.
            //Tile tm = new Tile(currentTile, null);
            TileView t = new TileView(clone, zone.getSheet(key)); //, 0, 0);
            t.setOpacity(0.7);
            SnapshotParameters sp = new SnapshotParameters();

            log.log(Level.INFO, " Tile size: {0}  scale: {1}", new Object[]{t.getSize(), t.getScaleX()});
            log.log(Level.INFO, " ScaleGroup.scale: {0}", scaleGroup.getScaleX());
            // Scale image to match zoom level.
            sp.setTransform(new Scale(scaleGroup.getScaleX(), scaleGroup.getScaleY()));
            // change opacity of image. Will be white background otherwise
            sp.setFill(Color.TRANSPARENT);

            setCursor(new ImageCursor(t.snapshot(sp, null)));
        } catch (CloneNotSupportedException ex) {
            log.log(Level.SEVERE, "Clone operation not supported!", ex);
        }
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        log.log(Level.FINE, "Zone [{0}] project state change: {1}",
                new Object[]{zone.getName(), type.name()});
        if (type == ChangeType.TILE) {
            updateCursor();
        }
    }

}
