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
import java.net.*;
import javax.swing.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;

import habotat.engine.*;

/**
 *
 * @author Mark Lindner
 */

class PropertiesDialog extends ComponentDialog
  {
  private KTextField t_name, t_url;
  private JTextArea t_comment;
  private DialogSet dialogs = DialogSet.getInstance();
  private JSpinner t_timeout;
  private static LocaleData loc
    = HabotatGUI.resmgr.getResourceBundle("habotat");

  /*
   */
  
  PropertiesDialog(Frame parent)
    {
    super(parent, loc.getMessage("window.title.server_properties"), true);

    setComment(loc.getMessage("window.comment.server_properties"));
    setResizable(false);
    }

  /*
   */
  
  protected Component buildDialogUI()
    {
    KPanel p_main = new KPanel();

    p_main.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = gbc.WEST;
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.fill = gbc.HORIZONTAL;
    
    KLabel l = new KLabel(loc.getMessage("label.server_name"),
                          SwingConstants.RIGHT);
    p_main.add(l, gbc);

    t_name = new KTextField(15);
    t_name.setMaximumLength(32);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    p_main.add(t_name, gbc);

    l = new KLabel(loc.getMessage("label.website"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    p_main.add(l, gbc);

    t_url = new KTextField(30);
    t_url.setMaximumLength(128);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_main.add(t_url, gbc);

    l = new KLabel(loc.getMessage("label.comments"), SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstInsets;
    gbc.anchor = gbc.NORTHWEST;
    gbc.gridwidth = 1;
    p_main.add(l, gbc);
    
    t_comment = new JTextArea(5, 30);
    KDocument doc = new KDocument();
    doc.setMaximumLength(256);
    t_comment.setDocument(doc);
    t_comment.setLineWrap(true);
    t_comment.setWrapStyleWord(true);
    KScrollPane sp = new KScrollPane(t_comment);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_main.add(sp, gbc);

    l = new KLabel(loc.getMessage("label.session_timeout"),
                   SwingConstants.RIGHT);
    gbc.insets = KiwiUtils.firstBottomInsets;
    gbc.anchor = gbc.WEST;
    gbc.gridwidth = 1;
    p_main.add(l, gbc);

    t_timeout = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
    p_main.add(t_timeout, gbc);

    l = new KLabel(loc.getMessage("label.minutes"));
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_main.add(l, gbc);
    
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
    t_name.requestFocus();
    }

  /*
   */

  protected boolean accept()
    {
    String u = t_url.getText().trim();

    if(! u.equals(""))
      {
      try
        {
        URL url = new URL(u);
        }
      catch(MalformedURLException ex)
        {
        dialogs.showMessageDialog(this, loc.getMessage("message.url_invalid"));
        return(false);
        }
      }
    
    return(true);
    }

  /*
   */
  
  String getServerName()
    {
    return(t_name.getText().trim());
    }

  /*
   */
  
  void setServerName(String text)
    {
    t_name.setText(text);
    }

  /*
   */
  
  String getServerURL()
    {
    return(t_url.getText().trim());
    }

  /*
   */

  void setServerURL(String url)
    {
    t_url.setText(url);
    }

  /*
   */
  
  String getServerComments()
    {
    return(t_comment.getText().trim());
    }

  /*
   */

  void setServerComments(String text)
    {
    t_comment.setText(text);
    }

  /*
   */

  void setSessionTimeout(int min)
    {
    t_timeout.setValue(new Integer(min));
    }

  /*
   */

  int getSessionTimeout()
    {
    return(((Integer)t_timeout.getValue()).intValue());
    }
  
  }

/* end of source file */
