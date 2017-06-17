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

package habotat.bots.stock;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jdom.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 *
 * For more info see http://www.gummy-stuff.org/Yahoo-data.htm
 */

public class StockBot extends Bot
  {
  private String helpText = "";
  private HashMap<String, ArrayList<String>> stocks;
  public static final int MAX_STOCKS = 10;
  private static final String NODE_USERS = "users", NODE_USER = "user",
    ATTR_NAME = "name", NODE_STOCK = "stock";
  private static final String URL_PREFIX = "http://finance.yahoo.com/d/quotes.csv?";
  private static final String URL_DETAIL = "http://finance.yahoo.com/q?s=";
  
  /*
   */
  
  public StockBot()
    {
    stocks = new HashMap<String, ArrayList<String>>();
    }

  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();

    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append("</b> - show quotes for all stocks in portfolio<br><b>");
    sb.append(kwd);
    sb.append(" list</b> - list stocks in portfolio<br><b>");
    sb.append(kwd);
    sb.append(" get</b> <i>stock</i> ... - get quotes for one or more stocks<br><b>");
    sb.append(kwd);
    sb.append(" add</b> <i>stock</i> - add <i>stock</i> to portfolio<br><b>");
    sb.append(kwd);
    sb.append(" remove</b> <i>stock</i> - remove <i>stock</i> from portfolio<br><b>");
    sb.append(kwd);
    sb.append(" clear</b> - remove all stocks from portfolio");

    helpText = sb.toString();
    }
  
  /*
   */
  
  public void start()
    {
    logInfo("StockBot started!");

    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }
    
    buildHelpText();
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      saveData();
      }
    catch(BotException ex)
      {
      logError("Unable to save data", ex);
      }

    logInfo("StockBot stopped!");
    }

  /*
   */

  protected void loadData(InputStream ins) throws BotException
    {
    try
      {
      Document data = readXML(ins);

      stocks.clear();

      Element root = data.getRootElement();
      Element userNode = XMLUtils.getChildElem(root, NODE_USERS, true);
      List list = userNode.getChildren(NODE_USER);
      Iterator iter = list.iterator();
      while(iter.hasNext())
        {
        Element node = (Element)iter.next();

        String name = XMLUtils.getStringAttr(node, ATTR_NAME, null, true);
        ArrayList<String> vec = getList(name);

        List list2 = node.getChildren(NODE_STOCK);
        Iterator iter2 = list2.iterator();
        for(int i = 0; (i < MAX_STOCKS) && iter2.hasNext(); i++)
          {
          Element node2 = (Element)iter2.next();

          String stock = XMLUtils.getStringAttr(node2, ATTR_NAME, null, true);

          if(! vec.contains(stock))
            vec.add(stock);
          }
        }
      }
    catch(InvalidXMLException ex)
      {
      throw(new BotException("Invalid XML data: " + ex.getMessage()));
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to load data", ex));
      }
    }

  /*
   */

  protected void saveData(OutputStream outs) throws BotException
    {
    logInfo("Saving data!");
    
    Element root = new Element("botdata");
    Document doc = new Document(root);

    Element usersNode = new Element(NODE_USERS);
    root.addContent(usersNode);

    Iterator<String> iter = stocks.keySet().iterator();
    while(iter.hasNext())
      {
      String name = iter.next();
      Element userNode = new Element(NODE_USER);
      XMLUtils.setStringAttr(userNode, ATTR_NAME, name);

      usersNode.addContent(userNode);

      ArrayList<String> vec = stocks.get(name);
      if(vec == null || vec.size() == 0)
        continue;

      Iterator<String> iter2 = vec.iterator();
      while(iter2.hasNext())
        {
        String stock = iter2.next();

        Element stockNode = new Element(NODE_STOCK);
        XMLUtils.setStringAttr(stockNode, ATTR_NAME, stock);

        userNode.addContent(stockNode);
        }
      }

    try
      {
      writeXML(doc, outs);
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to save data", ex));
      }
    }
  
  /*
   */

  private ArrayList<String> getList(String user)
    {
    ArrayList<String> v = stocks.get(user);
    if(v == null)
      {
      v = new ArrayList<String>();
      stocks.put(user, v);
      }

    return(v);
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String args[] = parseCommand(mevt.getMessage());

      if(args.length == 0)
        {
        ArrayList<String> v = getList(mevt.getSender());

        if((v == null) || (v.size() == 0))
          sendMessage(mevt.getSender(), helpText);
        else
          sendMessage(mevt.getSender(), fetchStocks(v));
        }
      else if((args.length == 1) && "list".startsWith(args[0]))
        {
        StringBuffer sb = new StringBuffer();
        sb.append("Portfolio:");
        ArrayList<String> v = getList(mevt.getSender());

        if((v == null) || (v.size() == 0))
          sb.append(" <i>Empty</i>");
        else
          {
          sb.append("<b>");
          Iterator<String> iter = v.iterator();
          while(iter.hasNext())
            {
            sb.append(' ');
            sb.append(iter.next());
            }
          sb.append("</b>");
          }

        sendMessage(mevt.getSender(), sb.toString());
        }
      else if((args.length == 2) && "add".startsWith(args[0]))
        {
        ArrayList<String> v = getList(mevt.getSender());

        if(v.size() == MAX_STOCKS)
          {
          sendMessage(mevt.getSender(), "You already have the maximum number of stocks in your portfolio.");
          return;
          }

        String stock = args[1].toUpperCase();
        String verify = verifyStock(stock);
        if(verify == null)
          sendMessage(mevt.getSender(), "That is not a valid ticker symbol.");
        else
          {
          if(!v.contains(stock))
            {
            sendMessage(mevt.getSender(), verify + " added to portfolio.");
            v.add(stock);
            }
          else
            sendMessage(mevt.getSender(), "That stock is already in your portfolio.");
          }
        }
      else if((args.length == 2) && "remove".startsWith(args[0]))
        {
        ArrayList<String> v = getList(mevt.getSender());
        String stock = args[1].toUpperCase();

        if(! v.remove(stock))
          sendMessage(mevt.getSender(), "That stock is not in your portfolio.");
        else
          sendMessage(mevt.getSender(), "Removed stock <b>" + stock + "</b>");
        }
      else if((args.length == 1) && "clear".startsWith(args[0]))
        {
        ArrayList<String> v = getList(mevt.getSender());
        v.clear();
        sendMessage(mevt.getSender(), "Portfolio cleared.");
        }
      else if((args.length >= 2) && "get".startsWith(args[0]))
        {
        ArrayList<String> v = new ArrayList<String>();
        int mx = Math.min(args.length, 10);
        for(int i = 1; i < mx; i++)
          v.add(args[i]);

        sendMessage(mevt.getSender(), fetchStocks(v));
        }
      else
        sendMessage(mevt.getSender(), helpText);
      }
    catch(BotException ex)
      {
      logError(ex.toString());
      }
    }

  /*
   */

  private String fetchStocks(ArrayList<String> stocks)
    {
    try
      {
      StringBuffer u = new StringBuffer(40);
      u.append(URL_PREFIX);
      u.append("s=");

      for(int i = 0; i < stocks.size(); i++)
        {
        if(i > 0)
          u.append('+');
        u.append(stocks.get(i));
        }

      u.append("&f=sl1c1m");
      
      URL url = new URL(u.toString());

      String csv = fetchURL(url);

      StringBuffer sb = new StringBuffer();
      sb.append("Latest quotes:");

      StringTokenizer st = new StringTokenizer(csv, "\n");
      while(st.hasMoreTokens())
        {
        String quote = st.nextToken();

        List<String> values = StringUtils.parseCSVLine(quote);

        float chg = 0.0f;

        try
          {
          chg = Float.parseFloat(values.get(2));
          }
        catch(NumberFormatException ex) {}

        String symbol = values.get(0);
        
        sb.append("<br><b><a href=\"");
        sb.append(URL_DETAIL);
        sb.append(symbol);
        sb.append("\">");
        sb.append(symbol);
        sb.append("</a>");
        sb.append("</b>   ");

        sb.append("<font color=\"");
        if(chg < 0.0f)
          sb.append("red");
        else if(chg > 0.0f)
          sb.append("#14742d");
        else
          sb.append("black");
        sb.append("\">");
        
        sb.append(values.get(1));
        sb.append("  ");
        sb.append(values.get(2));
        sb.append("</font>");
        sb.append("   (");
        sb.append(values.get(3));
        sb.append(")");
        }

      return(sb.toString());
      }
    catch(Exception ex)
      {
      logError(ex);
      return("<i>An error occurred while retrieving quotes.</i>");
      }
    }

  private String verifyStock(String stock)
    {
    String detail = null;
    
    try
      {
      URL url = new URL(URL_PREFIX + "s=" + stock + "&f=snl1");
      String csv = fetchURL(url);
      List<String> values = StringUtils.parseCSVLine(csv);
      if(values != null && values.size() >= 3)
        {
        if(values.get(0).equals(values.get(1))
           && (values.get(2).equals("0.00")))
          return(null);
        
        StringBuffer sb = new StringBuffer(30);
        sb.append("<b>");
        sb.append(stock);
        sb.append("</b> (");
        sb.append(values.get(1));
        sb.append(')');
        detail = sb.toString();
        }
      }
    catch(Exception ex)
      {
      logError(ex);
      }
    
    return(detail);
    }

  }

/* end of source file */
