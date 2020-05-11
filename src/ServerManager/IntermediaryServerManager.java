package ServerManager;

import utilities.InetSocketAddressComparator;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Vector;

public class IntermediaryServerManager{
	
	private TreeMap<String,TreeSet<InetSocketAddress>> serverIdToAddressTree;			// solo direcciones no usadas
	private TreeMap<InetSocketAddress,IntermediaryConnection> clientToConnectionTree;
	private TreeMap<InetSocketAddress,IntermediaryConnection> serverToConnectionTree;
	private LinkedList<IntermediaryConnection> pendingConnections;
	private TreeSet<InetSocketAddress> pendingAddresses;
	private LinkedList<IntermediaryConnection> finishedConnections; // --- eliminar
	private TreeSet<InetSocketAddress> finishedAddresses; // --- eliminar
	private LinkedList<String> finishedServerIdList;
	private TreeSet<String> finishedServerIdTree;
	private ReentrantReadWriteLock lock;

	public IntermediaryServerManager(){
		serverIdToAddressTree = new TreeMap<String,TreeSet<InetSocketAddress>>();
		clientToConnectionTree = new TreeMap<InetSocketAddress,IntermediaryConnection>(new InetSocketAddressComparator());	// agregar comparador
		serverToConnectionTree = new TreeMap<InetSocketAddress,IntermediaryConnection>(new InetSocketAddressComparator());
		pendingConnections = new LinkedList<IntermediaryConnection>();
		pendingAddresses = new TreeSet<InetSocketAddress>(new InetSocketAddressComparator());
		finishedConnections = new LinkedList<IntermediaryConnection>();	// --- eliinar
		finishedAddresses = new TreeSet<InetSocketAddress>(new InetSocketAddressComparator()); // --- eliminar
		finishedServerIdList = new LinkedList<String>();
		finishedServerIdTree = new TreeSet<String>();
		lock = new ReentrantReadWriteLock();
	}


	// modificar
	public boolean createPendingConnection(InetSocketAddress server, String serverId, String serverPassword){
		lock.writeLock().lock();

		boolean ans = false;
		if(server != null && serverId != null && serverPassword != null){
			if((!serverToConnectionTree.containsKey(server)) && (!finishedServerIdTree.contains(serverId)) && (!pendingAddresses.contains(server))){
				IntermediaryConnection connection = new IntermediaryConnection(server, serverId, serverPassword);
				pendingAddresses.add(server);
				pendingConnections.add(connection);
				ans = true;
			}
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean joinConnection(InetSocketAddress client, String serverId){
		lock.writeLock().lock();

		boolean ans = false;
		if(client != null && serverId != null){
			TreeSet<InetSocketAddress> servers = serverIdToAddressTree.get(serverId);
			if(servers != null && !servers.isEmpty()){
				InetSocketAddress server = servers.first();
				servers.remove(server);
				IntermediaryConnection connection = serverToConnectionTree.get(server);
				if(connection != null && connection.getClientAddress() == null){
					connection.setClientAddress(client);
					clientToConnectionTree.put(client, connection);
					ans = true;
				}
			}
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean addConnection(IntermediaryConnection connection){
		lock.writeLock().lock();
		boolean ans = false;
		if(connection != null && connection.isActive() && connection.getServerAddress() != null && connection.getServerId() != null){
			InetSocketAddress server = connection.getServerAddress();
			String serverId = connection.getServerId();

			// se elimina la anterior conexion con esa direccion de servidor asociada a un cliente
			IntermediaryConnection pastConnection = serverToConnectionTree.get(server);
			if(pastConnection != null && pastConnection.getClientAddress() != null)
			clientToConnectionTree.remove(pastConnection.getClientAddress());

			serverToConnectionTree.put(server, connection);
			
			TreeSet<InetSocketAddress> servers = serverIdToAddressTree.get(serverId);
			if(servers == null){
				serverIdToAddressTree.put(serverId, new TreeSet<InetSocketAddress>(new InetSocketAddressComparator()));	// comparador
				servers = serverIdToAddressTree.get(serverId);
			}
			servers.add(server);

			ans = true;
		}
		lock.writeLock().unlock();
		return ans;
	}

	public void removeConnection(IntermediaryConnection connection){
		lock.writeLock().lock();
		privateRemoveConnection(connection);
		lock.writeLock().unlock();
	}

	public boolean isEnabledConnection(IntermediaryConnection connection){
		lock.readLock().lock();
		boolean ans = false;
		InetSocketAddress server = connection.getServerAddress();
		if(server != null && serverToConnectionTree.containsKey(server))
		ans = true;

		lock.readLock().unlock();
		return ans;
	}	

	public IntermediaryConnection getConnectionByClientAddress(InetSocketAddress client){
		lock.readLock().lock();
		IntermediaryConnection connection = clientToConnectionTree.get(client);
		lock.readLock().unlock();
		return connection;
	}

	public IntermediaryConnection getConnectionByServerAddress(InetSocketAddress server){
		lock.readLock().lock();
		IntermediaryConnection connection = serverToConnectionTree.get(server);
		lock.readLock().unlock();
		return connection;
	}

	public Vector<String> getClientMessage(InetSocketAddress client){
		Vector<String> ans = null;
		boolean active = true;

		lock.readLock().lock();
		IntermediaryConnection connection = clientToConnectionTree.get(client);
		lock.readLock().unlock();

		while(active && ans == null){
			lock.readLock().lock();

			if(clientToConnectionTree.get(client) == null)
			active = false;
			else{
				if(!connection.isActive()){
					ans = connection.getClientMessage();
					active = false;
				}
				else
				ans = connection.getClientMessage();
			}
			lock.readLock().unlock();
		}

		lock.writeLock().lock();
		if(!active){
			if(connection != null)
			privateRemoveConnection(connection);
		}
		lock.writeLock().unlock();

		return ans;
	}

	public Vector<String> getServerMessage(InetSocketAddress server){
		Vector<String> ans = null;
		boolean active = true;
		
		lock.readLock().lock();
		IntermediaryConnection connection = serverToConnectionTree.get(server);
		lock.readLock().unlock();

		while(active && ans == null){
			lock.readLock().lock();
			if(serverToConnectionTree.get(server) == null)
			active = false;	
			else{
				if(!connection.isActive()){
					ans = connection.getServerMessage();
					active = false;
				}
				else
				ans = connection.getServerMessage();
			}
			lock.readLock().unlock();
		}
	
		lock.writeLock().lock();
		if(!active){
			if(connection != null)
			privateRemoveConnection(connection);
		}
		lock.writeLock().unlock();

		return ans;
	}

	public boolean setClientMessage(InetSocketAddress server, Vector<String> message){
		boolean ans = false;
		lock.writeLock().lock();

		IntermediaryConnection connection = serverToConnectionTree.get(server);
		if(connection != null){
			ans = connection.setClientMessage(message);
			if(!ans)
			privateRemoveConnection(connection);
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean setServerMessage(InetSocketAddress client, Vector<String> message){
		boolean ans = false;
		lock.writeLock().lock();

		IntermediaryConnection connection = clientToConnectionTree.get(client);
		if(connection != null){
			ans = connection.setServerMessage(message);
			if(!ans)
			privateRemoveConnection(connection);
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean isPendingConnection(InetSocketAddress server){
		lock.readLock().lock();
		boolean ans = pendingAddresses.contains(server);
		lock.readLock().unlock();
		return ans;
	}

	public IntermediaryConnection nextPendingConnection(){
		lock.readLock().lock();
		IntermediaryConnection ans = null;
		if(!pendingConnections.isEmpty())
		ans = pendingConnections.getFirst();
		lock.readLock().unlock();
		return ans;
	}

	public IntermediaryConnection removeNextPendingConnection(){
		lock.writeLock().lock();
		IntermediaryConnection ans = null;
		if(!pendingConnections.isEmpty()){
			ans = pendingConnections.remove(); 
			pendingAddresses.remove(ans.getServerAddress());
		}
		lock.writeLock().unlock();
		return ans;
	}

	/*public IntermediaryConnection removeNextFinishedConnection(){
		lock.writeLock().lock();
		IntermediaryConnection ans = null;
		if(!finishedConnections.isEmpty()){
			ans = finishedConnections.remove();
			finishedAddresses.remove(ans.getServerAddress());
		}
		lock.writeLock().unlock();
		return ans;
	}*/

	public String nextFinishedConnection(){
		lock.writeLock().lock();
		String ans = null;
		if(!finishedServerIdList.isEmpty())
		ans = finishedServerIdList.getFirst();
		lock.writeLock().unlock();
		return ans;
	}

	public String removeNextFinishedConnection(){
		lock.writeLock().lock();
		String ans = null;
		if(!finishedServerIdList.isEmpty()){
			ans = finishedServerIdList.remove();
			finishedServerIdTree.remove(ans);
		}
		lock.writeLock().unlock();
		return ans;
	}

	/*private void privateRemoveConnection(IntermediaryConnection connection){
		if(connection != null){
			if(connection.getClientAddress() != null)
			clientToConnectionTree.remove(connection.getClientAddress());
			if(connection.getServerAddress() != null){
				serverToConnectionTree.remove(connection.getServerAddress());
				if(connection.getServerId() != null){
					TreeSet<InetSocketAddress> servers = serverIdToAddressTree.get(connection.getServerId());
					if(servers != null)
					servers.remove(connection.getServerAddress());
					
					if(!finishedAddresses.contains(connection.getServerAddress())){
						finishedAddresses.add(connection.getServerAddress());
						finishedConnections.add(connection);
					}
				}
			}
		}
	}*/

	private void privateRemoveConnection(IntermediaryConnection connection){
		if(connection != null){
			if(connection.getClientAddress() != null)
			clientToConnectionTree.remove(connection.getClientAddress());
			if(connection.getServerAddress() != null){
				serverToConnectionTree.remove(connection.getServerAddress());
				if(connection.getServerId() != null){
					TreeSet<InetSocketAddress> servers = serverIdToAddressTree.get(connection.getServerId());
					if(servers != null)
					servers.remove(connection.getServerAddress());
					
					if(!finishedServerIdList.contains(connection.getServerId())){
						finishedServerIdList.add(connection.getServerId());
						finishedServerIdTree.add(connection.getServerId());

						//finishedAddresses.add(connection.getServerId());
						//finishedConnections.add(connection);
					}
				}
			}
		}
	}
}