package ClientServerImplementation;

public class ClientInformation{
	private String ip;
	private int port;

	public ClientInformation(String ip, int port){
		this.ip = ip;
		this.port = port;
	}

	public String getIp(){ return ip; }
	public int getPort(){ return port; }
}