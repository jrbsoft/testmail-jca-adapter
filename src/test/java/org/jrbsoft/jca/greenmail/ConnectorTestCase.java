
package org.jrbsoft.jca.greenmail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Logger;

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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(Arquillian.class)
public class ConnectorTestCase {
	private static Logger log = Logger.getLogger(ConnectorTestCase.class.getName());
	
	@Deployment
	public static EnterpriseArchive createDeployment() {
		final File ironJacamarXml = new File("../greenmail-adapter/src/main/resources/META-INF/ironjacamar.xml");
		final File raXml = new File("../greenmail-adapter/src/main/resources/META-INF/connector-ra.xml");

		final JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class, "test.jar");
		testArchive.addClasses(ConnectorTestCase.class);
		testArchive.addPackage(IGreenMailConnectionFactory.class.getPackage());

		final File[] files = Maven.resolver().loadPomFromFile("pom.xml")
				.resolve("com.icegreen:greenmail:1.3.1b")
				.withTransitivity()
				.asFile();

		final JavaArchive greenMailArchive = ShrinkWrap.create(JavaArchive.class, "greenMail.jar")
				.addPackage(IGreenMailConnectionFactory.class.getPackage())
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource(ironJacamarXml, "ironjacamar.xml")
				.addAsManifestResource(raXml, "connector-ra.xml");

		final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "greenMailAdapter.rar")
				.addAsLibrary(greenMailArchive)
				.addAsLibraries(files)
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsManifestResource(ironJacamarXml, "ironjacamar.xml")
				.addAsManifestResource(raXml, "connector-ra.xml");

		final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "greenMail.ear")
				.addAsModule(rar)
				.addAsModule(testArchive);

		return ear;
	}

	@Resource(mappedName = "java:jboss/mail/greenMail")
	private Session mailSession;

	@Resource(mappedName = "java:/eis/GreenMailConnectionFactory")
	private IGreenMailConnectionFactory connectionFactory1;

	@Test
	public void testGetConnection1() throws Throwable {
		assertNotNull(connectionFactory1);
		final IGreenMailConnection connection1 = connectionFactory1.getConnection();
		assertNotNull(connection1);
	}

	@Test
	public void testGetUtil() throws Throwable {
		assertNotNull(connectionFactory1);
		final IGreenMailConnection connection = connectionFactory1.getConnection();
		final GreenMailUtil util = connection.util();
		assertNotNull(util);
	}

	@Test
	public void testSendMail() throws Throwable {
		assertNotNull(connectionFactory1);
		final IGreenMailConnection connection = connectionFactory1.getConnection();
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
		assertTrue(msgs.length == 1);
	}

}
