package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;
import FileManager.FileDescriptionBuilder;
import FileManager.FileDescription;

import java.util.Vector;
import java.net.InetSocketAddress;

public class FileRegisterService extends Service{
	
	private CentralServerManager centralServerManager;

	public FileRegisterService(CentralServerManager centralServerManager){
		super("registrar-archivos");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		for(int i=0; i<command.size(); i++){
			FileDescription fileDescription = FileDescriptionBuilder.createFileDescription(command.get(i));
			if(fileDescription != null){
				if(!(centralServerManager.addFileDescription(address, fileDescription))){
					ans.add("invalido");
					return ans;
				}
			}
		}
		ans.add("correcto");
		return ans;
	}
}