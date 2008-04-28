package com.intellij.xml.index;

import com.intellij.util.xml.NanoXmlUtil;
import com.intellij.util.NullableFunction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class XsdTagNameBuilder extends NanoXmlUtil.IXMLBuilderAdapter {

  @Nullable
  public static Collection<String> computeTagNames(final InputStream is) {
    try {
      final XsdTagNameBuilder builder = new XsdTagNameBuilder();
      NanoXmlUtil.parse(is, builder);
      return builder.myTagNames;
    }
    finally {
      try {
        if (is != null) {
          is.close();
        }
      }
      catch (IOException e) {
        // can never happen
      }
    }
  }

  @Nullable
  public static Collection<String> computeTagNames(final VirtualFile file) {
    return VfsUtil.processInputStream(file, new NullableFunction<InputStream, Collection<String>>() {
      public Collection<String> fun(final InputStream inputStream) {
        return computeTagNames(inputStream);
      }
    });
  }

  private final Collection<String> myTagNames = new ArrayList<String>();
  private boolean myElementStarted;

  public void startElement(@NonNls final String name, @NonNls final String nsPrefix, @NonNls final String nsURI, final String systemID, final int lineNr)
      throws Exception {

    myElementStarted = nsPrefix != null && nsURI.equals("http://www.w3.org/2001/XMLSchema") && name.equals("element");
  }

  public void addAttribute(@NonNls final String key, final String nsPrefix, final String nsURI, final String value, final String type)
      throws Exception {
    if (myElementStarted && key.equals("name")) {
      myTagNames.add(value);
      myElementStarted = false;
    }
  }
}
