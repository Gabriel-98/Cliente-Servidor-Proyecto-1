package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class IntermediaryRegisterService extends Service{
	
	private CentralServerManager centralServerManager;

	public IntermediaryRegisterService(CentralServerManager centralServerManager){
		super("registrar-intermediario");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 1){
			try{
				int port = Integer.parseInt(command.get(0));
				InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
				InetSocketAddress listenerAddress = new InetSocketAddress(clientInformation.getIp(), port);
				if(centralServerManager.addIntermediaryServer(address, listenerAddress))
				ans.add("correcto");
				else
				ans.add("invalido");
			}
			catch(Exception e){ ans.add("error"); }	
		}
		else
		ans.add("error");
		
		return ans;
	}
}