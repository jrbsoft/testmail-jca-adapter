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

import java.io.IOException;
import java.io.InputStream;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * GreenMailConnectionImpl
 * 
 * @version $Revision: $
 */
public class TestMailConnectionImpl implements ITestMailConnection {

	/** ManagedConnection */
	private TestMailManagedConnection mc;

	/** ManagedConnectionFactory */
	private TestMailManagedConnectionFactory mcf;

	/**
	 * Default constructor
	 * 
	 * @param mc GreenMailManagedConnection
	 * @param mcf GreenMailManagedConnectionFactory
	 */
	public TestMailConnectionImpl(final TestMailManagedConnection mc, final TestMailManagedConnectionFactory mcf) {
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
	public void setUser(final String email, final String password) {
		mc.setUser(email, password);
	}

	@Override
	public void setUser(final String email, final String login, final String password) {
		mc.setUser(email, login, password);
	}

	@Override
	public MimeMessage[] getReceivedMessages() {
		return mc.getReceivedMessages();
	}

	@Override
	public MimeMessage newMimeMessage(InputStream inputStream) {
		return mc.newMimeMessage(inputStream);
	}

	@Override
	public MimeMessage newMimeMessage(String mailString) throws MessagingException {
		return mc.newMimeMessage(mailString);
	}

	@Override
	public boolean hasNonTextAttachments(Part m) {
		return mc.hasNonTextAttachments(m);
	}

	@Override
	public int getLineCount(String str) {
		return mc.getLineCount(str);
	}

	@Override
	public String getBody(Part msg) {
		return mc.getBody(msg);
	}

	@Override
	public String getHeaders(Part msg) {
		return mc.getHeaders(msg);
	}

	@Override
	public String getWholeMessage(Part msg) {
		return mc.getWholeMessage(msg);
	}

	@Override
	public byte[] getBodyAsBytes(Part msg) {
		return mc.getBodyAsBytes(msg);
	}

	@Override
	public byte[] getHeaderAsBytes(Part part) {
		return mc.getHeaderAsBytes(part);
	}

	@Override
	public String toString(Part msg) {
		return mc.toString();
	}

	@Override
	public String getAddressList(Address[] addresses) {
		return mc.getAddressList(addresses);
	}

	@Override
	public void sendTextEmail(String to, String from, String subject, String msg, Session session) {
		mc.sendTextEmail(to, from, subject, msg, session);
	}

	@Override
	public void sendAttachmentEmail(String to, String from, String subject, String msg, byte[] attachment, String contentType, String filename, String description, Session session) throws MessagingException, IOException {
		mc.sendAttachmentEmail(to, from, subject, msg, attachment, contentType, filename, description, session);
	}

	@Override
	public void deleteMessages(String email) throws RuntimeException {
		mc.deleteMessages(email);
	}

	@Override
	public void deleteUser(String email) throws RuntimeException {
		mc.deleteUser(email);
	}

	@Override
	public void deleteAllUsers() throws RuntimeException {
		mc.deleteAllUsers();
	}

}
