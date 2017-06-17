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

package habotat.engine;

import java.io.*;
import java.net.*;
import java.util.*;

import kiwi.io.*;
import kiwi.util.*;

import org.jdom.*;

import habotat.api.*;

/** This class simply delegates to the bot manager, passing along the BotDef
 * that is associated with the calling bot.
 *
 * @author Mark Lindner
 */

class BotContextImpl implements BotContext
  {
  private BotManager manager;
  private BotDef bot;

  /*
   */
  
  BotContextImpl(BotManager manager, BotDef bot)
    {
    this.manager = manager;
    this.bot = bot;
    }

  /*
   */
  
  public void sendMessage(String target, String message) throws BotException
    { 
    manager.sendMessage(bot, target, message);
    }

  /*
   */

  public int getMaxMessageLength()
    {
    return(manager.getMaxMessageLength(bot));
    }

  /*
   */
  
  public int createTimer(TimeSpec spec, boolean repeating) throws BotException
    {
    return(manager.createTimer(bot, spec, repeating));
    }

  /*
   */
  
  public int createTimer(Calendar time) throws BotException
    {
    return(manager.createTimer(bot, time));
    }

  /*
   */
  
  public void destroyTimer(int id) throws BotException
    {
    manager.destroyTimer(bot, id);
    }

  /*
   */
  
  public void destroyAllTimers() throws BotException
    {
    manager.destroyAllTimers(bot);
    }

  /*
   */
  
  public String getName()
    {
    return(bot.getName());
    }

  /*
   */
  
  public String getKeyword()
    {
    return(bot.getKeyword());
    }

  /*
   */
  
  public String getDescription()
    {
    return(bot.getDescription());
    }

  /*
   */
  
  public String fetchURL(URL url) throws BotException
    {

    try
      {
      HTMLPage pg = HTMLPage.fetch(url, null, false);

      return(pg.getText());
      }
    catch(IOException ex)
      { 
      throw(new BotException("communication error", ex));
      }
    }

  /*
   */
  
  public void addStyleSheet(String name, String data) throws BotException
    {
    manager.addStyleSheet(bot, name, data);
    }

  /*
   */
  
  public void removeStyleSheet(String name) throws BotException
    {
    manager.removeStyleSheet(bot, name);
    }

  /*
   */
  
  public String applyStyleSheet(String name, String data) throws BotException
    {
    return(manager.applyStyleSheet(bot, name, data));
    }

  /*
   */
  
  public InputStream getDataInputStream() throws BotException, IOException
    {
    return(manager.getDataInputStream(bot));
    }

  /*
   */
  
  public OutputStream getDataOutputStream() throws BotException, IOException
    {
    return(manager.getDataOutputStream(bot));
    }

  /*
   */
  
  public Document readXML(InputStream input) throws IOException, BotException
    {
    return(manager.readXML(input));
    }

  /*
   */
  
  public void writeXML(Document doc, OutputStream output) throws IOException
    {
    manager.writeXML(doc, output);
    }

  /*
   */
  
  public void logInfo(String text)
    {
    bot.logInfo(text);
    }

  /*
   */
  
  public void logWarning(String text)
    {
    bot.logWarning(text);
    }

  /*
   */
  
  public void logError(String text)
    {
    bot.logError(text);
    }

  /*
   */
  
  public BotSession fetchSession(String user)
    {
    return(manager.fetchSession(bot, user));
    }

  /*
   */
  
  public BotSession reserveSession(String user) throws BotException
    {
    return(manager.reserveSession(bot, user));
    }
  
  /*
   */

  public void releaseSession(String user)
    {
    manager.releaseSession(bot, user);
    }

  /*
   */

  public void addWatchedUser(String user) throws BotException
    {
    manager.addWatchedUser(bot, user);
    }

  /*
   */

  public void removeWatchedUser(String user) throws BotException
    {
    manager.removeWatchedUser(bot, user);
    }
  
  }

/* end of source file */
