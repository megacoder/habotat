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
import kiwi.ui.model.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

import habotat.engine.*;

/**
 * @author Mark Lindner
 */

class ConsoleDialog extends ComponentDialog
  implements ActionListener, KListModelListener
  {
  private JTabbedPane tabs;
  private KButton b_clear;
  private ConsolePanel p_sysConsole;
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  ConsoleDialog(Frame parent, String title)
    {
    super(parent, title, false, false);

    setComment(null);

    setAcceptButtonText(KiwiUtils.getResourceManager()
                        .getResourceBundle("KiwiDialogs")
                        .getMessage("kiwi.button.close"));
    }

  /*
   */

  protected Component buildDialogUI()
    {
    KPanel p_main = new KPanel();

    p_main.setLayout(new BorderLayout(5, 5));

    ResourceManager kresmgr = KiwiUtils.getResourceManager();
    
    b_clear = new KButton(KiwiUtils.getResourceManager()
                          .getResourceBundle("KiwiDialogs")
                          .getMessage("kiwi.button.clear"));
    b_clear.addActionListener(this);
    addButton(b_clear);

    tabs = new KTabbedPane();
    tabs.setFont(KiwiUtils.boldFont);
    Dimension dim = new Dimension(500, 400);
    tabs.setSize(dim);
    tabs.setPreferredSize(dim);
    tabs.setTabPlacement(JTabbedPane.LEFT);

    p_main.add("Center", tabs);

    p_sysConsole = new ConsolePanel();
    p_sysConsole.setTimestamps(true);
    
    tabs.addTab(loc.getMessage("heading.system"),
                KiwiUtils.getResourceManager().getIcon("monitor.gif"),
                p_sysConsole);
    tabs.setBackgroundAt(0, Color.white);

    HabotatGUI.log = p_sysConsole;
    HabotatGUI.server.log = p_sysConsole;

    HabotatGUI.logInfo(loc.getMessage("message.console_initialized"));
    
    return(p_main);
    }

  /*
   */
  
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == b_clear)
      {
      ConsolePanel console = (ConsolePanel)tabs.getSelectedComponent();
      console.clear();
      }
    }

  /*
   */

  private void createConsole(BotDef bot, int index)
    {
    ConsolePanel console = new ConsolePanel();
    console.setTimestamps(true);
    bot.setLoggingEndpoint(console);
    String name = bot.getName();
    tabs.insertTab(name, null, console, null, index + 1);
    }

  /*
   */

  public void itemsAdded(KListModelEvent evt)
    {
    int index = evt.getIndex();
    int endIndex = evt.getEndIndex();

    for(int i = index; i <= endIndex; i++)
      {
      BotDef bot = HabotatGUI.server.getBots().getItemAt(i);
      createConsole(bot, index);
      }
    }

  /*
   */

  public void itemsChanged(KListModelEvent evt)
    {
    }

  /*
   */

  public void itemsRemoved(KListModelEvent evt)
    {
    int index = evt.getIndex();
    int ct = evt.getEndIndex() - index;

    for(int i = ++index; ct >= 0; ct--)
      tabs.removeTabAt(i);
    }

  public void dataChanged(KListModelEvent evt)
    {

    for(int i = 1; i < tabs.getTabCount(); i++)
      tabs.removeTabAt(1);

    int len =  HabotatGUI.server.getBots().getItemCount();

    for(int i = 0; i < len; i++)
      {
      BotDef bot = HabotatGUI.server.getBots().getItemAt(i);
      createConsole(bot, i);
      }
    }

  }

/* end of source file */
