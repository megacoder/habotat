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

package habotat.bots.bidbot;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import kiwi.event.*;
import kiwi.text.*;
import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class BidBotUI extends KPanel implements PluginUI
  {
  private KTextField t_username, t_aimuser;
  private NumericField t_lag;
  private JPasswordField t_password;
  private BidBot bot;
  private DialogSet dialogs;
  private ResourceManager resmgr;
  private LocaleData loc;

  /*
   */
  
  BidBotUI(BidBot bot)
    {
    this.bot = bot;

    resmgr = new ResourceManager(getClass());
    loc = resmgr.getResourceBundle("bot");

    dialogs = DialogSet.getInstance();

    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.insets = KiwiUtils.firstInsets;
    gbc.anchor = gbc.WEST;
    gbc.weighty = 0;

    KLabel l = new KLabel(loc.getMessage("bot.label.ebay_user"),
                          SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    add(l, gbc);

    t_username = new KTextField(15);
    t_username.setMaximumLength(32);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    add(t_username, gbc);

    l = new KLabel(loc.getMessage("bot.label.ebay_password"),
                   SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    add(l, gbc);
    
    t_password = new JPasswordField(15);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    add(t_password, gbc);

    gbc.fill = gbc.HORIZONTAL;
    add(new JSeparator(), gbc);

    KLabelArea la = new KLabelArea(loc.getMessage("bot.comment.specify_user"),
                                   2, 30);
    add(la, gbc);

    l = new KLabel(loc.getMessage("bot.label.im_screenname"),
                   SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.fill = gbc.HORIZONTAL;
    add(l, gbc);

    t_aimuser = new KTextField(15);
    t_aimuser.setMaximumLength(32);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    add(t_aimuser, gbc);

    gbc.fill = gbc.HORIZONTAL;
    add(new JSeparator(), gbc);
    
    l = new KLabel(loc.getMessage("bot.label.time_adj"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstBottomInsets;
    add(l, gbc);

    t_lag = new NumericField(8, FormatConstants.DECIMAL_FORMAT);
    t_lag.setEditable(false);
    t_lag.setDecimals(3);
    t_lag.setGrouping(false);
    gbc.fill = gbc.NONE;
    add(t_lag, gbc);

    l = new KLabel(loc.getMessage("bot.label.seconds"));
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.gridwidth = gbc.REMAINDER;
    add(l, gbc);
    
    l = new KLabel("");
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = gbc.BOTH;
    add(l, gbc);
    }

  /*
   */

  public JComponent getComponent()
    {
    t_username.setText(bot.geteBayUsername());
    t_password.setText(bot.geteBayPassword());
    t_aimuser.setText(bot.getIMUser());
    t_lag.setValue((float)bot.getTimeLag() / 1000);

    t_username.requestFocus();
    
    return(this);
    }  

  /*
   */

  public boolean accept()
    {
    String user = t_username.getText().trim();
    String pw = new String(t_password.getPassword());
    String aim = t_aimuser.getText().trim();
    
    if((user.equals("")) || (pw.equals("")) || (aim.equals("")))
      {
      dialogs.showMessageDialog(KiwiUtils.getWindowForComponent(this),
                                loc.getMessage("bot.message.required_fields"));
      return(false);
      }
    
    return(true);
    }

  /*
   */
  
  public void commit()
    {
    bot.seteBayUsername(t_username.getText().trim());
    bot.seteBayPassword(new String(t_password.getPassword()));
    bot.setIMUser(t_aimuser.getText().trim());
    }

  /*
   */

  public void cancel()
    {
    /* do nothing here */
    }

  }

/* end of source file */
