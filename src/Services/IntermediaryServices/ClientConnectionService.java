package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ClientConnectionService extends Service{
	
	private IntermediaryServerManager intermediaryServerManager;

	public ClientConnectionService(IntermediaryServerManager intermediaryServerManager){
		super("conectar-nodo-cliente");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 1){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			String serverId = command.get(0);
			
			if(intermediaryServerManager.joinConnection(address, serverId))
			ans.add("correcto");
			else
			ans.add("invalido");
		}
		else
		ans.add("error");

		return ans;
	}
}