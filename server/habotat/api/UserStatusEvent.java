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

/**
 * A Bot event indicating that a user's status has changed
 * (online vs offline).
 *
 * @author Mark Lindner
 */

public final class UserStatusEvent extends BotEvent
  {
  private String user = null;
  private boolean online;

  /** Construct a new <code>UserStatusEvent</code>.
   *
   * @param source The object which is the source of the event.
   * @param user The user whose state has changed.
   * @param online The online status of the user.
   */
  
  public UserStatusEvent(Object source, String user, boolean online)
    {
    super(source);

    this.user = user;
    this.online = online;
    }

  /** Get the user name.
   *
   * @return The user.
   */
  
  public String getUser()
    {
    return(user);
    }

  /** Get the online status of the user.
   *
   * @return The online status.
   */
  
  public boolean isOnline()
    {
    return(online);
    }
  
  }

/* end of source file */
