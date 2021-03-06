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
package com.jetbrains.python.hierarchy;


import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author novokrest
 * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
 */
public abstract class HierarchyTreeStructureViewer extends TestCase {

  private static final String NODE_ELEMENT_NAME = "node";
  private static final String ANY_NODES_ELEMENT_NAME = "any";
  private static final String TEXT_ATTR_NAME = "text";
  private static final String BASE_ATTR_NAME = "base";

  /**
   * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
   */
  public static void checkHierarchyTreeStructure(final HierarchyTreeStructure treeStructure, final Document document) {
    final HierarchyNodeDescriptor rootNodeDescriptor = (HierarchyNodeDescriptor)treeStructure.getRootElement();
    rootNodeDescriptor.update();
    final Element rootElement = document.getRootElement();
    if (rootElement == null || !NODE_ELEMENT_NAME.equals(rootElement.getName())) {
      throw new IllegalArgumentException("Incorrect root element in verification resource");
    }
    checkNodeDescriptorRecursively(treeStructure, rootNodeDescriptor, rootElement);
  }

  /**
   * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
   */
  private static void checkNodeDescriptorRecursively(final HierarchyTreeStructure treeStructure,
                                                     final HierarchyNodeDescriptor descriptor,
                                                     final Element expectedElement) {
    checkBaseNode(treeStructure, descriptor, expectedElement);
    checkContent(descriptor, expectedElement);
    checkChildren(treeStructure, descriptor, expectedElement);
  }

  /**
   * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
   */
  private static void checkBaseNode(final HierarchyTreeStructure treeStructure,
                                    final HierarchyNodeDescriptor descriptor,
                                    final Element expectedElement) {
    final String baseAttrValue = expectedElement.getAttributeValue(BASE_ATTR_NAME);
    final HierarchyNodeDescriptor baseDescriptor = treeStructure.getBaseDescriptor();
    final boolean mustBeBase = "true".equalsIgnoreCase(baseAttrValue);
    assertTrue("Incorrect base node", mustBeBase ? baseDescriptor == descriptor : baseDescriptor != descriptor);
  }

  /**
   * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
   */
  private static void checkContent(final HierarchyNodeDescriptor descriptor, final Element expectedElement) {
    assertEquals(expectedElement.getAttributeValue(TEXT_ATTR_NAME), descriptor.getHighlightedText().getText());
  }

  /**
   * @see {@link com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestBase}
   */
  private static void checkChildren(final HierarchyTreeStructure treeStructure,
                                    final HierarchyNodeDescriptor descriptor,
                                    final Element element) {
    if (element.getChild(ANY_NODES_ELEMENT_NAME) != null) {
      return;
    }

    final Object[] children = treeStructure.getChildElements(descriptor);
    //noinspection unchecked
    final List<Element> expectedChildren = new ArrayList<Element>(element.getChildren(NODE_ELEMENT_NAME));

    final StringBuilder messageBuilder = new StringBuilder("Actual children of [" + descriptor.getHighlightedText().getText() + "]:\n");
    for (Object child : children) {
      final HierarchyNodeDescriptor nodeDescriptor = (HierarchyNodeDescriptor)child;
      nodeDescriptor.update();
      messageBuilder.append("    [").append(nodeDescriptor.getHighlightedText().getText()).append("]\n");
    }
    assertEquals(messageBuilder.toString(), expectedChildren.size(), children.length);

    Arrays.sort(children, new Comparator<Object>() {
      @Override
      public int compare(final Object first, final Object second) {
        return ((HierarchyNodeDescriptor)first).getHighlightedText().getText()
          .compareTo(((HierarchyNodeDescriptor)second).getHighlightedText().getText());
      }
    });

    Collections.sort(expectedChildren, new Comparator<Element>() {
      @Override
      public int compare(final Element first, final Element second) {
        return first.getAttributeValue(TEXT_ATTR_NAME).compareTo(second.getAttributeValue(TEXT_ATTR_NAME));
      }
    });

    //noinspection unchecked
    final Iterator<Element> iterator = expectedChildren.iterator();
    for (Object child : children) {
      checkNodeDescriptorRecursively(treeStructure, ((HierarchyNodeDescriptor)child), iterator.next());
    }
  }

  public static String dump(final HierarchyTreeStructure treeStructure, @Nullable HierarchyNodeDescriptor descriptor, int level) {
    StringBuilder s = new StringBuilder();
    dump(treeStructure, descriptor, level, s);
    return s.toString();
  }

  private static void dump(final HierarchyTreeStructure treeStructure,
                           @Nullable HierarchyNodeDescriptor descriptor,
                           int level,
                           StringBuilder b) {
    if (level > 10) {
      for(int i = 0; i<level; i++) b.append("  ");
      b.append("<Probably infinite part skipped>\n");
      return;
    }
    if(descriptor==null) descriptor = (HierarchyNodeDescriptor)treeStructure.getRootElement();
    for(int i = 0; i<level; i++) b.append("  ");
    descriptor.update();
    b.append("<node text=\"").append(descriptor.getHighlightedText().getText()).append("\"")
      .append(treeStructure.getBaseDescriptor() == descriptor ? " base=\"true\"" : "");

    final Object[] children = treeStructure.getChildElements(descriptor);
    if(children.length>0) {
      b.append(">\n");
      for (Object o : children) {
        HierarchyNodeDescriptor d = (HierarchyNodeDescriptor)o;
        dump(treeStructure, d, level + 1, b);
      }
      for(int i = 0; i<level; i++) b.append("  ");
      b.append("</node>\n");
    } else {
      b.append("/>\n");
    }
  }
}
