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
    public static enum Function { SELECT, STAMP_BASE, STAMP_ITEM }
    
    private final Zone zone;
    private final ZoneView zoneView;
    private final Group scaleGroup = new Group();
    private final EditorProject project;
    private final Rectangle stampHighlight;
    private final Rectangle propsHighlight;
    
    private Function function = Function.SELECT;
    
    public EditorZoneEditor( Zone model ) {
        this.zone = model;
        this.zoneView = new ZoneView(zone);
        
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        
        scaleGroup.getChildren().add(zoneView);
        setContent(new Group(scaleGroup));
        
        this.project =  EditorProject.getInstance();
        project.addListener(this);
        
        Tile t0 = zone.getTile(Zone.TileType.BASE, 0, 0);
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
                    new Object[]{ tt.getX(), tt.getY() }
            );
            
            int x = (int) (tt.getX()/dim/getZoom());
            if ( x >  (zone.getWidth()-1) ) {
                x = zone.getWidth() - 1;
            }
            //highlight.setLayoutX( dim*(int)(x/getZoom()) );
            stampHighlight.setLayoutX( dim*x );
            
            int y = (int) (tt.getY()/dim/getZoom());
            if ( y >  (zone.getHeight()-1) ) {
                y = zone.getHeight() - 1;
            }
            //highlight.setLayoutY( dim*(int)(y/getZoom()) );
            stampHighlight.setLayoutY( dim*y );
        }); 
        
        setOnMouseClicked((t) -> {
            switch ( function ) {
                case SELECT:
                    updatePropsHighlight(t, dim);
                    break;
                case STAMP_BASE:
                    doStampBase(t, Zone.TileType.BASE);
                    break;
                case STAMP_ITEM:
                    doStampBase(t, Zone.TileType.ITEM);                    
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

        // tell project what tiles at this location.
        project.setFocusedTile(Zone.TileType.BASE,
                zone.getTile(Zone.TileType.BASE, x, y)
        );
        project.setFocusedTile(Zone.TileType.ITEM,
                zone.getTile(Zone.TileType.ITEM, x, y)
        );
    }
    
    private void doStampBase(MouseEvent mevt, Zone.TileType f ) {
        double tileSize = 16;
        if (getCursor() instanceof ImageCursor) {
            ImageCursor c = (ImageCursor) getCursor();
            tileSize = c.getImage().getWidth();
            log.log(Level.FINER, "Tile Size: " + tileSize);
        }
        int x = (int) (mevt.getX() / tileSize);
        int y = (int) (mevt.getY() / tileSize);
        log.log(Level.INFO,
                "Stamp at: {0}x{1}",
                new Object[]{x, y});
        try {
            long uid = project.getCurrentTileUID();
            Character key = zone.getKeyFor( uid);
            try {
                //zone.swapTile(f, x, y, key + String.valueOf(project.getCurrentTileNum() ));
                Tile clone = (Tile) project.getCurrentSheetTile().clone();
                clone.setSheet(key);
                
                zone.swapTile(f, x, y, clone);
                project.setEdited(true);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(EditorZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            //zone.getTile(f, x, y).setMnemonic(key + String.valueOf( project.getCurrentTileNum()));
            //zone.getTile(f, x, y).setCode(zone.getCode())
        } catch (IndexOutOfBoundsException e) {
            log.log(Level.FINER, "User clicked stamp out of bounds.");
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

    public void setFunction( Function f ) {
        this.function = f;
        log.log(Level.FINE, 
                "Zone Editor [{0}] set function to: {1}", 
                new Object[]{zone.getName(), f.name()}
        );
        
        updateCursor(); // change cursor
        project.setFunction( this.function );
        muteTiles(this.function);
    }
    
    private void muteTiles( Function f) {
        for ( Node n: zoneView.getChildren() ) {
            if ( n instanceof TileView ) {
                TileView tv = (TileView)n;
                switch(f) {
                    case SELECT:
                        tv.setGrey(false);                        
                        break;
                    case STAMP_BASE:
                        //tv.setGrey(!tv.getTile().isMapTile());
                        tv.setGrey( !(tv.getTile() instanceof MapTile) );
                        break;
                    case STAMP_ITEM:
                        //tv.setGrey(tv.getTile().isMapTile());
                        tv.setGrey( tv.getTile() instanceof MapTile );
                        break;
                }
            }
        }
//        switch ( f ) {
//            case SELECT:
//                for ( Node n: zoneView.getChildren() ) {
//                    if ( n instanceof TileView ) {
//                        TileView tv = (TileView)n;
//                        tv.setGrey(false);
//                    }
//                }
//                break;
//                case STAMP_BASE:
//                    for ( Node n: zoneView.getChildren() ) {
//                        if ( n instanceof TileView ) {
//                            TileView tv = (TileView)n;
//                            tv.setGrey(!tv.getTile().isMapTile());
//                        }
//                    }
//                break;
//                case STAMP_ITEM:
//                    for ( Node n: zoneView.getChildren() ) {
//                        if ( n instanceof TileView ) {
//                            TileView tv = (TileView)n;
//                            tv.setGrey(tv.getTile().isMapTile());
//                        }
//                    }                
//                break;
//        }
    }
    
    private void updateCursor() {
        switch( function ) {
            case SELECT:
                    setCursor(Cursor.DEFAULT);
                break;
            case STAMP_BASE:
            case STAMP_ITEM:
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
            TileView t = new TileView(clone, zone.getSheet(key) ); //, 0, 0);
            t.setOpacity(0.7);
            SnapshotParameters sp = new SnapshotParameters();
            
            log.log(Level.INFO, " Tile size: " + t.getSize() + "  scale: " + t.getScaleX() );
            log.log(Level.INFO, " ScaleGroup.scale: " + scaleGroup.getScaleX() );
            // Scale image to match zoom level.
            sp.setTransform(new Scale(scaleGroup.getScaleX(), scaleGroup.getScaleY()));
            // change opacity of image. Will be white background otherwise
            sp.setFill(Color.TRANSPARENT);
                    
            setCursor( new ImageCursor(t.snapshot(sp, null)));
        } catch (CloneNotSupportedException ex) {
            log.log(Level.SEVERE, "Clone operation not supported!", ex);
        }
    }
    
    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        log.log(Level.FINE, "Zone [{0}] project state change: {1}", 
                new Object[]{zone.getName(),type.name()});
        if ( type == ChangeType.TILE ) {
            updateCursor();
        }
    }
    
}
