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

import java.util.*;

/**
 * An abstract class that represents a connection to an Instant Messenger
 * service.
 *
 * @author Mark Lindner
 */

public abstract class IMConnection
  {
  private ArrayList<IMConnectionListener> listeners
    = new ArrayList<IMConnectionListener>();

  /** Construct a new <code>IMConnection</code>.
   */

  protected IMConnection()
    {
    }

  /** Send an instant message.
   *
   * @param user The user to send the message to.
   * @param message The text of the message.
   * @throws habotat.api.IMException If an error occurs.
   */
  
  public abstract void sendMessage(String user, String message)
    throws IMException;

  /** Close the connection. */
  
  public abstract void close();

  /** Add a user to the list of users being watched for logins/logouts.
   *
   * @param user The user to watch.
   * @throws habotat.api.IMException If an error occurs.
   */
  
  public abstract void addWatchedUser(String user) throws IMException;

  /** Remove a user from the list of users being watched for logins/logouts.
   *
   * @param user The watched user.
   * @throws habotat.api.IMException If an error occurs.
   */
  
  public abstract void removeWatchedUser(String user) throws IMException;

  /** Add an <code>IMConnectionListener</code> to this connection's list of
   * listeners.
   *
   * @param listener The listener to add.
   */
  
  public final void addIMConnectionListener(IMConnectionListener listener)
    {
    listeners.add(listener);
    }

  /** Remove an <code>IMConnectionListener</code> from this connection's list
   * of listeners.
   *
   * @param listener The listener to remove.
   */
  
  public final void removeIMConnectionListener(IMConnectionListener listener)
    {
    listeners.remove(listener);
    }

  /** Notify all listeners that an instant message has been received.
   *
   * @param from The user who sent the message.
   * @param message The text of the message.
   */
  
  protected final void fireMessageReceived(String from, String message)
    {
    int sz = listeners.size();
    for(int i = 0; i < sz; i++)
      listeners.get(i).messageReceived(from, message);
    }

  /** Notify all listeners that a watched user has signed on.
   *
   * @param user The user who has signed on.
   */
  
  protected final void fireUserSignedOn(String user)
    {
    int sz = listeners.size();
    for(int i = 0; i < sz; i++)
      listeners.get(i).userSignedOn(user);
    }

  /** Notify all listeners that a watched user has signed off.
   *
   * @param user The user who has signed off.
   */
  
  protected final void fireUserSignedOff(String user)
    {
    int sz = listeners.size();
    for(int i = 0; i < sz; i++)
      listeners.get(i).userSignedOff(user);
    }

  /** Notify all listeners that the connection has been lost, most likely due
   * to a network error.
   */
  
  protected final void fireConnectionLost()
    {
    int sz = listeners.size();
    for(int i = 0; i < sz; i++)
      listeners.get(i).connectionLost();
    }
  
  }

/* end of source file */
