package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ConnectionRegisterService extends Service{
	
	private CentralServerManager centralServerManager;

	public ConnectionRegisterService(CentralServerManager centralServerManager){
		super("registrar-conexion");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 2){
			try{
				//int port = Integer.parseInt(command.get(0));
				InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
				String userId = command.get(0);
				String userPassword = command.get(1);
				if(centralServerManager.addServerNode(address, userId, userPassword))
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
