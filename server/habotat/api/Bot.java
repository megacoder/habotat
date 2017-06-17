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
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import kiwi.event.EventDispatcher;
import kiwi.util.KiwiUtils;

import org.jdom.Document;

/**
 * The base class for all IM Bots. This class provides methods for accessing
 * all of the services provided by the container.
 *
 * @author Mark Lindner
 */

public abstract class Bot implements BotLogger
  {
  private BotContext _context;
  private EventDispatcher _dispatcher
    = new EventDispatcher(this, "handleEvent", BotEvent.class);

  /** Construct a new Bot. The default constructor does nothing.
   */
  
  protected Bot()
    {
    }

  /** Get the name of the Bot.
   */
  
  public final String getName()
    {
    return((_context != null) ? _context.getName() : null);
    }

  /** Get the description text assigned to the Bot.
   */

  public final String getDescription()
    {
    return((_context != null) ? _context.getDescription() : null);
    }

  /** Get the keyword assigned to the Bot.
   */
  
  public final String getKeyword()
    {
    return((_context != null) ? _context.getKeyword() : null);
    }
  
  /** This method is called by the container when the Bot is loaded.
   */
  
  public final void setContext(BotContext context)
    {
    this._context = context;
    }

  /** Internal method for dispatching an event to the appropriate
   * <code>handleEvent()</code> method. This method should not be
   * called directly; it is for container use only.
   */

  public final void processEvent(BotEvent event)
    {
    try
      {
      _dispatcher.dispatch(event);
      }
    catch(InvocationTargetException ex)
      {
      if(ex != null)
        logError(ex.getCause());
      else
        logError("Unexpected exception during event handling.");
      }
    }
  
  /**
   * Handle container events, such as message events, user status
   * events, session timeout events, and timer events. This method may
   * be overloaded for different event types, in which case the most
   * specific method will be called automatically to handle a given
   * event.
   */
  
  public void handleEvent(BotEvent event)
    {
    }

  /** Send an instant message.
   *
   * @param target The IM screen name to send the message to.
   * @param message The text of the message.
   * @throws habotat.api.BotException If a communication error occurs.
   */
  
  protected final void sendMessage(String target, String message)
    throws BotException
    {
    if(_context != null)
      _context.sendMessage(target, message);
    }

  /** Get the maximum supported outgoing message length for this Bot.
   */

  protected final int getMaxMessageLength() throws BotException
    {
    if(_context == null)
      throw(new BotException("no context!"));

    return(_context.getMaxMessageLength());
    }

  /** Create a timer for real-time callbacks.
   *
   * @param spec A time specification that enumerates the times at which the
   * Bot should receive <code>TimerEvent</code>s.
   * @param repeating A flag indicating whether the timer is repeating, or
   * one-time.
   * @return A unique identifier which can be used as a handle for this timer.
   * @throws habotat.api.BotException If the container has already allocated
   * the maximum number of timers for this Bot.
   */
  
  protected final int createTimer(TimeSpec spec, boolean repeating)
    throws BotException
    {
    return((_context != null) ? _context.createTimer(spec, repeating): -1);
    }

  /** Create a repeating timer for real-time callbacks.
   *
   * @param spec A time specification that enumerates the times at which the
   * Bot should receive <code>TimerEvent</code>s.
   * @return A unique identifier which can be used as a handle for this timer.
   * @throws habotat.api.BotException If the container has already allocated
   * the maximum number of timers for this Bot.
   */
  
  protected final int createTimer(TimeSpec spec) throws BotException
    {
    return(createTimer(spec, true));
    }

  /** Create a timer for real-time callbacks.
   *
   * @param time The time at which the Bot should receive a
   * <code>TimerEvent</code>.
   * @return A unique identifier which can be used as a handle for this timer.
   * @throws habotat.api.BotException If the container has already allocated
   * the maximum number of timers for this Bot.
   */
  
  protected final int createTimer(Calendar time)
    throws BotException
    {
    return((_context != null) ? _context.createTimer(time): -1);
    }

  /** Destroy a timer.
   *
   * @param id The unique identifier for the timer.
   * @throws habotat.api.BotException If the specified timer does not exist,
   * or does not belong to this Bot.
   */
  
  protected final void destroyTimer(int id) throws BotException
    {
    if(_context != null)
      _context.destroyTimer(id);
    }

  /** Destroy all timers allocated for this Bot.
   */
  
  protected final void destroyAllTimers() throws BotException
    {
    if(_context != null)
      _context.destroyAllTimers();
    }

  /** Add an IM user to the watch list. A UserStatusEvent will be sent to the
   * calling bot whenever the user signs on or off.
   *
   * @throws habotat.api.BotException If the Bot is not currently active.
   */

  protected final void addWatchedUser(String user) throws BotException
    {
    if(_context != null)
      _context.addWatchedUser(user);
    }

  /** Remove an IM user from the watch list.
   *
   * @throws habotat.api.BotException If the Bot is not currently active.
   */

  protected final void removeWatchedUser(String user) throws BotException
    {
    if(_context != null)
      _context.removeWatchedUser(user);
    }
  

  /** Fetch the document at the specified URL.
   *
   * @param url The URL of the document.
   * @return The contents of the document, as a string.
   * @throws habotat.api.BotException If the URL is invalid or a communication
   * error occurred.
   */
  
  protected final String fetchURL(URL url)
    throws BotException
    {
    if(_context == null)
      return(null);
    else
      return(_context.fetchURL(url));
    }

  /** Apply an XSL transformation to an XML document.
   *
   * @param name The name of the XSL document to apply.
   * @param xml The XML document to transform.
   * @return The results of the transformation, as a string.
   * @throws habotat.api.BotException If the specified XSL document does not
   * exist, or if a transformer error occurred.
   */
  
  protected final String applyTransform(String name, String xml)
    throws BotException
    {
    if(_context == null)
      return(null);

    return(_context.applyStyleSheet(name, xml));
    }

  /**
   * Register an XSL document with the XSLT manager. XSL documents
   * must be registered before they can be used to perform
   * transformations on XML data.
   *
   * @param name The name for the document.
   * @param xslt The XSL document.
   * @throws habotat.api.BotException If the named document already
   * exists, or if the maximum number of XSLT documents are already
   * registered for this Bot.
   */

  protected final void addTransform(String name, String xslt)
    throws BotException
    {
    if(_context == null)
      return;

    _context.addStyleSheet(name, xslt);
    }

  /**
   * Unregister an XSL document from the XSLT manager.
   *
   * @param name The name of the document.
   * @throws habotat.api.BotException If an XSL document with the given name
   * is not registered for this Bot.
   */

  protected final void removeTransform(String name) throws BotException
    {
    if(_context == null)
      return;

    _context.removeStyleSheet(name);
    }

  /** This method is called when the Bot is started by the container. The
   * default implementation does nothing.
   */
  
  public void start()
    {
    }

  /** This method is called when the Bot is stopped by the container. The
   * default implementation does nothing.
   */
  
  public void stop()
    {
    }

  /** Load the Bot's persisted data into memory.
   *
   * @throws habotat.api.BotException If the data could not be read.
   */

  public final void loadData() throws BotException
    {
    InputStream ins = null;

    if(_context == null)
      throw(new BotException("no context!"));
    
    try
      {
      ins = _context.getDataInputStream();
      
      if(ins != null)
        loadData(ins);
      }
    catch(IOException ex)
      {
      throw(new BotException("Error obtaining input stream", ex));
      }
    finally
      {
      if(ins != null)
        try { ins.close(); } catch(IOException ex) { }
      }
    }

  /** Load the Bot's persisted data into memory from the given stream. This
   * method is a no-op by default and should be overridden by subclassers.
   *
   * @throws habotat.api.BotException If an error occurs while reading
   * the data.
   */

  protected synchronized void loadData(InputStream ins) throws BotException
    {
    // no-op default
    }

  /** Write the Bot's data to persistent store.
   *
   * @throws habotat.api.BotException If the data could not be written.
   */

  public final void saveData() throws BotException
    {
    OutputStream outs = null;

    if(_context == null)
      throw(new BotException("no context!"));
    
    try
      {
      outs = _context.getDataOutputStream();

      if(outs != null)
        saveData(outs);
      }
    catch(IOException ex)
      {
      throw(new BotException("Error obtaining output stream", ex));
      }
    finally
      {
      if(outs != null)
        try { outs.close(); } catch(IOException ex) { }
      }
    }

  /** Write the Bot's data to the given output stream. This method is
   * a no-op by default and should be overridden by subclassers.
   *
   * @throws habotat.api.BotException If an error occurs while writing the
   * data.
   */
  
  protected synchronized void saveData(OutputStream outs) throws BotException
    {
    // no-op default
    }
  
  /** Parse a command string into words, breaking the text on whitespace but
   * preserving literal strings that are enclosed in double quotes.
   *
   * @param command The text to parse.
   * @return The array of parsed words.
   */

  protected final String[] parseCommand(String command)
    {
    return(StringUtils.parseCommand(command));
    }

  /** Load a text resource into a string.
   *
   * @param path The path to the resource, relative to the calling class.
   * @return The resource, loaded as a string.
   * @throws habotat.api.BotException If the resource could not be loaded.
   */

  protected final String loadResource(String path) throws BotException
    {
    InputStream input = getClass().getResourceAsStream(path);

    if(input == null)
      throw(new BotException("Resource not found: " + path));

    try
      {
      ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
    
      byte buf[] = new byte[4096];
      int b;
      
      while((b = input.read(buf)) > 0)
        output.write(buf, 0, b);

      input.close();

      return(output.toString());
      }
    catch(IOException ex)
      {
      throw(new BotException("Failed to read resource: " + path));
      }
    }

  /**
   * Get the UI object for this Bot. The default implementation returns
   * <b>null</b>, indicating that no UI is available.
   */

  public PluginUI getUI()
    {
    return(null);
    }

  /**
   * Deserialize XML data from an input stream.
   *
   * @param input The input stream to read the XML data from.
   * @return The XML data, as a JDOM <code>Document</code> object.
   * @throws java.io.IOException If an I/O error occurred.
   * @throws habotat.api.BotException If an XML parsing error occurred.
   */

  protected final Document readXML(InputStream input)
    throws IOException, BotException
    {
    return((_context != null) ? _context.readXML(input) : null);
    }

  /**
   * Serialize XML data to an output stream.
   *
   * @param document The JDOM <code>Document</code> object to serialize.
   * @param output The output stream to write the XML data to.
   * @throws java.io.IOException If an I/O error occurred.
   */

  protected final void writeXML(Document document, OutputStream output)
    throws IOException
    {
    if(_context != null)
      _context.writeXML(document, output);
    }

  /**
   */

  public final void logInfo(String text)
    {
    if(_context != null)
      _context.logInfo(text);
    }

  /**
   */

  public final void logWarning(String text)
    {
    if(_context != null)
      _context.logWarning(text);
    }

  /**
   */

  public final void logError(String text)
    {
    if(_context != null)
      _context.logError(text);
    }

  /**
   * Log an error message along with a throwable as a stack trace.
   *
   * @param text The message.
   * @param t The throwable.
   */

  public final void logError(String text, Throwable t)
    {
    if(_context != null)
      _context.logError(text + "\n" + KiwiUtils.stackTraceToString(t));
    }
  
  /**
   * Log a throwable as a stack trace error message.
   *
   * @param t The throwable.
   */

  public final void logError(Throwable t)
    {
    if(_context != null)
      _context.logError(KiwiUtils.stackTraceToString(t));
    }

  /**
   * Fetch the session object associated with the given user.
   *
   * @param user The AIM user.
   * @return The session object for the given user, or <b>null</b> if no such
   * session exists.
   */

  protected final BotSession fetchSession(String user)
    {
    return((_context != null) ? _context.fetchSession(user) : null);
    }

  /**
   * Release the session object associated with the given user.
   *
   * @param user The AIM user.
   */

  protected final void releaseSession(String user)
    {
    if(_context != null)
      _context.releaseSession(user);
    }

  /**
   * Reserve a session object and associate it with the given user. All future
   * instant messages from the given user will be passed unparsed to the
   * calling bot, until either the session times out or is released via a call
   * to <code>releaseSession()</code>.
   *
   * @param user The AIM user.
   * @throws habotat.api.BotException If no free sessions are available.
   */
  
  protected final BotSession reserveSession(String user) throws BotException
    {
    return((_context != null) ? _context.reserveSession(user) : null);
    }

  }

/* end of source file */
