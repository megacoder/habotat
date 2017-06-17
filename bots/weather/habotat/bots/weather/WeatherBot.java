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

package habotat.bots.weather;

import java.io.*;
import java.net.*;
import java.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class WeatherBot extends Bot
  {
  private String urlPrefix = "http://rssweather.com/rss.php?forecast=zandh&alt=rss20a&zipcode=";
  private HashMap<String, Integer> zipCodes;
  private LRUCache<Integer, String> cache;
  private String helpText;
  private int cacheTimer, saveTimer;

  /**
   */
  
  public WeatherBot()
    {
    super();
    zipCodes = new HashMap<String, Integer>();
    cache = new LRUCache<Integer, String>();
    }

  /*
   */
  
  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();
    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append("</b> - display weather for your zipcode<br><b>");
    sb.append(kwd);
    sb.append("</b> <i>zipcode</i> - display weather for <i>zipcode</i><br><b>");
    sb.append(kwd);
    sb.append(" set</b> <i>zipcode</i> - set your <i>zipcode</i>");

    helpText = sb.toString();
    }

  /*
   */

  public void start()
    {
    logInfo("WeatherBot started!");

    try
      {
      String styleSheet = loadResource("xsl/weather.xsl");
      addTransform("rssweather", styleSheet);
      }
    catch(BotException ex)
      {
      logError(ex);
      }

    try
      {
      loadData();
      }
    catch(BotException ex)
      {
      logError("Unable to load data", ex);
      }

    try
      {
      TimeSpec tspec = new TimeSpec();
      tspec.setAllHours();
      tspec.clearAllMinutes();
      tspec.setMinute(0);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      saveTimer = createTimer(tspec);

      tspec = new TimeSpec();
      for(int i = 0; i < 24; i+= 4)
        tspec.setHour(i);
      tspec.clearAllMinutes();
      tspec.setMinute(0);
      tspec.setAllDays();
      tspec.setAllMonths();
      tspec.setAllDaysOfWeek();

      cacheTimer = createTimer(tspec);
      }
    catch(BotException ex) { }

    buildHelpText();
    }

  /*
   */
  
  public void stop()
    {
    try
      {
      destroyTimer(saveTimer);
      destroyTimer(cacheTimer);
      }
    catch(BotException ex)
      {
      }

    try
      {
      removeTransform("rssweather");
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

    logInfo("WeatherBot stopped!");
    }

  /*
   */
  
  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");
    
    BufferedReader reader = null;
    
    try
      {
      reader = new BufferedReader(new InputStreamReader(ins));

      zipCodes.clear();
      String s;
      
      while((s = reader.readLine()) != null)
        {
        StringTokenizer st = new StringTokenizer(s, " \t");
        if(st.countTokens() != 2)
          continue;

        String user = st.nextToken();
        String zip = st.nextToken();

        int z = 0;
        
        try
          {
          z = Integer.parseInt(zip);
          }
        catch(NumberFormatException ex)
          {
          }

        if(z < 10000 || z > 99999)
          continue;

        logInfo(user + " -> " + z);
        zipCodes.put(user, z);
        }
      
      reader.close();
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
    logInfo("Saving data");
    
    try
      {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs));

      Iterator<String> iter = zipCodes.keySet().iterator();
      while(iter.hasNext())
        {
        String user = iter.next();
        Integer zip = zipCodes.get(user);

        writer.write(user + " " + zip);
        writer.newLine();
        }

      writer.close();
      }
    catch(IOException ex)
      {
      throw(new BotException("Unable to save data", ex));
      }
    }

  /*
   */
  
  private void sendWeather(String target, int zip) throws BotException
    {
    String weather = cache.get(zip);
    
    if(weather == null)
      {
      sendMessage(target,
                  "One moment while I prepare your weather report....");
      
      try
        {
        URL url = new URL(urlPrefix + zip);
        String rawWeather = fetchURL(url);
        weather = applyTransform("rssweather", rawWeather);

        cache.put(zip, weather);
        }
      catch(MalformedURLException ex)
        {
        logError(ex);
        }
      }

    logInfo("Sending weather to: " + target);
    sendMessage(target, weather);
    }

  /*
   */
  
  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String args[] = parseCommand(mevt.getMessage());
      int zip = 0;

      if(args.length == 0)
        {
        Integer izip = zipCodes.get(mevt.getSender());
        if(izip != null)
          zip = izip.intValue();
        }
      else if(args.length == 1)
        {
        try
          {
          zip = Integer.parseInt(args[0]);
          }
        catch(NumberFormatException ex) { }

        if(zip < 10000 || zip > 99999)
          {
          sendMessage(mevt.getSender(), "That is not a valid zip code.");
          return;
          }
        }
      else if((args.length == 2) && args[0].equalsIgnoreCase("set"))
        {
        zip = 0;

        try
          {
          zip = Integer.parseInt(args[1]);
          }
        catch(NumberFormatException ex) { }

        if(zip < 10000 || zip > 99999)
          {
          sendMessage(mevt.getSender(), "That is not a valid zip code.");
          return;
          }
        
        zipCodes.put(mevt.getSender(), zip);
        }

      if(zip != 0)
        sendWeather(mevt.getSender(), zip);
      else
        sendMessage(mevt.getSender(), helpText);
      }
    catch(Exception ex)
      {
      logError(ex);
      }
    }

  /**
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
      catch(BotException ex)
        {
        logError("Unable to save data", ex);
        }
      }

    else if(id == cacheTimer)
      cache.clear();
    }
  
  }

/* end of source file */
