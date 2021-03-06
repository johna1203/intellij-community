/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.debugger.actions;

import com.intellij.debugger.ui.DebuggerPanelsManager;
import com.intellij.debugger.ui.impl.MainWatchPanel;
import com.intellij.debugger.ui.impl.watch.DebuggerTreeNodeImpl;
import com.intellij.debugger.ui.impl.watch.WatchItemDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

/**
 * User: lex
 * Date: Sep 26, 2003
 * Time: 8:34:01 PM
 */
public class EditWatchAction extends DebuggerAction {
  public void actionPerformed(final AnActionEvent e) {
    final DebuggerTreeNodeImpl selectedNode = getSelectedNode(e.getDataContext());
    if(selectedNode == null || !(selectedNode.getDescriptor() instanceof WatchItemDescriptor)) return;

    Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());

    MainWatchPanel watchPanel = DebuggerPanelsManager.getInstance(project).getWatchPanel();
    if(watchPanel != null) {
      watchPanel.editNode(selectedNode);
    }
  }

  public void update(AnActionEvent e) {
    final DebuggerTreeNodeImpl selectedNode = getSelectedNode(e.getDataContext());

    e.getPresentation().setVisible(selectedNode != null && selectedNode.getDescriptor() instanceof WatchItemDescriptor);
  }

};
