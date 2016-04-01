package camix;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ClientChatTest extends TestCase {

    ServiceChat serviceChat ;
	
	@Before
	public void setUp(){
		this.serviceChat = EasyMock.createMock(ServiceChat.class);
	}
	
	/**
	 * test de la sortie du Chat par le client lors de l'appelle de la methode quitter le chat
	 * @throws IOException
	 */
	@Test
	public void testSortieDuChat() throws IOException {
		String surnom = "surnom" ;
		String commande = "/q" ;
		CanalChat canal = new CanalChat("nomCanal");
		//on creer notre client
		ClientChat client = new ClientChat(serviceChat, null, surnom, canal) ;
		//on mock service chat concernant la methode a tester quitteChat
		this.serviceChat.quitteChat(client);
		EasyMock.replay(serviceChat);
		
		
		try {
			//on recupere la method
			Method methodeTraiteMessage = client.getClass().getDeclaredMethod("traiteMessage",String.class);
			methodeTraiteMessage.setAccessible(true);
			//on appelle la methode
			methodeTraiteMessage.invoke(client, commande);
		} catch (NoSuchMethodException e) {
			fail("la methode n'existe pas") ;
		} catch (SecurityException e) {
			fail("la methode ne correspond pas") ;
		} catch (IllegalAccessException e) {
			fail("nous n'avons pas l'acces a la methode");
		} catch (IllegalArgumentException e) {
			fail("les arguments de la méthode ne correspondent pas");
		} catch (InvocationTargetException e) {
			fail("l'appel de la methode engendre une erreur");
		}
		//on regarde si notre methode quitteChat est bien appelé
		EasyMock.verify(this.serviceChat);

	}
	
	
}
