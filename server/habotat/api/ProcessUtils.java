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

import java.io.*;

/**
 * A collection of static utility methods for running external programs.
 *
 * @author Mark Lindner
 */

public class ProcessUtils
  {
  private static Runtime rt = Runtime.getRuntime();

  /*
   */

  private ProcessUtils() { }
  
  /** Execute a program, capturing its output.
   *
   * @param command The command (with arguments) to execute.
   * @param buf The buffer to write the output of the command to.
   * @param maxLength The maximum number of bytes of output to capture.
   * Additional output will be read and discarded.
   * @throws java.io.IOException If an error
   * @return The exit status from the program.
   */
  
  public static int execute(String command[], StringBuffer buf,
                            int maxLength) throws IOException
    {
    return(execute(command, null, buf, maxLength));
    }

  /** Execute a program, capturing its output.
   *
   * @param command The command (with arguments) to execute.
   * @param dir The working directory for the subprocess.
   * @param buf The buffer to write the output of the command to.
   * @param maxLength The maximum number of bytes of output to capture.
   * Additional output will be read and discarded.
   * @throws java.io.IOException If an error
   * @return The exit status from the program.
   */
  
  public static int execute(String command[], File dir, StringBuffer buf,
                            int maxLength) throws IOException
    {
    Process proc;

    if(dir != null)
      proc = rt.exec(command, null, dir);
    else
      proc = rt.exec(command);

    try
      {
      proc.getOutputStream().close();
      }
    catch(IOException ex) { }

    ReaderThread r1 = new ReaderThread(
      new BufferedInputStream(proc.getInputStream()), buf, maxLength);
    r1.start();
    ReaderThread r2 = new ReaderThread(
      new BufferedInputStream(proc.getErrorStream()), null, maxLength);
    r2.start();

    int r;

    for(;;)
      {
      try
        {
        r = proc.waitFor();

        r1.interrupt();
        r1.join();

        r2.interrupt();
        r2.join();
        
        break;
        }
      catch(InterruptedException ex) { }
      }

    return(r);
    }

  /*
   */
  
  private static class ReaderThread extends Thread
    {
    private BufferedInputStream stream;
    private StringBuffer buffer;
    private byte buf[] = new byte[80];
    int len = 0, left;
    
    ReaderThread(BufferedInputStream stream, StringBuffer buffer,
                 int maxLength)
      {
      this.stream = stream;
      this.buffer = buffer;
      left = maxLength;
      
      setDaemon(true);
      }
    
    public void run()
      {
      for(;;)
        {
        try
          {
          int b = stream.read(buf);
          if((buffer != null) && (b > 0) && (left > 0))
            {
            int r = b;
            if(r > left)
              r = left;
            
            buffer.append(new String(buf, 0, r));
            
            left -= r;
            }
          else if(b <= 0)
            break;
          }
        catch(IOException ex)
          {
          break;
          }
        }

      try
        {
        stream.close();
        }
      catch(IOException ex) { }
      }
    }

  }

/* end of source file */
