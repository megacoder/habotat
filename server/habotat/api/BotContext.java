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

package habotat.api;

import java.io.*;
import java.net.URL;
import java.util.Calendar;

import org.jdom.Document;

/** An interface representing the execution engine in which a Bot is
 * embedded.  The corresponding methods in the <code>Bot</code> base
 * class automatically delegate to these methods, so this interface
 * does not need to be used directly by Bot implementors. See {@link
 * habotat.api.Bot the class habotat.api.Bot} for descriptions of
 * these methods.
 *
 * @author Mark Lindner
 */

public interface BotContext
  {
  // things that can only be done while bot is running:
  
  public void sendMessage(String target, String message) throws BotException;

  public int getMaxMessageLength();
  
  public int createTimer(TimeSpec spec, boolean repeating) throws BotException;

  public int createTimer(Calendar time) throws BotException;
  
  public void destroyTimer(int id) throws BotException;

  public void destroyAllTimers() throws BotException;

  public String getName();

  public String getKeyword();

  public String getDescription();

  public String fetchURL(URL url) throws BotException;

  public void addStyleSheet(String name, String data) throws BotException;
  
  public void removeStyleSheet(String name) throws BotException;
  
  public String applyStyleSheet(String name, String data) throws BotException;

  public void addWatchedUser(String user) throws BotException;

  public void removeWatchedUser(String user) throws BotException;
  
  // things that can be done when it's not running:

  public InputStream getDataInputStream() throws IOException, BotException;

  public OutputStream getDataOutputStream() throws IOException, BotException;

  public Document readXML(InputStream input) throws IOException, BotException;

  public void writeXML(Document doc, OutputStream out) throws IOException;

  public void logInfo(String text);

  public void logWarning(String text);

  public void logError(String text);

  public BotSession fetchSession(String user);

  public BotSession reserveSession(String user) throws BotException;
  
  public void releaseSession(String user);
  }

/* end of source file */
