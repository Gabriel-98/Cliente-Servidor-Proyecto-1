package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ClientMessageService extends Service{
	
	private IntermediaryServerManager intermediaryServerManager;

	public ClientMessageService(IntermediaryServerManager intermediaryServerManager){
		super("mensaje-nodo-cliente");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		// mensaje enviado al servidor
		if(intermediaryServerManager.setServerMessage(address, command)){
			Vector<String> receivedMessage = intermediaryServerManager.getClientMessage(address);
			if(receivedMessage == null)
			ans.add("invalido");
			else{
				ans = new Vector<String>(receivedMessage.size() + 3);
				ans.add("respuesta-" + getServiceName());
				ans.add(command.size() + " words");
				ans.add("correcto");
				for(int i=0; i<receivedMessage.size(); i++)
				ans.add(receivedMessage.get(i));
			}
		}
		else
		ans.add("invalido");

		return ans;
	}
}
