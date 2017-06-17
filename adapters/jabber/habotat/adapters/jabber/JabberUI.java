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

package habotat.adapters.jabber;

import java.awt.*;
import javax.swing.*;

import kiwi.text.*;
import kiwi.ui.*;
import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class JabberUI implements PluginUI
  {
  private JabberIMAdapter adapter;
  private KPanel panel;
  private KTextField t_host, t_service;
  private NumericField t_port;
  
  JabberUI(JabberIMAdapter adapter)
    {
    this.adapter = adapter;

    panel = new KPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = gbc.WEST;

    KLabel l = new KLabel("Server Host:", SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = KiwiUtils.firstInsets;
    panel.add(l, gbc);

    t_host = new KTextField(20);
    t_host.setMaximumLength(128);
    t_host.disableChars('\0', ',');
    t_host.disableChars('/', '/');
    t_host.disableChars(':', '@');
    t_host.disableChars('[', '^');
    t_host.disableChars('`', '`');
    t_host.disableChars('{', '~');
    
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastInsets;
    gbc.fill = gbc.NONE;
    panel.add(t_host, gbc);
    
    l = new KLabel("Port:", SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = KiwiUtils.firstInsets;
    panel.add(l, gbc);

    t_port = new NumericField(5, FormatConstants.INTEGER_FORMAT);
    t_port.setInputRequired(true);
    t_port.setGrouping(false);
    t_port.setMinValue(1);
    t_port.setMaxValue(65535);
    t_port.setMaximumLength(5);
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastInsets;
    gbc.fill = gbc.NONE;
    panel.add(t_port, gbc);

    l = new KLabel("Service Name:", SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = KiwiUtils.firstBottomInsets;
    panel.add(l, gbc);
    
    t_service = new KTextField(20);
    t_service.setMaximumLength(128);
    t_service.disableChars('\0', ',');
    t_service.disableChars('/', '/');
    t_service.disableChars(':', '@');
    t_service.disableChars('[', '^');
    t_service.disableChars('`', '`');
    t_service.disableChars('{', '~');
    
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.fill = gbc.NONE;
    panel.add(t_service, gbc);

    l = new KLabel();
    gbc.weightx = gbc.weighty = 1;
    gbc.fill = gbc.BOTH;
    panel.add(l, gbc);
    }

  public JComponent getComponent()
    {
    t_host.setText(adapter.getHost());
    t_port.setValue(adapter.getPort());
    t_service.setText(adapter.getService());
    
    return(panel);
    }

  public boolean accept()
    {
    String host = t_host.getText().trim();
    if(t_host.isEmpty())
      {
      // TODO: error dialog
      return(false);
      }

    String service = t_service.getText().trim();
    if(t_service.isEmpty())
      {
      // TODO: error dialog
      return(false);
      }
    
    if(! t_port.validateInput())
      {
      // TODO: error dialog
      return(false);
      }

    return(true);
    }

  public void cancel()
    {
    }

  public void commit()
    {
    adapter.setHost(t_host.getText().trim());
    adapter.setPort((int)t_port.getValue());
    adapter.setService(t_service.getText().trim());
    }
    
  }

/* end of source file */
