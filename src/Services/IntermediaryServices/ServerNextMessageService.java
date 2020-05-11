package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ServerNextMessageService extends Service{
	
	private IntermediaryServerManager intermediaryServerManager;

	public ServerNextMessageService(IntermediaryServerManager intermediaryServerManager){
		super("mensaje-nodo-servidor-siguiente");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		if(command.size() == 0){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			// Lee el mensaje que envio el nodo-cliente
			Vector<String> receivedMessage = intermediaryServerManager.getServerMessage(address);

			if(receivedMessage == null)
			ans.add("invalido");
			else{
				ans = new Vector<String>(receivedMessage.size() + 3);
				ans.add("respuesta-" + getServiceName());
				ans.add("");
				ans.add("correcto");
				for(int i=0; i<receivedMessage.size(); i++)
				ans.add(receivedMessage.get(i));
			}
		}
		else
		ans.add("error");

		return ans;
	}
}