package ServerManager;

import FileManager.FileDescription;
import utilities.InetSocketAddressComparator;
import utilities.RandomPassword;
import utilities.Pair;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Random;
import java.util.Map.Entry;

public class CentralServerManager implements Runnable{
	private final int maxNodesByQuery = 20;
	private final int maxFileDescriptionsByQuery = 100;
	private final int maxIntermediaryServers = 100;
	private long numberOfUsers;
	private TreeMap<String,User> idToUserTree;
	private TreeMap<InetSocketAddress,User> addressToUserTree;
	private TreeMap<InetSocketAddress,User> listenerAddressToUserTree;
	private TreeMap<String,TreeSet<FileDescription>> userFiles;					// archivos de cada usuario
	private TreeMap<FileDescription,TreeMap<User,Integer>> fileToUsersTree1;		// usuarios activos que tienen el archivo
	private TreeMap<FileDescription,Vector<User>> fileToUsersTree2;
	private Vector<FileDescription> fileDescriptionVector;
	//private Vector<ReentrantReadWriteLock> fileDescriptionLocks;		// lock para cada filedescription
	private ReentrantReadWriteLock lock;					// sobre inserciones y eliminaciones de usuarios
	private Trie mp3Trie, mp4Trie;

	// intemerdiarios
	private TreeMap<InetSocketAddress,TreeSet<User>> intermediaryToNodeTree;	// contiene los nodos que usan cada servidor intermediario
	private TreeMap<InetSocketAddress,InetSocketAddress> intermediaryPorts;		// puerto con el que se conecto -> puerto con el que escucha

	public CentralServerManager(){
		numberOfUsers = 0;
		idToUserTree = new TreeMap<String,User>();
		addressToUserTree = new TreeMap<InetSocketAddress,User>(new InetSocketAddressComparator());
		listenerAddressToUserTree = new TreeMap<InetSocketAddress,User>(new InetSocketAddressComparator());
		userFiles = new TreeMap<String,TreeSet<FileDescription>>();
		fileToUsersTree1 = new TreeMap<FileDescription,TreeMap<User,Integer>>();
		fileToUsersTree2 = new TreeMap<FileDescription,Vector<User>>();
		fileDescriptionVector = new Vector<FileDescription>();
		mp3Trie = new Trie(100);
		mp4Trie = new Trie(100);
		lock = new ReentrantReadWriteLock();

		intermediaryToNodeTree = new TreeMap<InetSocketAddress, TreeSet<User>>(new InetSocketAddressComparator());
		intermediaryPorts = new TreeMap<InetSocketAddress,InetSocketAddress>(new InetSocketAddressComparator());
	}

	public boolean addIntermediaryServer(InetSocketAddress address, InetSocketAddress listenerAddress){
		boolean ans = false;
		lock.writeLock().lock();
		if((intermediaryToNodeTree.size() < maxIntermediaryServers) && (!intermediaryToNodeTree.containsKey(listenerAddress))){
			intermediaryToNodeTree.put(listenerAddress, new TreeSet<User>());
			intermediaryPorts.put(address, listenerAddress);
			ans = true;
		}

		lock.writeLock().unlock();
		return ans;
	}

	public void removeIntermediaryServer(InetSocketAddress address){
		lock.writeLock().lock();
		if(address != null){
			InetSocketAddress listenerAddress = intermediaryPorts.get(address);
			if(listenerAddress != null){
				TreeSet<User> nodes = intermediaryToNodeTree.get(listenerAddress);
				if(!nodes.isEmpty()){
					User node = nodes.first();
					while(node != null){
						if(node.getListenerAddress() != null && node.getListenerAddress().equals(listenerAddress))
						node.setListenerAddress(null);
						node = nodes.higher(node);
					}
				}
				intermediaryToNodeTree.remove(listenerAddress);
			}
			intermediaryPorts.remove(address);
		}
		lock.writeLock().unlock();
	}

	public boolean addServerNode(InetSocketAddress intermediary, String id, String password){
		boolean ans = false;
		lock.writeLock().lock();
		User user = idToUserTree.get(id);
		InetSocketAddress listenerAddress = intermediaryPorts.get(intermediary);
		if(user != null && user.getPassword().equals(password) && listenerAddress != null){
			TreeSet<User> nodes = intermediaryToNodeTree.get(listenerAddress);
			if(nodes != null){
				if(user.getListenerAddress() == null){
					user.setListenerAddress(listenerAddress);
					nodes.add(user);
					ans = true;
				}
			}
		}
		lock.writeLock().unlock();
		return ans;
	}

	public void removeServerNode(InetSocketAddress intermediary, String id){
		lock.writeLock().lock();
		User user = idToUserTree.get(id);
		if(user != null){
			InetSocketAddress listenerAddress = intermediaryPorts.get(intermediary);
			if(listenerAddress != null){
				TreeSet<User> nodes = intermediaryToNodeTree.get(listenerAddress);
				if(nodes != null){
					if((user.getListenerAddress() != null) && (user.getListenerAddress().equals(listenerAddress))){
						user.setListenerAddress(null);
						nodes.remove(user);						
					}
				}
			}
		}
		lock.writeLock().unlock();
	}

	public InetSocketAddress getIntermediaryServer(){
		InetSocketAddress ans = null;
		lock.readLock().lock();

		if(!intermediaryToNodeTree.isEmpty()){
			Entry<InetSocketAddress,TreeSet<User>> entry = intermediaryToNodeTree.firstEntry();
			int minUsers = -1;
			while(entry != null){
				if(minUsers == -1 || entry.getValue().size() < minUsers){
					minUsers = entry.getValue().size();
					ans = entry.getKey();
				}
				entry = intermediaryToNodeTree.higherEntry(entry.getKey());
			}
		}

		lock.readLock().unlock();
		return ans;
	}

	public Vector<Pair<InetSocketAddress,String>> getServerNodes(InetSocketAddress address, FileDescription fileDescription){
		lock.readLock().lock();
		Vector<Pair<InetSocketAddress,String>> ans;

		ans = new Vector<Pair<InetSocketAddress,String>>();
		Vector<User> serverNodes = fileToUsersTree2.get(fileDescription);
		if(serverNodes != null){
			for(int i=0; i<20 && i<serverNodes.size(); i++){
				InetSocketAddress intermediary = serverNodes.get(i).getListenerAddress();
				if(intermediary != null){
					String id = serverNodes.get(i).getId();
					ans.add(new Pair<InetSocketAddress,String>(intermediary,id));
				}
			}
		}

		lock.readLock().unlock();
		return ans;
	}


	public User signInUser(InetSocketAddress address){
		lock.writeLock().lock();

		String password = RandomPassword.generatePassword(20, true, false, true);
		numberOfUsers++;
		String id = String.valueOf(numberOfUsers);
		User user = new User(id, "", password, address);
		idToUserTree.put(id, user);
		addressToUserTree.put(address, user);
		userFiles.put(id, new TreeSet<FileDescription>());
		user.setActive(true);

		lock.writeLock().unlock();
		return user;
	}

	public boolean logInUser(InetSocketAddress address, String idUser, String password){
		boolean ans = false;
		lock.writeLock().lock();

		User user = idToUserTree.get(idUser);
		if(user != null && user.getPassword().equals(password)){
			if(!(user.getAddress().equals(address))){
				addressToUserTree.remove(user.getAddress());
				user.setAddress(address);
				addressToUserTree.put(address, user);
			}
			user.setActive(true);
			ans = true;
		}

		if(ans == true){
			// se inserta al usuario en cada uno de los FileDescription asociados a el
			TreeSet<FileDescription> fileDescriptionSet = userFiles.get(user.getId());
			if(!fileDescriptionSet.isEmpty()){
				FileDescription fileDescription = fileDescriptionSet.first();
				while(fileDescription != null){
					TreeMap<User,Integer> userPositions = fileToUsersTree1.get(fileDescription);

					if(userPositions.get(user) == null){
						Vector<User> users = fileToUsersTree2.get(fileDescription);
						users.add(user);
						userPositions.put(user, users.size()-1);
					}
					fileDescription = fileDescriptionSet.higher(fileDescription);
				}
			}
		}

		lock.writeLock().unlock();
		return ans;
	}

	public void logOutUser(InetSocketAddress address){
		lock.writeLock().lock();
		User user = addressToUserTree.get(address);
		if(user != null){

			// Se elimina al usuario en cada uno de los FileDescription asociados en los registros de usuarios activos
			TreeSet<FileDescription> fileDescriptionSet = userFiles.get(user.getId());
			if(!fileDescriptionSet.isEmpty()){
				FileDescription fileDescription = fileDescriptionSet.first();
				while(fileDescription != null){
					TreeMap<User,Integer> userPositions = fileToUsersTree1.get(fileDescription);
					Vector<User> users = fileToUsersTree2.get(fileDescription);

					Integer pos = userPositions.get(user);
					if(pos != null){
						userPositions.remove(user);
						users.set(pos, users.get(users.size()-1));
						users.remove(users.size()-1);
						if(users.size() > 0 && pos < users.size())
						userPositions.put(users.get(pos), pos);
					}

					fileDescription = fileDescriptionSet.higher(fileDescription);
				}
			}

			// eliminar asociacion a intermediarios
			InetSocketAddress listenerAddress = user.getListenerAddress();
			if(listenerAddress != null){
				TreeSet<User> users = intermediaryToNodeTree.get(listenerAddress);
				users.remove(user);
			}
			
			user.setActive(false);
		}

		lock.writeLock().unlock();
	}

	public boolean addFileDescription(InetSocketAddress address, FileDescription fileDescription){
		boolean ans = false;
		lock.writeLock().lock();

		User user = addressToUserTree.get(address);
		if(user != null && user.isActive()){
			ans = true;

			TreeSet<FileDescription> files = userFiles.get(user.getId());
			if(!files.contains(fileDescription))
			files.add(fileDescription);
			
			if(fileToUsersTree1.get(fileDescription) == null){
				fileToUsersTree1.put(fileDescription, new TreeMap<User,Integer>());
				fileToUsersTree2.put(fileDescription, new Vector<User>());
				fileDescriptionVector.add(fileDescription);
			}
			
			TreeMap<User,Integer> positions = fileToUsersTree1.get(fileDescription);
			Vector<User> users = fileToUsersTree2.get(fileDescription);
			if(positions.get(user) == null){
				users.add(user);
				positions.put(user, users.size()-1);
			}
		}

		lock.writeLock().unlock();
		return ans;
	}

	public boolean clearUserFileDescriptions(InetSocketAddress address){
		boolean ans = false;
		lock.writeLock().lock();

		User user = addressToUserTree.get(address);
		if(user != null && user.isActive()){
			ans = true;

			// Se elimina al usuario en cada uno de los FileDescription asociados
			TreeSet<FileDescription> fileDescriptionSet = userFiles.get(user.getId());
			if(!fileDescriptionSet.isEmpty()){
				FileDescription fileDescription = fileDescriptionSet.first();
				while(fileDescription != null){
					TreeMap<User,Integer> userPositions = fileToUsersTree1.get(fileDescription);
					Vector<User> users = fileToUsersTree2.get(fileDescription);

					Integer pos = userPositions.get(user);
					if(pos != null){
						userPositions.remove(user);
						users.set(pos, users.get(users.size()-1));
						users.remove(users.size()-1);
						if(users.size() > 0 && pos < users.size())
						userPositions.put(users.get(pos), pos);
					}

					fileDescription = fileDescriptionSet.higher(fileDescription);
				}
			}

			// Reiniciar el arbol de descripciones de archivo del usuario
			userFiles.put(user.getId(), new TreeSet<FileDescription>());
		}

		lock.writeLock().unlock();
		return ans;
	}

	public Vector<FileDescription> filterMP3(InetSocketAddress address, String title, String artist, String album){
		lock.readLock().lock();
		Vector<FileDescription> ans;

		User user = addressToUserTree.get(address);
		if((user == null) || (!user.isActive()))
		ans = null;
		else{
			TreeSet<FileDescription> fileDescriptionSet = new TreeSet<FileDescription>();

			if(!title.equals("")){
				Vector<FileDescription> fdVector = mp3Trie.search(title);
				for(int i=0; i<fdVector.size(); i++)
				fileDescriptionSet.add(fdVector.get(i));
			}
			if(!artist.equals("")){
				Vector<FileDescription> fdVector = mp3Trie.search(artist);
				for(int i=0; i<fdVector.size(); i++)
				fileDescriptionSet.add(fdVector.get(i));
			}
			if(!album.equals("")){
				Vector<FileDescription> fdVector = mp3Trie.search(album);
				for(int i=0; i<fdVector.size(); i++)
				fileDescriptionSet.add(fdVector.get(i));
			}

			ans = new Vector<FileDescription>(fileDescriptionSet.size());
			if(!fileDescriptionSet.isEmpty()){
				FileDescription fileDescription = fileDescriptionSet.first();
				while(fileDescription != null){
					ans.add(fileDescription);
					fileDescription = fileDescriptionSet.higher(fileDescription);
				}
			}
		}

		lock.readLock().unlock();
		return ans;
	}

	public Vector<FileDescription> filterMP4(InetSocketAddress address, String name){
		lock.readLock().lock();
		Vector<FileDescription> ans;

		User user = addressToUserTree.get(address);
		if((user == null) || (!user.isActive()))
		ans = null;
		else{
			TreeSet<FileDescription> fileDescriptionSet = new TreeSet<FileDescription>();

			if(!name.equals("")){
				Vector<FileDescription> fdVector = mp4Trie.search(name);
				for(int i=0; i<fdVector.size(); i++)
				fileDescriptionSet.add(fdVector.get(i));
			}

			ans = new Vector<FileDescription>(fileDescriptionSet.size());
			if(!fileDescriptionSet.isEmpty()){
				FileDescription fileDescription = fileDescriptionSet.first();
				while(fileDescription != null){
					ans.add(fileDescription);
					fileDescription = fileDescriptionSet.higher(fileDescription);
				}
			}
		}

		lock.readLock().unlock();
		return ans;
	}

	@Override
	public void run(){
		while(true){
			lock.readLock();
			int size = fileDescriptionVector.size();
			lock.readLock();

			for(int i=0; i<size; i++){
				lock.readLock().lock();
				FileDescription fileDescription = fileDescriptionVector.get(i);
				if(fileDescription.getType().equals("mp3"))
				mp3Trie.add(fileDescription, fileToUsersTree2.get(fileDescription).size());
				else if(fileDescription.getType().equals("mp4"))
				mp4Trie.add(fileDescription, fileToUsersTree2.get(fileDescription).size());
				lock.readLock().unlock();
			}
		}
	}
}