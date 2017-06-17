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

package habotat.bots.execubot;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.io.File;
import java.util.*;
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

class ExecuBotUI extends KPanel implements PluginUI
  {
  private FileChooserField t_command;
  private KTextArea t_args;
  private ExecuBot bot;
  private DialogSet dialogs;
  private KFileChooserDialog d_file;
  private ResourceManager resmgr;
  private LocaleData loc;

  /*
   */

  ExecuBotUI(ExecuBot bot)
    {
    this.bot = bot;

    resmgr = new ResourceManager(getClass());
    loc = resmgr.getResourceBundle("bot");
    
    dialogs = DialogSet.getInstance();

    d_file = new KFileChooserDialog(
      KiwiUtils.getFrameForComponent(this),
      loc.getMessage("bot.window.title.select_command"),
      KFileChooser.OPEN_DIALOG);
    
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.insets = KiwiUtils.firstInsets;
    gbc.anchor = gbc.WEST;
    gbc.weighty = 0;

    KLabel l = new KLabel(loc.getMessage("bot.label.command"),
                          SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 0;
    add(l, gbc);

    t_command = new FileChooserField(15, 1024, d_file);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.weightx = 1;
    add(t_command, gbc);

    l = new KLabel(loc.getMessage("bot.label.arguments"),
                   SwingConstants.RIGHT);
    gbc.anchor = gbc.NORTHWEST;
    gbc.insets = KiwiUtils.firstInsets;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.weighty = 1;
    add(l, gbc);
    
    t_args = new KTextArea(5, 15);
    KScrollPane sp = new KScrollPane(t_args);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.weightx = 1;
    add(sp, gbc);
    }

  /*
   */
  
  public JComponent getComponent()
    {
    t_command.setFile(bot.getCommand());
    List<String> args = bot.getArguments();
    StringBuffer sb = new StringBuffer();

    if(args == null)
      t_args.setText(null);
    else
      {
      for(Iterator<String> iter = args.iterator(); iter.hasNext(); )
        {
        String arg = iter.next();
        boolean quote = (arg.indexOf(' ') >= 0);
        
        if(quote)
          sb.append('\"');
        
        sb.append(arg);
        
        if(quote)
          sb.append('\"');
        }

      t_args.setText(sb.toString());
      }

    t_command.requestFocus();

    return(this);
    }

  /*
   */

  public boolean accept()
    {
    File f = t_command.getFile();

    if(f == null)
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
    bot.setCommand(t_command.getFile());

    String argt = t_args.getText().trim();

    String vec[] = kiwi.util.StringUtils.wordBreak(argt, '\"');

    ArrayList<String> list = new ArrayList<String>(vec.length);
    for(int i = 0; i < vec.length; i++)
      list.add(vec[i]);

    bot.setArguments(list);
    }

  /*
   */

  public void cancel()
    {
    /* do nothing here */
    }
    
  }

/* end of source file */
