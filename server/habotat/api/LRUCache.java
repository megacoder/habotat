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

/** An LRU cache for arbitrary data.
 *
 * @author Mark Lindner
 */

public class LRUCache<K, V> extends LinkedHashMap<K, V>
  {
  private int maxCapacity = 0;

  /** Construct a new <code>LRUCache</code> with unlimited capacity. */
  
  public LRUCache()
    {
    super();
    }

  /** Construct a new <code>LRU</code> with the given maximum capacity.
   *
   * @param capacity The maximum capacity of the cache.
   */
  
  public LRUCache(int capacity)
    {
    super(capacity, 0.75f, true);

    maxCapacity = capacity;
    }

  /**
   */
  
  protected final boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
    boolean drop = (maxCapacity > 0) && (size() > maxCapacity);

    if(drop)
      itemDropped(eldest.getValue());

    return(drop);
    }

  /** This method is called when the least recently accessed item is dropped
   * from the cache. This occurs when a new item is added to a cache that is
   * full. If the cache was created without a maximum capacity, this method
   * will never be called. The default implementation does nothing.
   *
   * @param item The item that was dropped.
   */

  protected void itemDropped(V item)
    {
    }
  
  }

/* end of source file */
