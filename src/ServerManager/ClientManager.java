package ServerManager;

import FileManager.Directory;
import FileManager.FileDescriptionBuilder;
import FileManager.FileDescription;
import FileManager.FileWriter;
import FileManager.MP3File;
import utilities.Pair;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Vector;

public class ClientManager{

	private final int blockSize = 1048576;
	private int numberOfDownloaders;
	private int numberOfServerNodes;
	private String userId, userPassword;
	private Directory directory;
	private TreeMap<FileDescription,File> files;
	private TreeMap<FileDescription,FileWriter> downloads;
	private TreeMap<FileDescription,Integer> downloaders;
	private InetSocketAddress intermediaryServer;
	private boolean activeClient;
	private boolean activeClientUpdater;
	private Entry<FileDescription,FileWriter> currentEntry;

	private ReentrantReadWriteLock userLock;
	private ReentrantReadWriteLock filesLock;
	private ReentrantReadWriteLock downloadsLock;
	private ReentrantReadWriteLock clientLock;
	private ReentrantReadWriteLock clientUpdaterLock;
	private ReentrantReadWriteLock intermediaryServerLock;

	
	public ClientManager(){
		numberOfDownloaders = 0;
		numberOfServerNodes = 0;
		userId = null;
		userPassword = null;
		directory = null;
		files = new TreeMap<FileDescription,File>();
		downloads = new TreeMap<FileDescription,FileWriter>();
		downloaders = new TreeMap<FileDescription,Integer>();
		intermediaryServer = null;
		activeClient = false;
		activeClientUpdater = false;
		currentEntry = null;
		userLock = new ReentrantReadWriteLock();
		filesLock = new ReentrantReadWriteLock();
		downloadsLock = new ReentrantReadWriteLock();
		clientLock = new ReentrantReadWriteLock();
		clientUpdaterLock = new ReentrantReadWriteLock();
		intermediaryServerLock = new ReentrantReadWriteLock();
	}

	/*** METODOS SOBRE INFORMACION DEL USUARIO ***/
	public void setUser(String userId, String userPassword){
		userLock.writeLock().lock();
		this.userId = userId;
		this.userPassword = userPassword;
		userLock.writeLock().unlock();
	}

	public String getUserId(){
		userLock.readLock().lock();
		String ans = userId;
		userLock.readLock().unlock();
		return ans;
	}

	public String getUserPassword(){
		userLock.readLock().lock();
		String ans = userPassword;
		userLock.readLock().unlock();
		return ans;
	}

	/*** METODOS SOBRE LOS ARCHIVOS DEL USUARIO ***/
	public void setFolder(File folder){
		filesLock.writeLock().lock();
		if(folder != null)
		directory = new Directory(folder);
		filesLock.writeLock().unlock();
	}

	public void updateFiles(){
		filesLock.writeLock().lock();
		files = new TreeMap<FileDescription,File>();
		if(directory != null){
			Vector<Pair<FileDescription,File>> fdsAndFiles = directory.getFiles();
			for(int i=0; i<fdsAndFiles.size(); i++)
			files.put(fdsAndFiles.get(i).getFirst(), fdsAndFiles.get(i).getSecond());
		}
		filesLock.writeLock().unlock();
	}

	public Vector<FileDescription> getFileDescriptions(){
		filesLock.readLock().lock();
		Vector<FileDescription> ans = new Vector<FileDescription>(files.size());
		if(!files.isEmpty()){
			Entry<FileDescription,File> entry = files.firstEntry();
			while(entry != null){
				ans.add(entry.getKey());
				entry = files.higherEntry(entry.getKey());
			}
		}
		filesLock.readLock().unlock();
		return ans;
	}
	
	public boolean isPresent(FileDescription fileDescription){
		filesLock.readLock().lock();
		boolean ans = false;
		if(fileDescription != null)
		ans = files.containsKey(fileDescription);
		filesLock.readLock().unlock();
		return ans;
	}

	public File getFile(FileDescription fileDescription){
		filesLock.readLock().lock();
		File ans = null;
		if(fileDescription != null)
		ans = files.get(fileDescription);
		filesLock.readLock().unlock();
		return ans;
	}

	/*** METODOS SOBRE LAS DESCARGAS PENDIENTES Y LOS PROCESOS QUE ESTAN DESCARGANDO ***/
	public boolean addDownload(File folder, FileDescription fileDescription){
		String fileName = fileDescription.getFileName();
		String newFileName = "";

		for(int i=0; i<fileName.length(); i++){
			if(32 <= fileName.charAt(i) && fileName.charAt(i) <= 126 && fileName.charAt(i) != '/')
			newFileName += fileName.charAt(i);
		}

		downloadsLock.writeLock().lock();
		boolean ans = false;
		try{
			if(!downloads.containsKey(fileDescription)){
				File file = new File(folder.getAbsolutePath() + "/" + newFileName);
				FileWriter fw = new FileWriter(file, fileDescription.getFileSize(), 0, blockSize);
				downloads.put(fileDescription, fw);
				downloaders.put(fileDescription, 0);
			}
			ans = true;
		}
		catch(Exception e){}
		downloadsLock.writeLock().unlock();
		return ans;
	}

	public void removeDownload(FileDescription fileDescription){
		downloadsLock.writeLock().lock();
		if(fileDescription != null){
			if(downloaders.containsKey(fileDescription))
			numberOfDownloaders -= downloaders.get(fileDescription);
			downloads.remove(fileDescription);
			downloaders.remove(fileDescription);
		}
		downloadsLock.writeLock().unlock();
	}

	public FileDescription nextDownload(){
		downloadsLock.writeLock().lock();

		FileDescription ans = null;
		if(currentEntry == null){
			if(!downloads.isEmpty())
			currentEntry = downloads.firstEntry();
		}
		else{
			currentEntry = downloads.higherEntry(currentEntry.getKey());
			if(currentEntry == null){
				if(!downloads.isEmpty())
				currentEntry = downloads.firstEntry();
			}
		}
		if(currentEntry != null)
		ans = currentEntry.getKey();

		downloadsLock.writeLock().unlock();
		return ans;
	}

	public FileWriter getFileWriterByDownload(FileDescription fileDescription){
		downloadsLock.readLock().lock();
		FileWriter ans = null;
		if(fileDescription != null)
		ans = downloads.get(fileDescription);
		downloadsLock.readLock().unlock();
		return ans;
	}

	public void addDownloader(FileDescription fileDescription){
		downloadsLock.writeLock().lock();
		if(fileDescription != null && downloaders.containsKey(fileDescription)){
			int count = downloaders.get(fileDescription);
			downloaders.put(fileDescription, count + 1);
			numberOfDownloaders++;
		}		
		downloadsLock.writeLock().unlock();
	}

	public void removeDownloader(FileDescription fileDescription){
		downloadsLock.writeLock().lock();
		if(fileDescription != null && downloaders.containsKey(fileDescription)){
			int count = downloaders.get(fileDescription);
			downloaders.put(fileDescription, count - 1);
			numberOfDownloaders--;
		}	
		downloadsLock.writeLock().unlock();
	}

	public int getNumberOfDownloaders(){
		downloadsLock.readLock().lock();
		int ans = numberOfDownloaders;
		downloadsLock.readLock().unlock();
		return ans;
	}

	public int getNumberOfDownloadersByFileDescription(FileDescription fileDescription){
		downloadsLock.readLock().lock();
		int ans = 0;
		if(fileDescription != null && downloaders.containsKey(fileDescription))
		ans = downloaders.get(fileDescription);
		downloadsLock.readLock().unlock();
		return ans;
	}

	/*** METODOS SOBRE LOS NODOS SERVIDORES Y EL SERVIDOR INTERMEDIARIO CON EL QUE SE COMUNICAN ***/
	public void setIntermediaryServer(InetSocketAddress intermediaryServer){
		intermediaryServerLock.writeLock().lock();
		this.intermediaryServer = intermediaryServer;
		numberOfServerNodes = 0;
		intermediaryServerLock.writeLock().unlock();
	}

	public InetSocketAddress getIntermediaryServer(){
		intermediaryServerLock.readLock().lock();
		InetSocketAddress ans = intermediaryServer;
		intermediaryServerLock.readLock().unlock();
		return ans;
	}

	public void addOneServerNode(){
		intermediaryServerLock.writeLock().lock();
		numberOfServerNodes++;
		intermediaryServerLock.writeLock().unlock();
	}

	public void removeOneServerNode(){
		intermediaryServerLock.writeLock().lock();
		numberOfServerNodes--;
		intermediaryServerLock.writeLock().unlock();
	}

	public int getNumberOfServerNodes(){
		intermediaryServerLock.readLock().lock();
		int ans = numberOfServerNodes;
		intermediaryServerLock.readLock().unlock();
		return ans;
	}

	/*** METODOS SOBRE LA PARTE DEL CLIENTE QUE HACE CONSULTAS AL SERVIDOR CENTRAL ***/
	public void setActiveClient(boolean activeClient){
		clientLock.writeLock().lock();
		this.activeClient = activeClient;
		clientLock.writeLock().unlock();
	}

	public boolean isActiveClient(){
		clientLock.readLock().lock();
		boolean ans = activeClient;
		clientLock.readLock().unlock();
		return ans;
	}

	/*** METODOS SOBRE LA PARTE DEL CLIENTE QUE ACTUALIZA LAS CONEXIONES A LOS SERVIDORES INTERMEDIARIOS Y CENTRAL***/
	public void setActiveClientUpdater(boolean activeClientUpdater){
		clientUpdaterLock.writeLock().lock();
		this.activeClientUpdater = activeClientUpdater;
		clientUpdaterLock.writeLock().unlock();
	}

	public boolean isActiveClientUpdater(){
		clientUpdaterLock.readLock().lock();
		boolean ans = activeClientUpdater;
		clientUpdaterLock.readLock().unlock();
		return ans;
	}
}
