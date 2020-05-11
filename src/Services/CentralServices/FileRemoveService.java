package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;

import java.util.Vector;
import java.net.InetSocketAddress;

public class FileRemoveService extends Service{
	
	private CentralServerManager centralServerManager;

	public FileRemoveService(CentralServerManager centralServerManager){
		super("borrar-archivos");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");
		if(command.size() == 0){
			InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
			if(centralServerManager.clearUserFileDescriptions(address))
			ans.add("correcto");
			else
			ans.add("invalido");
		}
		else
		ans.add("error");
		
		return ans;
	}
}