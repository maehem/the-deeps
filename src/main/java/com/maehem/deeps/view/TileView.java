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
package com.maehem.deeps.view;

import static com.maehem.deeps.Deeps.log;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.TileListener;
import com.maehem.deeps.model.Zone;
import java.util.logging.Level;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class TileView extends Group implements TileListener {
    private final int dim;  // logical tile dimension
    private final Tile tile;
    private final ImageView view;
    private final double dSc; // image tile dimension
    private final Zone zone;
    private final Rectangle greyOut;
    
    public TileView( Tile tm,  Zone zone ) {//, int x, int y ) {
        this.zone = zone;
        this.tile = tm;
        SheetModel sm = zone.getSheet(tm.getSheetReference());
        this.dim = sm.getSize();
        this.dSc = dim*sm.getFidelity();
        this.view = new ImageView(sm.getImage());
        this.greyOut = new Rectangle( dim, dim );
        
        initTile( sm ); //, x, y);
        
        updateTileVisible();
        
        tm.addListener(this);
    }
    
    /**
     * TileView for Sheets in editors.  Does not listen to the tile.
     * 
     * @param tm
     * @param sm 
     */
    public TileView( Tile tm, SheetModel sm ) { //, int x, int y ) {
        this.zone = null;
        this.tile = tm;
        this.dim = sm.getSize();
        this.dSc = dim*sm.getFidelity();
        this.view = new ImageView(sm.getImage());
        this.greyOut = new Rectangle( dim, dim );
        
        initTile( sm ); //, x, y);       
    }
    
    private void initTile(SheetModel sm  ) { //, int x, int y) {        
        // Normalize the scale relative to fidelity.
        view.setScaleX(1.0/sm.getFidelity());
        view.setScaleY(1.0/sm.getFidelity());
        
        int cx = tile.getTileNum()%sm.getWidth();
        int cy = tile.getTileNum()/sm.getWidth();
    
        view.setViewport(new Rectangle2D( cx*dSc, cy*dSc, dSc, dSc ));

        greyOut.setFill(Color.DARKGRAY);
        greyOut.setOpacity(0.5);
        greyOut.setVisible(false);

        this.getChildren().addAll(new StackPane(new Group(view), greyOut));
        this.setLayoutX(tile.getX()*dim);
        this.setLayoutY(tile.getY()*dim);   
    }
    
    public final int getSize() {
        return dim;
    }

    public Tile getTile() {
        return tile;
    }
    
    @Override
    public void tileCodeChanged(Tile tile) {
        log.log(Level.INFO, "Tile code changed.{0},{1}  {2}:{3}", 
                new Object[]{
                    tile.getX(), tile.getY(),
                    tile.getMnemonic(), tile.getDescription()
                }
        );
        // Sheet might have changed.  Need to get it.
        SheetModel sm = zone.getSheet(tile.getSheetReference());
        int cx = tile.getTileNum()%sm.getWidth();
        int cy = tile.getTileNum()/sm.getWidth();
        updateTileVisible();
        setGrey(false);
        view.setViewport(new Rectangle2D( cx*dSc, cy*dSc, dSc, dSc ));
    }
    
    private void updateTileVisible() {
        if ( !tile.isMapTile() ) {
            view.setVisible(tile.getTileNum() > 0);
            greyOut.setVisible(tile.getTileNum() > 0);
        }
    }
    
    public void setGrey( boolean b) {
        if ( !tile.isMapTile() ) {
            this.greyOut.setVisible(false);
            this.setOpacity(b?0.3:1.0);
        } else {
            this.greyOut.setVisible(b);
            this.setOpacity(1.0);
        }
    }
    
    public boolean isGrey() {
        return greyOut.isVisible();
    }
}