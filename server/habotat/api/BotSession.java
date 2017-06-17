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

/** Session object for maintaining contextual information in a Bot. A
 * fixed number of sessions are created for each Bot. Each session is
 * associated with an IM user. A session must be explicitly reserved before
 * it can be used, and released when it is no longer needed. See
 * {@link habotat.api.Bot#fetchSession fetchSession() on habotat.api.Bot}.
 *
 * <p> Sessions have a <i>state</i>, which is an integer value, with 0
 * representing "no state".
 *
 * @author Mark Lindner
 */

public interface BotSession
  {
  /** Set the session state.
   *
   * @param state The new state.
   */
  
  public void setState(int state);

  /** Get the session state.
   *
   * @return The current state.
   */

  public int getState();

  /** Store a string in the session.
   *
   * @param key The lookup key.
   * @param value The string value to store.
   */
  
  public void putString(String key, String value);

  /** Restore a string from the session.
   *
   * @param key The lookup key.
   * @return The string value.
   */
  
  public String getString(String key);

  /** Store an int in the session.
   *
   * @param key The lookup key.
   * @param value The int value to store.
   */
  
  public void putInt(String key, int value);

  /** Restore an int from the session.
   *
   * @param key The lookup key.
   * @return The int value.
   */
  
  public int getInt(String key);

  /** Store a float in the session.
   *
   * @param key The lookup key.
   * @param value The float value to store.
   */
  
  public void putFloat(String key, float value);

  /** Restore a float from the session.
   *
   * @param key The lookup key.
   * @return The float value.
   */
  
  public float getFloat(String key);

  /** Store a boolean in the session.
   *
   * @param key The lookup key.
   * @param value The boolean value to store.
   */
  
  public void putBoolean(String key, boolean value);

  /** Restore a boolean from the session.
   *
   * @param key The lookup key.
   * @return The boolean value.
   */
  
  public boolean getBoolean(String key);

  /** Store an arbitrary object in the session.
   *
   * @param key The lookup key.
   * @param value The object to store.
   */
  
  public void putObject(String key, Object value);

  /** Restore an object from the session.
   *
   * @param key The lookup key.
   * @return The object.
   */
  
  public Object getObject(String key);

  }

/* end of source file */
