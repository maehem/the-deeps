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
package com.maehem.deeps;

import com.maehem.deeps.model.GameModel;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Zone;
import java.io.IOException;

/**
 *  DO NOT USE!    REALLY OLD CODE.
 * @author Mark J Koch ( GitHub @maehem)
 */
public class StartingZone /*extends ZoneView*/ implements GameModel {

    private static final String TILEMAP_PATH="/tiles/kenny-tinydungeon.png";
    //private static final String TILEMAP_PATH="/tiles/kenny-tinydungeon";
    

    // Index n tiles in to the image map.  n = x * y
    private static int[][] map = {
        { 40, 40, 40, 40, 40, 45, 40, 40, 40, 40, 45, 40, 40, 40, 40, 40, 40, 40, 45, 40, 40, 40, 40, 45, 40, 40 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24, 24,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
    };
    
    // Index n tiles in to the image map.  n = x * y
    // Props layer
    private static int[][] item = {
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  84,  85,  86,  87,  88,  89,  90,  91,  92,  93,  94,  95,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  96,  97,  98,  99, 100, 101, 102, 103, 104, 105, 106, 107,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
    };
    
    private Zone model = new Zone( null, "Tiny Dungeon", 26, 15);
    
    public StartingZone() throws IOException {
        //super(TILEMAP_PATH, map, item);
        
    }


    @Override
    public SheetModel getSheet(Long uid) {
        return null;
    }

    @Override
    public void addSheet(SheetModel sheet) {
    }
    
    
}
