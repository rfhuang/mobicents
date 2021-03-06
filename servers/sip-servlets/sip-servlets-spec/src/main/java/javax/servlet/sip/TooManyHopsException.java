/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package javax.servlet.sip;
/**
 * Thrown when a SIP Servlet application attempts to obtain a Proxy object for a request with a Max-Forwards header with value 0.
 * The application may catch this exception and generate its own response. Otherwise the exception will propagate to the container which will catch it and generate a 483 (Too many hops) response.
 */
public class TooManyHopsException extends javax.servlet.ServletException{
    /**
     * Constructs a new TooManyHopsException exception, without any message.
     */
    public TooManyHopsException(){
         super();
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified message.
     * @param msg a String specifying the text of the exception message
     */
    public TooManyHopsException(java.lang.String msg){
         super(msg);
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public TooManyHopsException(java.lang.String message, java.lang.Throwable cause){
         super(message, cause);
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more than wrappers for other throwables.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public TooManyHopsException(java.lang.Throwable cause){
    	super(cause);
    }

}
