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
 * A Bot event indicating the receipt of an instant message.
 *
 * @author Mark Lindner
 */

public final class MessageEvent extends BotEvent
  {
  private String message = null;
  private String sender = null;

  /** Construct a new <code>MessageEvent</code>.
   *
   * @param source The object which is the source of the event.
   * @param message The text of the instant message.
   * @param sender The AIM user from which the message was received.
   */
  
  public MessageEvent(Object source, String message, String sender)
    {
    super(source);

    this.message = message;
    this.sender = sender;
    }

  /** Get the text of the instant message.
   *
   * @return The message text.
   */
  
  public String getMessage()
    {
    return(message);
    }

  /** Get the sender of the instant message.
   *
   * @return The sender.
   */
  
  public String getSender()
    {
    return(sender);
    }
  
  }

/* end of source file */
