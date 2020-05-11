import ClientServerImplementation.ConnectionManager;
import ClientServerImplementation.ServiceConfiguration;
import ClientServerImplementation.Client;
import ServerManager.IntermediaryConnection;
import ServerManager.IntermediaryServerManager;
import Services.IntermediaryServices.NodeDeleteService;
import Services.IntermediaryServices.ClientConnectionService;
import Services.IntermediaryServices.ServerConnectionService;
import Services.IntermediaryServices.ClientMessageService;
import Services.IntermediaryServices.ServerMessageService;
import Services.IntermediaryServices.ServerNextMessageService;
import Services.IntermediaryServices.ClientNextMessageService;


import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;
import java.util.Scanner;


class IntermediaryToCentral extends Client{

	private IntermediaryServerManager intermediaryServerManager;
	private int port;	// puerto por el que va a escuchar las conexiones
	private boolean active;

	public IntermediaryToCentral(String serverIp, int serverPort, IntermediaryServerManager intermediaryServerManager, int port) throws Exception{
		super(serverIp, serverPort);
		this.intermediaryServerManager = intermediaryServerManager;
		this.port = port;
		active = true;
	}

	public boolean registerIntermediary(){
		Vector<String> message = new Vector<String>();
		message.add("registrar-intermediario");
		message.add("");
		message.add(String.valueOf(port));
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			active = false;
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-registrar-intermediario")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
			if(receivedMessage.get(2).equals("error"))
			active = false;
		}
		return false;
	}

	public boolean registerConnection(String id, String password){
		Vector<String> message = new Vector<String>();
		message.add("registrar-conexion");
		message.add("");
		message.add(id);
		message.add(password);
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			active = false;
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-registrar-conexion")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
			if(receivedMessage.get(2).equals("error"))
			active = false;
		}
		return false;
	}

	// Envia mensaje indicando que ya no tiene conexion del nodo con ese identificador
	public boolean removeConnection(String id){
		Vector<String> message = new Vector<String>();
		message.add("eliminar-conexion");
		message.add("");
		message.add(id);
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			active = false;
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-eliminar-conexion")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
			if(receivedMessage.get(2).equals("error"))
			active = false;
		}
		return false;
	}

	@Override
	public void run(){
		/*active = registerIntermediary();
		while(active){
			IntermediaryConnection intermediaryConnection = intermediaryServerManager.removeValidation();
			if(intermediaryConnection != null){
				boolean ans = registerConnection(intermediaryConnection.getServerId(), intermediaryConnection.getServerPassword());
				if(ans)
				intermediaryServerManager.enableAddress(intermediaryConnection.getServerAddress());
				else
				intermediaryServerManager.deleteIntermediaryConnectionByServerNode(intermediaryConnection.getServerAddress());
			}
			intermediaryConnection = intermediaryServerManager.removeFinishedConnection();
			if(intermediaryConnection != null){
				intermediaryServerManager.deleteIntermediaryConnectionByServerNode(intermediaryConnection.getServerAddress());
				removeConnection(intermediaryConnection.getServerId(), intermediaryConnection.getServerPassword());
			}
		}
		close();*/

		boolean active = registerIntermediary();
		while(active){
			IntermediaryConnection connection = intermediaryServerManager.nextPendingConnection();
			if(connection != null){
				registerConnection(connection.getServerId(), connection.getServerPassword());
				intermediaryServerManager.addConnection(connection);
				intermediaryServerManager.removeNextPendingConnection();
			}
			if(active){
				String serverId = intermediaryServerManager.nextFinishedConnection();
				if(serverId != null){
					removeConnection(serverId);
					intermediaryServerManager.removeNextFinishedConnection();
				}
			}
		}
	}
}



public class IntermediaryServer{
	
	public static void main(String[] args){
		int port = 0;
		try{ port = Integer.parseInt(args[0]); }
		catch(Exception e){
			System.out.println("Error! No ingreso puerto o es un puerto invalido");
			return;
		}


		IntermediaryServerManager intermediaryServerManager = new IntermediaryServerManager();
		IntermediaryToCentral intermediaryToCentral;
		ConnectionManager connectionManager;

		try{
			connectionManager = new ConnectionManager("intermediario", port, 1024);
			intermediaryToCentral = new IntermediaryToCentral("185.253.153.60", 11000, intermediaryServerManager, port);
		}
		catch(Exception e){
			System.out.println("No se pudo crear el servidor");
			return;
		}

		// servicios que se ejecutan al desconectarse un cliente
		connectionManager.setDisconnectService(new NodeDeleteService(intermediaryServerManager));

		// adicionar servicios
		connectionManager.addService(new ClientConnectionService(intermediaryServerManager));
		connectionManager.addService(new ServerConnectionService(intermediaryServerManager));
		connectionManager.addService(new ClientMessageService(intermediaryServerManager));
		connectionManager.addService(new ServerMessageService(intermediaryServerManager));
		connectionManager.addService(new ServerNextMessageService(intermediaryServerManager));
		connectionManager.addService(new ClientNextMessageService(intermediaryServerManager));

		// adicionar configuracion servicios
		connectionManager.addServiceConfiguration(new ServiceConfiguration("conectar-nodo-cliente", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-conectar-nodo-cliente", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("conectar-nodo-servidor", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-conectar-nodo-servidor", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("mensaje-nodo-cliente", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-cliente", true, false));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("mensaje-nodo-cliente-siguiente", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-cliente-siguiente", true, false));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("mensaje-nodo-servidor", true, false));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-servidor", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("mensaje-nodo-servidor-siguiente", true, true));
		connectionManager.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-servidor-siguiente", true, true));

		// Deshabilitar mostrar mensajes
		connectionManager.setShowMessages(false);
		intermediaryToCentral.setShowMessages(false);

		ExecutorService hilos = Executors.newFixedThreadPool(2);
		hilos.execute(intermediaryToCentral);
		hilos.execute(connectionManager);
	}
}