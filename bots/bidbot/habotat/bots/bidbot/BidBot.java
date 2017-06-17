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

// bug: how to determine if we won a dutch auction?
// bug: what if eBay removes a listing?

package habotat.bots.bidbot;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jdom.*;

import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class BidBot extends Bot
  {
  private String helpText = null;
  private static final String NODE_USER = "user",
    NODE_AUCTION = "auction", ATTR_NAME = "name", ATTR_EBAY_USER = "ebayUser",
    ATTR_EBAY_PASSWORD = "ebayPassword", ATTR_ID = "id", ATTR_TITLE = "title",
    ATTR_MAX_BID = "maxBid", ATTR_BID_DELTA = "bidDelta",
    ATTR_QTY = "quantity", ATTR_BID_QTY = "bidQuantity",
    ATTR_STATUS = "status";
  private ArrayList<eBayServer.Auction> auctions
    = new ArrayList<eBayServer.Auction>(MAX_AUCTIONS);
  private static final int MAX_AUCTIONS = 10;
  private static final int DEFAULT_BID_DELTA = 10;
  private eBayServer ebay;
  private int bidDelta = DEFAULT_BID_DELTA;
  private String owner = null;
  private String ebayUser, ebayPassword;
  private int saveTimer, checkTimer;
  private HashMap bidTimers = new HashMap();
  private BidBotUI ui = null;

  private class SniperThread implements Runnable
    {
    private eBayServer.Auction auction;
    
    SniperThread(eBayServer.Auction auction)
      {
      this.auction = auction;
      }
    
    public void run()
      {
      logInfo("Sniper thread starting...");
      long end = auction.getSnipeTime();
      logInfo("Snipe time is: "
              + LocaleManager.getDefault().formatDateTime(new Date(end)));
      logInfo("Current time is: "
              + LocaleManager.getDefault().formatDateTime(new Date()));
      long goTime = end - (20 * 1000);
        
      for(;;)
        {
        try
          {
          long now = System.currentTimeMillis();
          
          int toSleep = (int)(goTime - now);
          if(toSleep <= 0)
            break;
          
          logInfo("sniper thread sleeping for " + toSleep + " ms");
          Thread.currentThread().sleep(toSleep);
          }
        catch(InterruptedException ex)
          {
          }
        }

      try
        {
        logInfo("go time; starting bid process");
        ebay.placeBid(auction, end);

        logInfo("Bid placed on " + auction.getID() + " at " + new Date());
        }
      catch(IOException ex)
        {
        logError("bid couldn't be placed; I/O error: " + ex.toString());
        }

      }
    }

  /*
   */

  public BidBot()
    {
    super();

    ebay = new eBayServer(this);
    }

  /*
   */
  
  public void init()
    {
    }
  
  /*
   */
  
  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();

    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append(" list</b> - list all scheduled snipes<br><b>");
    sb.append(kwd);
    sb.append(" snipe</b> <i>item#</i> <i>max</i> [<i>quantity</i>] - schedule new snipe<br><b>");
    sb.append(kwd);
    sb.append(" cancel</b> <i>n</i> - cancel snipe #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" time</b> <i>seconds</i> - set bid time (seconds before auction end)<br><b>");
    sb.append(kwd);
    sb.append(" setmax</b> <i>n</i> <i>amount</i> - change maximum bid on snipe #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" setqty</b> <i>n</i> <i>quantity</i> - change bid quantity on snipe #<i>n</i><br><b>");
    sb.append(kwd);
    sb.append(" clear</b> - remove completed auctions from list");

    helpText = sb.toString();
    }
  
  /*
   */
  
  public void start()
    {
    
    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }

    logInfo("in addAuction()");
    logInfo("Signed in? " + ebay.signedIn());
    
    if(! ebay.signedIn())
      {
      try
        {
        //System.out.println("signing in");
        logInfo("Signing in to eBay...");
        ebay.signIn(ebayUser, ebayPassword);
        logInfo("Done.");
        //System.out.println("signed in!");
        }
      catch(IOException ex)
        {
        //System.out.println("fialed to sign in");
        logError("Exception signing in!", ex);
        }
      }
    
    ebay.syncTime();
    updateAuctions();

    try
      {
      TimeSpec tspec = new TimeSpec();
      tspec.setAllHours();
      tspec.clearAllMinutes();
      for(int i = 0; i < 60; i += 5)
        tspec.setMinute(i);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      checkTimer = createTimer(tspec);

      tspec = new TimeSpec();
      tspec.setAllHours();
      tspec.clearAllMinutes();
      tspec.setMinute(0);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      saveTimer = createTimer(tspec);

      // create timers for all auctions

      Iterator<eBayServer.Auction> iter = auctions.iterator();
      while(iter.hasNext())
        makeAuctionTimer(iter.next());
      }
    catch(BotException ex) { }
    
    buildHelpText();

    // watch the user

    try
      {
      addWatchedUser(getIMUser());
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      destroyTimer(saveTimer);
      destroyTimer(checkTimer);
      }
    catch(BotException ex)
      {
      }

    try
      {
      removeWatchedUser(getIMUser());
      }
    catch(BotException ex) { }

    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }

    if(ebay.signedIn())
      {
      try
        {
        logInfo("Signing out of eBay...");
        ebay.signOut();
        logInfo("Done.");
        }
      catch(IOException ex)
        {
        logError("Exception signing out!", ex);
        }
      }
    }

  /*
   */
  
  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");
    
    try
      {
      Document data = readXML(ins);

      // now copy all info into objects

      auctions.clear();

      Element root = data.getRootElement();

      Element userNode = XMLUtils.getChildElem(root, NODE_USER, true);
      
      owner = XMLUtils.getStringAttr(userNode, ATTR_NAME, null, true);
      ebayUser = XMLUtils.getStringAttr(userNode, ATTR_EBAY_USER, null,
                                        true);
      ebayPassword = XMLUtils.getStringAttr(userNode, ATTR_EBAY_PASSWORD,
                                            null, true);
      bidDelta = XMLUtils.getIntAttr(userNode, ATTR_BID_DELTA,
                                     DEFAULT_BID_DELTA, false);
      
      if(bidDelta < 3 || bidDelta > 60)
        bidDelta = DEFAULT_BID_DELTA;
      
      List list = userNode.getChildren(NODE_AUCTION);
      Iterator iter = list.iterator();
      
      for(int i = 0; (i < MAX_AUCTIONS) && iter.hasNext(); i++)
        {
        Element node = (Element)iter.next();
        
        logInfo("read auction node");
        
        try
          {
          String id = XMLUtils.getStringAttr(node, ATTR_ID, null, true);
          
          if(id.equals(""))
            continue;
          
          String title = XMLUtils.getStringAttr(node, ATTR_TITLE, null,
                                                true);
          double maxBid = XMLUtils.getFloatAttr(node, ATTR_MAX_BID, 0.0,
                                                true);
          
          if(maxBid <= 0)
            continue;
          
          int bidDelta = XMLUtils.getIntAttr(node, ATTR_BID_DELTA,
                                             DEFAULT_BID_DELTA, false);
          if((bidDelta < 3)  || (bidDelta > 60))
            continue;
          
          int quantity = XMLUtils.getIntAttr(node, ATTR_QTY, 1, false);
          if(quantity < 1)
            continue;
          
          int bidQuantity = XMLUtils.getIntAttr(node, ATTR_BID_QTY, 1, false);
          
          if((bidQuantity < 1) || (bidQuantity > quantity))
            bidQuantity = 1;
          
          String s = XMLUtils.getStringAttr(node, ATTR_STATUS, null, true);
          
          int status;
          if(s.equalsIgnoreCase("pending"))
            status = eBayServer.Auction.PENDING;
          else if(s.equalsIgnoreCase("won"))
            status = eBayServer.Auction.WON;
          else if(s.equalsIgnoreCase("lost"))
            status = eBayServer.Auction.LOST;
          else
            continue;

          // System.out.println("ebay pointer is: " + ebay);
          eBayServer.Auction auc = ebay.newAuction(id, title, maxBid,
                                                   bidDelta, quantity,
                                                   bidQuantity, status);
          
          if(! ebay.getAuctionInfo(auc))
            logError("Can't get auction info for item: " + id);
          else
            addAuction(auc);
          }
        catch(InvalidXMLException ex)
          {
          logError(ex);
          // skip it
          }
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      throw(new BotException("Error reading data"));
      }
    }

  /*
   */
  
  long getTimeLag()
    {
    if(ebay == null)
      return(0);
    
    return(ebay.getTimeLag());
    }

  /*
   */
  
  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data");

    Element root = new Element("botdata");
    Document doc = new Document(root);
    
    Element userNode = new Element(NODE_USER);

    XMLUtils.setStringAttr(userNode, ATTR_NAME, owner);
    XMLUtils.setStringAttr(userNode, ATTR_EBAY_USER, ebayUser);
    XMLUtils.setStringAttr(userNode, ATTR_EBAY_PASSWORD, ebayPassword);
    XMLUtils.setIntAttr(userNode, ATTR_BID_DELTA, bidDelta);
    
    root.addContent(userNode);

    Iterator<eBayServer.Auction> iter = auctions.iterator();
    while(iter.hasNext())
      {
      eBayServer.Auction auction = iter.next();

      Element aucNode = new Element(NODE_AUCTION);

      XMLUtils.setStringAttr(aucNode, ATTR_ID, auction.getID());
      XMLUtils.setStringAttr(aucNode, ATTR_TITLE, auction.getTitle());
      XMLUtils.setFloatAttr(aucNode, ATTR_MAX_BID, auction.getMaximumBid());
      XMLUtils.setIntAttr(aucNode, ATTR_BID_DELTA, auction.getBidDelta());
      XMLUtils.setIntAttr(aucNode, ATTR_QTY, auction.getQuantity());
      XMLUtils.setIntAttr(aucNode, ATTR_BID_QTY, auction.getBidQuantity());
      
      String status = null;
      switch(auction.getStatus())
        {
        case eBayServer.Auction.PENDING:
          status = "pending";
          break;
          
        case eBayServer.Auction.LOST:
          status = "lost";
          break;
          
        case eBayServer.Auction.WON:
          status = "won";
          break;
        }
      
      if(status == null)
        continue;

      XMLUtils.setStringAttr(aucNode, ATTR_STATUS, status);
      
      userNode.addContent(aucNode);
      }
    
    try
      {
      writeXML(doc, outs);
      }
    catch(IOException ex)
      {
      throw(new BotException("Error writing data", ex));
      }
    }

  /*
   */

  private void updateAuctions()
    {
    logInfo("Updating auction info!");
    Iterator<eBayServer.Auction> iter = auctions.iterator();
    boolean updated = false;

    while(iter.hasNext())
      {
      eBayServer.Auction auc = iter.next();

      if(! auc.isPending())
        continue;

      try
        {
        updated = true;
        
        if(! ebay.getAuctionInfo(auc))
          {
          logError("Auction vanished!");
          continue;
          }

        logInfo("Auction ends at " + auc.getEndTime());
        logInfo("time left: " + auc.getTimeLeft());
        
        if(auc.isEnded())
          {
          logInfo("Auction " + auc.getID() + " is ended.");
          logInfo("High bidder is: " + auc.getHighBidder()
                  + ", and we are: " + ebayUser);

          boolean won = false;
          String highBidder = auc.getHighBidder();
          if(highBidder != null)
            won = highBidder.equals(ebayUser);
          
          auc.setStatus(won ? auc.WON : auc.LOST);
          
          StringBuffer sb = new StringBuffer();

          if(! auc.isNotified())
            {
            if(won)
              {
              sb.append("You won <a href=\"");
              sb.append(auc.getURL());
              sb.append("\">");
              sb.append(auc.getTitle());
              sb.append("</a> for <b>");
              sb.append(auc.getCurrentBidFormatted());
              sb.append("</b>! :-)");
              }
            else
              {
              sb.append("You did not win <a href=\"");
              sb.append(auc.getURL());
              sb.append("\">");
              sb.append(auc.getTitle());
              sb.append("</a>; the final bid was <b>");
              sb.append(auc.getCurrentBidFormatted());
              sb.append("</b>. :-(");
              }
            
            sendMessage(owner, sb.toString());

            auc.setNotified(true);
            }
          }
        }
      catch(Exception ex)
        {
        logError(ex);
        }
      }

    if(! updated)
      {
      // nothing was updated, so we should ping eBay to keep our session
      // alive

      ebay.ping();
      }
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    if(! mevt.getSender().equals(owner))
      return; // ignore other users
      
    try
      {
      String args[] = parseCommand(mevt.getMessage());

      if((args.length == 2) && "time".startsWith(args[0]))
        {
        int val = 0;

        try
          {
          val = Integer.parseInt(args[1]);
          }
        catch(NumberFormatException ex) { }

        if(val < 3)
          sendMessage(mevt.getSender(), "Time must be a number greater than or equal to 3.");
        else
          {
          bidDelta = val;
          sendMessage(mevt.getSender(), "Bid time offset is now " + val + " seconds.");
          }          
        }

      else if((args.length >= 3) && (args.length <= 4)
              && "snipe".startsWith(args[0]))
        {
        String item = args[1];
        double bid = 0;

        try { bid = Double.parseDouble(args[2]); }
        catch(NumberFormatException ex) { }

        int qty = 1;
        if(args.length == 4)
          {
          try { qty = Integer.parseInt(args[3]); }
          catch(NumberFormatException ex) { }
          }

        if((bid <= 0) || (qty < 1))
          {
          sendMessage(mevt.getSender(), "Bid amount and/or quantity are invalid.");
          return;
          }

        eBayServer.Auction info = ebay.newAuction(item);
        ebay.getAuctionInfo(info);

        if(! info.isValid())
          {
          sendMessage(mevt.getSender(), "That auction does not exist.");
          return;
          }

        if(bid < info.getMinimumBid())
          {
          sendMessage(mevt.getSender(), "The minimum bid for this item is "
                      + info.getMinimumBidFormatted());
          return;
          }

        if(qty > info.getQuantity())
          {
          sendMessage(mevt.getSender(), "There are only "
                      + info.getQuantity() + " available.");
          return;
          }

        info.setBidQuantity(qty);
        info.setMaximumBid(bid);
        info.setBidDelta(bidDelta);

        addAuction(info);
        makeAuctionTimer(info);

        sendMessage(mevt.getSender(), "Snipe successfully scheduled!");
        }

      else if((args.length == 1) && "clear".startsWith(args[0]))
        {
        int ct = 0;
          
        for(int i = auctions.size() - 1; i >= 0; i--)
          {
          eBayServer.Auction auc = auctions.get(i);
          if(auc.isEnded())
            {
            auctions.remove(i);
            ct++;
            }
          }

        if(ct == 0)
          sendMessage(mevt.getSender(), "There are no completed auctions in the list.");
        else
          sendMessage(mevt.getSender(), ct + " completed auctions removed.");
        }

      else if((args.length == 2) && "cancel".startsWith(args[0]))
        {
        int n = 0;

        try { n = Integer.parseInt(args[1]); }
        catch(NumberFormatException ex) { }

        if(n < 1 || n > auctions.size())
          sendMessage(mevt.getSender(), "Invalid snipe number.");
        else
          {
          removeAuction(--n);

          /* @@ here we need to cancel the timer and remove it from the hash table */
            
          sendMessage(mevt.getSender(), "Snipe cancelled.");
          }
        }
        
      else if((args.length == 1) && "list".startsWith(args[0]))
        {
        StringBuffer sb = new StringBuffer();
        sb.append("Auction Snipe List:");

        if(auctions.size() == 0)
          sb.append(" <i>empty</i>");
        else
          {
          int i = 0;
          Iterator<eBayServer.Auction> iter = auctions.iterator();
          while(iter.hasNext())
            {
            sb.append("<br><b>");
            sb.append(String.valueOf(++i));
            sb.append("</b>. <a href=\"");
            eBayServer.Auction auc = iter.next();
            sb.append(auc.getURL());
            sb.append("\">");
            sb.append(auc.getTitle());
            sb.append("</a> ");

            if(! auc.hasBids())
              sb.append("<i>");
            else if(auc.getStatus() == eBayServer.Auction.WON)
              sb.append("<font color=\"green\"><b>");
            sb.append(auc.getCurrentBidFormatted());
            if(! auc.hasBids())
              sb.append("</i>");
            else if(auc.getStatus() == eBayServer.Auction.WON)
              sb.append("</b></font>");
              
            if(auc.getQuantity() > 1)
              {
              sb.append(" x");
              sb.append(String.valueOf(auc.getQuantity()));
              }
              
            sb.append(" (");
            sb.append("<b>");
            if(auc.getStatus() == eBayServer.Auction.LOST)
              sb.append("<font color=\"red\">");
            sb.append(auc.getMaximumBidFormatted());
            if(auc.getStatus() == eBayServer.Auction.LOST)
              sb.append("</font>");

            if(auc.getQuantity() > 1)
              {
              sb.append(" x");
              sb.append(String.valueOf(auc.getBidQuantity()));
              }
              
            sb.append("</b>");
            sb.append(") ");
            sb.append(auc.getTimeLeftFormatted());
            if(! auc.isEnded())
              {
              sb.append(" (-");
              sb.append(auc.getBidDelta());
              sb.append("s)");
              }

            switch(auc.getStatus())
              {
              case eBayServer.Auction.WON:
                sb.append(" :-)");
                break;

              case eBayServer.Auction.LOST:
                sb.append(" :-(");
                break;
              }
            }
          }

        sendMessage(mevt.getSender(), sb.toString());          
        }

      else if((args.length == 3) && "setmax".startsWith(args[0]))
        {
        int n = -1;
        try { n = Integer.parseInt(args[1]); }
        catch(NumberFormatException ex) { }

        if(n < 1 || n > auctions.size())
          {
          sendMessage(mevt.getSender(), "Please specify a valid snipe number.");
          return;
          }

        eBayServer.Auction info = auctions.get(--n);
        ebay.getAuctionInfo(info);

        double bid = 0;
        try { bid = Double.parseDouble(args[2]); }
        catch(NumberFormatException ex) { }

        if(bid < info.getMinimumBid())
          {
          sendMessage(mevt.getSender(), "The minimum bid for this item is "
                      + info.getMinimumBidFormatted());
          return;
          }

        info.setMaximumBid(bid);

        sendMessage(mevt.getSender(), "Maximum bid updated.");
        }

      else if((args.length == 3) && "setqty".startsWith(args[0]))
        {
        int n = -1;
        try { n = Integer.parseInt(args[1]); }
        catch(NumberFormatException ex) { }

        if(n < 1 || n > auctions.size())
          {
          sendMessage(mevt.getSender(), "Please specify a valid snipe number.");
          return;
          }

        eBayServer.Auction info = auctions.get(--n);
        ebay.getAuctionInfo(info);

        // @@ unfinished
          
        }
        
      else
        sendMessage(mevt.getSender(), helpText);        
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    catch(IOException ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  public void handleEvent(TimerEvent tevt)
    {
    int id = tevt.getTimerID();

    if(id == saveTimer)
      {
      try
        {
        saveData();
        }
      catch(Exception ex)
        {
        logError("Unable to save data", ex);
        }
      }
    else if(id == checkTimer)
      updateAuctions();
    else
      {
      logInfo("Timer event is for a snipe: id=" + id);
      Integer key = new Integer(id);        
      eBayServer.Auction auc = (eBayServer.Auction)bidTimers.remove(key);
      if(auc == null)
        return;

      Thread sniper = new Thread(new SniperThread(auc));
      sniper.start();

        
      logInfo("Sniper thread started.");
      }
    }
  
  /*
   */
  
  public void handleEvent(UserStatusEvent uevt)
    {
    logInfo(uevt.getUser() + " signed " + (uevt.isOnline() ? "on" : "off"));
    
    updateAuctions();
    }

  /*
   */

  private void addAuction(eBayServer.Auction auction) throws BotException
    {
    auctions.add(auction);
    }

  /*
   */
  
  private void removeAuction(int index)
    {
    auctions.remove(index);

    }

  /*
   */

  private void makeAuctionTimer(eBayServer.Auction auction)
    throws BotException
    {
    if(auction.isEnded())
      return;
    
    // here we schedule a timer for the end time - 2 min.
    
    Calendar cal = Calendar.getInstance();
    logInfo("Current time: "
            + LocaleManager.getDefault().formatDateTime(cal));
    
    cal.setTimeInMillis(auction.getSnipeTime());
    cal.add(Calendar.MINUTE, -2);
    
    logInfo("snipe timer will fire at: "
            + LocaleManager.getDefault().formatDateTime(cal));
    
    int timer = createTimer(cal);
    logInfo("snipe timer created: ID = " + timer);
    
    synchronized(bidTimers)
      {
      bidTimers.put(new Integer(timer), auction);
      }
    }
  
  /*
   */

  void seteBayUsername(String user)
    {
    ebayUser = user;
    }

  String geteBayUsername()
    {
    return(ebayUser);
    }

  void seteBayPassword(String password)
    {
    ebayPassword = password;
    }

  String geteBayPassword()
    {
    return(ebayPassword);
    }

  void setIMUser(String user)
    {
    owner = user;
    }

  String getIMUser()
    {
    return(owner);
    }

  /*
   */

  public PluginUI getUI()
    {
    if(ui == null)
      ui = new BidBotUI(this);
    
    return(ui);
    }  
  
  }

/* end of source file */
