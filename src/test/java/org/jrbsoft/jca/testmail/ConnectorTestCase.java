
package org.jrbsoft.jca.testmail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConnectorTestCase {
	
	@Deployment
	public static EnterpriseArchive createDeployment() {
		final File ironJacamarXml = new File("../greenmail-adapter/src/main/resources/META-INF/ironjacamar.xml");
		final File raXml = new File("../greenmail-adapter/src/main/resources/META-INF/connector-ra.xml");

		final JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class, "test.jar");
		testArchive.addClasses(ConnectorTestCase.class);
		testArchive.addPackage(ITestMailConnectionFactory.class.getPackage());

		final File[] files = Maven.resolver().loadPomFromFile("pom.xml")
				.resolve("com.icegreen:greenmail:1.3.1b")
				.withTransitivity()
				.asFile();

		final JavaArchive greenMailArchive = ShrinkWrap.create(JavaArchive.class, "greenMail.jar")
				.addPackage(ITestMailConnectionFactory.class.getPackage())
				.addAsManifestResource(ironJacamarXml, "ironjacamar.xml")
				.addAsManifestResource(raXml, "connector-ra.xml");

		final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "greenMailAdapter.rar")
				.addAsLibrary(greenMailArchive)
				.addAsLibraries(files)
				.addAsManifestResource(ironJacamarXml, "ironjacamar.xml")
				.addAsManifestResource(raXml, "connector-ra.xml");

		final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "greenMail.ear")
				.addAsModule(rar)
				.addAsModule(testArchive);

		return ear;
	}

	@Resource(mappedName = "java:jboss/mail/greenMail")
	private Session mailSession;

	@Resource(mappedName = "java:/eis/TestMailConnectionFactory")
	private ITestMailConnectionFactory connectionFactory;

	@Test
	public void testGetConnection() throws Throwable {
		assertNotNull(connectionFactory);
		final ITestMailConnection connection1 = connectionFactory.getConnection();
		assertNotNull(connection1);
	}

	@Test
	public void testSendMailWithSession() throws Throwable {
		final ITestMailConnection connection = connectionFactory.getConnection();
		try {
			connection.setUser("testUser1@noreply", "password");
			connection.setUser("testUser2@noreply", "password");
			connection.sendTextEmail("testUser1@noreply", "testUser2@noreply", "subject", "I there", mailSession);		
			final MimeMessage[] msgs = connection.getReceivedMessages();
			assertTrue(msgs.length == 1);
		} finally {
			connection.deleteMessages("testUser1@noreply");
			connection.deleteMessages("testUser2@noreply");
		}
	}

	@Test
	public void testSendMail() throws Throwable {
		final ITestMailConnection connection = connectionFactory.getConnection();
		try {
			connection.setUser("testUser1@noreply", "password");
			connection.setUser("testUser2@noreply", "password");
	
			final MimeMessage m = new MimeMessage(mailSession);
			final Address from = new InternetAddress("testUser1@noreply");
			final Address[] to = new InternetAddress[] { new InternetAddress("testUser2@noreply") };
	
			m.setFrom(from);
			m.setRecipients(Message.RecipientType.TO, to);
			m.setSubject("JBoss AS 7 Mail");
			m.setSentDate(new java.util.Date());
			m.setContent("Mail sent from JBoss AS 7", "text/plain");
			Transport.send(m);
	
			final MimeMessage[] msgs = connection.getReceivedMessages();
			assertTrue("But was = " + msgs.length, msgs.length == 1);
		} finally {
			connection.deleteMessages("testUser1@noreply");
			connection.deleteMessages("testUser2@noreply");
		}
	}
}
