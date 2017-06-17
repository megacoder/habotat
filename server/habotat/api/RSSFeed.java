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
 * An object representing an RSS feed. In addition to storing the feed type,
 * title, and URL, the object provides fields for assigning a keyword to the
 * feed, and for storing the content of the feed itself.
 *
 * @author Mark Lindner
 */

public class RSSFeed
  {
  /** RSS Type for "new" feed types (0.91, 0.92, 2.0) */
  public static final int RSS_NEW = 0;
  /** RSS Type for "old" feed types (0.90, 1.0) */
  public static final int RSS_OLD = 1;
  /** The maximum allowed value for an RSS <i>type</i> parameter. */
  public static final int RSS_MAX = RSS_OLD;
  private static final String feedTypes[] = { "0.91/0.92/2.0", "0.90/1.0" };
  private int type;
  private URL url;
  private String title;
  private String keyword;
  private String content = null;

  /** Construct a new, uninitialized <code>RSSFeed</code>.
   */
  
  public RSSFeed()
    {
    this(RSS_NEW, "", "", null);
    }

  /** Construct a new <code>RSSFeed</code> with the given type, title, keyword,
   * and URL.
   *
   * @param type The type of feed: <code>RSS_NEW</code> for RSS v0.91,
   * v0.92, or v2.0 feeds; or <code>RSS_OLD</code> for RSS v0.90 or
   * v1.0 feeds.
   * @param title The title of the feed.
   * @param keyword A keyword for the feed.
   * @param url The URL of the feed.
   */
  
  public RSSFeed(int type, String title, String keyword, URL url)
    {
    setType(type);
    this.title = title;
    this.keyword = keyword;
    this.url = url;
    }

  /** Set the URL for the feed.
   *
   * @param url The URL.
   */
  
  public void setURL(URL url)
    {
    this.url = url;
    }

  /** Get the URL for the feed.
   *
   * @return The URL.
   */
  
  public URL getURL()
    {
    return(url);
    }

  /** Set the title of the feed.
   *
   * @param title The title.
   */
  
  public void setTitle(String title)
    {
    this.title = title;
    }

  /** Get the title of the feed.
   *
   * @return The title.
   */
  
  public String getTitle()
    {
    return(title);
    }

  /** Set the keyword for the feed.
   *
   * @param keyword The keyword.
   */
  
  public void setKeyword(String keyword)
    {
    this.keyword = keyword;
    }

  /** Get the keyword for the feed.
   *
   * @return The keyword.
   */
  
  public String getKeyword()
    {
    return(keyword);
    }

  /** Set the type of the feed.
   *
   * @param type The type of feed: <code>RSS_NEW</code> for RSS v0.91,
   * v0.92, or v2.0 feeds; or <code>RSS_OLD</code> for RSS v0.90 or
   * v1.0 feeds.
   */

  public void setType(int type)
    {
    if(type < 0 || type > RSS_MAX)
      type = RSS_NEW;

    this.type = type;
    }

  /** Get the type of the feed.
   *
   * @return The type.
   */
  
  public int getType()
    {
    return(type);
    }

  /** Get the type of the feed, formatted as a string that is suitable for
   * display purposes.
   *
   * @return The formatted type.
   */
  
  public String getTypeAsString()
    {
    return(feedTypes[type]);
    }

  /** Set the content for the feed.
   *
   * @param content The textual content.
   */
  
  public void setContent(String content)
    {
    this.content = content;
    }

  /** Get the content for the feed.
   *
   * @return The textual content.
   */
  
  public String getContent()
    {
    return(content);
    }

  /** Get a string representation that is suitable for display purposes of
   * the given feed type.
   *
   * @param type The feed type.
   * @return The formatted type.
   * @throws java.lang.IllegalArgumentException If <i>type</i> is invalid.
   */
  
  public static String getStringForType(int type)
    {
    if(type < 0 || type > RSS_MAX)
      throw(new IllegalArgumentException("Invalid type"));

    return(feedTypes[type]);
    }
  
  }

/* end of source file */
