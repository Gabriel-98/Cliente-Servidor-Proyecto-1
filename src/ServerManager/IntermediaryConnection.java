package ServerManager;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.net.InetSocketAddress;
import java.util.Vector;
import java.util.LinkedList;

public class IntermediaryConnection{

	private String id, serverId, serverPassword;
	private InetSocketAddress clientAddress, serverAddress;
	//private Vector<String> clientMessage;
	private LinkedList<Vector<String>> clientMessages;	// lista de mensajes del cliente
	private Vector<String> serverMessage;		// ultimo mensaje recibido
	private ReentrantReadWriteLock lock;
	private boolean active;
	
	public IntermediaryConnection(InetSocketAddress serverAddress, String serverId, String serverPassword){
		this.serverAddress = serverAddress;
		this.serverId = serverId;
		this.serverPassword = serverPassword;
		clientAddress = null;
		serverAddress = null;
		//clientMessage = null;
		clientMessages = new LinkedList<Vector<String>>();
		serverMessage = null;
		active = true;
		lock = new ReentrantReadWriteLock();
	}

	public String getServerId(){ return serverId; }
	public String getServerPassword(){ return serverPassword; }

	public boolean isActive(){
		lock.readLock().lock();
		boolean ans = active;
		lock.readLock().unlock();
		return ans;
	}

	public InetSocketAddress getClientAddress(){
		lock.readLock().lock();
		InetSocketAddress ans = clientAddress;
		lock.readLock().unlock();
		return ans;
	}

	public InetSocketAddress getServerAddress(){
		lock.readLock().lock();
		InetSocketAddress ans = serverAddress;
		lock.readLock().unlock();
		return ans;
	}

	public boolean setClientAddress(InetSocketAddress address){
		boolean ans = false;
		lock.writeLock().lock();

		if(active){
			if(serverAddress != null && clientAddress == null){
				clientAddress = address;
				ans = true;
			}
			else
			active = false;
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean setServerAddress(InetSocketAddress address){
		boolean ans = false;
		lock.writeLock().lock();

		if(active){
			if(serverAddress == null && clientAddress == null){
				serverAddress = address;
				ans = true;
			}
			else
			active = false;
		}

		lock.writeLock().unlock();
		return ans;
	}	

	public Vector<String> getClientMessage(){
		Vector<String> ans;
		lock.readLock().lock();

		if(!active)
		ans = null;
		else if(clientMessages == null)
		ans = null;
		else if(clientMessages.size() == 0)
		ans = null;
		else{
			ans = clientMessages.remove();
			/*ans = new Vector<String>(clientMessage.size());
			for(int i=0; i<clientMessage.size(); i++)
			ans.add(clientMessage.get(i));*/
		}

		lock.readLock().unlock();
		return ans;
	}

	public Vector<String> getServerMessage(){
		Vector<String> ans;
		lock.readLock().lock();

		if(!active)
		ans = null;
		else if(serverMessage == null)
		ans = null;
		else{
			ans = new Vector<String>(serverMessage.size());
			for(int i=0; i<serverMessage.size(); i++)
			ans.add(serverMessage.get(i));
			serverMessage = null;
		}

		lock.readLock().unlock();
		return ans;
	}

	public boolean setClientMessage(Vector<String> message){
		lock.writeLock().lock();

		boolean ans = false;
		if(clientAddress != null && serverAddress != null && active && message != null){
			ans = true;
			clientMessages.add(message);
			/*clientMessage = new Vector<String>(message.size());
			for(int i=0; i<message.size(); i++)
			clientMessage.add(message.get(i));*/
		}
		else
		active = false;

		lock.writeLock().unlock();
		return ans;
	}

	public boolean setServerMessage(Vector<String> message){
		boolean ans = false;
		lock.writeLock().lock();

		if(clientAddress != null && serverAddress != null && active && serverMessage == null && message != null){
			ans = true;
			serverMessage = new Vector<String>(message.size());
			for(int i=0; i<message.size(); i++)
			serverMessage.add(message.get(i));
		}
		else
		active = false;

		lock.writeLock().unlock();

		return ans;
	}
}
