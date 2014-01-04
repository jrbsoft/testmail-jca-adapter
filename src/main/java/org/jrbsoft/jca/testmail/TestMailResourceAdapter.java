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
package org.jrbsoft.jca.testmail;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * GreenMailResourceAdapter
 * 
 * @version $Revision: $
 */
@Connector(reauthenticationSupport = false, transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class TestMailResourceAdapter implements ResourceAdapter, java.io.Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = 1L;

	/** The logger */
	private static Logger log = Logger.getLogger(TestMailResourceAdapter.class.getName());
	
	private ConcurrentMap<String, GreenMailUser> userMap = new ConcurrentHashMap<String, GreenMailUser>();

	/** The GreenMail instance, if started */
	private GreenMail greenMail = null;

	/** protocols */
	@ConfigProperty(defaultValue = "smtp:3025")
	private String protocols;
	
	/**
	 * Default constructor
	 */
	public TestMailResourceAdapter() {
	}
	
	/**
	 * Returns the configured protocols.
	 * @return The value
	 */
	public String getProtocols() {
		return protocols;
	}

	/**
	 * Sets the protocols.
	 * @param protocols as comma separated list. Supported are: smtp, smtps, pop3, pop3s, imap, imaps
	 */
	public void setProtocols(String protocols) {
		this.protocols = protocols;
	}

	public GreenMail getGreenMail() {
		return greenMail;
	}

	/**
	 * This is called during the activation of a message endpoint.
	 * 
	 * @param endpointFactory A message endpoint factory instance.
	 * @param spec An activation spec JavaBean instance.
	 * @throws ResourceException generic exception
	 */
	public void endpointActivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec) throws ResourceException {
		log.finest("endpointActivation()");

	}

	/**
	 * This is called when a message endpoint is deactivated.
	 * 
	 * @param endpointFactory A message endpoint factory instance.
	 * @param spec An activation spec JavaBean instance.
	 */
	public void endpointDeactivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec) {
		log.finest("endpointDeactivation()");

	}

	/**
	 * This is called when a resource adapter instance is bootstrapped.
	 * 
	 * @param ctx A bootstrap context containing references
	 * @throws ResourceAdapterInternalException indicates bootstrap failure.
	 */
	public void start(final BootstrapContext ctx) throws ResourceAdapterInternalException {
		log.finest("start()");
		final ServerSetup[] setup = getSetup();
		greenMail = new GreenMail(setup);
		greenMail.start();
	}

	/**
	 * This is called when a resource adapter instance is undeployed or during
	 * application server shutdown.
	 */
	public void stop() {
		log.finest("stop()");
		greenMail.stop();
	}

	/**
	 * This method is called by the application server during crash recovery.
	 * 
	 * @param specs An array of ActivationSpec JavaBeans
	 * @throws ResourceException generic exception
	 * @return An array of XAResource objects
	 */
	public XAResource[] getXAResources(final ActivationSpec[] specs) throws ResourceException {
		log.finest("getXAResources()");
		return null;
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return A hash code value for this object.
	 */
	@Override
	public int hashCode() {
		int result = 17;
		if (protocols != null) {
			result += 31 * result + 7 * protocols.hashCode();
		} else {
			result += 31 * result + 7;
		}
		return result;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 * 
	 * @param other The reference object with which to compare.
	 * @return true if this object is the same as the obj argument, false otherwise.
	 */
	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof TestMailResourceAdapter)) {
			return false;
		}
		
		boolean result = true;
		final TestMailResourceAdapter obj = (TestMailResourceAdapter) other;
		if (result) {
			if (protocols == null) {
				result = obj.getProtocols() == null;
			} else {
				result = protocols.equals(obj.getProtocols());
			}
		}
		return result;
	}
	
	//-----------------------------------------------------------------------||
	//-- Private Methods ----------------------------------------------------||
	//-----------------------------------------------------------------------||
	
	private ServerSetup[] getSetup() {
		final String[] items = protocols.split(",", -1);
		final ServerSetup[] protocolSetups = new ServerSetup[items.length];
		for (int i = 0 ; i < items.length; i++) {
			String item = items[i];
			final String[] subitem = item.split(":", -1);
			if (subitem.length == 2) {
				final String protocol = subitem[0];
				final String port = subitem[1];
				protocolSetups[i] = new ServerSetup(Integer.valueOf(port), null, protocol);
			}
		}		
		return protocolSetups;
	}
	
}
