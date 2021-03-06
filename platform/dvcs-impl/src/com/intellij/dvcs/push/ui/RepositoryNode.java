/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.intellij.dvcs.push.ui;

import com.intellij.dvcs.push.TargetEditor;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RepositoryNode extends CheckedTreeNode implements EditableTreeNode, Comparable<RepositoryNode> {
  @NotNull private final RepositoryWithBranchPanel myRepositoryPanel;
  private ProgressIndicator myCurrentIndicator;

  public RepositoryNode(@NotNull RepositoryWithBranchPanel repositoryPanel) {
    super(repositoryPanel);
    myRepositoryPanel = repositoryPanel;
  }

  public boolean isCheckboxVisible() {
    return true;
  }

  @Override
  public void render(@NotNull ColoredTreeCellRenderer renderer) {
    String repositoryPath = myRepositoryPanel.getRepositoryName();
    renderer.append(repositoryPath, SimpleTextAttributes.GRAY_ATTRIBUTES);
    renderer.appendFixedTextFragmentWidth(120);
    renderer.append(myRepositoryPanel.getSourceName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    renderer.append(myRepositoryPanel.getArrow(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    TargetEditor targetEditor = myRepositoryPanel.getTargetEditor();
    targetEditor.render(renderer);
    Insets insets = BorderFactory.createEmptyBorder().getBorderInsets(targetEditor);
    renderer.setBorder(new EmptyBorder(insets));
  }

  @Override
  public Object getUserObject() {
    return myRepositoryPanel;
  }

  @Override
  public void fireOnChange() {
    myRepositoryPanel.fireOnChange();
  }

  @Override
  public void fireOnCancel() {
    myRepositoryPanel.fireOnCancel();
  }

  @Override
  public void fireOnSelectionChange(boolean isSelected) {
    myRepositoryPanel.fireOnSelectionChange(isSelected);
  }

  @Override
  public void stopLoading() {
    if (myCurrentIndicator != null && myCurrentIndicator.isRunning()) {
      myCurrentIndicator.cancel();
    }
  }

  @Override
  @NotNull
  public ProgressIndicator startLoading() {
    return myCurrentIndicator = new EmptyProgressIndicator();
  }

  public int compareTo(@NotNull RepositoryNode repositoryNode) {
    String name = myRepositoryPanel.getRepositoryName();
    RepositoryWithBranchPanel panel = (RepositoryWithBranchPanel)repositoryNode.getUserObject();
    return name.compareTo(panel.getRepositoryName());
  }
}
