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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * GreenMailManagedConnection
 * 
 * @version $Revision: $
 */
public class TestMailManagedConnection implements ManagedConnection {

	/** The logger */
	private static Logger log = Logger.getLogger(TestMailManagedConnection.class.getName());

	/** The logwriter */
	private PrintWriter logwriter;

	/** ManagedConnectionFactory */
	private TestMailManagedConnectionFactory mcf;

	/** Listeners */
	private List<ConnectionEventListener> listeners;

	/** Connection */
	private TestMailConnectionImpl connection;
	
	/** The users belonging to this connection  */
	private ConcurrentMap<String, GreenMailUser> userMap = new ConcurrentHashMap<String, GreenMailUser>();

	/**
	 * Default constructor
	 * 
	 * @param mcf mcf
	 */
	public TestMailManagedConnection(final TestMailManagedConnectionFactory mcf) {
		this.mcf = mcf;
		this.logwriter = null;
		this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
		this.connection = null;
	}

	/**
	 * Creates a new connection handle for the underlying physical connection
	 * represented by the ManagedConnection instance.
	 * 
	 * @param subject Security context as JAAS subject
	 * @param cxRequestInfo ConnectionRequestInfo instance
	 * @return generic Object instance representing the connection handle.
	 * @throws ResourceException generic exception if operation fails
	 */
	public Object getConnection(final Subject subject, final ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		log.finest("getConnection()");
		connection = new TestMailConnectionImpl(this, mcf);
		return connection;
	}

	/**
	 * Used by the container to change the association of an application-level
	 * connection handle with a ManagedConneciton instance.
	 * 
	 * @param connection Application-level connection handle
	 * @throws ResourceException generic exception if operation fails
	 */
	public void associateConnection(final Object connection) throws ResourceException {
		log.finest("associateConnection()");

		if (connection == null) {
			throw new ResourceException("Null connection handle");
		}

		if (!(connection instanceof TestMailConnectionImpl)) {
			throw new ResourceException("Wrong connection handle");
		}

		this.connection = (TestMailConnectionImpl) connection;
	}

	/**
	 * Application server calls this method to force any cleanup on the
	 * ManagedConnection instance.
	 * 
	 * @throws ResourceException generic exception if operation fails
	 */
	public void cleanup() throws ResourceException {
		log.finest("cleanup()");
	}

	/**
	 * Destroys the physical connection to the underlying resource manager.
	 * 
	 * @throws ResourceException generic exception if operation fails
	 */
	public void destroy() throws ResourceException {
		log.finest("destroy()");
	}

	/**
	 * Adds a connection event listener to the ManagedConnection instance.
	 * 
	 * @param listener A new ConnectionEventListener to be registered
	 */
	public void addConnectionEventListener(final ConnectionEventListener listener) {
		log.finest("addConnectionEventListener()");
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}
		listeners.add(listener);
	}

	/**
	 * Removes an already registered connection event listener from the
	 * ManagedConnection instance.
	 * 
	 * @param listener already registered connection event listener to be removed
	 */
	public void removeConnectionEventListener(final ConnectionEventListener listener) {
		log.finest("removeConnectionEventListener()");
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}
		listeners.remove(listener);
	}

	/**
	 * Close handle
	 * 
	 * @param handle The handle
	 */
	void closeHandle(final ITestMailConnection handle) {
		final ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
		event.setConnectionHandle(handle);
		for (ConnectionEventListener cel : listeners) {
			cel.connectionClosed(event);
		}

	}

	/**
	 * Gets the log writer for this ManagedConnection instance.
	 * 
	 * @return Character output stream associated with this Managed-Connection instance
	 * @throws ResourceException generic exception if operation fails
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		log.finest("getLogWriter()");
		return logwriter;
	}

	/**
	 * Sets the log writer for this ManagedConnection instance.
	 * 
	 * @param out Character Output stream to be associated
	 * @throws ResourceException generic exception if operation fails
	 */
	public void setLogWriter(final PrintWriter out) throws ResourceException {
		log.finest("setLogWriter()");
		logwriter = out;
	}

	/**
	 * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
	 * 
	 * @return LocalTransaction instance
	 * @throws ResourceException generic exception if operation fails
	 */
	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new NotSupportedException("getLocalTransaction() not supported");
	}

	/**
	 * Returns an <code>javax.transaction.xa.XAresource</code> instance.
	 * 
	 * @return XAResource instance
	 * @throws ResourceException generic exception if operation fails
	 */
	public XAResource getXAResource() throws ResourceException {
		throw new NotSupportedException("getXAResource() not supported");
	}

	/**
	 * Gets the metadata information for this connection's underlying EIS
	 * resource manager instance.
	 * 
	 * @return ManagedConnectionMetaData instance
	 * @throws ResourceException generic exception if operation fails
	 */
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		log.finest("getMetaData()");
		return new TestMailManagedConnectionMetaData();
	}
	
	public void setUser(final String email, final String password) {
		final GreenMailUser user = ((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().setUser(email, password);
		userMap.putIfAbsent(email, user);
	}

    public void setUser(final String email, final String login, final String password) {
    	final GreenMailUser user = ((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().setUser(email, login, password);
    	userMap.putIfAbsent(email, user);
    }
    	
    public void deleteMessages(final String email) throws RuntimeException {
    	final GreenMailUser user = userMap.get(email);
    	if (user != null) {
			try {
				final MailFolder folder = ((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().getManagers().getImapHostManager().getInbox(user);
				folder.deleteAllMessages();
			} catch (FolderException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
    	} else {
    		log.warning("User does not exists for this connection. Email address: " + email);
    	}
    }
	
	public void deleteUser(String email) throws RuntimeException {
		final GreenMailUser user = userMap.get(email);
    	if (user != null) {
			try {
				final MailFolder folder = ((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().getManagers().getImapHostManager().getInbox(user);
				folder.deleteAllMessages();
				((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().getManagers().getUserManager().deleteUser(user);
			} catch (FolderException | UserException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
    	} else {
    		log.warning("User does not exists for this connection. Email address: " + email);
    	}
    }
	
	public void deleteAllUsers() throws RuntimeException {
		final Iterator<GreenMailUser> it = userMap.values().iterator();		
		while (it.hasNext()) {
			final GreenMailUser user = it.next();
			deleteUser(user.getEmail());
			it.remove();
		}
	}
        
    public MimeMessage[] getReceivedMessages() {
    	return ((TestMailResourceAdapter)mcf.getResourceAdapter()).getGreenMail().getReceivedMessages();
    }
    
	public MimeMessage newMimeMessage(final InputStream inputStream) {
		return GreenMailUtil.newMimeMessage(inputStream);
	}

	public MimeMessage newMimeMessage(final String mailString) throws MessagingException {
		return GreenMailUtil.newMimeMessage(mailString);
	}

	public boolean hasNonTextAttachments(final Part m) {
		return GreenMailUtil.hasNonTextAttachments(m);
	}
	
	public int getLineCount(final String str) {
		return GreenMailUtil.getLineCount(str);
	}

	public String getBody(final Part msg) {
		return GreenMailUtil.getBody(msg);
	}

	public String getHeaders(final Part msg) {
		return GreenMailUtil.getHeaders(msg);
	}

	public String getWholeMessage(final Part msg) {
		return GreenMailUtil.getWholeMessage(msg);
	}

	public byte[] getBodyAsBytes(final Part msg) {
		return GreenMailUtil.getBodyAsBytes(msg);
	}

	public byte[] getHeaderAsBytes(final Part part) {
		return GreenMailUtil.getHeaderAsBytes(part);
	}

	public String toString(Part msg) {
		return GreenMailUtil.toString(msg);
	}
	
	public void sendTextEmail(final String to, final String from, final String subject, final String msg, final Session session) {
		try {
			Address[] tos = new javax.mail.Address[0];
			tos = new InternetAddress[] { new InternetAddress(to) };
			final Address[] froms = new InternetAddress[] { new InternetAddress(from) };
			final MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setSubject(subject);
			mimeMessage.setFrom(froms[0]);
			mimeMessage.setText(msg);
			Transport.send(mimeMessage, tos);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
		
	public String getAddressList(Address[] addresses) {
		return GreenMailUtil.getAddressList(addresses);
	}
	
	public void sendAttachmentEmail(final String to, final String from, final String subject, final String msg, final byte[] attachment, final String contentType, final String filename, final String description, final Session session) throws MessagingException, IOException {
		final Address[] tos = new InternetAddress[]{new InternetAddress(to)};
		final Address[] froms = new InternetAddress[]{new InternetAddress(from)};
		final MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(subject);
        mimeMessage.setFrom(froms[0]);

        final MimeMultipart multiPart = new MimeMultipart();
        final MimeBodyPart textPart = new MimeBodyPart();
        multiPart.addBodyPart(textPart);
        textPart.setText(msg);

        final MimeBodyPart binaryPart = new MimeBodyPart();
        multiPart.addBodyPart(binaryPart);

        final DataSource ds = new DataSource() {
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(attachment);
            }

            public OutputStream getOutputStream() throws IOException {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byteStream.write(attachment);
                return byteStream;
            }

            public String getContentType() {
                return contentType;
            }

            public String getName() {
                return filename;
            }
        };
        
        binaryPart.setDataHandler(new DataHandler(ds));
        binaryPart.setFileName(filename);
        binaryPart.setDescription(description);

        mimeMessage.setContent(multiPart);
        Transport.send(mimeMessage, tos);
	}
	
}
