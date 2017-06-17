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

/** Exception indicating invalid XML data.
 *
 * @author Mark Lindner
 */

public class InvalidXMLException extends Exception
  {
 
  /** Construct a new <code>InvalidXMLException</code>.
   */
  
  public InvalidXMLException()
    {
    super();
    }

  /** Construct a new <code>InvalidXMLException</code> with a message.
   *
   * @param message The exception message.
   */
  
  public InvalidXMLException(String message)
    {
    super(message);
    }

  /** Construct a new <code>InvalidXMLException</code> with a message and a cause.
   *
   * @param message The exception message.
   * @param cause The cause.
   */

  public InvalidXMLException(String message, Throwable cause)
    {
    super(message, cause);
    }

  /** Construct a new <code>InvalidXMLException</code> with a cause.
   *
   * @param cause The cause.
   */

  public InvalidXMLException(Throwable cause)
    {
    super(cause);
    }
  
  }

/* end of source file */
