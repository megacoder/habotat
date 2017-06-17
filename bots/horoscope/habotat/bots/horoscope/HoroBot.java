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

package habotat.bots.horoscope;

import java.io.*;
import java.net.*;
import java.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class HoroBot extends Bot
  {
  private String news = null;
  private URL urls[];
  private int refreshTimer = 0;
  private static final String zodiac[]
    = new String[] { "Aquarius", "Aries", "Cancer",
                     "Capricorn", "Gemini", "Leo",
                     "Libra", "Pisces", "Sagittarius",
                     "Scorpio", "Taurus", "Virgo" };
  private static final String zodiacList = "<b>Aq</b>uarius <b>Ar</b>ies <b>Can</b>cer <b>Cap</b>ricorn <b>G</b>emini <b>Le</b>o <b>Li</b>bra <b>P</b>isces <b>Sa</b>gittarius <b>Sc</b>orpio <b>T</b>aurus <b>V</b>irgo";
  private String horoscopes[];
  private String helpText = null;
  private HashMap<String, Integer> zodiacSigns;

  /*
   */
  
  public HoroBot()
    {
    super();
    zodiacSigns = new HashMap<String, Integer>();

    urls = new URL[zodiac.length];
    horoscopes = new String[zodiac.length];

    try
      {
      for(int i = 0; i < zodiac.length; i++)
        {
        urls[i] = new URL("http://interglacial.com/rss/onion_" + zodiac[i]
                          + ".rdf");

        horoscopes[i] = null;
        }
      }
    catch(Exception ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  private int verifySign(String sign)
    {
    sign = sign.toLowerCase();
    
    for(int i = 0; i < zodiac.length; i++)
      {
      if(zodiac[i].toLowerCase().startsWith(sign))
        return(i);
      }

    return(-1);
    }
  
  /*
   */

  private void buildHelpText()
    {
    StringBuffer sb = new StringBuffer();
    String kwd = getKeyword();

    sb.append("Commands:<br><b>");
    sb.append(kwd);
    sb.append("</b> - display your horoscope<br><b>");
    sb.append(kwd);
    sb.append("</b> <i>sign</i> - display horoscope for <i>sign</i><br><b>");
    sb.append(kwd);
    sb.append(" set</b> <i>sign</i> - set your zodiac <i>sign</i><br>");
    sb.append("where <i>sign</i> is one of: " + zodiacList);

    helpText = sb.toString();
    }
  
  /*
   */
  
  public void start()
    {
    TimeSpec tspec = new TimeSpec(3, 0); // 3:00 AM

    try
      {
      refreshTimer = createTimer(tspec);
      }
    catch(BotException ex) { }

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
      destroyTimer(refreshTimer);
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
    }

  /*
   */

  protected void loadData(InputStream ins) throws BotException
    {
    logInfo("Loading data");
    
    try
      {
      BufferedReader reader = new BufferedReader(new InputStreamReader(ins));

      zodiacSigns.clear();
      String s;
      
      while((s = reader.readLine()) != null)
        {
        StringTokenizer st = new StringTokenizer(s, " \t");
        if(st.countTokens() != 2)
          continue;

        String user = st.nextToken();
        String sign = st.nextToken();

        logInfo(user + " -> " + sign);
        zodiacSigns.put(user, new Integer(sign));
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

      Iterator<String> iter = zodiacSigns.keySet().iterator();
      while(iter.hasNext())
        {
        String user = iter.next();
        Integer sign = zodiacSigns.get(user);

        writer.write(user + " " + sign.intValue());
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
  
  private void sendHoroscope(String target, int sign) throws BotException
    {
    if(horoscopes[sign] == null)
      {
      String rawNews = fetchURL(urls[sign]);

      horoscopes[sign] = applyTransform("rss", rawNews);
      }

    logInfo("Sending horoscope to: " + target);
    sendMessage(target, horoscopes[sign]);
    }

  /*
   */

  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      String args[] = parseCommand(mevt.getMessage());
      int sign = -1;

      if(args.length == 0)
        {
        Integer val = zodiacSigns.get(mevt.getSender());
        if(val != null)
          sign = val.intValue();
        }
      else if(args.length == 1)
        sign = verifySign(args[0]);
      else if((args.length == 2) && args[0].equalsIgnoreCase("set"))
        {
        sign = verifySign(args[1]);
        if(sign != -1)
          zodiacSigns.put(mevt.getSender(), new Integer(sign));
        }

      if(sign != -1)
        sendHoroscope(mevt.getSender(), sign);
      else
        sendMessage(mevt.getSender(), helpText);
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }

  /*
   */
  
  public void handleEvent(TimerEvent tevt)
    {
    if(tevt.getTimerID() == refreshTimer)
      {
      // clear all the horoscopes, so they'll get refreshed.
      
      for(int i = 0; i < horoscopes.length; i++)
        horoscopes[i] = null;
      
      logInfo("Horoscopes cleared.");
      }
    }
  
  }

/* end of source file */
