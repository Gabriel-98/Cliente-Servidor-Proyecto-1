package Services.CentralServices;

import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ServerManager.CentralServerManager;
import FileManager.FileDescription;

import java.util.Vector;
import java.net.InetSocketAddress;

public class FileSearchService extends Service{
	
	private CentralServerManager centralServerManager;

	public FileSearchService(CentralServerManager centralServerManager){
		super("buscar-archivos");
		this.centralServerManager = centralServerManager;
	}

	@Override
	public Vector<String> solver(ClientInformation clientInformation, Vector<String> command){
		Vector<String> ans = new Vector<String>();
		ans.add("respuesta-" + getServiceName());
		ans.add("");

		InetSocketAddress address = new InetSocketAddress(clientInformation.getIp(), clientInformation.getPort());
		if(command.size() >= 1){
			String type = command.get(0);
			if(type.equals("mp3")){
				if(command.size() == 4){
					String title, artist, album;
					title = command.get(1);
					artist = command.get(2);
					album = command.get(3);

					Vector<FileDescription> fileDescriptionVector = centralServerManager.filterMP3(address, title, artist, album);
					if(fileDescriptionVector == null)
					ans.add("invalido");
					else{
						ans = new Vector<String>(fileDescriptionVector.size() + 3);
						ans.add("respuesta-" + getServiceName());
						ans.add("");
						ans.add("correcto");
						for(int i=0; i<fileDescriptionVector.size(); i++)
						ans.add(fileDescriptionVector.get(i).convertToString());
					}
				}
				else
				ans.add("error");
			}
			else if(type.equals("mp4")){
				if(command.size() == 2){
					String name = command.get(1);
					Vector<FileDescription> fileDescriptionVector = centralServerManager.filterMP4(address, name);
					System.out.println("mp4" + " " + name);
					if(fileDescriptionVector == null)
					ans.add("invalido");
					else{
						ans = new Vector<String>(fileDescriptionVector.size() + 3);
						ans.add("respuesta-" + getServiceName());
						ans.add("");
						ans.add("correcto");
						for(int i=0; i<fileDescriptionVector.size(); i++)
						ans.add(fileDescriptionVector.get(i).convertToString());
					}
				}
				else
				ans.add("error");
			}
			else
			ans.add("error");
		}
		else
		ans.add("error");

		return ans;
	}
}