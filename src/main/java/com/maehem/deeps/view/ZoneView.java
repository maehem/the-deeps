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
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.ZoneListener;
import com.maehem.deeps.model.Zone;
import com.maehem.deeps.model.Zone.TileType;
import java.util.logging.Level;
import javafx.scene.Group;
import javafx.scene.Node;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class ZoneView extends Group implements ZoneListener {

    private final Zone zone;
    
    public ZoneView( Zone zm ) {
        this.zone = zm;
        
        log.log(Level.FINER, "Create ZoneView for : {0}", zm.getName());
        
        buildMap(TileType.BASE);
        buildMap(TileType.ITEM);
        
        zone.addListener(this);        
    }
    
    private void buildMap( TileType type) {
        log.log(Level.FINER, "  Build Map for: {0}", type.name());
        for ( int y=0; y<zone.getHeight(); y++) {
            for ( int x=0; x<zone.getWidth(); x++) {
                Tile tm  = zone.getTile(type, x, y);
                if ( type.equals(TileType.ITEM) && tm.getTileNum()==0 ) {
                    tm.setMap(false);
                }  
                
                TileView t = new TileView(tm, zone ); //, x, y);                
                getChildren().add(t);
            }
        }
    }
    
    public final Zone getModel() {
        return zone;
    }
    
    @Override
    public void zoneTileChanged(Tile t) {
    }

    @Override
    public void zoneTileSwapped(Tile tOld, Tile tNew) {
        if ( tOld == null ) {
            log.log(Level.WARNING, "Tried to swap old tile that is null!");
            return;
        }
        for ( Node n: getChildrenUnmodifiable() ) {
            if ( n instanceof TileView ) {
                TileView tv = (TileView)n;
                if ( tv.getTile().equals(tOld) ) {
                    int idx = getChildren().indexOf(tv);
                    TileView tvNew = new TileView(tNew, zone);
                    tvNew.setGrey(tv.isGrey());
                    getChildren().set(idx, tvNew);
                    return;
                }
            }
        }
    }
    
}
