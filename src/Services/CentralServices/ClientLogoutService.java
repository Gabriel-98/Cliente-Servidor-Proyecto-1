package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ClientLogoutService extends Service{
	
	private CentralServerManager centralServerManager;

	public ClientLogoutService(CentralServerManager centralServerManager){
		super("cerrar-sesion-cliente");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		centralServerManager.logOutUser(address);
		return null;
	}
}