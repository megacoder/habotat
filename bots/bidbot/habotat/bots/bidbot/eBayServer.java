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

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import kiwi.io.*;
import kiwi.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class eBayServer
  {
  private CookieStore cookies = new CookieStore();
  
  private SimpleDateFormat timeFormat0, timeFormat1;
  private Pattern patTitle, patHighBid, patHighBidder, patTime, patMaxBidForm,
    patKeyForm, patQuantity, patYouWon;
  private final String curPatterns[] = new String[] { "US $", "AU $", "GBP",
                                                      "EUR", "CAD", "DEM",
                                                      "FRF" };
  
  private final String curPrefixes[] = new String[] { "US $", "AU $", "GBP ",
                                                      "EUR ", "CAD ", "DEM ",
                                                      "FRF " };
  private NumberFormat currFormat;
  private long timeLag = 0;
  private BotLogger log;
  private boolean signedIn = false;

  private URL urlTime, urlSignIn, urlSignOut, urlPing, urlISAPI;
  private static final String URL_BID = "http://offer.ebay.com/ws/eBayISAPI.dll?MfcISAPICommand=MakeBid&maxbid=&item=";
  private static final String URL_ISAPI = "http://offer.ebay.com/ws/eBayISAPI.dll";
  private static final String URL_VIEWITEM = URL_ISAPI + "?ViewItem&item=";
  private String username;
  
  /*
   */

  eBayServer(BotLogger log)
    {
    this.log = log;
    
    timeFormat0 = new SimpleDateFormat("MMM d, yyyy HH:mm:ss z");
    timeFormat1 = new SimpleDateFormat("MMM-d-yy HH:mm:ss z");

    try
      {
      urlTime = new URL("http://cgi.ebay.com/aw-cgi/eBayISAPI.dll?TimeShow");
      urlSignIn
        = new URL("https://signin.ebay.com/ws/eBayISAPI.dll?SignIn");
      
      urlSignOut = new URL("http://signin.ebay.com/ws/eBayISAPI.dll?SignIn");

      urlPing = new URL("http://pages.ebay.com/aboutebay/contact.html");

      urlISAPI = new URL(URL_ISAPI);
      }
    catch(MalformedURLException ex)
      {
      }
    
    currFormat = NumberFormat.getInstance();
    currFormat.setMinimumFractionDigits(2);
    currFormat.setMaximumFractionDigits(2);
    currFormat.setGroupingUsed(true);
    
    patTime = Pattern.compile("The official eBay Time is now:\\s+[A-Za-z]+,\\s+([A-Za-z]+\\s+[0-9]{2},\\s+[0-9]{4}\\s+[0-9]{2}:[0-9]{2}:[0-9]{2}\\s+[A-Z]{3})");
    patTitle = Pattern.compile("eBay: ([^\\n]+) \\(item ([0-9]+)\\s+end time\\s+([^\\)]+)\\)");
//    patTitle = Pattern.compile("eBay: ([^\\n]+) [\\\\(item [0-9]+\\s+end time\\s+([^\\)]+)\\)");
    patHighBid = Pattern.compile("(?:(?:Winning|Current|Starting) bid|Sold for):\\s+(?:([A-Z]+(?: \\$)?)([0-9\\.,]+))");
    
    patHighBidder = Pattern.compile("(?:Buyer|(?:Winning|High) bidder):\\s+([A-Za-z0-9_-]+)");
    patQuantity = Pattern.compile("Quantity:\\s+([0-9,]+)");
    patYouWon = Pattern.compile("You won the item!");
    }

  /*
   */

  public void syncTime()
    {
    try
      {
      Date eBayTime = getOfficialTime();
      long ourTime = System.currentTimeMillis();
    
      timeLag = (eBayTime.getTime() - ourTime);
      }
    catch(Exception ex)
      {
      }

    log.logInfo("Time lag: " + timeLag / 1000 + " sec.");
    }

  /*
   */
  
  public void ping()
    {
    if(signedIn)
      {
      try
        {
        HTMLPage.fetch(urlPing, cookies);
        }
      catch(IOException ex)
        {
        }
      }
    }
  
  /*
   */

  public boolean signedIn()
    {
    return(signedIn);
    }
  
  /*
   */

  public long getTimeLag()
    {
    return(timeLag);
    }
  
  /*
   */
  
  public Auction newAuction(String id)
    {
    return(new Auction(id));
    }

  /*
   */
  
  public Auction newAuction(String id, String title, double bid, int bidDelta,
                            int quantity, int bidQuantity)
    {
    return(new Auction(id, title, bid, bidDelta, quantity, bidQuantity));
    }

  /*
   */
  
  public Auction newAuction(String id, String title, double bid, int bidDelta,
                            int quantity, int bidQuantity, int status)
    {
    return(new Auction(id, title, bid, bidDelta, quantity, bidQuantity,
                       status));
    }
  
  /*
   */

  public boolean getAuctionInfo(Auction auction) throws IOException
    {
    Matcher mat;

    //System.out.println("fetching URL: " + auction.getURL());

    HTMLPage pg = HTMLPage.fetch(auction.getURL(), cookies);

    // get title & end date
    
    mat = pg.scan(patTitle);

    if(mat != null)
      {
      ParsePosition pos = new ParsePosition(0);
      auction.setEndTime(timeFormat1.parse(mat.group(3), pos));

      auction.setTitle(mat.group(1));
      }

    // get high bid

    mat = pg.scan(patHighBid);
    if(mat != null)
      {
      String curr = mat.group(1);

      for(int i = 0; i < curPatterns.length; i++)
        {
        if(curr.startsWith(curPatterns[i]))
          auction.setCurrency(i);
        }

      ParsePosition pos = new ParsePosition(0);
      auction.setCurrentBid(currFormat.parse(mat.group(2), pos).doubleValue());
      }

    // get high bidder

    mat = pg.scan(patYouWon);
    if(mat != null)
      auction.setHighBidder(username);
    else
      {
      mat = pg.scan(patHighBidder);
      if(mat != null)
        auction.setHighBidder(mat.group(1));
      }

    // get quantity (if dutch)

    mat = pg.scan(patQuantity);
    if(mat != null)
      auction.setQuantity(Integer.parseInt(mat.group(1)));

    return(true);
    }

  /**
   */
  
  public void signIn(String username, String password) throws IOException
    {
    this.username = username;
    
    HTMLPage pg = HTMLPage.fetch(urlSignIn, cookies);

    HTMLForm form = pg.getForm("SignInForm");
    form.set("userid", username);
    form.set("pass", password);

    HTMLPage pg2 = form.submit(cookies);

    //System.out.println(pg2.getText());

    signedIn = true;

    // System.out.println("signed in");
    }

  /**
   */
  
  public void signOut() throws IOException
    {
    HTMLPage pg = HTMLPage.fetch(urlSignOut, cookies);    

    cookies.clear();

    signedIn = false;
    }
  
  /**
   */

  public Date getOfficialTime() throws IOException
    {
    HTMLPage pg = HTMLPage.fetch(urlTime, cookies);

    Matcher mat = pg.scan(patTime);

    Date eBayTime = null;
    
    if(mat != null)
      {
      ParsePosition pos = new ParsePosition(0);
      eBayTime = timeFormat0.parse(mat.group(1), pos);
      
      // System.out.println(eBayTime);
      }

    return(eBayTime);
    }

  /**
   */

  public boolean placeBid(Auction auction, long snipeTime) throws IOException
    {
    URL url = new URL(URL_BID + auction.getID());

    HTMLPage pg1 = HTMLPage.fetch(url, cookies);

    HTMLForm form1 = pg1.findForm(urlISAPI);

    if(form1 == null)
      {
      System.out.println("can't find PlaceBid form!");
      return(false);
      }
    
    form1.set("maxbid", currFormat.format(auction.getMaximumBid()));
    form1.set("quant", String.valueOf(auction.getBidQuantity()));

    HTMLPage pg2 = form1.submit(cookies);

    HTMLForm form2 = pg2.getForm("PlaceBid");

    for(;;)
      {
      try
        {
        long now = System.currentTimeMillis();
        int toSleep = (int)(snipeTime - now);
        if(toSleep <= 0)
          break;
          
        System.out.println("confirmation: waiting for " + toSleep + " ms");
        Thread.currentThread().sleep(toSleep);
        }
      catch(InterruptedException ex)
        {
        }
      }
    
    System.out.println("---submitting confirmation---");
    HTMLPage pg3 = form2.submit(cookies);

    System.out.println("Result: " + pg3.getText());

    return(true);
    }

  /*
   */

  private String formatCurrency(double amount, int currency)
    {
    if((amount < 0) || (currency < 0) || (currency >= curPrefixes.length))
      return("???");

    return(curPrefixes[currency] + currFormat.format(amount));
    }

  /*
   */

  /*
  public static void main(String args[]) throws Exception
    {
    HTMLPage pg = HTMLPage.fetch(new URL("http://cgi.ebay.com/ws/eBayISAPI.dll?ViewItem&item=6429010953"), null, false);

    System.out.println(pg.getText());
    }
  */

  public static void main(String args[]) throws Exception
    {
    eBayServer ebay = new eBayServer(new BotLogger()
        {
        public void logInfo(String text)
          {
          System.err.println("INFO: " + text);
          }

        public void logWarning(String text)
          {
          System.err.println("WARN: " + text);
          }

        public void logError(String text)
          {
          System.err.println("ERR:  " + text);
          }
        });

    System.out.println("Signing in...");
    ebay.signIn("XXX", "YYY");
    System.out.println("Done.");

//    Auction auction = ebay.newAuction("5982610320");
    Auction auction = ebay.newAuction("6006830273");

    auction.setMaximumBid(19.11);
    
    ebay.placeBid(auction, System.currentTimeMillis());

    System.out.println(auction);
    
    ebay.signOut();
    }

  /*
   */

  public class Auction
    {
    static final int CUR_UNKNOWN = -1, CUR_USD = 0, CUR_AUD = 1, CUR_EUR = 2,
      CUR_GBP = 3, CUR_FRF = 4, CUR_DEM = 5, CUR_CAD = 6;

    static final int PENDING = 0, WON = 1, LOST = 2;
    
    private String id;
    private String title;
    private double curBid;
    private double maxBid;
    private int currency;
    private Date endTime;
    private boolean ended;
    private String highBidder;
    private int quantity;
    private int bidQuantity;
    private boolean dutch;
    private int bidDelta = 10;
    private int status = PENDING;
    private boolean notified = false;
    private boolean bidChanged = false;
    private URL url;

    /*
     */
    
    Auction(String id)
      {
      this(id, null, 0, 0);
      }

    /*
     */
    
    Auction(String id, String title, double maxBid, int bidDelta)
      {
      this(id, title, maxBid, bidDelta, 1, PENDING);
      }

    /*
     */
    
    Auction(String id, String title, double maxBid, int bidDelta, int quantity,
            int bidQuantity)
      {
      this(id, title, maxBid, bidDelta, quantity, bidQuantity, PENDING);
      }

    /*
     */
    
    Auction(String id, String title, double maxBid, int bidDelta, int quantity,
            int bidQuantity, int status)
      {
      this.id = id;
      this.title = title;
      this.maxBid = maxBid;
      this.bidDelta = bidDelta;
      this.quantity = quantity;
      this.bidQuantity = bidQuantity;
      this.status = status;

      try
        {
        url = new URL(URL_VIEWITEM + id);
        }
      catch(MalformedURLException ex) { }
      }

    public URL getURL()
      {
      return(url);
      }

    /*
     */
    
    String getID()
      {
      return(id);
      }

    /*
     */
    
    void setStatus(int status)
      {
      this.status = status;
      }

    /*
     */
    
    int getStatus()
      {
      return(status);
      }
    
    /*
     */
    
    boolean isValid()
      {
      return(title != null);
      }

    /*
     */
    
    boolean hasBids()
      {
      return(highBidder != null);
      }

    /*
     */
    
    String getTitle()
      {
      return(title);
      }

    /*
     */
    
    void setTitle(String title)
      {
      this.title = title;
      }

    /*
     */
    
    int getCurrency()
      {
      return(currency);
      }

    /*
     */
    
    void setCurrency(int currency)
      {
      this.currency = currency;
      }

    /*
     */
    
    double getCurrentBid()
      {
      return(curBid);
      }

    /*
     */
    
    String getCurrentBidFormatted()
      {
      return(formatCurrency(curBid, currency));
      }

    /*
     */
    
    void setCurrentBid(double curBid)
      {
      if(this.curBid != curBid)
        {
        bidChanged = true;
        this.curBid = curBid;
        }
      }

    /*
     */

    boolean isCurrentBidChanged()
      {
      boolean flag = bidChanged;
      bidChanged = false;

      return(flag);
      }
    
    /*
     */
        
    void setMaximumBid(double maxBid)
      {
      this.maxBid = maxBid;
      }

    /*
     */
    
    double getMaximumBid()
      {
      return(maxBid);
      }

    /*
     */
    
    String getMaximumBidFormatted()
      {
      return(formatCurrency(maxBid, currency));
      }

    /*
     */
    
    double getMinimumBid()
      {
      double amt = 0;
      
      if(hasBids())
        {
        amt = curBid;
        amt += getBidIncrement(curBid);
        }
      else
        amt = curBid;

      return(amt);
      }

    /*
     */
    
    String getMinimumBidFormatted()
      {
      return(formatCurrency(getMinimumBid(), currency));
      }
    
    /*
     */
    
    Date getEndTime()
      {
      return(endTime);
      }

    /*
     */
    
    void setEndTime(Date endTime)
      {
      this.endTime = endTime;
      }

    /*
     */
   
    String getHighBidder()
      {
      return(highBidder);
      }

    /*
     */
    
    void setHighBidder(String highBidder)
      {
      this.highBidder = highBidder;
      }

    /*
     */
    
    int getQuantity()
      {
      return(quantity);
      }

    /*
     */
    
    void setQuantity(int quantity)
      {
      this.quantity = quantity;
      }

    /*
     */
    
    int getBidQuantity()
      {
      return(bidQuantity);
      }

    /*
     */
    
    void setBidQuantity(int bidQuantity)
      {
      this.bidQuantity = bidQuantity;
      }
    
    /*
     */
    
    int getBidDelta()
      {
      return(bidDelta);
      }

    /*
     */
    
    void setBidDelta(int bidDelta)
      {
      this.bidDelta = bidDelta;
      }

    /*
     */
    
    boolean isDutch()
      {
      return(dutch);
      }

    /*
     */
    
    void setDutch(boolean dutch)
      {
      this.dutch = dutch;
      }

    /*
     */
    
    int getTimeLeft()
      {
      if(endTime == null)
        return(0);
      
      long now = System.currentTimeMillis() + getTimeLag();

      long left = (endTime.getTime() - now) / 1000;
      
      return(left < 0 ? 0 : (int)left);
      }

    long getSnipeTime()
      {
      long time = endTime.getTime() - timeLag;
      time -= (bidDelta * 1000);

      return(time);
      }

    /*
     */
    
    String getTimeLeftFormatted()
      {
      int left = getTimeLeft();

      if(left == 0)
        return("Ended");

      StringBuffer sb = new StringBuffer(10);

      int days = left / 86400;
      left %= 86400;
      
      int hours = left / 3600;
      left %= 3600;
      
      int min = left / 60;
      left %= 60;
      
      int sec = left;
      
      if(days > 0)
        {
        sb.append(String.valueOf(days));
        sb.append("d ");
        }
      
      sb.append(String.valueOf(hours));
      sb.append(':');
      
      if(min < 10)
        sb.append('0');
      
      sb.append(String.valueOf(min));
      
      sb.append(':');
      if(sec < 10)
        sb.append('0');
      
      sb.append(String.valueOf(sec));

      return(sb.toString());
      }

    /*
     */

    boolean isPending()
      {
      return(status == PENDING);
      }
    
    /*
     */
    
    boolean isEnded()
      {
      return(getTimeLeft() == 0);
      }

    /*
     */

    void setNotified(boolean notified)
      {
      this.notified = notified;
      }

    boolean isNotified()
      {
      return(notified);
      }  
    }

  static double getBidIncrement(double amount)
    {
    if(amount < 1)
      return(0.5);
    else if(amount < 5)
      return(0.25);
    else if(amount < 25)
      return(0.5);
    else if(amount < 100)
      return(1);
    else if(amount < 250)
      return(2.5);
    else if(amount < 500)
      return(5);
    else if(amount < 1000)
      return(10);
    else if(amount < 2500)
      return(25);
    else if(amount < 5000)
      return(50);
    else
      return(100);
    }
  
  }

/* end of source file */
