package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class IntermediaryRemoveService extends Service{
	
	private CentralServerManager centralServerManager;

	public IntermediaryRemoveService(CentralServerManager centralServerManager){
		super("remover-intermediario");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		System.out.println("desconectar " + clientInformation.getIp() + " " + clientInformation.getPort());
		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		centralServerManager.removeIntermediaryServer(address);
		return null;
	}
}