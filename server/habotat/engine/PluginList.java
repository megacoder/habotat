/* ----------------------------------------------------------------------------
   Habotat
   Copyright (C) 2004-05 Mark A. Lindner

   This file is part of Habotat.
   
   This program is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at your
   option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 
   The author may be contacted at <mark_a_lindner@yahoo.com>
   ----------------------------------------------------------------------------
*/

package habotat.engine;

import java.io.*;
import java.util.*;

import org.jdom.*;

import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

/**
 *
 * @author Mark Lindner
 */

public class PluginList extends DefaultKListModel<PluginDef>
  {
  private Document document;
  private Element baseNode;
  private PluginLocator pluginLocator;
  private static final String ATTR_FILE = "file";
  private String groupNode, itemNode, pluginType;

  /*
   */
  
  PluginList(String groupNode, String itemNode, String pluginType)
    {
    this.groupNode = groupNode;
    this.itemNode = itemNode;
    this.pluginType = pluginType;
    }

  /*
   */

  public void addItem(PluginDef pdef)
    {
    int i;
    String name = pdef.getName();
    
    for(i = 0; i < getItemCount(); i++)
      {
      PluginDef p = (PluginDef)getItemAt(i);

      int d = p.getName().compareToIgnoreCase(name);
      if(d >= 0)
        break;
      }

    super.insertItemAt(pdef, i);
    }

  /*
   */

  void read(Document document, PluginLocator locator)
    {
    this.document = document;
    this.pluginLocator = locator;
    Element root = document.getRootElement();
    baseNode = root.getChild(groupNode);

    if(baseNode == null)
      {
      baseNode = new Element(groupNode);
      root.addContent(baseNode);
      }

    clear();

    List list = baseNode.getChildren(itemNode);
    Iterator iter = list.iterator();
    while(iter.hasNext())
      {
      Element node = (Element)iter.next();

      String file = node.getAttributeValue(ATTR_FILE);

      try
        {
        Plugin plugin = pluginLocator.loadPlugin(file, pluginType);
        PluginDef pdef = new PluginDef(new File(file), plugin);
        addItem(pdef);
        }
      catch(PluginException ex)
        {
        // @@ need to communicate this to the GUI somehow...
        ex.printStackTrace();
        }
      }
    }

  /*
   */

  void write()
    {
    baseNode.getChildren().clear();

    Iterator<PluginDef> iter = iterator();
    while(iter.hasNext())
      {
      PluginDef pdef = iter.next();

      Element node = new Element(itemNode);
      node.setAttribute(ATTR_FILE, pdef.getFile().getAbsolutePath());

      baseNode.addContent(node);
      }
    }

  /*
   */

  public PluginDef findPlugin(String name)
    {
    Iterator<PluginDef> iter = iterator();
    while(iter.hasNext())
      {
      PluginDef pdef = iter.next();

      if(pdef.getName().equals(name))
        return(pdef);
      }

    return(null);
    }

  /*
   */
  
  public PluginDef findPlugin(File file)
    {
    Iterator<PluginDef> iter = iterator();
    while(iter.hasNext())
      {
      PluginDef pdef = iter.next();

      if(pdef.getFile().equals(file))
        return(pdef);
      }

    return(null);
    }
  
  }

/* end of source file */
