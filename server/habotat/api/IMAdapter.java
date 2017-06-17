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

import org.jdom.*;

/** An abstract base class for IM adapters. An IM adapter provides an
 * abstracted interface to a specific IM service.
 *
 * @author Mark Lindner
 */

public abstract class IMAdapter
  {
  /** The server host. */
  protected String host;
  /** The server port. */
  protected int port;

  protected static final String ATTR_HOST = "imServerHost",
    ATTR_PORT = "imServerPort";
  
  /** Construct a new <code>IMAdapter</code> with the given host and port.
   *
   * @param host The IM server host.
   * @param port The IM server port.
   */
  
  protected IMAdapter(String host, int port)
    {
    this.host = host;
    this.port = port;
    }

  /** Open a connection to the IM service.
   *
   * @param username The user to sign in as.
   * @param password The user's password.
   * @throws habotat.api.IMException If an error occurs.
   * @return A new<code>IMConnection</code>.
   */
  
  public abstract IMConnection openConnection(
    String username, String password) throws IMException;

  /** Get the maximum message length that the IM service allows.
   *
   * @return The maximum length, in characters.
   */

  public abstract int getMaxMessageLength();

  /** Get the UI for configuring this IM adapter. The default implementation
   * returns <b>null</b>, indicating that no UI is available.
   */

  public PluginUI getUI()
    {
    return(null);
    }

  /** Get the IM server host.
   */
  
  public String getHost()
    {
    return(host);
    }

  /** Set the IM server host.
   */
  
  public void setHost(String host)
    {
    this.host = host;
    }

  /** Get the IM server port.
   */
  
  public int getPort()
    {
    return(port);
    }

  /** Set the IM server pot.
   */
  
  public void setPort(int port)
    {
    this.port = port;
    }

  /** Write the adapter's settings to an XML element. The default
   * implementation does nothing.
   *
   * @param element The element to write to.
   */
  
  public void writeSettings(Element element)
    {
    }

  /** Read the adapter's settings from an XML element. The default
   * implementation does nothing.
   *
   * @param element The element to read from.
   */

  public void readSettings(Element element)
    {
    }

  }

/* end of source file */
