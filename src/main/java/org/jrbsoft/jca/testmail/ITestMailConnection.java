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
 * GreenMailConnection
 * 
 * @version $Revision: $
 */
public interface ITestMailConnection {

	public void setUser(String email, String password);

	public void setUser(String email, String login, String password);
	
	public void sendTextEmail(String to, String from, String subject, String msg, Session session);

	public void sendAttachmentEmail(String to, String from, String subject, String msg, final byte[] attachment, final String contentType, final String filename, final String description, final Session session) throws MessagingException, IOException;

	public MimeMessage[] getReceivedMessages();
	
	public MimeMessage newMimeMessage(InputStream inputStream);
	
	public MimeMessage newMimeMessage(String mailString) throws MessagingException;
	
	public String getBody(Part msg);
	
	public String getHeaders(Part msg);
	
	public String getWholeMessage(Part msg);

	public String toString(Part msg);

	public String getAddressList(Address[] addresses);

	public byte[] getBodyAsBytes(Part msg);

	public byte[] getHeaderAsBytes(Part part);

	public boolean hasNonTextAttachments(Part m);
	
	public int getLineCount(String str);
		
	public void deleteMessages(String email) throws RuntimeException;
	
	public void deleteUser(String email) throws RuntimeException;
	
	public void deleteAllUsers() throws RuntimeException;
	
}
