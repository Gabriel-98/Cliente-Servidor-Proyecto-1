package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ServerConnectionService extends Service{
	
	private IntermediaryServerManager intermediaryServerManager;

	public ServerConnectionService(IntermediaryServerManager intermediaryServerManager){
		super("conectar-nodo-servidor");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 2){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			String userId = command.get(0);
			String userPassword = command.get(1);
			if(intermediaryServerManager.createPendingConnection(address, userId, userPassword)){
				while(intermediaryServerManager.isPendingConnection(address)){}
				if(intermediaryServerManager.getConnectionByServerAddress(address) != null)
				ans.add("correcto");
				else
				ans.add("invalido");
			}
			else
			ans.add("invalido");
		}
		else
		ans.add("error");

		return ans;
	}
}