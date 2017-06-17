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

package habotat.gui;

import habotat.engine.*;

import java.awt.*;
import javax.swing.*;

import kiwi.ui.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

/**
 *
 * @author Mark Lindner
 */
  
class PluginCellRenderer extends AbstractCellRenderer
  {
  private KPanel renderer;
  private KLabel l_title, l_icon;
  private KLabelArea l_desc;

  /*
   */
  
  PluginCellRenderer()
    {
    renderer = new KPanel();
    renderer.setLayout(new BorderLayout(2, 2));

    l_title = new KLabel("");
    l_title.setFont(KiwiUtils.boldFont);
    renderer.add("North", l_title);

    l_desc = new KLabelArea(3, 20);
    l_desc.setFont(KiwiUtils.plainFont);
    renderer.add("Center", l_desc);

    l_icon = new KLabel();
    renderer.add("West", l_icon);
    }

  /**
   */

  protected JComponent getCellRenderer(JComponent component, Object value,
                                       int row, int column)
    {
    PluginDef pdef = (PluginDef)value;
    Plugin plugin = pdef.getPlugin();

    l_title.setText(plugin.getName() + " v " + plugin.getVersion());
    l_title.setFont(KiwiUtils.boldFont); // why do we have to do this repeatedly?
    l_icon.setIcon(plugin.getIcon());

    l_desc.setText(plugin.getDescription());

    return(renderer);
    }
  
  }

/* end of source file */
