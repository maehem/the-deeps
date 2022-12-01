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

import com.maehem.deeps.model.MapTile;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.view.TileView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorSheetView extends VBox implements EditorProjectListener {

    public static final Logger log = Logger.getLogger("deeps");

    private double zoom = 1.5;
    //private double imgScale;
    private final ImageView imgView;
    private final SheetModel sheet;
    private final StackPane imgViewGroup;
    private final ScrollPane sp;
    private final Rectangle selectedRect;
    //private final ArrayList<Rectangle> tileGrey = new ArrayList<>();
    private final Group rectG;
    private final Rectangle area;
    private final Group scalableImageView;
    private int selectedX = -1;
    private int selectedY = -1;
    private final EditorProject project;
    private final Group tileGroup;

    public EditorSheetView( EditorProject project, SheetModel sm ) {
        this.project = project;
        this.sheet = sm;
        
        int tSize = sheet.getSize();
        Button zoomIn = new Button("+");
        Font f = new Font(zoomIn.getFont().getSize() - 2);
        zoomIn.setFont(f);
        zoomIn.setOnAction((t) -> {
            setZoom( getZoom() + 0.5);            
        });
        Button zoomOut = new Button("-");
        zoomOut.setFont(f);
        zoomOut.setOnAction((t) -> {
            setZoom( getZoom() - 0.5);
        });
        
        HBox toolbar = new HBox(zoomOut, zoomIn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(4));

        this.imgView = new ImageView(sheet.getImage());
        imgView.setScaleX(1.0/sheet.getFidelity());
        imgView.setScaleY(1.0/sheet.getFidelity());
        scalableImageView = new Group(imgView);
        //Group imgGroup = new Group(scalableImageView);
        
        this.selectedRect = new Rectangle( tSize, tSize );
        this.selectedRect.setVisible(false);

        // Upper left corner is 0,0
        selectedRect.setStroke(Color.valueOf("#00bb00"));
        selectedRect.setStrokeWidth(2.0);
        selectedRect.setStrokeType(StrokeType.INSIDE);
        selectedRect.setFill(Color.TRANSPARENT);
        area = new Rectangle(
                sheet.getWidth()  * tSize, 
                sheet.getHeight() * tSize
        );
        area.setFill(Color.TRANSPARENT);
        area.setStroke(Color.LIGHTGRAY);
        area.setStrokeWidth(1);

        tileGroup = new Group();
        // Put controllable Rectangle over each tile to create a visual context.
        for ( int y = 0; y< sheet.getHeight(); y++ ) {
            for ( int x=0; x<sheet.getWidth(); x++ ) {
                int tilenum = (sheet.getWidth()*y)+x;
                Tile t = sheet.getTile(tilenum);
                TileView tv = new TileView(t, sheet ); //, x, y);
                tileGroup.getChildren().add(tv);
            }
        }
        
        // Wrap rectG to make them relative to each other.
        rectG = new Group( tileGroup,area, selectedRect);
        
        // Wrap rectG in another group to cause bounds to shrink to scaled result.
        Group clickGroup = new Group(rectG);
        imgViewGroup = new StackPane(/*imgGroup ,*/ clickGroup);
        
        sp = new ScrollPane(imgViewGroup);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        
        area.setOnMouseClicked((t) -> {
            selectedX = (int)(t.getX()/tSize);
            selectedY = (int)(t.getY()/tSize);
            log.log(Level.FINE, "Mouse click on tile: {0}x{1}", 
                    new Object[]{ selectedX, selectedY}
            );
            selectedRect.setTranslateX((selectedX*tSize));
            selectedRect.setTranslateY((selectedY*tSize));
            selectedRect.setVisible(true);
            project.setCurrentSheetTile(sheet.getUID(), getTileAt(selectedX, selectedY));
        });
        
        setZoom(getZoom()); // Cause proper scale of image.

        getChildren().addAll(toolbar, sp);
        project.addListener(this);
    }
    
    public Tile getTileAt( int x, int y ) {
        for( Node n: tileGroup.getChildren() ) {
            if ( n instanceof TileView ) {
                Tile t = ((TileView)n).getTile();
                if ( t.getX() == x && t.getY() == y ) {
                    return t;
                }
            }
        }
        return null;
    }
    
    public final double getZoom() {
        return zoom;
    }
    
    public final void setZoom( double zoom ) {
        if ( zoom < 1.0 ) {
            zoom = 1.0;
        }
        if ( zoom > 4.0 ) {
            zoom = 4.0;
        }
        this.zoom = zoom;

        scalableImageView.setScaleX(zoom);
        scalableImageView.setScaleY(zoom);
        rectG.setScaleX(zoom);
        rectG.setScaleY(zoom);
        selectedRect.setStrokeWidth(2.0/zoom);
        
        log.log(Level.INFO, "Zoom set to: {0}", getZoom());
    }

    public SheetModel getSheet() {
        return sheet;
    }
    
    /**
     * Return X coordinate of selected tile in sheet. 
     * Unselected if either X is less than zero.
     * 
     * @return selected tile
     */
    public int getSelectedX() {
        return selectedX;
    }
    
    /**
     * Return X coordinate of selected tile in sheet. 
     * Unselected if either X is less than zero.
     * 
     * @return selected tile
     */
    public int getSelectedY() {
        return selectedY;
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        if ( type == ChangeType.FUNC ) {
            log.log(Level.FINE, "EditorSheetView function changed.");
            // update greyed out tiles
            for ( Node n: tileGroup.getChildren()) {
                if ( n instanceof TileView ) {
                    TileView tv = (TileView)n;
                    switch ( p.getFunction() ) {
                        case SELECT:
                            tv.setGrey(false);
                            break;
                        case STAMP_MAP:
                            tv.setGrey(!(tv.getTile() instanceof MapTile));
                            break;
                        case STAMP_FIXTURE:
                            tv.setGrey(tv.getTile() instanceof MapTile);
                            break;
                    }
                } else {
                    log.log(Level.INFO, "   Not a TilelView.");
                }
            }
        }
    }
}
