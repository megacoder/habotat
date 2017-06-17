/* ----------------------------------------------------------------------------
   Habotat
   Copyright (C) 2004-05 Mark A. Lindner

   This file is part of Habotat.
   
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 
   The author may be contacted at:
   
   mark_a_lindner@yahoo.com
   ----------------------------------------------------------------------------
*/

package habotat.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import kiwi.io.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import habotat.engine.*;

/**
 * @author Mark Lindner
 */

class AdaptersDialog extends ComponentDialog implements ActionListener,
                                             ListSelectionListener
  {
  private JList l_adapters;
  private KButton b_add, b_remove;
  private ListSelectionModel adapterSel;
  private KListModelListAdapter model;
  private KFileChooserDialog d_jarchooser = null;
  private PluginList adapterModel;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
  
  AdaptersDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.adapters"), true, false);

    setComment(loc.getMessage("window.comment.adapters"));
    setAcceptButtonText(KiwiUtils.getResourceManager()
                        .getResourceBundle("KiwiDialogs")
                        .getMessage("kiwi.button.close"));
    }

  protected Component buildDialogUI()
    {
    adapterModel = HabotatGUI.server.getAdapters();
    
    KPanel p_main = new KPanel();

    p_main.setLayout(new BorderLayout(5, 5));

    JToolBar tb = new JToolBar();
    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);
    
    p_main.add("North", tb);

    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    b_add = new KButton(kresmgr.getIcon("plus.gif"));
    b_add.setOpaque(false);
    b_add.setToolTipText(loc.getMessage("tooltip.add_adapter"));
    b_add.addActionListener(this);
    tb.add(b_add);

    b_remove = new KButton(kresmgr.getIcon("minus.gif"));
    b_remove.setEnabled(false);
    b_remove.setOpaque(false);
    b_remove.setToolTipText(loc.getMessage("tooltip.remove_adapter"));
    b_remove.addActionListener(this);
    tb.add(b_remove);

    l_adapters = new JList();

    model = new KListModelListAdapter(l_adapters);
    model.setListModel(adapterModel);
    l_adapters.setModel(model);
    
    l_adapters.setCellRenderer(new PluginCellRenderer());

    adapterSel = l_adapters.getSelectionModel();
    adapterSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    adapterSel.addListSelectionListener(this);
    
    KScrollPane sp = new KScrollPane(l_adapters);
    sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_NEVER);

    Dimension sz = new Dimension(350, 300);
    sp.setSize(sz);
    sp.setPreferredSize(sz);
    
    p_main.add("Center", sp);

    return(p_main);
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
          HabotatGUI.appFrame, loc.getMessage("window.title.select_adapter"),
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
          if(adapterModel.findPlugin(files[i]) != null)
            continue; // skip duplicates
          
          try
            {
            Plugin plugin = HabotatGUI.server.loadAdapter(files[i]);
            adapterModel.addItem(new PluginDef(files[i], plugin));
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
      {
      PluginDef plugin = getSelectedPlugin();
      if(plugin == null)
        return;

      if(plugin.isInUse())
        {
        dialogs.showMessageDialog(HabotatGUI.appFrame,
                                  loc.getMessage("message.adapter_referenced",
                                                 plugin.toString()));
        }
      else
        {
        adapterModel.removeItem(plugin);
        HabotatGUI.server.saveData();
        }
      }
    }

  /*
   */
  
  public void valueChanged(ListSelectionEvent evt)
    {
    Object o = evt.getSource();

    if(o == adapterSel)
      {
      boolean empty = adapterSel.isSelectionEmpty();

      b_remove.setEnabled(! empty);
      }
    }

  PluginDef getSelectedPlugin()
    {
    return((PluginDef)l_adapters.getSelectedValue());
    }
  
  }

/* end of source file */
