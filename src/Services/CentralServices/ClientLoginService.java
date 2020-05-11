package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ClientLoginService extends Service{
	
	private CentralServerManager centralServerManager;

	public ClientLoginService(CentralServerManager centralServerManager){
		super("iniciar-sesion-cliente");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 2){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());

			if(centralServerManager.logInUser(address, command.get(0), command.get(1)))
			ans.add("correcto");
			else
			ans.add("invalido");
		}
		else
		ans.add("error");

		return ans;
	}
}