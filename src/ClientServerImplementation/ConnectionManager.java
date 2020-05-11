package ClientServerImplementation;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeSet;
import java.util.TreeMap;

import utilities.InetSocketAddressComparator;

// server: acepta nuevos clientes
public class ConnectionManager implements Runnable{

	private String name;
	private int port;
	private int maxClients;
	private ServerSocket server;
	private TreeSet<InetSocketAddress> clients;
	private ExecutorService hilos;
	private ReentrantReadWriteLock clientTreeLock;
	private boolean showConnections;
	private boolean showMessages;
	private TreeMap<String,Service> services;
	private TreeMap<String,ServiceConfiguration> serviceConfigurations;
	private Service disconnectService;
	protected boolean enableAddServices;

	public ConnectionManager(String name, int port, int maxClients) throws Exception {
		this.name = name;
		this.port = port;
		this.maxClients = maxClients;
		showConnections = true;
		showMessages = true;
		services = new TreeMap<String,Service>();
		serviceConfigurations = new TreeMap<String,ServiceConfiguration>();
		disconnectService = null;
		enableAddServices = true;
		try{
			server = new ServerSocket(port);
		}
		catch(Exception e){ throw new Exception("No se pudo crear el servidor"); }
		clients = new TreeSet<InetSocketAddress>(new InetSocketAddressComparator());
		hilos = Executors.newFixedThreadPool(maxClients);
		clientTreeLock = new ReentrantReadWriteLock();
	}

	public String getName(){ return name; }
	public int getPort(){ return port; }

	public boolean getShowConnections(){ return showConnections; }
	public boolean getShowMessages(){ return showMessages; }
	public void setShowConnections(boolean showConnections){ this.showConnections = showConnections; }
	public void setShowMessages(boolean showMessages){ this.showMessages = showMessages; }
	
	public Service getDisconnectService(){ return disconnectService; }
	public void setDisconnectService(Service disconnectService){ this.disconnectService = disconnectService; }

	public void addService(Service service){
		if(enableAddServices)
		services.put(service.getServiceName(), service);
	}

	public Service findService(String serviceName){ return services.get(serviceName); }


	public void addServiceConfiguration(ServiceConfiguration serviceConfiguration){
		if(enableAddServices)
		serviceConfigurations.put(serviceConfiguration.getServiceName(), serviceConfiguration);
	}

	public ServiceConfiguration findServiceConfiguration(String serviceName){ return serviceConfigurations.get(serviceName); }

	protected boolean addClient(Socket client){
		boolean ans = false;
		clientTreeLock.writeLock().lock();
		if(clients.size() < maxClients){
			clients.add(new InetSocketAddress(client.getInetAddress(), client.getPort()));
			ans = true;
		}
		clientTreeLock.writeLock().unlock();
		return ans;
	}

	protected void deleteClient(Socket client){
		clientTreeLock.writeLock().lock();
		clients.remove(new InetSocketAddress(client.getInetAddress(), client.getPort()));
		clientTreeLock.writeLock().unlock();
	}

	public int numberOfClients(){
		clientTreeLock.readLock().lock();
		int ans = clients.size();
		clientTreeLock.readLock().unlock();
		return ans;
	}

	@Override
	public void run(){
		enableAddServices = false;
		while(true){
			try{
				Socket client = server.accept();
				if(numberOfClients() < maxClients){
					try{
						hilos.execute(new CommunicationManager(this, client));
					}
					catch(Exception e){
						System.out.println(e.getMessage());
						client.close();
					}
				}
				else
				client.close();				
			}
			catch(Exception e){
				System.out.println("No se pudo establecer conexion con el cliente");
			}
		}
	}
}