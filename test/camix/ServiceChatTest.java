package camix;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ServiceChatTest extends TestCase{
	
	private ClientChat client ;
	
	@Before
	public void setUp(){
		this.client = EasyMock.createMock(ClientChat.class);
	}
	
	@Test
	public void testInformeDepartClient(){
		final String surnom = "surnom" ;
		final String message = String.format(ProtocoleChat.MESSAGE_DEPART_CHAT, surnom) ;

		ServiceChat service = new ServiceChat("Canal",1234);
		EasyMock.expect(client.donneSurnom()).andReturn(surnom);
		
		EasyMock.expectLastCall().times(1) ;
		
		client.envoieContacts(message);
		
		EasyMock.replay(this.client);
		
		service.informeDepartClient(client);
		
		EasyMock.verify(client);
	}
	
	@Test
	public void testAjoutCanal_nonPresent(){
		ServiceChat service = new ServiceChat("MainCanal",1234);
		final String nomCanal = "SecondCanal" ;
		final String messageSucess = String.format(ProtocoleChat.MESSAGE_CREATION_CANAL, nomCanal) ;

		try {
			//on récupère les différents attributs
			Field attribut = ServiceChat.class.getDeclaredField("canaux");
			attribut.setAccessible(true);
			@SuppressWarnings("unchecked")
			Hashtable<String,CanalChat> lesCanaux = (Hashtable<String, CanalChat>) attribut.get(service);
			assertEquals("Le bon nombre de canaux",1, lesCanaux.size());
			
			client.envoieMessage(messageSucess);
			EasyMock.expectLastCall().times(1);

			EasyMock.replay(this.client);
			
			service.ajouteCanal(client, nomCanal);
			
			assertEquals("Le bon nombre de canaux",2, lesCanaux.size());
			
			EasyMock.verify(client);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("La récupération de l'attribut \"attribut\" pose problème (mal référencé) Exception : " +e) ;
		}
	}
	
	@Test
	public void testAjoutCanal_present(){
		final String nomCanal = "mainCanal" ;
		ServiceChat service = new ServiceChat(nomCanal,1234);
		final String messageEchec = String.format(ProtocoleChat.MESSAGE_CREATION_IMPOSSIBLE_CANAL, nomCanal) ;

		try {
			//on récupère les différents attributs
			Field attribut = ServiceChat.class.getDeclaredField("canaux");
			attribut.setAccessible(true);
			@SuppressWarnings("unchecked")
			Hashtable<String,CanalChat> lesCanaux = (Hashtable<String, CanalChat>) attribut.get(service);
			assertEquals("Le bon nombre de canaux",1, lesCanaux.size());
			
		
		
			client.envoieMessage(messageEchec);
			EasyMock.expectLastCall().times(1);
	
			EasyMock.replay(this.client);
			
			service.ajouteCanal(client, nomCanal);
			assertEquals("Le bon nombre de canaux",1, lesCanaux.size());
			EasyMock.verify(client);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("La récupération de l'attribut \"attribut\" pose problème (mal référencé) Exception : " +e) ;
		}	
	}
	

}
