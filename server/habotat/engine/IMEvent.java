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

import java.util.*;

/**
 * A general-purpose IM event.
 *
 * @author Mark Lindner
 */

class IMEvent extends EventObject
  {
  private String sender;
  private String message;

  /** Construct a new <code>IMEvent</code>.
   *
   * @param source The object that is the source of the event.
   */

  IMEvent(Object source, String sender, String message)
    {
    super(source);

    this.sender = sender;
    this.message = message;
    }

  /*
   */

  String getSender()
    {
    return(sender);
    }

  /*
   */
  
  String getMessage()
    {
    return(message);
    }

  }

/* end of source file */
