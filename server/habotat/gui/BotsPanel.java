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
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;

import kiwi.event.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;

import habotat.engine.*;
import habotat.api.*;

/**
 * @author Mark Lindner
 */

class BotsPanel extends KPanel implements ActionListener, ListSelectionListener
  {
  private KTable l_bots;
  private JButton b_new, b_delete, b_edit, b_start, b_stop;
  private BotDialog d_bot = null;
  private KListModel botModel;
  private KListModelTableAdapter model;
  private ListSelectionModel botSel;
  private PluginChooserDialog d_plugin = null;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
  private ExceptionDialog d_exception
    = new ExceptionDialog(HabotatGUI.appFrame,
                          loc.getMessage("window.title.exception"));

  /*
   */
  
  BotsPanel()
    {
    super(UIChangeManager.getInstance().getDefaultTexture());
    setBorder(KiwiUtils.defaultBorder);

    botModel = HabotatGUI.server.getBots();

    setLayout(new BorderLayout(5, 5));

    JToolBar tb = new JToolBar();
    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);
    
    add("North", tb);

    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    b_start = new JButton(kresmgr.getIcon("play.gif"));
    b_start.setEnabled(false);
    b_start.setOpaque(false);
    b_start.addActionListener(this);
    b_start.setToolTipText(loc.getMessage("tooltip.start_bot"));
    tb.add(b_start);

    b_stop = new JButton(kresmgr.getIcon("stop.gif"));
    b_stop.setEnabled(false);
    b_stop.setOpaque(false);
    b_stop.addActionListener(this);
    b_stop.setToolTipText(loc.getMessage("tooltip.stop_bot"));
    tb.add(b_stop);
    
    b_new = new JButton(kresmgr.getIcon("newdoc.gif"));
    b_new.setOpaque(false);
    b_new.addActionListener(this);
    b_new.setToolTipText(loc.getMessage("tooltip.new_bot"));
    tb.add(b_new);

    b_edit = new JButton(kresmgr.getIcon("edit.gif"));
    b_edit.setOpaque(false);
    b_edit.addActionListener(this);
    b_edit.setEnabled(false);
    b_edit.setToolTipText(loc.getMessage("tooltip.edit_bot"));
    tb.add(b_edit);
    
    b_delete = new JButton(kresmgr.getIcon("delete.gif"));
    b_delete.setOpaque(false);
    b_delete.addActionListener(this);
    b_delete.setEnabled(false);
    b_delete.setToolTipText(loc.getMessage("tooltip.delete_bot"));
    tb.add(b_delete);

    model = new KListModelTableAdapter(botModel);

    Icon i_on = kresmgr.getIcon("led-green-on.gif");
    Icon i_off = kresmgr.getIcon("led-green-off.gif");

    ToggleCellRenderer statusRenderer
      = new ToggleCellRenderer(i_on, i_off);

    l_bots = new KTable();
    l_bots.setAutoResizeMode(KTable.AUTO_RESIZE_ALL_COLUMNS);
    l_bots.setAutoCreateColumnsFromModel(true);
    l_bots.addMouseListener(new TableRowMouseAdapter()
        {
        public void rowDoubleClicked(int row, int button)
          {
          doEdit(getSelectedBot());
          }
        });
    botSel = l_bots.getSelectionModel();
    botSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    l_bots.setModel(model);
    
    l_bots.configureColumn(0, 25, 25, 25, statusRenderer, null);

    botSel.addListSelectionListener(this);
    KScrollPane sp = new KScrollPane(l_bots);
    sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setBackground(Color.white);
    
    sp.setSize(new Dimension(350, 300));
    sp.setPreferredSize(new Dimension(350, 300));
    
    add("Center", sp);
    }

  /*
   */
  
  public void valueChanged(ListSelectionEvent evt)
    {
    Object o = evt.getSource();

    if(o == botSel)
      {
      boolean empty = botSel.isSelectionEmpty();

      if(empty)
        {
        b_edit.setEnabled(false);
        b_delete.setEnabled(false);
        b_start.setEnabled(false);
        b_stop.setEnabled(false);
        }
      else
        updateButtonStates();
      }
    }

  /*
   */

  private void updateButtonStates()
    {
    BotDef bot = getSelectedBot();

    boolean active = bot.isRunning();
    
    b_edit.setEnabled(true);
    b_delete.setEnabled(! active);
    b_start.setEnabled(! active);
    b_stop.setEnabled(active);
    }

  /*
   */
  
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == b_new)
      {
      doEdit(null);
      }

    else if(o == b_edit)
      {
      doEdit(getSelectedBot());
      }

    else if(o == b_delete)
      {
      BotDef bot = getSelectedBot();
      doDelete(bot);
      }

    else if(o == b_stop)
      {
      BotDef bot = getSelectedBot();
      HabotatGUI.appFrame.showStatus(loc.getMessage("message.stopping_bot",
                                                    bot.getName()));
      KiwiUtils.busyOn(this);
      HabotatGUI.server.getBotManager().stopBot(bot);
      botModel.updateItem(bot);
      KiwiUtils.busyOff(this);
      HabotatGUI.appFrame.showStatus(loc.getMessage("message.done"));
      updateButtonStates();
      }

    else if(o == b_start)
      {
      BotDef bot = getSelectedBot();
      HabotatGUI.appFrame.showStatus(loc.getMessage("message.starting_bot",
                                                    bot.getName()));
      KiwiUtils.busyOn(this);
      try
        {
        HabotatGUI.server.getBotManager().startBot(bot);
        botModel.updateItem(bot); 
        KiwiUtils.busyOff(this);
        }
      catch(IOException ex)
        {
        KiwiUtils.busyOff(this);
        dialogs.showMessageDialog(
          KiwiUtils.getWindowForComponent(this),
          loc.getMessage("message.starting_bot_failed", ex.getMessage()));
        }
      
      HabotatGUI.appFrame.showStatus(loc.getMessage("message.done"));
      updateButtonStates();
      }    
    }

  /*
   */
  
  private void doDelete(BotDef bot)
    {
    if(bot != null)
      {
      if(dialogs.showQuestionDialog(
           KiwiUtils.getWindowForComponent(this),
           loc.getMessage("message.delete_bot_confirm", bot.getName())))
        {
        botModel.removeItem(bot);
        bot.destroyBot();
        HabotatGUI.server.saveData();
        }
      }
    }

  /*
   */
  
  private void doEdit(BotDef bot)
    {
    boolean add = false;

    if(bot == null)
      {
      if(d_plugin == null)
        d_plugin = new PluginChooserDialog(HabotatGUI.appFrame);

      KiwiUtils.cascadeWindow(this, d_plugin);
      d_plugin.setVisible(true);

      if(d_plugin.isCancelled())
        return;

      try
        {
        bot = new BotDef();
        bot.setPlugin(d_plugin.getSelectedPlugin());

        HabotatGUI.server.getBotManager().createContext(bot);

      
        bot.createBot();
        
        add = true;
        }
      catch(BotException ex)
        {
        d_exception.setException(loc.getMessage("error.bot_creation"), ex);
        KiwiUtils.centerWindow(HabotatGUI.appFrame, d_exception);
        d_exception.setVisible(true);
        return;
        }
      }
    
    if(d_bot == null)
      d_bot = new BotDialog(HabotatGUI.appFrame);
    
    KiwiUtils.cascadeWindow(this, d_bot);
    d_bot.setBot(bot);
    d_bot.setVisible(true);
    
    if(! d_bot.isCancelled())
      {
      if(add)
        botModel.addItem(bot);
      else
        botModel.updateItem(bot);

      HabotatGUI.server.saveData();
      }
    }

  /*
   */
 
  BotDef getSelectedBot()
    {
    int row = l_bots.getSelectedRow();
    if(row < 0)
      return(null);

    return((BotDef)botModel.getItemAt(row));
    }
 
  }

/* end of source file */
