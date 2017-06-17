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

package habotat.bots.news;

import java.io.*;
import java.net.*;
import java.util.*;

import habotat.api.*;

/**
 * @author Mark Lindner
 */

public class NewsBot extends Bot
  {
  private String urlPrefix = "http://news.search.yahoo.com/news/rss?p=";

  /*
   */
  
  public NewsBot()
    {
    super();
    }

  /*
   */

  public void start()
    {
    logInfo("NewsBot started!");
    }

  /*
   */

  public void stop()
    {
    logInfo("NewsBot stopped!");
    }

  /*
   */
  
  private void sendNews(String target, String keywords) throws BotException
    {
    try
      {
      String keywordsEncoded = URLEncoder.encode(keywords, "UTF-8");
      URL url = new URL(urlPrefix + keywordsEncoded);
      
      logInfo("Fetching URL: " + url.toString());
      
      String rawNews = fetchURL(url);
      String news = applyTransform("rss", rawNews);
      
      sendMessage(target, news);
      }
    catch(UnsupportedEncodingException ex)
      {
      // won't happen
      }
    catch(MalformedURLException ex)
      {
      logError(ex);
      }
    }

  /*
   */

  public void handleEvent(MessageEvent mevt)
    {
    try
      {
      StringTokenizer tok = new StringTokenizer(mevt.getMessage());

      if(tok.countTokens() == 0)
        {
        sendMessage(mevt.getSender(), "Commands:<br><b>" + getKeyword()
                    + "</b> <i>keywords</i> - displays news headlines matching <i>keywords</i>.");
        }
      else
        {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        while(tok.hasMoreTokens())
          {
          if(!first)
            sb.append(' ');
          else
            first = false;

          sb.append(tok.nextToken());
          }

        sendNews(mevt.getSender(), sb.toString());
        }
      }
    catch(BotException ex)
      {
      logError(ex);
      }
    }
  
  }

/* end of source file */
