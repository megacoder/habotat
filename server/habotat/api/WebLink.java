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

import java.net.*;

/**
 * An object representing a Web link. A Web link consists of a URL for a
 * resource on the Web, and a textual title describing that resource.
 *
 *
 * @author Mark Lindner
 */

public class WebLink
  {
  private URL url;
  private String title = null;
  private String html = null;

  /** Construct a new <code>WebLink</code> with no title.
   *
   * @param url The URL for the link.
   */
  
  public WebLink(URL url)
    {
    this(url, null);
    }

  /** Construct a new <code>WebLink</code> with no title.
   *
   * @param url The URL for the link.
   */
  
  public WebLink(String url) throws MalformedURLException
    {
    this(new URL(url), null);
    }

  /** Construct a new <code>WebLink</code>.
   *
   * @param url The URL for the link.
   * @param title The title for the link.
   */
  
  public WebLink(URL url, String title)
    {
    this.url = url;

    if(title != null)
      {
      title = title.trim();
      if(title.equals(""))
        title = null;
      }
    
      this.title = title;
    }

  /** Construct a new <code>WebLink</code>.
   *
   * @param url The URL for the link.
   * @param title The title for the link.
   */
  
  public WebLink(String url, String title) throws MalformedURLException
    {
    this(new URL(url), title);
    }

  /** Get the HTML representation of this link.
   *
   * @return The link, formatted as HTML.
   */
  
  public String toHTML()
    {
    if(html == null)
      {
      StringBuffer sb = new StringBuffer(40);
      sb.append("<a href=\"");
      sb.append(url.toString());
      sb.append("\">");
      sb.append(title != null ? title : url.toString());
      sb.append("</a>");
      
      html = sb.toString();
      }

    return(html);
    }

  /** Set the URL for this link.
   *
   * @param url The URL.
   */
  
  public void setURL(URL url)
    {
    this.url = url;
    html = null;
    }

  /** Get the URL for this link.
   *
   * @return The URL.
   */
  
  public URL getURL()
    {
    return(url);
    }

  /** Set the title for this link.
   *
   * @param title The title.
   */
  
  public void setTitle(String title)
    {
    this.title = title;
    html = null;
    }

  /** Get the title for this link.
   *
   * @return The title.
   */
  
  public String getTitle()
    {
    return(title);
    }

  /** Return a string representation of this link.
   *
   * @return The URL for this link, as a string.
   */
  
  public String toString()
    {
    return(url.toString());
    }
  
  }

/* end of source file */
