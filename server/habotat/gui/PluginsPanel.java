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
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import kiwi.event.*;
import kiwi.io.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import habotat.engine.*;

/**
 *
 * @author Mark Lindner
 */

class PluginsPanel extends KPanel
  implements ActionListener, ListSelectionListener
  {
  private JList l_plugins;
  private JButton b_add, b_remove, b_reload;
  private PluginList pluginModel;
  private KListModelListAdapter model;
  private ListSelectionModel pluginSel;
  private KFileChooserDialog d_jarchooser = null;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
  private ExceptionDialog d_exception
    = new ExceptionDialog(HabotatGUI.appFrame,
                          loc.getMessage("window.title.exception"));

  /*
   */
  
  PluginsPanel()
    {
    super(UIChangeManager.getInstance().getDefaultTexture());
    setBorder(KiwiUtils.defaultBorder);

    pluginModel = HabotatGUI.server.getPlugins();

    setLayout(new BorderLayout(5, 5));

    JToolBar tb = new JToolBar();

    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);
    
    add("North", tb);

    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    b_add = new JButton(kresmgr.getIcon("plus.gif"));
    b_add.setOpaque(false);
    b_add.addActionListener(this);
    b_add.setToolTipText(loc.getMessage("tooltip.add_plugin"));
    tb.add(b_add);

    b_remove = new JButton(kresmgr.getIcon("minus.gif"));
    b_remove.setOpaque(false);
    b_remove.addActionListener(this);
    b_remove.setEnabled(false);
    b_remove.setToolTipText(loc.getMessage("tooltip.remove_plugin"));
    tb.add(b_remove);

    b_reload = new JButton(HabotatGUI.resmgr.getIcon("reload.gif"));
    b_reload.setOpaque(false);
    b_reload.addActionListener(this);
    b_reload.setEnabled(false);
    b_reload.setToolTipText(loc.getMessage("tooltip.reload_plugin"));
    tb.add(b_reload);

    l_plugins = new JList();
    model = new KListModelListAdapter(l_plugins);
    model.setListModel(pluginModel);
    
    pluginSel = l_plugins.getSelectionModel();
    pluginSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    l_plugins.setModel(model);

    l_plugins.setCellRenderer(new PluginCellRenderer());

    pluginSel.addListSelectionListener(this);
    KScrollPane sp = new KScrollPane(l_plugins);
    sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_NEVER);

    Dimension sz = new Dimension(300, 300);
    sp.setSize(sz);
    sp.setPreferredSize(sz);
    
    add("Center", sp);
    }

  /*
   */
  
  public void valueChanged(ListSelectionEvent evt)
    {
    Object o = evt.getSource();

    if(o == pluginSel)
      {
      boolean empty = pluginSel.isSelectionEmpty();

      b_remove.setEnabled(! empty);
      b_reload.setEnabled(! empty);
      }
    }

  /*
   */

  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == b_add)
      {
      if(d_jarchooser == null)
        {
        d_jarchooser = new KFileChooserDialog(
          HabotatGUI.appFrame,
          loc.getMessage("window.title.select_plugin_file"),
          KFileChooser.OPEN_DIALOG);
        KFileChooser chooser = d_jarchooser.getFileChooser();
        FileExtensionFilter filter = new FileExtensionFilter(
          "jar", loc.getMessage("label.jar_files"));
        chooser.addChoosableFileFilter(filter);
        }

      KiwiUtils.cascadeWindow(this, d_jarchooser);
      d_jarchooser.setVisible(true);

      if(!d_jarchooser.isCancelled())
        {
        File files[] = d_jarchooser.getSelectedFiles();

        for(int i = 0; i < files.length; i++)
          {
          if(pluginModel.findPlugin(files[i]) != null)
            continue; // skip duplicates
          
          try
            {
            Plugin plugin = HabotatGUI.server.loadPlugin(files[i]);
            pluginModel.addItem(new PluginDef(files[i], plugin));
            }
          catch(Exception ex)
            {
            dialogs.showMessageDialog(HabotatGUI.appFrame,
                                      loc.getMessage("message.jar_not_adapter",
                                                     files[i]));
            }
          }
        
        }

      HabotatGUI.server.saveData();
      }

    else if(o == b_remove)
      doDelete(getSelectedPlugin());

    else if(o == b_reload)
      doReload(getSelectedPlugin());
    }

  /*
   */

  private void doDelete(PluginDef plugin)
    {
    if(plugin != null)
      {
      if(plugin.isReferenced())
        {
        dialogs.showMessageDialog(
           KiwiUtils.getWindowForComponent(this),
           loc.getMessage("message.plugin_referenced", plugin.toString()));
        }
      else if(dialogs.showQuestionDialog(
                KiwiUtils.getWindowForComponent(this),
                loc.getMessage("message.remove_plugin_confirm")))
        {
        pluginModel.removeItem(plugin);
        HabotatGUI.server.saveData();
        }
      }
    }

  /*
   */

  private void doReload(PluginDef plugin)
    {
    if(plugin != null)
      {
      if(plugin.isInUse())
        {
        dialogs.showMessageDialog(
          KiwiUtils.getWindowForComponent(this),
          loc.getMessage("message.plugin_active", plugin.toString()));
        }
      
      else if(dialogs.showQuestionDialog(
                KiwiUtils.getWindowForComponent(this),
                loc.getMessage("message.reload_plugin_confirm",
                               plugin.toString())))
        {
        try
          {
          plugin.reload();
          pluginModel.updateItem(plugin);
          HabotatGUI.server.saveData();
          }
        catch(Exception ex)
          {
          d_exception.setException(loc.getMessage("error.plugin_reload"), ex);
          KiwiUtils.centerWindow(HabotatGUI.appFrame, d_exception);
          d_exception.setVisible(true);
          }
        }
      }
    }

  /*
   */
  
  PluginDef getSelectedPlugin()
    {
    return((PluginDef)l_plugins.getSelectedValue());
    }
  
  }

/* end of source file */
