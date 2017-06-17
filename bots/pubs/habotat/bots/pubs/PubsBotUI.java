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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import kiwi.event.*;
import kiwi.ui.*;
import kiwi.ui.model.*;
import kiwi.ui.dialog.*;
import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

class PubsBotUI extends KPanel
  implements PluginUI, ActionListener, ListSelectionListener
  {
  private KTable l_feeds;
  private JComboBox b_hours, b_minutes, b_minafter;
  private ListSelectionModel feedSel;
  private KButton b_new, b_delete, b_edit;
  private KListModel feedModel;
  private KListModelTableAdapter model;
  private KRadioButton b_byhour, b_bymin;
  private ButtonGroup group;
  private int hourValues[] = { 1, 2, 3, 4, 6, 8, 12, 24 };
  private int minValues[] = { 5, 10, 15, 20, 30 };
  private PubsBot bot;
  private RSSFeedDialog d_rss = null;
  private DialogSet dialogs = DialogSet.getInstance();
  private DefaultListCellRenderer cellRenderer;
  private ResourceManager resmgr;
  private LocaleData loc;
  
  /*
   */
  
  PubsBotUI(PubsBot bot)
    {
    this.bot = bot;

    resmgr = new ResourceManager(getClass());
    loc = resmgr.getResourceBundle("bot");
    
    feedModel = bot.getFeeds();
    
    setLayout(new BorderLayout(5, 5));

    cellRenderer = new DefaultListCellRenderer();
    cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
    
    // toolbar

    JToolBar tb = new JToolBar();
    tb.setOpaque(false);
    tb.setBorderPainted(false);
    tb.setFloatable(false);

    add("North", tb);

    ResourceManager kresmgr = KiwiUtils.getResourceManager();

    b_new = new KButton(kresmgr.getIcon("newdoc.gif"));
    b_new.setOpaque(false);
    b_new.addActionListener(this);
    b_new.setToolTipText(loc.getMessage("bot.tooltip.new_feed"));
    tb.add(b_new);

    b_edit = new KButton(kresmgr.getIcon("edit.gif"));
    b_edit.addActionListener(this);
    b_edit.setEnabled(false);
    b_edit.setToolTipText(loc.getMessage("bot.tooltip.edit_feed"));
    tb.add(b_edit);
    
    b_delete = new KButton(kresmgr.getIcon("delete.gif"));
    b_delete.addActionListener(this);
    b_delete.setEnabled(false);
    b_delete.setToolTipText(loc.getMessage("bot.tooltip.remove_feed"));
    tb.add(b_delete);

    // feed list
    
    model = new KListModelTableAdapter(feedModel);

    l_feeds = new KTable();
    l_feeds.setAutoResizeMode(KTable.AUTO_RESIZE_ALL_COLUMNS);
    l_feeds.setAutoCreateColumnsFromModel(true);
    l_feeds.setModel(model);
    l_feeds.addMouseListener(new TableRowMouseAdapter()
        {
        public void rowDoubleClicked(int row, int button)
          {
          doEdit(getSelectedFeed());
          }
        });

    l_feeds.configureColumn(0, 100, 100, 1000, null, null);
    l_feeds.configureColumn(1, 160, 160, 1000, null, null);
    l_feeds.configureColumn(2, 80, 80, 1000, null, null);    

    feedSel = l_feeds.getSelectionModel();
    feedSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    feedSel.addListSelectionListener(this);

    KScrollPane sp = new KScrollPane(l_feeds);
    sp.setOpaque(true);
    sp.setBackground(Color.white);

    add("Center", sp);

    // refresh settings

    KPanel p_refresh = new KPanel();
    p_refresh.setBorder(
      new CompoundBorder(new TitledBorder(
                           new EtchedBorder(),
                           loc.getMessage("bot.heading.clear_feed_cache")),
                         KiwiUtils.defaultBorder));

    p_refresh.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.fill = gbc.NONE;
    gbc.anchor = gbc.WEST;
    KLabel l;

    group = new ButtonGroup();
    b_byhour = new KRadioButton("");
    b_byhour.addActionListener(this);
    b_byhour.setSelected(true);
    group.add(b_byhour);

    gbc.insets = KiwiUtils.firstInsets;
    p_refresh.add(b_byhour, gbc);

    l = new KLabel(loc.getMessage("bot.label.every"));
    p_refresh.add(l, gbc);

    Integer hrs[] = new Integer[hourValues.length];
    for(int i = 0; i < hrs.length; i++)
      hrs[i] = new Integer(hourValues[i]);
    b_hours = new JComboBox(hrs);
    b_hours.setRenderer(cellRenderer);
    p_refresh.add(b_hours, gbc);

    l = new KLabel(loc.getMessage("bot.label.hours"));
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastInsets;
    p_refresh.add(l, gbc);

    l = new KLabel();
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstInsets;
    p_refresh.add(l, gbc);

    l = new KLabel(loc.getMessage("bot.label.at"), SwingConstants.RIGHT);
    p_refresh.add(l, gbc);

    Integer minafter[] = new Integer[12];
    for(int i = 0; i < minafter.length; i++)
      minafter[i] = new Integer(i * 5);

    b_minafter = new JComboBox(minafter);
    b_minafter.setRenderer(cellRenderer);
    p_refresh.add(b_minafter, gbc);

    l = new KLabel(loc.getMessage("bot.label.minutes_past"));
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastInsets;
    p_refresh.add(l, gbc);

    b_bymin = new KRadioButton("");
    b_bymin.addActionListener(this);
    group.add(b_bymin);
    gbc.gridwidth = 1;
    gbc.insets = KiwiUtils.firstBottomInsets;
    p_refresh.add(b_bymin, gbc);

    l = new KLabel(loc.getMessage("bot.label.every"));
    p_refresh.add(l, gbc);
    
    Integer mins[] = new Integer[minValues.length];
    for(int i = 0; i < mins.length; i++)
      mins[i] = new Integer(minValues[i]);
    b_minutes = new JComboBox(mins);
    b_minutes.setRenderer(cellRenderer);
    b_minutes.setEnabled(false);
    p_refresh.add(b_minutes, gbc);
    
    l = new KLabel(loc.getMessage("bot.label.minutes"));
    gbc.gridwidth = gbc.REMAINDER;
    gbc.insets = KiwiUtils.lastBottomInsets;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 1;
    p_refresh.add(l, gbc);
        
    add("South", p_refresh);    
    }

  /*
   */
  
  public JComponent getComponent()
    {
    int i;
    
    int min = bot.getRefreshMinutes();
    for(i = 0; i < minValues.length; i++)
      if(min == minValues[i])
        b_minutes.setSelectedIndex(i);

    int hours = bot.getRefreshHours();
    for(i = 0; i < hourValues.length; i++)
      if(hours == hourValues[i])
        b_hours.setSelectedIndex(i);

    int minPast = bot.getRefreshMinutesPast();
    if(minPast < 0 || minPast > 55)
      minPast = 0;

    b_minafter.setSelectedIndex(minPast / 5);
    
    return(this);
    }

  /*
   */
  
  public boolean accept()
    {
    return(true);
    }

  /*
   */
  
  public void commit()
    {
    int hours = -1, minutes = -1, minPast = 0;
    
    if(b_byhour.isSelected())
      {
      int index = b_hours.getSelectedIndex();
      if(index >= 0)
        hours = hourValues[index];
      }

    if(b_bymin.isSelected())
      {
      int index = b_minutes.getSelectedIndex();
      if(index >= 0)
        minutes = minValues[index];
      }

    if(b_minafter.isEnabled())
      {
      Integer val = (Integer)b_minafter.getSelectedItem();
      minPast = val.intValue();
      }

    bot.setRefreshInterval(hours, minutes, minPast);

    bot.updatePubsList();
    bot.flushCache();
    }

  /*
   */
  
  public void cancel()
    {
    /* undo changes here */
    }

  /*
   */
  
  public void actionPerformed(ActionEvent evt)
    {
    Object o = evt.getSource();

    if(o == b_new)
      doEdit(null);

    else if(o == b_delete)
      doDelete(getSelectedFeed());

    else if(o == b_edit)
      doEdit(getSelectedFeed());

    else if(o == b_bymin)
      {
      b_hours.setEnabled(false);
      b_minafter.setEnabled(false);

      b_minutes.setEnabled(true);
      }

    else if(o == b_byhour)
      {
      b_minutes.setEnabled(false);

      b_hours.setEnabled(true);
      b_minafter.setEnabled(true);
      }
    }

  /*
   */
  
  public void valueChanged(ListSelectionEvent evt)
    {
    Object o = evt.getSource();

    if(o == feedSel)
      {
      boolean empty = feedSel.isSelectionEmpty();

      b_edit.setEnabled(! empty);
      b_delete.setEnabled(! empty);
      }
    }

  /*
   */
  
  RSSFeed getSelectedFeed()
    {
    int row = l_feeds.getSelectedRow();
    if(row < 0)
      return(null);

    return((RSSFeed)feedModel.getItemAt(row));
    }

  /*
   */
  
  private void doEdit(RSSFeed feed)
    {
    boolean add = false;

    if(feed == null)
      {
      feed = new RSSFeed();
      add = true;
      }
    
    if(d_rss == null)
      d_rss = new RSSFeedDialog(KiwiUtils.getFrameForComponent(this), loc);
    
    KiwiUtils.cascadeWindow(KiwiUtils.getWindowForComponent(this), d_rss);
    d_rss.setFeed(feed);
    d_rss.setVisible(true);
    
    if(! d_rss.isCancelled())
      {
      if(add)
        feedModel.addItem(feed);
      else
        feedModel.updateItem(feed);
      }
    }

  /*
   */
  
  private void doDelete(RSSFeed feed)
    {
    if(feed != null)
      {
      if(dialogs.showQuestionDialog(
           KiwiUtils.getWindowForComponent(this),
           loc.getMessage("bot.message.confirm_remove",
                          feed.getTitle())))
        {
        feedModel.removeItem(feed);
        }
      }
    }
  
  }

/* end of source file */
