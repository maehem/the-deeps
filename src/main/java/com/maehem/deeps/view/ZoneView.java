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
import com.maehem.deeps.model.MapTile;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.ZoneListener;
import com.maehem.deeps.model.Zone;
import java.util.logging.Level;
import javafx.scene.Group;
import javafx.scene.Node;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class ZoneView extends Group implements ZoneListener {

    private final Zone zone;
    private int focusX = 0;
    private int focusY = 0;

    public ZoneView(Zone zm) {
        this.zone = zm;

        log.log(Level.FINER, "Create ZoneView for : {0}", zm.getName());

        //buildMap(TileType.BASE);
        //buildMap(TileType.ITEM);
        buildMap();
        buildFixtures();

        zone.addListener(this);
    }

    private void buildMap(/* TileType type */) {
        log.log(Level.FINER, "  Build Map");
        for (int y = 0; y < zone.getHeight(); y++) {
            for (int x = 0; x < zone.getWidth(); x++) {
                Tile tm = zone.getMapTile(x, y);
                if (tm != null) {
//                    if (type.equals(TileType.ITEM) && tm.getIndex() == 0) {
//                        //tm.setMap(false);
//                        continue;  // Don't place Item.index 0 tiles.
//                    }

                    TileView t = new TileView(tm, zone); //, x, y);                
                    getChildren().add(t);
                } else /*if (type == TileType.BASE)*/ {
                    log.log(Level.SEVERE,
                            "Zone: {0} MapTile {2}x{3} didn''t load!",
                            new Object[]{zone.getName(), x, y});
                }
            }
        }
    }
    
    private void buildFixtures() {
        log.log(Level.FINER, "  Build Fixtures" );
        for (int y = 0; y < zone.getHeight(); y++) {
            for (int x = 0; x < zone.getWidth(); x++) {
                //Tile tm = zone.getTile(type, x, y);
                Tile tm = zone.getFixtureTile(x, y);
                if (tm != null) {
                    if ( /*type.equals(TileType.ITEM) && */ tm.getIndex() == 0) {
                        //tm.setMap(false);
                        continue;  // Don't place Item.index 0 tiles.
                    }

                    TileView t = new TileView(tm, zone); //, x, y);                
                    getChildren().add(t);
                } 
//                else if (type == TileType.BASE) {
//                    log.log(Level.SEVERE,
//                            "Zone: {0} Type: {1}  Tile {2}x{3} didn''t load!",
//                            new Object[]{zone.getName(), type.name(), x, y});
//                } // else  TileType.ITEM tiles that are null, nothing to do.
            }
        }
    }

    public final Zone getModel() {
        return zone;
    }

    public int getFocusX() {
        return focusX;
    }
    
    public int getFocusY() {
        return focusY;
    }
    
    public void setFocusX( int x ) {
        this.focusX = x;
    }
    
    public void setFocusY( int y ) {
        this.focusY = y;
    }
    
    @Override
    public void zoneTileChanged(Tile t) {
    }

    @Override
    public void zoneTileSwapped(Tile tOld, Tile tNew) {
        if (tOld == null) { // Add new non-map tile.
            if (tOld instanceof MapTile) {
                log.log(Level.SEVERE, "Tried to insert MapTile where there was previously no tile.\n    This should not be possible!");
                return;
            }
            log.log(Level.INFO, "New non-map Tile created at: {0}x{1}", new Object[]{tNew.getX(), tNew.getY()});
            TileView tvNew = new TileView(tNew, zone);
            tvNew.setGrey(false);
            getChildren().add(tvNew);

            //return;
        } else {
            for (Node n : getChildrenUnmodifiable()) {
                if (n instanceof TileView) {
                    TileView tv = (TileView) n;
                    if (tv.getTile().equals(tOld)) {
                        int idx = getChildren().indexOf(tv);
                        Node node = getChildren().get(idx);
                        if (node instanceof TileView) {
                            TileView tvOld = (TileView) node;
                            tvOld.getTile().removeListener(tvOld);
                        }
                        log.log(Level.INFO, "Swapped Tile at: {0}x{1}", new Object[]{tNew.getX(), tNew.getY()});
                        TileView tvNew = new TileView(tNew, zone);
                        tvNew.setGrey(tv.isGrey()); // copy grey value from old tile
                        getChildren().set(idx, tvNew);
                        return;
                    }
                }
            }
        }
    }

}
