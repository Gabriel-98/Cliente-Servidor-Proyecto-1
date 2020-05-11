package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class IntermediaryServerSearchService extends Service{
	
	private CentralServerManager centralServerManager;

	public IntermediaryServerSearchService(CentralServerManager centralServerManager){
		super("buscar-servidor-intermediario");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		if(command.size() == 0){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			InetSocketAddress intermediary = centralServerManager.getIntermediaryServer();
			if(intermediary == null)
			ans.add("invalido");
			else{
				ans.add("correcto");
				ans.add(intermediary.getAddress().getHostAddress() + " " + String.valueOf(intermediary.getPort()));
			}
		}
		else
		ans.add("error");

		return ans;
	}
}