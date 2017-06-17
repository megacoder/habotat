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

/**
 * @author Mark Lindner
 */

class AccountsPanel extends KPanel
  implements ActionListener, ListSelectionListener
  {
  private KTable l_accounts;
  private JButton b_new, b_delete, b_edit, b_start, b_stop;
  private AccountDialog d_account = null;
  private AdapterChooserDialog d_adapter = null;
  private KListModel accountModel;
  private KListModelTableAdapter model;
  private ListSelectionModel accountSel;
  private DialogSet dialogs = DialogSet.getInstance();
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /*
   */
  
  AccountsPanel()
    {
    super(UIChangeManager.getInstance().getDefaultTexture());
    setBorder(KiwiUtils.defaultBorder);
    
    accountModel = HabotatGUI.server.getAccounts();
    
    setLayout(new BorderLayout(5, 5));

    JToolBar tb = new JToolBar();
    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);
    
    add("North", tb);
    
    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    b_start = new JButton(kresmgr.getIcon("play.gif"));
    b_start.setOpaque(false);
    b_start.addActionListener(this);
    b_start.setEnabled(false);
    b_start.setToolTipText(loc.getMessage("tooltip.sign_on"));
    tb.add(b_start);

    b_stop = new JButton(kresmgr.getIcon("stop.gif"));
    b_stop.setOpaque(false);
    b_stop.addActionListener(this);
    b_stop.setEnabled(false);
    b_stop.setToolTipText(loc.getMessage("tooltip.sign_off"));
    tb.add(b_stop);
    
    b_new = new JButton(kresmgr.getIcon("newdoc.gif"));
    b_new.setOpaque(false);
    b_new.addActionListener(this);
    b_new.setToolTipText(loc.getMessage("tooltip.new_account"));
    tb.add(b_new);

    b_edit = new JButton(kresmgr.getIcon("edit.gif"));
    b_edit.setOpaque(false);
    b_edit.addActionListener(this);
    b_edit.setEnabled(false);
    b_edit.setToolTipText(loc.getMessage("tooltip.edit_account"));
    tb.add(b_edit);
    
    b_delete = new JButton(kresmgr.getIcon("delete.gif"));
    b_delete.setOpaque(false);
    b_delete.addActionListener(this);
    b_delete.setEnabled(false);
    b_delete.setToolTipText(loc.getMessage("tooltip.remove_account"));
    tb.add(b_delete);
    
    model = new KListModelTableAdapter(accountModel);

    Icon i_on = kresmgr.getIcon("led-green-on.gif");
    Icon i_off = kresmgr.getIcon("led-green-off.gif");
    
    ToggleCellRenderer statusRenderer
      = new ToggleCellRenderer(i_on, i_off);
    
    l_accounts = new KTable();
    l_accounts.setAutoResizeMode(KTable.AUTO_RESIZE_ALL_COLUMNS);
    l_accounts.setAutoCreateColumnsFromModel(true);
    l_accounts.addMouseListener(new TableRowMouseAdapter()
        {
        public void rowDoubleClicked(int row, int button)
          {
          doEdit(getSelectedAccount());
          }
        });
    accountSel = l_accounts.getSelectionModel();
    accountSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    l_accounts.setModel(model);
    
    l_accounts.configureColumn(0, 25, 25, 25, statusRenderer, null);
    l_accounts.configureColumn(1, 175, 175, 175);
    
    accountSel.addListSelectionListener(this);
    KScrollPane sp = new KScrollPane(l_accounts);
    sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setBackground(Color.white);

    sp.setSize(new Dimension(300, 300));
    sp.setPreferredSize(new Dimension(300, 300));
    
    add("Center", sp);
    }

  /*
   */
  
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == b_start)
      {
      startBots(getSelectedAccount());
      updateButtonStates();
      }

    else if(o == b_stop)
      {
      stopBots(getSelectedAccount());
      updateButtonStates();
      }
    
    else if(o == b_new)
      doEdit(null);
    
    else if(o == b_edit)
      doEdit(getSelectedAccount());
    
    else if(o == b_delete)
      doDelete(getSelectedAccount());
    }

  /*
   */

  private void startBots(AccountDef account)
    {
    HabotatGUI.appFrame.showStatus(loc.getMessage("message.starting_bots",
                                                  account.getScreenName()));
    KiwiUtils.busyOn(this);
    try
      {
      HabotatGUI.server.getBotManager().startBots(account);
      accountModel.updateItem(account);
      KiwiUtils.busyOff(this);
      }
    catch(IOException ex)
      { 
      KiwiUtils.busyOff(this);
      dialogs.showMessageDialog(
          KiwiUtils.getWindowForComponent(this),
          loc.getMessage("message.starting_bots_failed", ex.getMessage()));
      }
    
    HabotatGUI.appFrame.showStatus(loc.getMessage("message.done"));
    }

  /*
   */

  private void stopBots(AccountDef account)
    {
    HabotatGUI.appFrame.showStatus(loc.getMessage("message.starting_bots",
                                                  account.getScreenName()));
    KiwiUtils.busyOn(this);
    HabotatGUI.server.getBotManager().stopBots(account);
    accountModel.updateItem(account);
    KiwiUtils.busyOff(this);
    HabotatGUI.appFrame.showStatus(loc.getMessage("message.done"));
    }
  
  /*
   */
  
  private void doDelete(AccountDef account)
    {
    if(account != null)
      {
      if(account.isReferenced())
        {
        dialogs.showMessageDialog(
          KiwiUtils.getWindowForComponent(this),
          loc.getMessage("message.account_referenced",
                         account.getScreenName()));
        }
      else if(dialogs.showQuestionDialog(
                KiwiUtils.getWindowForComponent(this),
                loc.getMessage("message.account_delete_confirm",
                               account.getScreenName())))
        {
        accountModel.removeItem(account);
        HabotatGUI.server.saveData();
        }
      }
    }

  /*
   */

  private void doEdit(AccountDef account)
    {
    boolean add = false;

    if(account == null)
      {
      if(d_adapter == null)
        d_adapter = new AdapterChooserDialog(HabotatGUI.appFrame);

      KiwiUtils.cascadeWindow(this, d_adapter);
      d_adapter.setVisible(true);

      if(d_adapter.isCancelled())
        return;
      
      account = new AccountDef();
      account.setPlugin(d_adapter.getSelectedPlugin());
      
      add = true;
      }
    
    if(d_account == null)
      d_account = new AccountDialog(HabotatGUI.appFrame);
    
    KiwiUtils.cascadeWindow(this, d_account);
    d_account.setAccount(account);
    d_account.setVisible(true);
    
    if(! d_account.isCancelled())
      {
      if(add)
        accountModel.addItem(account);
      else
        accountModel.updateItem(account);

      HabotatGUI.server.saveData();
      }
    }

  /*
   */
  
  AccountDef getSelectedAccount()
    {
    int row = l_accounts.getSelectedRow();
    if(row < 0)
      return(null);

    return((AccountDef)accountModel.getItemAt(row));
    }

  /*
   */

  private void updateButtonStates()
    {
    AccountDef account = getSelectedAccount();
    
    boolean connected = account.isConnected();
    
    b_start.setEnabled(! connected);
    b_stop.setEnabled(connected);
    b_delete.setEnabled(! connected);
    }

  /*
   */
  
  public void valueChanged(ListSelectionEvent evt)
    {
    Object o = evt.getSource();

    if(o == accountSel)
      {
      boolean empty = accountSel.isSelectionEmpty();

      b_edit.setEnabled(! empty);

      if(empty)
        {
        b_start.setEnabled(false);
        b_stop.setEnabled(false);
        b_delete.setEnabled(false);
        }
      else
        updateButtonStates();
      }
    }
  
  }

/* end of source file */
