import ClientServerImplementation.Client;
import ClientServerImplementation.ServiceConfiguration;
import FileManager.FileReader;
import FileManager.FileWriter;
import ServerManager.ClientManager;
import ServerManager.CharacterTranslator;
import utilities.Utilities;

import java.net.InetSocketAddress;
import java.io.File;
import java.lang.Thread;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;
import java.util.Scanner;


import FileManager.*;


class ClientToCentral extends Client{

	private ClientManager clientManager;
	private Vector<FileDescription> fileDescriptionList;		// archivos obtenidos en la ultima consulta

	public ClientToCentral(String serverIp, int serverPort, ClientManager clientManager) throws Exception{
		super(serverIp, serverPort);
		this.clientManager = clientManager;
		fileDescriptionList = new Vector<FileDescription>();
		setShowMessages(false);		
		clientManager.setActiveClient(true);
	}

	public boolean register(){
		Vector<String> message = new Vector<String>();
		message.add("registrar-cliente");
		message.add("");
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			clientManager.setActiveClient(false);
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 5 && receivedMessage.get(0).equals("respuesta-registrar-cliente") && receivedMessage.get(2).equals("correcto")){
			String userId, userPassword;
			userId = receivedMessage.get(3);
			userPassword = receivedMessage.get(4);
			clientManager.setUser(userId, userPassword);
			return true;
		}
		clientManager.setActiveClient(false);
		return false;
	}

	// inactivo
	public boolean login(){
		String userId, userPassword;
		userId = clientManager.getUserId();
		userPassword = clientManager.getUserPassword();
		if(userId == null || userPassword == null)
		return false;

		Vector<String> message = new Vector<String>();
		message.add("iniciar-sesion-cliente");
		message.add("");
		message.add(userId);
		message.add(userPassword);
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			clientManager.setActiveClient(false);
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-iniciar-sesion-cliente") && receivedMessage.get(2).equals("correcto"))
		return true;
		return false;
	}

	public boolean registerFiles(){
		// actualizar archivos
		clientManager.updateFiles();
		Vector<FileDescription> fileDescriptionVector = clientManager.getFileDescriptions();

		// registrar
		Vector<String> message = new Vector<String>();
		message.add("registrar-archivos");
		message.add("");
		int byteCounter = 8 + message.get(0).length() + message.get(1).length(); 
		for(int i=0; i<fileDescriptionVector.size(); i++){
			String fileDescriptionString = fileDescriptionVector.get(i).convertToString();
			if(byteCounter + 2 + fileDescriptionString.length() < 500){
				byteCounter += 2 + fileDescriptionString.length();
				message.add(fileDescriptionString);
			}
			else{
				String ans = send(message);
				if(ans.equals("error") || ans.equals("desconectar")){
					clientManager.setActiveClient(false);
					return false;
				}

				Vector<String> receivedMessage = recv();
				if(!(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-registrar-archivos") && receivedMessage.get(2).equals("correcto"))){
					clientManager.setActiveClient(false);
					return false;
				}

				message = new Vector<String>();
				message.add("registrar-archivos");
				message.add("");
				byteCounter = 8 + message.get(0).length() + message.get(1).length();

				byteCounter += 2 + fileDescriptionString.length();
				message.add(fileDescriptionString);
			}
		}
		if(fileDescriptionVector.size() > 0){
			String ans = send(message);
			if(ans.equals("error") || ans.equals("desconectar")){
				clientManager.setActiveClient(false);
				return false;
			}

			Vector<String> receivedMessage = recv();
			if(!(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-registrar-archivos") && receivedMessage.get(2).equals("correcto"))){
				clientManager.setActiveClient(false);
				return false;
			}
		}

		return true;
	}

	public boolean removeFiles(){
		Vector<String> message = new Vector<String>();
		message.add("borrar-archivos");
		message.add("");
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			clientManager.setActiveClient(false);
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-borrar-archivos") && receivedMessage.get(2).equals("correcto"))
		return true;

		clientManager.setActiveClient(false);
		return false;
	}

	public boolean searchFiles(Vector<String> words){
		Vector<String> message = new Vector<String>();
		message.add("buscar-archivos");
		message.add("");
		for(int i=0; i<words.size(); i++)
		message.add(words.get(i));

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			clientManager.setActiveClient(false);
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() >= 3 && receivedMessage.get(0).equals("respuesta-buscar-archivos") && receivedMessage.get(2).equals("correcto")){
			fileDescriptionList = new Vector<FileDescription>(receivedMessage.size() - 3);
			for(int i=3; i<receivedMessage.size(); i++){
				FileDescription fileDescription = FileDescriptionBuilder.createFileDescription(receivedMessage.get(i));
				fileDescriptionList.add(fileDescription);
			}
			return true;
		}

		clientManager.setActiveClient(false);
		return false;
	}



	public void showCommands(){
		System.out.println("***LISTA DE COMANDOS***");
		System.out.println("actualizar-archivos");
		System.out.println("buscar-archivos [tipo] [ .. parametros .. ]");
		System.out.println("buscar-archivos mp3 [titulo] [artista] [album]");
		//System.out.println("buscar-archivos mp4 [titulo]");
		System.out.println("borrar-archivos");
		System.out.println("descargar-archivos [id1] [id2] ... [idn]");
	}

	public void printText(int firstSpaces, String text, int maxSize, int lastSpaces){
		String newText = "";
		if(text == null)
		newText = "-";
		else
		newText = CharacterTranslator.newString(text);

		for(int i=0; i<firstSpaces; i++)
		System.out.print(" ");
		for(int i=0; i<text.length() && i<maxSize; i++)
		System.out.print(newText.charAt(i));
		for(int i=text.length(); i<maxSize; i++)
		System.out.print(" ");
		for(int i=0; i<lastSpaces; i++)
		System.out.print(" ");
	}

	@Override
	public void run(){
		boolean active = true;
		Scanner sc = new Scanner(System.in);
		String line;

		active = register();
		if(active){
			while(active){
				line = sc.nextLine();
				Vector<String> words = Utilities.getWords(line);
				if(words.size() >= 1){ 
					if(words.get(0).equals("borrar-archivos"))
					active = removeFiles();
					else if(words.get(0).equals("actualizar-archivos")){
						active = removeFiles();
						if(active)
						active = registerFiles();
					}
					else if(words.get(0).equals("buscar-archivos")){
						System.out.println("[*] Ingrese el tipo de archivo: ");
						String type = sc.nextLine();

						if(type.equals("mp3")){
							String title, artist, album;
							Vector<String> words2 = new Vector<String>();
							System.out.println("[*] Ingrese el titulo: ");
							title = sc.nextLine();
							System.out.println("[*] Ingrese el artista: ");
							artist = sc.nextLine();
							System.out.println("[*] Ingrese el album: ");
							album = sc.nextLine();

							words2.add(type);
							words2.add(title);
							words2.add(artist);
							words2.add(album);

							active = searchFiles(words2);
							if(active){
								System.out.println("LISTA DE ARCHIVOS");
								printText(2, "ID", 2, 2);
								printText(12, "NOMBRE", 6, 12);
								printText(5, "TAMA" + (char)165 + "O", 6, 5);
								printText(9, "TITULO", 6, 9);
								printText(8, "ARTISTA", 7, 8);
								printText(9, "ALBUM", 5, 9);
								System.out.println();
								for(int i=0; i<fileDescriptionList.size(); i++){
									FileDescription fd = fileDescriptionList.get(i);
									printText(0, String.valueOf(i), 3, 3);
									printText(0, fd.getFileName(), 26, 4);
									printText(0, String.valueOf(fd.getFileSize()), 12, 4);
									printText(0, fd.getValue("title"), 20, 4);
									printText(0, fd.getValue("artist"), 19, 4);
									printText(0, fd.getValue("album"), 19, 4);
									System.out.println();
								}
							}
						}
						else if(type.equals("mp4")){
							String name;
							Vector<String> words2 = new Vector<String>();
							System.out.println("[*] Ingrese el nombre: ");
							name = sc.nextLine();

							words2.add(type);
							words2.add(name);
							active = searchFiles(words2);
							if(active){
								System.out.println("LISTA DE ARCHIVOS");
								printText(2, "ID", 2, 2);
								printText(12, "NOMBRE", 6, 12);
								printText(5, "TAMA" + (char)165 + "O", 6, 5);
								System.out.println();
								for(int i=0; i<fileDescriptionList.size(); i++){
									FileDescription fd = fileDescriptionList.get(i);
									printText(0, String.valueOf(i), 3, 3);
									printText(0, fd.getFileName(), 26, 4);
									printText(0, String.valueOf(fd.getFileSize()), 12, 4);
									System.out.println();
								}
							}
						}
						else
						System.out.println("[*] Error! Tipo de archivo incorrecto");
					}
					else if(words.get(0).equals("descargar-archivos")){
						System.out.println("[*] Ingrese la direccion de la carpeta donde las va descargar:");
						line = sc.nextLine();
						File folder = new File(line);
						try{
							if(folder.exists() && folder.isDirectory()){
								int i;
								for(i=1; i<words.size(); i++){
									try{
										int pos = Integer.parseInt(words.get(i));
										if(pos < 0 || pos >= fileDescriptionList.size())
										break;
									}
									catch(Exception e){ break; }
								}
								if(i < words.size())
								System.out.println("[*] Error con el comando \"descargar archivos\"");
								else{
									for(i=1; i<words.size(); i++){
										int pos = Integer.parseInt(words.get(i));
										clientManager.addDownload(folder, fileDescriptionList.get(pos));
									}
									System.out.println("[*] Descargando ...");
								}
							}
							else
							System.out.println("[*] La carpeta no existe");
						}
						catch(Exception e){ System.out.println("[*] La carpeta no existe o no se puede acceder"); }						
					}
					else
					System.out.println("[*] Comando incorrecto");
				}
			}
		}
		else
		System.out.println("[*] No se pudo registar");

		close();
		clientManager.setActiveClient(false);
		//
		System.out.println("[*] Se desconecto del servidor central");
	}
}





class ClientUpdaterToCentral extends Client{
	private ClientManager clientManager;
	private ExecutorService threadPool;
	private boolean active;

	public ClientUpdaterToCentral(String serverIp, int serverPort, ClientManager clientManager) throws Exception{
		super(serverIp, serverPort);
		this.clientManager = clientManager;
		active = true;
		threadPool = Executors.newFixedThreadPool(64);
		clientManager.setActiveClientUpdater(true);
		setShowMessages(false);
	}

	public boolean searchIntermediary(){
		Vector<String> message = new Vector<String>();
		message.add("buscar-servidor-intermediario");
		message.add("");

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			active = false;
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() >= 3 && receivedMessage.get(0).equals("respuesta-buscar-servidor-intermediario")){
			if(receivedMessage.size() == 4 && receivedMessage.get(2).equals("correcto")){
				try{
					Vector<String> words = Utilities.getWords(receivedMessage.get(3));
					if(words.size() != 2)
					return false;
					String ip = words.get(0);
					int port = Integer.parseInt(words.get(1));
					InetSocketAddress intermediary = new InetSocketAddress(ip, port);
					clientManager.setIntermediaryServer(intermediary);
					return true;
				}
				catch(Exception e){ return false; }
			}
			if(receivedMessage.size() == 3 && receivedMessage.get(2).equals("error"))
			active = false;
		}
		return false;
	}

	public boolean searchServerNodes(FileDescription fileDescription){
		Vector<String> message = new Vector<String>();
		message.add("buscar-nodos-servidores");
		message.add("");
		message.add(fileDescription.convertToString());

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar")){
			active = false;
			return false;
		}

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() >= 3 && receivedMessage.get(0).equals("respuesta-buscar-nodos-servidores")){
			if(receivedMessage.get(2).equals("correcto")){
				for(int i=3; i<receivedMessage.size(); i++){
					Vector<String> words = Utilities.getWords(receivedMessage.get(i));
					if(words.size() == 3){
						try{
							String ip = words.get(0);
							int port = Integer.parseInt(words.get(1));
							String serverId = words.get(2);
							InetSocketAddress address = new InetSocketAddress(ip, port);

							if(clientManager.getNumberOfDownloadersByFileDescription(fileDescription) < 4){
								ClientNodeToIntermediary c = new ClientNodeToIntermediary(ip, port, clientManager, serverId, fileDescription, clientManager.getFileWriterByDownload(fileDescription));
								c.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-cliente", true, false));
								c.addServiceConfiguration(new ServiceConfiguration("respuesta-mensaje-nodo-cliente-siguiente", true, false));
								threadPool.execute(c);
							}
						}
						catch(Exception e){}
					}
				}
				return true;
			}
			if(receivedMessage.equals("error"))
			active = false;
		}
		return false;
	}

	@Override
	public void run(){
		boolean active = true;
		while(active){
			try{ Thread.sleep(100); }
			catch(Exception e){ break; }
			if(clientManager.getIntermediaryServer() == null)
			searchIntermediary();
			if(active && clientManager.getIntermediaryServer() != null && clientManager.getNumberOfServerNodes() < 16){
				try{
					InetSocketAddress intermediary = clientManager.getIntermediaryServer();
					ServerNodeToIntermediary serverNodeToIntermediary = new ServerNodeToIntermediary(intermediary.getAddress().getHostAddress(), intermediary.getPort(), clientManager);
					serverNodeToIntermediary.addServiceConfiguration(new ServiceConfiguration("mensaje-nodo-servidor", true, false));
					threadPool.execute(serverNodeToIntermediary);
				}
				catch(Exception e){}
			}
			if(active && clientManager.getNumberOfServerNodes() == 0)
			clientManager.setIntermediaryServer(null);

			if(active){
				FileDescription fd = clientManager.nextDownload();
				if(fd != null){
					FileWriter fw = clientManager.getFileWriterByDownload(fd);
					if(fw.isFinished()){
						clientManager.removeDownload(fd);
					}
					else{
						if(clientManager.getNumberOfDownloaders() < 16){
							searchServerNodes(fd);
						}
					}
				}
			}

			/*if(active && clientManager.getNumberOfDownloaders() < 16){
				FileDescription fd = clientManager.nextDownload();
				if(fd != null){
					searchServerNodes(fd);
				}
			}*/
		}
		close();
		clientManager.setActiveClientUpdater(false);
	}
}




class ClientNodeToIntermediary extends Client{
	private final int blockSize = 32768;
	private final int preloadedBlockSize = 1048576;
	private String serverId;
	private ClientManager clientManager;
	private FileDescription fileDescription;
	private FileWriter fw;
	private long lim1, lim2;
	private char[] array;

	public ClientNodeToIntermediary(String serverIp, int serverPort, ClientManager clientManager, String serverId, FileDescription fileDescription, FileWriter fw) throws Exception{
		super(serverIp, serverPort);
		this.clientManager = clientManager;
		this.serverId = serverId;
		this.fileDescription = fileDescription;
		this.fw = fw;
		lim1 = -1;	lim2 = -1;
		array = new char[preloadedBlockSize];
		clientManager.addDownloader(fileDescription);
		setShowMessages(false);
	}

	public boolean connect(){
		Vector<String> message = new Vector<String>();
		message.add("conectar-nodo-cliente");
		message.add("");
		message.add(serverId);
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-conectar-nodo-cliente")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	public boolean fileMessage(){
		Vector<String> message = new Vector<String>(4);
		message.add("mensaje-nodo-cliente");
		message.add("");
		message.add("1");
		message.add(fileDescription.convertToString());
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-cliente")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	// modificar
	public boolean preloadedFragmentMessage(long pos1, long pos2){
		Vector<String> message = new Vector<String>(5);
		message.add("mensaje-nodo-cliente");
		message.add("");
		message.add("3");
		message.add(String.valueOf(pos1));
		message.add(String.valueOf(pos2));
		String ans = send(message);

		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-cliente")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}


	public boolean nextFragmentMessage(long pos1, long pos2){
		Vector<String> message = new Vector<String>(4);
		message.add("mensaje-nodo-cliente-siguiente");
		message.add("");
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 4 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-cliente-siguiente")){
			if(receivedMessage.get(2).equals("correcto")){
				String fragment = receivedMessage.get(3);
				for(int i=0; i<fragment.length(); i++)
				array[(int)(pos1-lim1+i)] = fragment.charAt(i);
				
				return true;
			}
		}
		return false;
	}

	@Override
	public void run(){
		boolean active;
		active = connect();
		active = fileMessage();
		while((active) && (!fw.isFinished())){
			long pos = fw.next();
			lim1 = pos;
			lim2 = Math.min(fw.size() - 1, pos + preloadedBlockSize - 1);

			active = preloadedFragmentMessage(lim1, lim2);
			for(long i=lim1; i<=lim2 && active; i+=blockSize)
			active = nextFragmentMessage(i, Math.min(lim2, i + blockSize - 1));
			
			if(active){
				boolean result = fw.writeStringConcurrent(new String(array, 0, (int)(lim2+1-lim1)), lim1);
				if(!result)
				break;
			}
		}
		if(!fw.isFinished() && lim1 != -1)
		fw.cancel(lim1);
		close();
		clientManager.removeDownloader(fileDescription);
	}
}



class ServerNodeToIntermediary extends Client{
	//private String connectionId, connectionPassword;
	private FileReader fr;
	private ClientManager clientManager;
	private File file;
	private FileDescription fileDescription;
	private String preloadedBlock;
	private long lim1, lim2;
	private long pos1, pos2;
	private int op;
	private final int blockSize = 32768;


	public ServerNodeToIntermediary(String serverIp, int serverPort, ClientManager clientManager) throws Exception{
		super(serverIp, serverPort);
		this.clientManager = clientManager;
		file = null;
		fileDescription = null;
		preloadedBlock = "";
		lim1 = -1;
		lim2 = -1;
		pos1 = 0;
		pos2 = 0;
		fr = null;
		op = 0;
		clientManager.addOneServerNode();
		setShowMessages(false);
	}

	public boolean connect(){
		String serverId = clientManager.getUserId();
		String serverPassword = clientManager.getUserPassword();
		if(serverId == null || serverPassword == null)
		return false;

		Vector<String> message = new Vector<String>();
		message.add("conectar-nodo-servidor");
		message.add("");
		message.add(serverId);
		message.add(serverPassword);
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-conectar-nodo-servidor")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	public boolean nextMessage(){
		Vector<String> message = new Vector<String>();
		message.add("mensaje-nodo-servidor-siguiente");
		message.add("");
		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() >= 4 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-servidor-siguiente")){
			if(receivedMessage.get(2).equals("correcto")){
				if(receivedMessage.get(3).equals("1")){		// Indica cual archivo va descargar
					op = 1;
					if(receivedMessage.size() == 5){
						fileDescription = FileDescriptionBuilder.createFileDescription(receivedMessage.get(4));
						file = clientManager.getFile(fileDescription);
						if(file != null && file.exists()){
							try{
								fr = new FileReader(file);
								return true;
							}
							catch(Exception e){ return false; }
						}
						return false;
					}
				}
				else if(receivedMessage.get(3).equals("2")){	// Indica el fragmento del archivo que debe enviar
					op = 2;
					if(receivedMessage.size() == 6){
						try{
							pos1 = Long.parseLong(receivedMessage.get(4));
							pos2 = Long.parseLong(receivedMessage.get(5));
							if(file != null && file.exists() && 0 <= pos1 && pos1 <= pos2 && pos2 < fr.size())
							return true;
						}
						catch(Exception e){ return false; }
					}
				}
				else if(receivedMessage.get(3).equals("3")){	// Indica el fragmento del archivo que debe precargar
					op = 3;
					if(receivedMessage.size() == 6){
						try{
							lim1 = Long.parseLong(receivedMessage.get(4));
							lim2 = Long.parseLong(receivedMessage.get(5));
							if(file != null && file.exists() && 0 <= lim1 && lim1 <= lim2 && lim2 < fr.size()){
								pos1 = lim1;
								pos2 = Math.min(lim1 + blockSize - 1, lim2);
								return true;
							}
						}
						catch(Exception e){ return false; }
					}
				}
			}
		}
		return false;
	}

	public boolean fileMessage(){
		Vector<String> message = new Vector<String>(3);
		message.add("mensaje-nodo-servidor");
		message.add("");

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-servidor")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	public boolean fragmentMessage(){
		Vector<String> message = new Vector<String>(3);
		message.add("mensaje-nodo-servidor");
		message.add("Fragmento [" + pos1 + " - " + pos2 + "] del archivo "  + "archivo");

		if(lim1 <= pos1 && pos1 <= pos2 && pos2 <= lim2 && (pos2 - pos1 <= 60000)){
			String fragment = preloadedBlock.substring((int)(pos1-lim1), (int)(pos2+1-lim1));
			message.add(fragment);
		}
		else
		return false;

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;

		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-servidor")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	public boolean preloadedFragmentMessage(){
		Vector<String> message = new Vector<String>(2);
		message.add("mensaje-nodo-servidor");
		message.add("Fragmento [" + lim1 + " - " + lim2 + "] del archivo "  + "archivo" + " se ha precargado");

		if(0 <= lim1 && lim1 <= lim2 && lim2 < fr.size()){
			preloadedBlock = fr.readString(lim1, lim2);
			if(preloadedBlock == null)
			return false;
		}
		else
		return false;

		String ans = send(message);
		if(ans.equals("error") || ans.equals("desconectar"))
		return false;


		Vector<String> receivedMessage = recv();
		if(receivedMessage.size() == 3 && receivedMessage.get(0).equals("respuesta-mensaje-nodo-servidor")){
			if(receivedMessage.get(2).equals("correcto"))
			return true;
		}
		return false;
	}

	@Override
	public void run(){
		boolean active = connect();
		if(active){
			while(nextMessage()){
				if(op == 1){
					if(!fileMessage())
					break;
				}
				else if(op == 2){
					if(!fragmentMessage())
					break;
				}
				else{
					if(!preloadedFragmentMessage())
					break;
					else{
						while(fragmentMessage()){
							pos1 = pos2 + 1;
							pos2 = Math.min(pos1 + blockSize - 1, lim2);
							if(pos1 > lim2)
							break;
						}
					}
				}
			}
		}
		else{}

		close();
		clientManager.removeOneServerNode();
	}
}


public class ClientNode{	

	private static String serverIp = "185.253.153.60";
	private static int serverPort = 10000;
	private static ExecutorService hilos;
	private static ClientManager clientManager;

	public static void createClientUser(){
		// se ejecutara en el hilo principal
		try{
			ClientToCentral clientToCentral = new ClientToCentral(serverIp, serverPort, clientManager);
			clientToCentral.run();
		}
		catch(Exception e){ System.out.println("Error al conectarse"); }

	}

	public static void createClientUpdaterToCentral(){
		try{
			ClientUpdaterToCentral clientUpdaterToCentral = new ClientUpdaterToCentral(serverIp, serverPort, clientManager);
			hilos.execute(clientUpdaterToCentral);
		}
		catch(Exception e){ System.out.println("Error al conectarse"); }
	}


	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		clientManager = new ClientManager();
		clientManager.setFolder(new File("publico"));
		hilos = Executors.newFixedThreadPool(3);

		while(true){
			if(sc.hasNextLine()){
				String line = sc.nextLine();
				if(line.equals("conectar-central")){
					if(clientManager.isActiveClient())
					System.out.println("ya estaba conectado al servidor central");
					else{
						createClientUpdaterToCentral();
						createClientUser();	
					}
				}
			}
		}
	}
}