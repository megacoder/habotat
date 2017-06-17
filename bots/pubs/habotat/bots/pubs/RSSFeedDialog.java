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

package habotat.bots.pubs;

import java.awt.*;
import java.net.*;
import javax.swing.*;

import kiwi.ui.*;
import kiwi.ui.dialog.*;
import kiwi.ui.model.*;
import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class RSSFeedDialog extends ComponentDialog
  {
  private KTextField t_title, t_keyword;
  private JTextArea t_url;
  private JComboBox b_type;
  private RSSFeed feed = null;
  private DialogSet dialogs;
  private LocaleData loc;

  /*
   */
  
  RSSFeedDialog(Frame parent, LocaleData loc)
    {
    super(parent, loc.getMessage("bot.window.title.feed_details"), true, true);

    this.loc = loc;

    dialogs = DialogSet.getInstance();
    
    setComment(loc.getMessage("bot.window.comment.feed_details"));

    installDialogUI();
    }

  /*
   */
  
  protected Component buildDialogUI()
    {
    if(loc == null)
      return(null);
    
    KPanel p_main = new KPanel();

    p_main.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.anchor = gbc.WEST;
    gbc.fill = gbc.HORIZONTAL;
    KLabel l;

    l = new KLabel(loc.getMessage("bot.label.title"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    p_main.add(l, gbc);

    t_title = new KTextField(25);
    t_title.setMaximumLength(50);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    p_main.add(t_title, gbc);

    l = new KLabel(loc.getMessage("bot.label.keyword"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.insets = KiwiUtils.firstInsets;
    p_main.add(l, gbc);

    t_keyword = new KTextField(10);
    t_keyword.setMaximumLength(16);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    p_main.add(t_keyword, gbc);

    l = new KLabel(loc.getMessage("bot.label.url"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.anchor = gbc.NORTHWEST;
    gbc.insets = KiwiUtils.firstInsets;
    p_main.add(l, gbc);

    t_url = new JTextArea(3, 35);
    KDocument doc = new KDocument();
    doc.setMaximumLength(255);
    t_url.setDocument(doc);
    t_url.setLineWrap(true);
    t_url.setWrapStyleWord(false);
    gbc.insets = KiwiUtils.lastInsets;
    gbc.gridwidth = gbc.REMAINDER;
    gbc.fill = gbc.NONE;
    gbc.anchor = gbc.WEST;
    KScrollPane sp = new KScrollPane(t_url);
    sp.setOpaque(true);
    sp.setBackground(Color.white);
    p_main.add(sp, gbc);

    l = new KLabel(loc.getMessage("bot.label.rss_type"), SwingConstants.RIGHT);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstBottomInsets;
    p_main.add(l, gbc);
    
    b_type = new JComboBox();
    for(int i = 0; i <= RSSFeed.RSS_MAX; i++)
      b_type.addItem(RSSFeed.getStringForType(i));
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.gridwidth = gbc.REMAINDER;
    p_main.add(b_type, gbc);

    KPanel p = new KPanel();
    p.setLayout(new BorderLayout(5, 5));

    p.add("North", new JSeparator());
    p.add("Center", p_main);
    p.add("South", new JSeparator());
    
    return(p);
    }

  /*
   */

  protected boolean accept()
    {
    String title = t_title.getText().trim();
    String keyword = t_keyword.getText().trim();
    String urlText = t_url.getText().trim();
    URL url = null;
    
    if(title.equals("") || keyword.equals("") || urlText.equals(""))
      {
      dialogs.showMessageDialog(this,
                                loc.getMessage("bot.message.required_fields"));
      return(false);
      }

    if(! keyword.matches("^[A-Za-z][A-Za-z0-9\\-]*$"))
      {
      dialogs.showMessageDialog(this,
                                loc.getMessage("bot.message.keyword_invalid"));
      }

    try
      {
      url = new URL(urlText);
      }
    catch(MalformedURLException ex)
      {
      dialogs.showMessageDialog(this,
                                loc.getMessage("bot.message.url_invalid"));
      return(false);
      }

    feed.setTitle(title);
    feed.setKeyword(keyword);
    feed.setURL(url);
    feed.setType(b_type.getSelectedIndex());
    
    return(true);
    }

  /*
   */

  protected void startFocus()
    {
    t_title.requestFocus();
    }
  
  /*
   */

  void setFeed(RSSFeed feed)
    {
    this.feed = feed;

    t_title.setText(feed.getTitle());
    t_keyword.setText(feed.getKeyword());
    URL url = feed.getURL();
    t_url.setText(url == null ? "" : url.toString());
    b_type.setSelectedIndex(feed.getType());
    }
  
  }

/* end of source file */
