package camix;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Classe service chat de Camix.
 * 
 * @version 3.0.0.etu
 * @author Matthias Brun
 * 
 */
public class ServiceChat
{
	/**
	 * La socket de connexion des nouveaux clients.
	 */
	private ServerSocket socketChat; 

	/**
	 * L'ensemble des canaux du chat.
	 */
	private Hashtable<String, CanalChat> canaux;

	/**
	 * Le canal par défaut du chat.
	 */
	private CanalChat canalDefaut;

	private Integer port ;
	
	/**
	 * Constructeur d'un service chat.
	 *
	 * @param canal le nom du canal par défaut.
	 * @param port le port d'écoute du service.
	 *
	 * @throws IOException exception d'entrée/sortie.
	 *
	 */
	public ServiceChat(String canal, Integer port)
	{
		// Création de l'ensemble des canaux.
		this.canaux = new Hashtable<String, CanalChat>();

		// Création du canal par défaut.
		this.canalDefaut = new CanalChat(canal);
		this.canaux.put(this.canalDefaut.donneNom(), this.canalDefaut);
		
		// Initialisation de la socket.
		this.socketChat = null;
		
		this.port = port ;
	}

	/**
	 * Lancement du service du chat.
	 *
	 * @param port le port d'écoute du service.
	 * 
	 * @throws IOException exception d'entrée/sortie.
	 *
	 */
	public void lanceService() throws IOException
	{		
		// Ouverture de la socket serveur.
		try {
			this.socketChat = new ServerSocket(this.port);
		}
		catch (IOException ex) {
			System.err.print("Problème de création de la socket du chat.");
			throw ex;
		}
		
		// Lancement du service.
		try {
			this.service();
		}
		catch (IOException ex) {
			System.err.print("Problème lors du service du chat.");
			this.ferme();
			throw ex;
		}
	}
	
	/**
	 * Lancement du service du chat.
	 * 
	 * @throws IOException exception d'entrée/sortie.
	 *
	 * @see ServiceChatClient
	 */
	private void service() throws IOException 
	{
		// Mise en écoute sur la socket serveur.
		while (true) {
			// Création d'une socket de communication avec un client.
			final Socket socket = this.socketChat.accept();

			// Création d'un client.
			final ClientChat client = new ClientChat(this, socket, "?", this.canalDefaut);

			// Enregistrement du client dans le canal par défaut.
			this.canalDefaut.ajouteClient(client);

			// Lancement d'un thread de service au client.
			client.lanceService();
			
			// Informations d'arrivée d'un nouveau client dans le chat.
			this.informeArriveeClient(client);
			
			System.out.println("Ouverture connexion client (id : " + client.donneId() + ")");
		}
	}

	/**
	 * Fermeture du chat.
	 *
	 * @throws IOException exception d'entrée/sortie.
	 */
	private void ferme() throws IOException
	{
		// Fermeture de la socket serveur.
		try {
			this.socketChat.close();
		} 
		catch (IOException ex) {
			System.err.println("Problème de fermeture de la socket " + this.socketChat);
			throw ex;
		}	
	}
	
	/**
	 * Informe de l'arrivée d'un client dans le chat.
	 *
	 * <p>Le message d'arrivée d'un client dans le chat est envoyé à tous les clients du canal par défaut.</p>
	 * <p>Le message d'accueil d'un client dans le chat est envoyé au nouveau client."</p>
	 *
	 * @param client le nouveau client.
	 * 
	 * @see ProtocoleChat
	 */
	private void informeArriveeClient(ClientChat client)
	{
		String message;

		message = String.format(ProtocoleChat.MESSAGE_ARRIVEE_CHAT, this.canalDefaut.donneNom());
		client.envoieContacts(message);

		message = String.format(ProtocoleChat.MESSAGE_ACCUEIL_CHAT);
		client.envoieMessage(message);
	}

	/**
	 * Informe du départ d'un client du chat.
	 *
	 * <p>Le message de départ d'un client du chat est envoyé aux clients du canal du client partant.</p>
	 *
	 * @param client le client qui quitte le chat.
	 * 
	 * @see ProtocoleChat
	 */
	public void informeDepartClient(ClientChat client)
	{
		String message;

		message = String.format(ProtocoleChat.MESSAGE_DEPART_CHAT, client.donneSurnom());
		client.envoieContacts(message);
	}


	/**
	 * Change le surnom d'un client.
	 * 
	 * <p>Le nouveau surnom n'est pas contraint.</p>
	 * <p>Le message de changement de surnom du client est émis dans le canal du client.</p>
	 *
	 * @param client le client concerné.
	 * @param surnom le nouveau surnom.
	 * 
	 * @see ProtocoleChat 
	 */
	public void changeSurnomClient(ClientChat client, String surnom)
	{
		final String ancienSurnom = client.donneSurnom();
		String message;

		client.changeSurnom(surnom);
		message = String.format(ProtocoleChat.MESSAGE_CHANGEMENT_SURNOM, ancienSurnom, client.donneSurnom());
		client.envoieCanal(message);
	}

	/**
	 * Change le canal d'un client.
	 * 
	 * <p>Le canal voulu doit exister. S'il n'existe pas, le client ne change pas de canal.</p>
	 * <p>Un message de départ du client est émis dans le canal que quitte le client.</p>
	 * <p>Un message d'arrivée du client est émis dans le canal que rejoint le client.</p>
	 *
	 * @param client le client concerné.
	 * @param nom le nom du nouveau canal.
	 * 
	 * @see ProtocoleChat 
	 */
	public void changeCanalClient(ClientChat client, String nom)
	{
		// Synchronization :
		// Pour éviter de bouger un client dans un canal en cours de suppression.
		synchronized (this.canaux) {
			final CanalChat canal = this.canaux.get(nom);
			String message;

			if (canal != null) {
				message  = String.format(ProtocoleChat.MESSAGE_DEPART_CANAL, 
									client.donneSurnom(), client.donneCanal().donneNom());
				client.envoieCanal(message);

				client.changeCanal(canal);

				message  = String.format(ProtocoleChat.MESSAGE_ARRIVEE_CANAL, 
									client.donneSurnom(), client.donneCanal().donneNom());
				client.envoieCanal(message);
			} else {
				message = String.format(ProtocoleChat.MESSAGE_NON_EXISTENCE_CANAL_DEMANDE);
				client.envoieMessage(message);
			}
		}
	}

	/**
	 * Ajoute un canal au chat.
	 *
	 * <p>Le nom du canal n'est pas contraint.</p>
	 * <p>Si un canal du même nom existe déjà, le canal n'est pas créé 
	 * et un message d'impossibilité de création de canal est émis au client à l'origine de la commande.</p>
	 * <p>Si le canal est créé un message de création de canal est émis au client à l'origine de la commande.</p>
	 *
	 * @param client le client à l'origine de la commande.
	 * @param nom le nom du nouveau canal.
	 * 
	 * @see ProtocoleChat 
	 */
	public void ajouteCanal(ClientChat client, String nom)
	{
		String message;

		// Synchronization :
		// Pour éviter de créer deux canaux de même nom en même temps.
		synchronized (this.canaux) {
			if (this.canaux.get(nom) == null) {
				// Si le canal n'existe pas déjà.
				final CanalChat canal = new CanalChat(nom);
				this.canaux.put(canal.donneNom(), canal);
				message = String.format(ProtocoleChat.MESSAGE_CREATION_CANAL, nom);
			} else {
				// Si le canal existe déjà.
				message = String.format(ProtocoleChat.MESSAGE_CREATION_IMPOSSIBLE_CANAL, nom);
			}
		}
		client.envoieMessage(message);
	}

	/**
	 * Supprime un canal du chat (si le canal est vide).
	 *
	 * <p>Tout client peut supprimer un canal.</p>
	 * <p>Le client à l'origine de la commande de suppression reçoit le message
	 * de suppression du canal si le canal est bien supprimé.</p> 
	 * <p>Le canal est supprimé uniquement s'il existe et qu'il est vide. 
	 * Dans le cas contraire, les messages de non existence du canal ou de canal 
	 * non vide sont respectivement émis au client à l'origine de la commande.</p>
	 * <p>Le canal par défaut du chat ne peut pas être supprimé. 
	 * Une tentative de suppression de ce canal retourne le message d'impossibilité de 
	 * supprimer le canal par défaut du chat au client à l'origine de la commande.</p> 
	 *
	 * @param client le client à l'origine de la commande.
	 * @param nom le nom du canal à supprimer.
	 * 
	 * @see ProtocoleChat 
	 */
	public void supprimeCanal(ClientChat client, String nom)
	{
		String message;

		// Synchronization :
		// Pour éviter de supprimer un canal utilisé.
		synchronized (this.canaux) {
			final CanalChat canal = this.canaux.get(nom);
			if (canal != null) {
				// Le canal existe.
				if (canal != this.canalDefaut) {
					// Le canal n'est pas le canal par défaut du chat.
					if (canal.donneNombreClients() == 0) {					
						// Le canal est vide (sans client).
						this.canaux.remove(nom);
						message = String.format(ProtocoleChat.MESSAGE_SUPPRESSION_CANAL, nom);
					} else {
						// Le canal n'est pas vide.
						message = String.format(ProtocoleChat.MESSAGE_SUPPRESSION_CANAL_NON_VIDE, nom);
					}
				} else {
					// Le canal est le canal par défaut du chat.
					message = String.format(ProtocoleChat.MESSAGE_SUPPRESSION_CANAL_PAR_DEFAUT, 
								this.canalDefaut.donneNom());
				}
			} else {
				// Le canal n'existe pas.
				message = String.format(ProtocoleChat.MESSAGE_SUPPRESSION_CANAL_INEXISTANT, nom);
			}
		}
		client.envoieMessage(message);
	}

	/**
	 * Afficher les canaux disponibles dans le chat.
	 *
	 * <p>Les canaux disponibles ainsi que le nombre de clients par canaux 
	 * sont envoyés au client à l'origine de la requête."</p>
	 *
	 * @param client le client à qui afficher les canaux.
	 * 
	 * @see ProtocoleChat 
	 */
	public void afficheCanaux(ClientChat client)
	{
		String message = String.format(ProtocoleChat.MESSAGE_CANAUX_DISPONIBLES_EN_TETE);

		// Pour chaque canaux.
		final Iterator<String> iter = this.canaux.keySet().iterator();

		while (iter.hasNext()) {
			final CanalChat canal = this.canaux.get(iter.next());
			message = message.concat(
				String.format(ProtocoleChat.MESSAGE_CANAUX_DISPONIBLES_CANAL, 
						canal.donneNom(), canal.donneNombreClients())
			);
		}
		client.envoieMessage(message);
	}

	/**
	 * Afficher les informations personnelles sur un client.
	 *
	 * <p>Les informations personnelles du client sont émis au client à l'origine de la requête.</p>
	 *
	 * @param client le client concerné.
	 * 
	 * @see ProtocoleChat 
	 */
	public void afficheInformationsClient(ClientChat client)
	{
		final String message = String.format(ProtocoleChat.MESSAGE_INFORMATIONS_PERSONNELLES, 
							client.donneSurnom(), client.donneCanal().donneNom());
		
		client.envoieMessage(message);
	}

	/**
	 * Afficher l'aide sur les commandes (et services) disponibles dans le chat.
	 *
	 * @param client le client à qui afficher l'aide.
	 */
	public void afficheAide(ClientChat client)
	{
		final String message = String.format(ProtocoleChat.MESSAGE_AIDE);

		client.envoieMessage(message);
	}
	
	/**
	 * Fermeture d'une connexion avec un client.
	 *
	 * @param client le client concerné.
	 *
	 */
	public void fermeConnexion(ClientChat client) 
	{
		System.out.println("Fermeture connexion client (id : " + client.donneId() + ").");

		// Information de déconnexion du client.
		this.informeDepartClient(client);
		
		// Fermeture de la connexion du client.
		client.fermeConnexion();
	}
	
	/**
	 * Quittage(si si �a ce dit) du chat avec un client.
	 *
	 * @param client le client concerné.
	 *
	 */
	public void quitteChat(ClientChat client){
		//envoie le message au client qu'il quitte le chat
		client.envoieMessage(ProtocoleChat.MESSAGE_QUITTE_CHAT);
		
		//on ferme la co
		client.fermeToi() ;
		
		// le thread lorsqu'il aura fini de boucler appelera ferme connexion 
		//qui fermera la connexion et previendra le depart du client aux autres user
		//�a a �t� impl�ment� comme ca donc on fait comme ca
	}
}
