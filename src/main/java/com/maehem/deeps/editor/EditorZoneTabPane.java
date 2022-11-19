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

import com.maehem.deeps.model.Zone;
import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorZoneTabPane extends TabPane implements EditorProjectListener {

    private final EditorProject project;

    public EditorZoneTabPane() throws IOException {
        this.project = EditorProject.getInstance();
        project.addListener(this);

        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        if (type == ChangeType.LOADED || type == ChangeType.ZONE_NAME) {
            for (Tab t : getTabs()) {
                if (t.getContent() instanceof EditorZoneTab) {

                    EditorZoneTab tC = (EditorZoneTab) t.getContent();
                    for (Node n : tC.getChildren()) {
                        if (n instanceof EditorZoneEditor) {
                            p.removeListener((EditorZoneEditor) n);
                        }
                    }
                }
            }

            getTabs().clear();

            for (Zone zone : project.getZones()) {
                EditorZoneTab zt = new EditorZoneTab(zone);

                Tab tab = new Tab(zone.getName(), zt);
                tab.setTooltip(new Tooltip(
                        zone.getName()
                ));
                getTabs().add(tab);
            }
        }
    }
}
