package ClientServerImplementation;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.TreeMap;
import java.util.Vector;

import java.lang.Thread;
import utilities.Utilities;

public abstract class Client implements Runnable{

	private String serverName;
	private Socket server;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean showConnections;
	private boolean showMessages;
	private TreeMap<String,ServiceConfiguration> serviceConfigurations;
	private int windowSize = 65536;

	public Client(String serverIp, int serverPort) throws Exception{
		try{
			serverName = null;
			server = new Socket(serverIp, serverPort);
			in = new DataInputStream(server.getInputStream());
			out = new DataOutputStream(server.getOutputStream());
			serviceConfigurations = new TreeMap<String,ServiceConfiguration>();
			showConnections = true;
			showMessages = true;
		}
		catch(Exception e){ throw new Exception("El servidor no fue encontrado"); }

		String answer = firstMessage();
		if(answer.equals("desconectar"))
		throw new Exception("No se pudo conectar al servidor");
	}

	public String getServerName(){ return serverName; }
	public String getServerIp(){ return server.getInetAddress().getHostAddress(); }
	public int getServerPort(){ return server.getPort(); }

	public int getLocalPort(){ return server.getLocalPort(); }

	public void setShowConnections(boolean showConnections){ this.showConnections = showConnections; }
	public void setShowMessages(boolean showMessages){ this.showMessages = showMessages; }

	public void addServiceConfiguration(ServiceConfiguration serviceConfiguration){
		serviceConfigurations.put(serviceConfiguration.getServiceName(), serviceConfiguration);
	}

	public ServiceConfiguration findServiceConfiguration(String serviceName){ return serviceConfigurations.get(serviceName); }

	private String firstMessage(){
		Vector<String> message = new Vector<String>();
		Vector<String> answerMessage;
		message.add("conexion");

		String answer = send(message);
		if(answer.equals("desconectar"))
		return answer;

		answerMessage = recv();
		if(answerMessage.size() == 1 && answerMessage.get(0).equals("desconectar"))
		return "desconectar";
		else if(answerMessage.size() == 2 && answerMessage.get(0).equals("aceptada"));
		serverName = answerMessage.get(1);
		
		return "correcto";
	}

	public Vector<String> recv(){
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

	public void printReceived(Vector<String> message){
		if(showMessages){
			if(!(message.size() == 1 && message.get(0).equals("desconectar"))){
				System.out.print("Mensaje Recibido del servidor (" + server.getInetAddress().getHostAddress() + ":" + server.getPort() + ")\t->\t");
				if(message.size() >= 1){
					ServiceConfiguration serviceConfiguration = findServiceConfiguration(message.get(0));
					if(serviceConfiguration == null)
					System.out.println(Utilities.getString(message));
					else
					System.out.println(serviceConfiguration.getVisibleMessage(message));
				}
			}
		}
	}

	public void printSent(Vector<String> message){
		if(showMessages){
			if(!(message.size() == 1 && message.get(0).equals("desconectar"))){
				System.out.print("Mensaje Enviado al servidor (" + server.getInetAddress().getHostAddress() + ":" + server.getPort() + ")\t->\t");
				if(message.size() >= 1){
					ServiceConfiguration serviceConfiguration = findServiceConfiguration(message.get(0));
					if(serviceConfiguration == null)
					System.out.println(Utilities.getString(message));
					else
					System.out.println(serviceConfiguration.getVisibleMessage(message));
				}
			}
		}
	}

	public void printConnection(){
		if(showConnections)
		System.out.println("Se conecto al servidor (" + serverName + ", " + server.getInetAddress().getHostAddress() + ":" + server.getPort() + ")");
	}

	public void printDisconnection(){
		if(showConnections)
		System.out.println("Se desconecto del servidor (" + serverName + ", " + server.getInetAddress().getHostAddress() + ":" + server.getPort() + ")");
	}

	public void close(){
		try{
			server.close();
		}
		catch(Exception e){ System.out.println("Error cerrando el socket"); }
	}

	public boolean isClosed(){
		return server.isClosed();
	}
}
