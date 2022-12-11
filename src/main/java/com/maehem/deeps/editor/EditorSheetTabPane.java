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

import static com.maehem.deeps.editor.EditorProjectListener.ChangeType.CLEARED;
import com.maehem.deeps.model.SheetModel;
import java.io.IOException;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Mark J Koch (@maehem on GitHub)
 */
public class EditorSheetTabPane extends TabPane implements EditorProjectListener {

    private final EditorProject project;

    public EditorSheetTabPane() throws IOException {
        this.project = EditorProject.getInstance();
        setMinHeight(30);
        project.addListener(this);
    }

    @Override
    public void projectStateChanged(EditorProject p, ChangeType type) {
        switch (type) {
            case LOADED:
            case SHEET_NAME:
                getTabs().clear();

                for (SheetModel sm : project.getSheets()) {
                    EditorSheetView sheet = new EditorSheetView(project, sm);

                    Tab tab = new Tab(sm.getName(), sheet);
                    tab.setTooltip(new Tooltip(
                            sheet.getSheet().getName()
                            + " by "
                            + sheet.getSheet().getAuthor()
                    ));
                    getTabs().add(tab);
                }
                break;
            case CLEARED:
                getTabs().clear();
        }
    }

}
