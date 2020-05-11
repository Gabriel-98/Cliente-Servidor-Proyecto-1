package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;
import FileManager.FileDescriptionBuilder;
import FileManager.FileDescription;
import utilities.Pair;

import java.util.Vector;
import java.net.InetSocketAddress;

public class ServerNodeSearchService extends Service{
	
	private CentralServerManager centralServerManager;

	public ServerNodeSearchService(CentralServerManager centralServerManager){
		super("buscar-nodos-servidores");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		if(command.size() == 1){
			FileDescription fileDescription = FileDescriptionBuilder.createFileDescription(command.get(0));
			if(fileDescription == null)
			ans.add("error");
			else{
				InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
				Vector<Pair<InetSocketAddress,String>> serverNodeVector = centralServerManager.getServerNodes(address, fileDescription);
				if(serverNodeVector == null)
				ans.add("invalido");
				else{
					ans = new Vector<String>(serverNodeVector.size() + 3);
					ans.add("respuesta-" + getServiceName());
					ans.add("");
					ans.add("correcto");
					for(int i=0; i<serverNodeVector.size(); i++){
						String nodeInformation = "";
						nodeInformation += serverNodeVector.get(i).getFirst().getAddress().getHostAddress();
						nodeInformation += " " + String.valueOf(serverNodeVector.get(i).getFirst().getPort());
						nodeInformation += " " + serverNodeVector.get(i).getSecond();
						ans.add(nodeInformation);
					}
				}
			}
		}
		else
		ans.add("error");

		return ans;
	}
}
