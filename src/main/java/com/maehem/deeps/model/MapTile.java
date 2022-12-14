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
package com.maehem.deeps.model;

/**
 *
 * @author mark
 */
public class MapTile extends Tile {
            
    
    public MapTile(Zone zone, Character sheet, int index, int x, int y, String props) {
        super(zone, sheet, index, x, y );//, props);
        applyFlags(props);
    }

    public MapTile(Zone zone, int index, int x, int y, String props) {
        super(zone, 'A', index, x, y );//, props);
        applyFlags(props);
    }

    @Override
    protected void configureFlagSetting(String flag) {}
    
}
