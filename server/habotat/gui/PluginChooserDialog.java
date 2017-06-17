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

import java.awt.*;
import javax.swing.*;

import kiwi.event.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;

import habotat.engine.*;

/**
 *
 * @author Mark Lindner
 */

class PluginChooserDialog extends ComponentDialog
  {
  private JList l_plugins;
  private ListSelectionModel pluginSel;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /*
   */
  
  PluginChooserDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.select_bot_type"), true, true);
    
    setComment(loc.getMessage("window.comment.select_bot_type"));
    }

  /*
   */
  
  protected Component buildDialogUI()
    {
    l_plugins = new JList();

    l_plugins.addMouseListener(new ListItemMouseAdapter()
        {
        public void itemDoubleClicked(int item, int button)
          {
          doAccept();
          }
        });

    KListModel pluginModel = HabotatGUI.server.getPlugins();
    KListModelListAdapter model = new KListModelListAdapter(l_plugins);
    model.setListModel(pluginModel);
    
    pluginSel = l_plugins.getSelectionModel();
    pluginSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    l_plugins.setModel(model);
    
    l_plugins.setCellRenderer(new PluginCellRenderer());

    KScrollPane sp = new KScrollPane(l_plugins);
    Dimension sz = new Dimension(300, 300);
    sp.setSize(sz);
    sp.setPreferredSize(sz);
    
    return(sp);
    }

  /*
   */

  protected boolean accept()
    {
    boolean empty = pluginSel.isSelectionEmpty();

    if(empty)
      dialogs.showMessageDialog(this,
                                loc.getMessage("message.nothing_selected"));
    
    return(! empty);
    }

  /*
   */

  PluginDef getSelectedPlugin()
    {
    return((PluginDef)l_plugins.getSelectedValue());
    }
  
  }

/* end of source file */
