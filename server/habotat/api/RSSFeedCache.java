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

/** An LRU cache for <code>RSSFeed</code>s. The cache allows RSS feeds
 * to be associated with keys of an arbitrary type.
 *
 * @author Mark Lindner
 */

public class RSSFeedCache<K> extends LRUCache<K, RSSFeed>
  {
  /** Construct a new <code>RSSFeedCache</code> with unlimited capacity. */
  
  public RSSFeedCache()
    {
    super();
    }

  /** Construct a new <code>RSSFeedCache</code> with the given maximum
   * capacity.
   *
   * @param capacity The maximum capacity of the cache.
   */
  
  public RSSFeedCache(int capacity)
    {
    super(capacity);
    }

  /** This method is called when the least recently accessed feed is dropped
   * from the cache. This occurs when a new feed is added to a cache that is
   * full. If the cache was created without a maximum capacity, this method
   * will never be called. The default implementation does nothing.
   *
   * @param feed The feed that was dropped.
   */
  
  protected void itemDropped(RSSFeed feed)
    {
    }

  }

// eof
