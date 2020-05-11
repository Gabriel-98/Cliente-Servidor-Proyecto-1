package Services.IntermediaryServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.IntermediaryServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ServerMessageService extends Service{
	
	private IntermediaryServerManager intermediaryServerManager;

	public ServerMessageService(IntermediaryServerManager intermediaryServerManager){
		super("mensaje-nodo-servidor");
		this.intermediaryServerManager = intermediaryServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add(command.size() + " words");

		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		Vector<String> message = new Vector<String>(command.size());
		for(int i=0; i<command.size(); i++)
		message.add(command.get(i));

		// mensaje enviado al cliente
		if(intermediaryServerManager.setClientMessage(address, message))
		ans.add("correcto");
		else
		ans.add("invalido");

		return ans;
	}
}