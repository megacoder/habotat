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

import kiwi.event.*;
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

class HabotatFrame extends KFrame implements ActionListener
  {
  private StatusBar statusBar;
  private KTable l_bots;
  private JMenuItem mi_exit, mi_prefs, mi_props, mi_console, mi_adapters,
    mi_about;
  private JTabbedPane tabs;
  private DialogSet dialogs;
  private PropertiesDialog d_props = null;
  private PreferencesDialog d_prefs = null;
  private ConsoleDialog d_console = null;
  private AdaptersDialog d_adapters = null;
  private AboutFrame d_about = null;
  private KListModel bots;
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /*
   */
  
  HabotatFrame()
    {
    super(loc.getMessage("window.title.habotat"));

    dialogs = new DialogSet(this, DialogSet.CASCADE_PLACEMENT);
    
    JMenuBar mb = new JMenuBar();

    JMenu m_file = new JMenu(loc.getMessage("menu.file"));
    mb.add(m_file);

    mi_console = new JMenuItem(loc.getMessage("menu.item.open_console"));
    mi_console.addActionListener(this);
    m_file.add(mi_console);

    m_file.add(new JSeparator());

    mi_about = new JMenuItem(loc.getMessage("menu.item.about"));
    mi_about.addActionListener(this);
    m_file.add(mi_about);
    
    m_file.add(new JSeparator());
    
    mi_exit = new JMenuItem(loc.getMessage("menu.item.exit"));
    mi_exit.addActionListener(this);
    m_file.add(mi_exit);

    JMenu m_edit = new JMenu(loc.getMessage("menu.edit"));
    mb.add(m_edit);

    mi_adapters = new JMenuItem(loc.getMessage("menu.item.im_adapters"));
    mi_adapters.addActionListener(this);
    m_edit.add(mi_adapters);
    
    mi_props = new JMenuItem(loc.getMessage("menu.item.server_props"));
    mi_props.addActionListener(this);
    m_edit.add(mi_props);

    m_edit.add(new JSeparator());

    mi_prefs = new JMenuItem(loc.getMessage("menu.item.prefs"));
    mi_prefs.addActionListener(this);
    m_edit.add(mi_prefs);
    
    setMenuBar(mb);
    
    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    KPanel p_main = getMainContainer();
    p_main.setBorder(KiwiUtils.defaultBorder);
    p_main.setLayout(new BorderLayout(5, 5));

    tabs = new JTabbedPane();
    tabs.setOpaque(false);

    p_main.add("Center", tabs);

    // accounts
    
    AccountsPanel p_accounts = new AccountsPanel();
    tabs.addTab(loc.getMessage("heading.accounts"),
                HabotatGUI.resmgr.getIcon("users.gif"), p_accounts);

    // plugins

    PluginsPanel p_plugins = new PluginsPanel();
    tabs.addTab(loc.getMessage("heading.plugins"),
                KiwiUtils.getResourceManager().getIcon("plugin.gif"),
                p_plugins);

    // bots
    
    BotsPanel p_bots = new BotsPanel();
    tabs.addTab(loc.getMessage("heading.bots"),
                HabotatGUI.resmgr.getIcon("module.gif"), p_bots);

    statusBar = new StatusBar();
    p_main.add("South", statusBar);

    pack();

    // other stuff

    KiwiUtils.busyOn(this);
    d_console = new ConsoleDialog(KiwiUtils.getPhantomFrame(),
                                  loc.getMessage("window.title.bot_console"));
    KiwiUtils.busyOff(this);

    if(HabotatGUI.config.getBoolean(HabotatGUI.CK_CONSOLE))
      d_console.setVisible(true);

    bots = HabotatGUI.server.getBots();
    bots.addListModelListener(d_console);

    HabotatGUI.server.loadData();
    
    showStatus(loc.getMessage("message.welcome"));
    }

  /*
   */
  
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == mi_props)
      {
      if(d_props == null)
        {
        KiwiUtils.busyOn(this);
        d_props = new PropertiesDialog(this);
        KiwiUtils.busyOff(this);
        }

      d_props.setServerName(HabotatGUI.server.getName());
      d_props.setServerURL(HabotatGUI.server.getURL());
      d_props.setServerComments(HabotatGUI.server.getComments());
      
      KiwiUtils.cascadeWindow(this, d_props);
      d_props.setVisible(true);
      if(! d_props.isCancelled())
        {
        HabotatGUI.server.setName(d_props.getServerName());
        HabotatGUI.server.setURL(d_props.getServerURL());
        HabotatGUI.server.setComments(d_props.getServerComments());
        showStatus(loc.getMessage("message.server_props_updated"));
        }
      }

    else if(o == mi_prefs)
      {
      if(d_prefs == null)
        {
        KiwiUtils.busyOn(this);
        d_prefs = new PreferencesDialog(this);
        KiwiUtils.busyOff(this);
        }

      KiwiUtils.cascadeWindow(this, d_prefs);
      d_prefs.setVisible(true);
      if(! d_prefs.isCancelled())
        {
        HabotatGUI.saveConfig();
        showStatus(loc.getMessage("message.prefs_updated"));
        }
      }

    else if(o == mi_adapters)
      {
      if(d_adapters == null)
        {
        KiwiUtils.busyOn(this);
        d_adapters = new AdaptersDialog(this);
        KiwiUtils.busyOff(this);
        }

      KiwiUtils.cascadeWindow(this, d_adapters);
      d_adapters.setVisible(true);
      }

    else if(o == mi_about)
      {
      if(d_about == null)
        {
        KiwiUtils.busyOn(this);
        d_about = new AboutFrame(loc.getMessage("window.title.about"),
                                 HabotatGUI.resmgr.getURL("habotat.html"));
        d_about.setSize(new Dimension(400, 500));
        d_about.setResizable(false);
        KiwiUtils.busyOff(this);
        }

      KiwiUtils.cascadeWindow(this, d_about);
      d_about.setVisible(true);
      }

    else if(o == mi_console)
      d_console.setVisible(true);

    else if(o == mi_exit)
      {
      if(confirmExit())
        {
        HabotatGUI.server.getBotManager().stopAllBots();
        HabotatGUI.shutdown();
        }
      }
    }

  /*
   */

  public void showStatus(String text)
    {
    statusBar.setText(text);
    }

  /*
   */
  
  private boolean confirmExit()
    {
    return(dialogs.showQuestionDialog(loc.getMessage("message.exit_confirm")));
    }

  /*
   */
  
  protected boolean canClose()
    {
    return(confirmExit());
    }

  }

/* end of source file */
