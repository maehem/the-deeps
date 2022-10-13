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
import java.io.InputStream;
import java.util.Properties;
import javafx.scene.Group;
import javafx.scene.image.Image;

/**
 *
 * @author mark
 */
public class Zone extends Group {

    private final int d;
    private final int sheetWidth;
    private final int sheetHeight;
    private Image tileMap;
    
    public Zone( String path ) throws IOException {
        this.tileMap = new Image(getClass().getResourceAsStream(path));
        // TODO: Check if tileMap is NULL and do something about it.
        String[] fName = path.split(".png");
        InputStream props = getClass().getResourceAsStream(fName[0] + ".properties");
        Properties p = new Properties();
        p.load(props);
        this.d = Integer.valueOf(p.getProperty("size"));
        this.sheetWidth = Integer.valueOf(p.getProperty("width"));
        this.sheetHeight = Integer.valueOf(p.getProperty("height"));
        

    }
    
    public int getDimension() {
        return d;
    }
    
    public Image getSheet() {
        return tileMap;
    }
    
    public int getSheetWidth() {
        return sheetWidth;
    }
    
    public int getSheetHeight() {
        return sheetHeight;
    }
}
