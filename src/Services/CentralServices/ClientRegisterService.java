package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;
import ServerManager.User;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ClientRegisterService extends Service{
	
	private CentralServerManager centralServerManager;

	public ClientRegisterService(CentralServerManager centralServerManager){
		super("registrar-cliente");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 0){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			User user = centralServerManager.signInUser(address);
				
			if(user == null)
			ans.add("invalido");
			else{
				ans.add("correcto");
				ans.add(user.getId());
				ans.add(user.getPassword());
			}
		}
		else
		ans.add("error");
		
		return ans;
	}
}