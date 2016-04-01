package camix;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class CanalChatTest extends TestCase{
	
	public ClientChat client ;
	public CanalChat canal ;
	
	@Before
	public void setUp(){
		this.client = EasyMock.createMock(ClientChat.class);
		this.canal = new CanalChat("toto");
	}
	
	
	@Test
	public void testAjouteClient_nonPresent(){		
		EasyMock.expect(client.donneId()).andReturn("anyId");
		EasyMock.expectLastCall().times(4);
		EasyMock.replay(this.client);
		
		assertEquals((int)this.canal.donneNombreClients(), 0);
		assertFalse(this.canal.estPresent(this.client)) ;
		
		this.canal.ajouteClient(this.client);
		assertEquals((int)this.canal.donneNombreClients(), 1);
		assertTrue(this.canal.estPresent(this.client)) ;
		EasyMock.verify(this.client);
	}

	@Test
	public void testAjouteClient_present(){		
		EasyMock.expect(client.donneId()).andReturn("anyId");
		EasyMock.expectLastCall().times(5);
		EasyMock.replay(this.client);
		
		assertEquals((int)this.canal.donneNombreClients(), 0);
		assertFalse(this.canal.estPresent(this.client)) ;
		
		this.canal.ajouteClient(this.client);
		assertEquals((int)this.canal.donneNombreClients(), 1);
		
		this.canal.ajouteClient(this.client);
		assertEquals((int)this.canal.donneNombreClients(), 1);
		assertTrue(this.canal.estPresent(this.client)) ;
		
		EasyMock.verify(this.client);
	}

}
