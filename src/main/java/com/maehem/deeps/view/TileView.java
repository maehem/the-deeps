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
import com.maehem.deeps.model.EntityTile;
import com.maehem.deeps.model.FixtureTile;
import static com.maehem.deeps.model.FixtureTile.SHAD;
import com.maehem.deeps.model.IntegerTileProperty;
import com.maehem.deeps.model.MapTile;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.TileListener;
import com.maehem.deeps.model.TileProperty;
import com.maehem.deeps.model.Zone;
import java.util.logging.Level;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class TileView extends Group implements TileListener {

    private static final double FIXTURE_GREY_OPACITY = 0.2;

    private final int dim;  // logical tile dimension
    private final Tile tile;
    private final ImageView view;
    private final double dSc; // image tile dimension
    private final Zone zone;
    private final Rectangle greyOut;
    private DropShadow dropShadow = new DropShadow();

    public TileView(Tile tm, Zone zone) {
        this.zone = zone;
        this.tile = tm;
        SheetModel sm = zone.getSheet(tm.getSheet());
        this.dim = sm.getSize();
        this.dSc = dim * sm.getFidelity();
        this.view = new ImageView(sm.getImage());
        this.greyOut = new Rectangle(dim, dim);

        initTile(sm);
        updateTileVisible();
        tm.addListener(this);
    }

    /**
     * TileView for Sheets in editors. Does not listen to the tile.
     *
     * @param tm
     * @param sm
     */
    public TileView(Tile tm, SheetModel sm) { //, int x, int y ) {
        this.zone = null;
        this.tile = tm;
        this.dim = sm.getSize();
        this.dSc = dim * sm.getFidelity();
        this.view = new ImageView(sm.getImage());
        this.greyOut = new Rectangle(dim, dim);

        initTile(sm); //, x, y);       
    }

    private void initTile(SheetModel sm) { //, int x, int y) {        
        // Normalize the scale relative to fidelity.
        view.setScaleX(1.0 / sm.getFidelity());
        view.setScaleY(1.0 / sm.getFidelity());

        int cx = tile.getIndex() % sm.getWidth();
        int cy = tile.getIndex() / sm.getWidth();

        view.setViewport(new Rectangle2D(cx * dSc, cy * dSc, dSc, dSc));

        greyOut.setFill(Color.DARKGRAY);
        greyOut.setOpacity(0.5);
        greyOut.setVisible(false);

        if ( getTile() instanceof FixtureTile  ) {
            int umbra = ((FixtureTile)getTile()).getUmbra();
            log.log(Level.FINER, "Tile is a FixtureTile:  umbra={0}", umbra);
            if ( umbra > 0 ) {
                dropShadow.setBlurType(BlurType.GAUSSIAN);
                dropShadow.setRadius(16.0);
                dropShadow.setOffsetX(0.0);
                dropShadow.setOffsetY(16.0);
                setDropShadow(umbra);
                view.setEffect(dropShadow);
            }
        }
        if ( getTile() instanceof EntityTile  ) {
            int umbra = ((EntityTile)getTile()).getUmbra();
            log.log(Level.FINER, "Tile is a EntityTile:  SHAD={0}", umbra);
            if ( umbra > 0 ) {
                dropShadow.setBlurType(BlurType.GAUSSIAN);
                dropShadow.setRadius(10.0);
                dropShadow.setOffsetX(2.0);
                dropShadow.setOffsetY(-10.0);
                setDropShadow(umbra);
                view.setEffect(dropShadow);
                this.setTranslateY(-3.0);
            }
        }
               
        this.getChildren().addAll(new StackPane(new Group(view), greyOut));
        Bounds bounds = view.getBoundsInParent(); // when a drop shadow, adjust position.
        this.setLayoutX(tile.getX() * dim  - (bounds.getWidth()-dim)/2.0 );
        this.setLayoutY(tile.getY() * dim  - (bounds.getHeight()-dim)/2.0 );
    }

    private void setDropShadow( int val ) {
        dropShadow.setColor(Color.color(0.0, 0.0, 0.0, val/100.0 ));        
    }
    
    public final int getSize() {
        return dim;
    }

    public Tile getTile() {
        return tile;
    }

    @Override
    public void tileCodeChanged(Tile tile) {
        log.log(Level.INFO, "Tile code changed. {0},{1}  {2}:{3}",
                new Object[]{
                    tile.getX(), tile.getY(),
                    tile.getMnemonic(), tile.getDescription()
                }
        );
        // Sheet might have changed.  Need to get it.
        SheetModel sm = zone.getSheet(tile.getSheet());
        int cx = tile.getIndex() % sm.getWidth();
        int cy = tile.getIndex() / sm.getWidth();
        updateTileVisible();
        setGrey(false);
        view.setViewport(new Rectangle2D(cx * dSc, cy * dSc, dSc, dSc));
    }

    private void updateTileVisible() {
        if (!(tile instanceof MapTile)) {
            view.setVisible(tile.getIndex() > 0);
            greyOut.setVisible(tile.getIndex() > 0);
        }
    }

    public void setGrey(boolean b) {
        if (!(tile instanceof MapTile)) {
            this.greyOut.setVisible(false);
            this.setOpacity(b ? FIXTURE_GREY_OPACITY : 1.0);
        } else {
            this.greyOut.setVisible(b);
            this.setOpacity(1.0);
        }
    }

    public boolean isGrey() {
        return greyOut.isVisible();
    }

    @Override
    public void tilePropertyChanged(Tile tile, String propName) {
//        log.log(Level.INFO, "(old)TileView: Tile Property changed: {0}", propName);
//        if ( propName.equals("umbra") ) {
//            if ( tile instanceof FixtureTile ) {
//                FixtureTile t = (FixtureTile) tile;
//                setDropShadow(t.getUmbra());
//            }
//        }
    }

    @Override
    public void tilePropertyChanged(TileProperty property) {
        log.log(Level.FINER, "TileView: Tile Property changed: {0}", property.getFlag());
        if ( property.getParent() instanceof FixtureTile ) {
            //FixtureTile t = (FixtureTile) property.getParent();
            switch (property.getFlag() ) {
                case FixtureTile.SHAD:
                    if ( property instanceof IntegerTileProperty ) {
                        setDropShadow(((IntegerTileProperty) property).getValue());
                    }
                    break;
            }
        }
        if ( property.getParent() instanceof EntityTile ) {
            //EntityTile t = (EntityTile) property.getParent();
            switch (property.getFlag() ) {
                case EntityTile.SHAD:
                    if ( property instanceof IntegerTileProperty ) {
                        setDropShadow(((IntegerTileProperty) property).getValue());
                    }
                    break;
            }
        }
    }
}
