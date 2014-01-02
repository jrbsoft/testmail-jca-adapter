/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
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
package org.jrbsoft.jca.greenmail;

import java.util.logging.Logger;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * GreenMailConnectionImpl
 * 
 * @version $Revision: $
 */
public class GreenMailConnectionImpl implements IGreenMailConnection {
	
	/** The logger */
	private static Logger log = Logger.getLogger(GreenMailConnectionImpl.class.getName());

	/** ManagedConnection */
	private GreenMailManagedConnection mc;

	/** ManagedConnectionFactory */
	private GreenMailManagedConnectionFactory mcf;

	/**
	 * Default constructor
	 * 
	 * @param mc
	 *            GreenMailManagedConnection
	 * @param mcf
	 *            GreenMailManagedConnectionFactory
	 */
	public GreenMailConnectionImpl(final GreenMailManagedConnection mc, final GreenMailManagedConnectionFactory mcf) {
		this.mc = mc;
		this.mcf = mcf;
	}

	/**
	 * Close
	 */
	public void close() {
		mc.closeHandle(this);
	}

	@Override
	public GreenMailUser setUser(final String email, final String password) {
		return mc.setUser(email, password);
	}

	@Override
	public GreenMailUser setUser(final String email, final String login, final String password) {
		return mc.setUser(email, login, password);
	}

	@Override
	public MimeMessage[] getReceivedMessages() {
		return mc.getReceivedMessages();
	}

	@Override
	public GreenMailUtil util() {
		return mc.util();
	}

}
