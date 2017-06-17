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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;

import habotat.api.*;
import habotat.engine.*;

/**
 * @author Mark Lindner
 */

class BotDialog extends ComponentDialog
  {
  private KTextField t_name, t_keyword, t_type;
  private JTextArea t_desc;
  private JComboBox b_account;
  private ListEditor l_access;
  private JSpinner t_sessions;
  private BotDef bot = null;
  private KTabbedPane tabs;
  private KPanel p_control;
  private PluginUI ui = null;
  private boolean tabAdjusting = false, tabsInited = false;
  private OrderedListModel userModel;
  private JCheckBox b_autostart;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
  
  BotDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.bot"), true);

    setComment(loc.getMessage("window.comment.bot"));
    setResizable(false);
    }

  /*
   */

  protected Component buildDialogUI()
    {
    KPanel p_main = new KPanel();

    p_main.setLayout(new GridLayout(1, 0));

    tabs = new KTabbedPane()
        {
        protected boolean canLeaveTab(int currentTab, int newTab)
          {
          boolean ok;
          
          if(currentTab == 0)
            ok = acceptSettings();
          else
            ok = acceptControlPanel();
          
          return(ok);
          }
        };
    tabs.setOpaque(false);

    p_main.add(tabs);

    // settings tab

    KPanel p_settings = new KPanel(
      UIChangeManager.getInstance().getDefaultTexture());
    p_settings.setBorder(KiwiUtils.defaultBorder);
    tabs.addTab(loc.getMessage("heading.bot_details"),
                HabotatGUI.resmgr.getIcon("tag.gif"), p_settings);

    p_settings.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = gbc.WEST;
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weighty = 0;
    
    KLabel l = new KLabel(loc.getMessage("label.name"), SwingConstants.RIGHT);
    p_settings.add(l, gbc);

    t_name = new KTextField(10);
    t_name.setMaximumLength(32);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_name, gbc);

    l = new KLabel(loc.getMessage("label.type"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);
    
    t_type = new KTextField(20);
    t_type.setFocusable(false);
    t_type.setEditable(false);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_type, gbc);
    
    l = new KLabel(loc.getMessage("label.keyword"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);

    t_keyword = new KTextField(10);
    t_keyword.setMaximumLength(16);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    p_settings.add(t_keyword, gbc);

    l = new KLabel(loc.getMessage("label.description"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    gbc.anchor = gbc.NORTHWEST;
    gbc.fill = gbc.HORIZONTAL;
    p_settings.add(l, gbc);

    t_desc = new JTextArea(3, 25);
    KDocument doc = new KDocument();
    doc.setMaximumLength(256);
    t_desc.setDocument(doc);
    t_desc.setLineWrap(true);
    t_desc.setWrapStyleWord(true);
    KScrollPane sp = new KScrollPane(t_desc);
    sp.setSize(new Dimension(100, 70));
    sp.setMinimumSize(new Dimension(100, 70));
    sp.setOpaque(true);
    sp.setBackground(Color.white);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.weightx = 1;
    gbc.anchor = gbc.WEST;
    p_settings.add(sp, gbc);

    l = new KLabel(loc.getMessage("label.account"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    p_settings.add(l, gbc);

    b_account = new JComboBox();

    KListModelComboBoxAdapter adapter
      = new KListModelComboBoxAdapter(b_account);
    adapter.setListModel(HabotatGUI.server.getAccounts());
    b_account.setModel(adapter);

    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(b_account, gbc);

    l = new KLabel(loc.getMessage("label.max_sessions"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);

    t_sessions = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    gbc.fill = gbc.NONE;
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_sessions, gbc);

    l = new KLabel(loc.getMessage("label.access_list"), SwingConstants.RIGHT);
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.anchor = gbc.NORTHWEST;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);

    userModel = new OrderedListModel();
    l_access = new ListEditor(15, 32, userModel);
    
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    gbc.weighty = 1;
    p_settings.add(l_access, gbc);

    b_autostart = new JCheckBox(loc.getMessage("button.auto_start_bot"));
    b_autostart.setOpaque(false);
    gbc.weighty = 0;
    gbc.insets = KiwiUtils.lastBottomInsets;
    p_settings.add(b_autostart, gbc);
    
    // control panel tab

    p_control = new KPanel(UIChangeManager.getInstance().getDefaultTexture());
    p_control.setBorder(KiwiUtils.defaultBorder);
    tabs.addTab(loc.getMessage("heading.control_panel"),
                HabotatGUI.resmgr.getIcon("gears.gif"), p_control);

    p_control.setLayout(new GridLayout(1, 0));
    
    KPanel p = new KPanel();
    p.setLayout(new BorderLayout(5, 5));

    p.add("North", new JSeparator());
    p.add("Center", p_main);
    p.add("South", new JSeparator());
    
    return(p);
    }

  /*
   */
  
  protected void startFocus()
    {
    if(t_name.isEditable())
      t_name.requestFocus();
    else
      t_keyword.requestFocus();
    }

  /*
   */

  public void setVisible(boolean flag)
    {
    if(flag)
      tabs.reset();

    super.setVisible(flag);
    }

  /*
   */
  
  void setBot(BotDef bot)
    {
    this.bot = bot;

    String name = bot.getName();
    if(name == null)
      {
      setComment(loc.getMessage("window.comment.bot_new"));
      t_name.setEditable(true);
      t_name.setOpaque(true);
      t_name.setFocusable(true);
      t_name.setText("");
      }
    else
      {
      setComment(loc.getMessage("window.comment.bot", name));
      t_name.setEditable(false);
      t_name.setFocusable(false);
      t_name.setOpaque(false);
      t_name.setText(name);
      }

    t_keyword.setText(bot.getKeyword());
    t_desc.setText(bot.getDescription());

    PluginDef pdef = bot.getPlugin();
    
    t_type.setText(pdef == null ? null : pdef.toString());
    b_account.setSelectedItem(bot.getAccount());

    t_sessions.setValue(new Integer(bot.getMaxSessions()));
    b_autostart.setSelected(bot.getAutoStart());

    userModel.clear();
    
    Iterator<String> iter = bot.getUsers();
    while(iter.hasNext())
      userModel.addElement(iter.next());

    // install control panel

    p_control.removeAll();

    ui = bot.getBot().getUI();
    if(ui != null)
      {
      p_control.add(ui.getComponent());
      tabs.setEnabledAt(1, true);
      }
    else
      tabs.setEnabledAt(1, false);

    tabs.reset();
    }

  /*
   */

  protected boolean accept()
    {
    boolean ok = false;

    int tab = tabs.getSelectedIndex();
    
    if(tab == 0)
      ok = acceptSettings();
    else
      ok = acceptControlPanel();

    if(ok && (ui != null))
      {
      ui.commit();
      try
        {
        bot.saveData();
        }
      catch(Exception ex)
        {
        HabotatGUI.logError("Error saving data");
        // TODO: display a dialog box here
        ex.printStackTrace();
        }
      }

    return(ok);
    }

  /*
   */

  protected void cancel()
    {
    if(ui != null)
      ui.cancel();
    }

  /*
   */

  private boolean acceptControlPanel()
    {
    if(ui != null)
      return(ui.accept());
    else
      return(true);
    }
  
  /*
   */

  private boolean acceptSettings()
    {
    String name = t_name.getText().trim();
    String keyword = t_keyword.getText().trim();
    String desc = t_desc.getText().trim();

    if(name.equals("") || keyword.equals("")
       || (b_account.getSelectedItem() == null))
      {
      dialogs.showMessageDialog(this,
                                loc.getMessage("message.bot.required_fields"));
      return(false);
      }

    if(! keyword.matches("^[A-Za-z][A-Za-z0-9\\-_]*$"))
      {
      dialogs.showMessageDialog(this,
                                loc.getMessage("message.bot.invalid_keyword"));
      return(false);
      }

    if(t_name.isEditable())
      {
      if(HabotatGUI.server.getBots().findBot(name) != null)
        {
        dialogs.showMessageDialog(
          this, loc.getMessage("message.bot.duplicate_name", name));
        return(false);
        }

      if(! name.matches("^[A-Za-z][A-Za-z0-9\\-_]*$"))
        {
        dialogs.showMessageDialog(this,
                                  loc.getMessage("message.bot.invalid_name"));
        return(false);
        }
      }

    if(bot != null)
      {
      bot.setName(name);
      bot.setKeyword(keyword);
      bot.setDescription(desc);
      bot.setAccount((AccountDef)b_account.getSelectedItem());
      bot.setMaxSessions(((Integer)t_sessions.getValue()).intValue());
      bot.setAutoStart(b_autostart.isSelected());

      bot.clearAccessList();

      Iterator iter = userModel.iterator();
      while(iter.hasNext())
        bot.addUser((String)iter.next());
      }

    return(true);
    }

  /*
   */
  
  BotDef getBot()
    {
    return(bot);
    }

  }

/* end of source file */
