package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;
import ServerManager.IntermediaryConnection;

import java.util.Vector;
import java.net.InetSocketAddress;

public class NodeDeleteService extends Service{

	private IntermediaryServerManager intermediaryServerManager;

	public NodeDeleteService(IntermediaryServerManager intermediaryServerManager){
		super("eliminar-nodo");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		
		IntermediaryConnection connection = intermediaryServerManager.getConnectionByClientAddress(address);
		if(connection != null)
		intermediaryServerManager.removeConnection(connection);
		
		connection = intermediaryServerManager.getConnectionByServerAddress(address);
		if(connection != null)
		intermediaryServerManager.removeConnection(connection);
		return null;
	}

}
