package ClientServerImplementation;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;

import utilities.Utilities;

// Representa el intercambio de mensajes con el cliente
public class CommunicationManager implements Runnable{
	
	private ConnectionManager connectionManager;
	private Socket client;
	private ClientInformation clientInformation;
	private DataInputStream in;
	private DataOutputStream out;
	private int windowSize = 65536;

	public CommunicationManager(ConnectionManager connectionManager, Socket client) throws Exception{
		try{
			this.connectionManager = connectionManager;
			this.client = client;
			clientInformation = new ClientInformation(client.getInetAddress().getHostAddress(), client.getPort());
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
		}
		catch(Exception e){
			throw new Exception("No se pudo crear el cliente");
		}

		boolean ans = connectionManager.addClient(client);
		if(!ans)
		throw new Exception("El cliente " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " no se pudo conectar. El servidor esta a su maximo de clientes");
		printConnection();
	}

	public boolean isClosed(){
		return client.isClosed();	
	}

	private void printConnection(){
		if(connectionManager.getShowConnections())
		System.out.println("Se conecto el cliente " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
	}

	private void printDisconnection(){
		if(connectionManager.getShowConnections())
		System.out.println("El cliente " + client.getInetAddress().getHostAddress() + ":" + client.getPort() + " se desconecto");
	}

	private void printReceived(Vector<String> message){
		if(connectionManager.getShowMessages()){
			if(!(message.size() == 1 && message.get(0).equals("desconectar"))){
				System.out.print("Mensaje Recibido de (" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + ")\t->\t");
				if(message.size() >= 1){
					ServiceConfiguration serviceConfiguration = connectionManager.findServiceConfiguration(message.get(0));
					if(serviceConfiguration == null)
					System.out.println(Utilities.getString(message));
					else
					System.out.println(serviceConfiguration.getVisibleMessage(message));
				}
			}
		}
	}

	private void printSent(Vector<String> message){
		if(connectionManager.getShowMessages()){
			if(!(message.size() == 1 && message.get(0).equals("desconectar"))){
				System.out.print("Mensaje Enviado a (" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + ")\t->\t");
				if(message.size() >= 1){
					ServiceConfiguration serviceConfiguration = connectionManager.findServiceConfiguration(message.get(0));
					if(serviceConfiguration == null)
					System.out.println(Utilities.getString(message));
					else
					System.out.println(serviceConfiguration.getVisibleMessage(message));
				}
			}
		}
	}

	private Vector<String> recv(){
		Vector<String> words = new Vector<String>();

		int messageSize = 0, numberOfWords = 0;
		for(int i=0; i<2; i++){
			int c;
			try{ c = in.readByte(); }
			catch(Exception e){
				words.add("desconectar");
				return words;
			}
			if(c < 0)
			c += 256;
			messageSize *= 256;
			messageSize += c;
		}
		if(messageSize < 2 ||  messageSize+2 > windowSize){
			words.add("error");
			words.add("falla en el protocolo");
			return words;
		}

		char[] arrayMessage = new char[messageSize];
		for(int i=0; i<messageSize; i++){
			int c;
			try{ c = in.readByte(); }
			catch(Exception e){
				words.add("desconectar");
				return words;
			}
			if(c < 0)
			c += 256;
			arrayMessage[i] = (char)c;
		}

		numberOfWords = 256 * arrayMessage[0] + arrayMessage[1];
		int i,j;
		for(i=0,j=2; i<numberOfWords && j+2 <= messageSize; i++){
			int size = 256 * arrayMessage[j] + arrayMessage[j+1];
			if(j+2+size > messageSize)
			break;
			
			char[] arrayWord = new char[size];
			for(int l=0; l<size; l++)
			arrayWord[l] = arrayMessage[j+2+l];
		
			words.add(new String(arrayWord));
			j += 2 + size;
		}

		if(i < numberOfWords){
			words = new Vector<String>();
			words.add("error");
			words.add("falla en el protocolo");
		}
		else
		printReceived(words);

		return words;
	}

	public String send(Vector<String> words){
		int messageSize = 2 + 2 * words.size();
		for(int i=0; i<words.size(); i++)
		messageSize += words.get(i).length();

		if(messageSize + 2 <= windowSize){
			char[] arrayMessage = new char[messageSize + 2];
			arrayMessage[0] = (char)(messageSize / 256);
			arrayMessage[1] = (char)(messageSize % 256);
			arrayMessage[2] = (char)(words.size() / 256);
			arrayMessage[3] = (char)(words.size() % 256);
			for(int i=0,j=4; i<words.size(); i++){
				String word = words.get(i);
				arrayMessage[j] = (char)(word.length() / 256);
				arrayMessage[j+1] = (char)(word.length() % 256);
				
				for(int l=0; l<word.length(); l++)
				arrayMessage[j+2+l] = word.charAt(l);
				j += 2 + word.length();
			}
			String message = new String(arrayMessage);

			try{ out.writeBytes(message); }
			catch(Exception e){ return "desconectar"; }
			printSent(words);
			return message;
		}
		else
		return "error";
	}

	@Override
	public void run(){
		while(true){
			Vector<String> command = recv();
			if(command.size() == 1 && (command.get(0).equals("desconectar") || command.get(0).equals("error")))
			break;

			Vector<String> answer = solverCommand(command);
			if(answer.size() >= 1 && answer.get(0).equals("desconectar"))
			break;

			String ans = send(answer);
			if(ans.equals("desconectar") || ans.equals("error"))
			break;
		}
		try{
			client.close();
		}
		catch(Exception e){ System.out.println("No se pudo cerrar el socket"); }

		if(connectionManager.getDisconnectService() != null)
		connectionManager.getDisconnectService().solver(clientInformation, null);

		connectionManager.deleteClient(client);
		printDisconnection();
	}


	private Vector<String> solverCommand(Vector<String> command){
		Vector<String> answer = new Vector<String>();

		if(command.size() == 0){
			answer.add("error");
			return answer;
		}
		if(command.get(0).equals("conexion")){
			answer.add("aceptada");
			answer.add(connectionManager.getName());
			return answer;
		}
		Vector<String> newCommand = new Vector<String>(command.size()-2);
		for(int i=2; i<command.size(); i++)
		newCommand.add(command.get(i));
		
		Service service = connectionManager.findService(command.get(0));
		if(service == null){
			answer.add("error");
			answer.add("servicio no encontrado");
			return answer;
		}
		return service.solver(clientInformation, newCommand);
	}
}
