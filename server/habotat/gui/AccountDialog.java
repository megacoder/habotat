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
import javax.swing.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;
import kiwi.util.plugin.*;

import habotat.api.*;
import habotat.engine.*;

/**
 * @author Mark Lindner
 */

class AccountDialog extends ComponentDialog
  {
  private KTextField t_account, t_protocol;
  private JPasswordField t_passwd;
  private JTextArea t_comment;
  private AccountDef account = null;
  private KTabbedPane tabs;
  private KPanel p_adapter;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");
  private PluginUI ui = null;
  
  /*
   */
  
  AccountDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.account"), true);

    setComment(loc.getMessage("window.comment.account"));
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
          if(! AccountDialog.this.isVisible())
            return(true);
          
          boolean ok;
          
          if(currentTab == 0)
            ok = acceptSettings();
          else
            ok = acceptAdapterSettings();
          
          return(ok);
          }
        };
    tabs.setOpaque(false);

    p_main.add(tabs);

    // settings tab
    
    KPanel p_settings = new KPanel(
      UIChangeManager.getInstance().getDefaultTexture());
    p_settings.setBorder(KiwiUtils.defaultBorder);
    tabs.addTab(loc.getMessage("heading.account_details"),
                HabotatGUI.resmgr.getIcon("tag.gif"), p_settings);

    p_settings.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = gbc.WEST;
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.fill = gbc.HORIZONTAL;

    KLabel l = new KLabel(loc.getMessage("label.protocol"),
                          SwingConstants.RIGHT);
    p_settings.add(l, gbc);

    t_protocol = new KTextField(10);
    t_protocol.setEditable(false);
    t_protocol.setFocusable(false);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_protocol, gbc);
    
    l = new KLabel(loc.getMessage("label.screen_name"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    p_settings.add(l, gbc);

    t_account = new KTextField(10);
    t_account.setMaximumLength(32);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_account, gbc);

    l = new KLabel(loc.getMessage("label.password"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);

    t_passwd = new JPasswordField(10);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(t_passwd, gbc);

    l = new KLabel(loc.getMessage("label.comments"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstBottomInsets;
    gbc.anchor = gbc.NORTHWEST;
    gbc.gridwidth = 1;
    p_settings.add(l, gbc);
    
    t_comment = new JTextArea(5, 30);
    KDocument doc = new KDocument();
    doc.setMaximumLength(256);
    t_comment.setDocument(doc);
    t_comment.setLineWrap(true);
    t_comment.setWrapStyleWord(true);
    KScrollPane sp = new KScrollPane(t_comment);
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_settings.add(sp, gbc);

    // adapter settings tab

    p_adapter = new KPanel(
      UIChangeManager.getInstance().getDefaultTexture());
    p_adapter.setLayout(new GridLayout(1, 0));
    p_adapter.setBorder(KiwiUtils.defaultBorder);
    tabs.addTab(loc.getMessage("heading.adapter_config"),
                HabotatGUI.resmgr.getIcon("gears.gif"), p_adapter);
    
    //

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
    if(t_account.isEditable())
      t_account.requestFocus();
    else
      t_passwd.requestFocus();
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
  
  void setAccount(AccountDef account)
    {
    this.account = account;
    
    String name = account.getScreenName();
    
    if(name == null)
      {
      setComment(loc.getMessage("window.comment.account_new"));
      t_account.setEditable(true);
      t_account.setFocusable(true);
      t_account.setOpaque(true);
      t_account.setText("");
      }
    else
      {
      setComment(loc.getMessage("window.comment.account", name));
      t_account.setEditable(false);
      t_account.setFocusable(false);
      t_account.setOpaque(false);
      t_account.setText(name);
      }

    t_protocol.setText(account.getPlugin().getName());
    
    String passwd = account.getPassword();
    t_passwd.setText(passwd == null ? "" : passwd);

    String comment = account.getComment();
    t_comment.setText(comment == null ? "" : comment);

    // install adapter UI

    p_adapter.removeAll();

    IMAdapter adapter = account.getAdapter();
    if(adapter != null)
      {
      ui = adapter.getUI();

      if(ui != null)
        {
        p_adapter.add(ui.getComponent());
        tabs.setEnabledAt(1, true);
        }
      else
        tabs.setEnabledAt(1, false);
      }
    else
      tabs.setEnabledAt(1, false);
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
      ok = acceptAdapterSettings();

    if(ok && (ui != null))
      {
      ui.commit();

      // save the data (somewhere)
      }

    return(ok);
    }

  /*
   */

  private boolean acceptSettings()
    {
    String name = t_account.getText().trim();
    String passwd = new String(t_passwd.getPassword());
    String comment = t_comment.getText().trim();

    if(name.equals("") || passwd.equals(""))
      {
      System.out.println("dialogs: " + dialogs);
      
      dialogs.showMessageDialog(
        this, loc.getMessage("message.account.required_fields"));
      return(false);
      }

    if(t_account.isEditable())
      {
      AccountDef acct = HabotatGUI.server.getAccounts().findAccount(
        name, account.getPlugin());

      if(acct != null)
        {
        dialogs.showMessageDialog(
          this, loc.getMessage("message.account.duplicate_name", name));
        return(false);
        }
      }
    
    if(account != null)
      {
      account.setScreenName(name);
      account.setPassword(passwd);
      account.setComment(comment);
      }

    return(true);
    }

  private boolean acceptAdapterSettings()
    {
    if(ui != null)
      return(ui.accept());
    else
      return(true);
    }

  /*
   */
  
  AccountDef getAccount()
    {
    return(account);
    }

  }

/* end of source file */
