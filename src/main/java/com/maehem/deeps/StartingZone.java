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

import java.io.IOException;

/**
 *
 * @author mark
 */
public class StartingZone extends Zone {

    private static final String TILEMAP_PATH="/tiles/kenny-tinydungeon.png";

    // Index n tiles in to the image map.  n = x * y
    private int[][] map = {
        { 40, 40, 40, 40, 40, 45, 40, 40, 40, 40, 45, 40, 40 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0,  0, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24, 24,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24,  0,  0,  0,  0,  0, 24,  0,  0 },
        {  0,  0,  0,  0, 24,  0,  0,  0,  0,  0, 24, 24,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24, 24,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0,  0, 24,  0,  0,  0 },
    };
    
    public StartingZone() throws IOException {
        super(TILEMAP_PATH);
        int d = getDimension();
        
        buildMap();
    }
    
    private void buildMap() {
        Tile t1 = new Tile(getSheet(), 0, 0, getDimension());
        t1.setLayoutX(0);
        
        for ( int y=0; y<map.length; y++) {
            for ( int x=0; x<map[0].length; x++) {
                int mx = map[y][x]%getSheetWidth();
                int my = map[y][x]/getSheetWidth();
                Tile t = new Tile(getSheet(), mx, my, getDimension());
                t.setLayoutX(x*getDimension());
                t.setLayoutY(y*getDimension());
                getChildren().add(t);
            }
        }
    }
}
