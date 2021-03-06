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
package org.mobicents.servlet.sip.startup.loading.rules.request;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.URI;

/**
 * @author Thomas Leseney
 */
public class Param implements Extractor {
	
	private String param;
	
	public Param(String token, String param) {
		if (!token.equals("uri")) {
			throw new IllegalArgumentException("Invalid expression: param after " + token);
		}
		this.param = param;
	}
	
	public Object extract(Object input) {
		URI uri = (URI) input;
		if (uri.isSipURI()) {
			return ((SipURI) uri).getParameter(param);
		} else if ("tel".equals(uri.getScheme())) {
			return ((TelURL) uri).getParameter(param);
		}
		return null;
	}
}
