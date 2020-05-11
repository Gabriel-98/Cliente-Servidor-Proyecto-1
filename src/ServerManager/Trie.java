package ServerManager;

import FileManager.FileDescription;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Vector;

public class Trie{
	private final int maxNodes = 1000000;
	private final int maxSubstringSize = 50;
	private int maxElementsByNode;
	private CharacterTranslator characterTranslator;
	private TrieNode root;
	private Vector<TrieNode> nodes;
	private ReentrantReadWriteLock lock;		// este lock solo sera de escritura si va borrar nodos
	private ReentrantReadWriteLock vectorLock;	// solo se enfoca en sincronizar inserciones cuando el lock principal esta en read
												// permitira rechazar rapidamente nuevas consultas cuando este bloqueado
	private ReentrantReadWriteLock activeLock;	// se utiliza para modificar el valor de active
	private boolean active;

	public Trie(int maxElementsByNode){
		this.maxElementsByNode = maxElementsByNode;
		this.characterTranslator = new CharacterTranslator();
		lock = new ReentrantReadWriteLock();
		vectorLock = new ReentrantReadWriteLock();
		activeLock = new ReentrantReadWriteLock();
		root = new TrieNode(maxElementsByNode, ' ', null);
		nodes = new Vector<TrieNode>(maxNodes);
		active = true;
	}

	public boolean isActive(){
		activeLock.readLock().lock();
		boolean ans = active;
		activeLock.readLock().unlock();
		return ans;
	}


	public void setActive(boolean active){
		activeLock.writeLock().lock();
		this.active = active;
		activeLock.writeLock().unlock();
	}

	public boolean add(FileDescription fileDescription, int score){
		boolean ans = true;
		String text = fileDescription.getFileName();
		text = characterTranslator.newString(text);

		activeLock.readLock().lock();
		if(!active){
			activeLock.readLock().unlock();
			ans = false;
		}
		else{
			lock.readLock().lock();
			activeLock.readLock().unlock();
			for(int i=0; i<text.length(); i++){
				TrieNode node = root;
				for(int j=0; j<maxSubstringSize && i+j < text.length(); j++){
					TrieNode child = node.getChild(text.charAt(i+j));
					if(child == null){
						node.addChild(text.charAt(i+j));
						node = node.getChild(text.charAt(i+j));

						vectorLock.writeLock().lock();
						if(nodes.size() < maxNodes)
						nodes.add(node);
						else
						ans = false;
						vectorLock.writeLock().unlock();
					}
					else
					node = child;

					if(node != null)
					node.addFileDescription(fileDescription, score);
				}
			}
			lock.readLock().unlock();
		}
		return ans;
	}

	public Vector<FileDescription> search(String pattern){
		pattern = characterTranslator.newString(pattern);
		Vector<FileDescription> ans = new Vector<FileDescription>();

		activeLock.readLock().lock();
		if(!active)
		activeLock.readLock().unlock();
		else{
			lock.readLock().lock();
			activeLock.readLock().unlock();
			TrieNode node = root;
			int i;
			for(i=0; i<pattern.length(); i++){
				node = node.getChild(pattern.charAt(i));
				if(node == null)
				break;
			}
			if(i == pattern.length() && node != null)
			ans = node.getFileDescriptionVector();
			lock.readLock().unlock();
		}
		return ans;
	}

	public boolean clearEmptyNodes(){
		activeLock.readLock().lock();
		if(active){
			activeLock.readLock().unlock();
			return false;
		}
		else{
			lock.writeLock().lock();
			activeLock.readLock().unlock();

			// eliminar el enlace entre nodos
			for(int i=0; i<nodes.size(); i++){
				if(nodes.get(i).numberOfFileDescriptions() == 0){
					TrieNode p = nodes.get(i).getAncestor();
					char c = nodes.get(i).getChar();
					if(p != null)
					p.removeChild(c);
				}
			}
			// reorganiza el vector de nodos
			for(int i=nodes.size()-1; i>=0; i--){
				if(nodes.get(i).numberOfFileDescriptions() == 0){
					nodes.set(i, nodes.get(nodes.size()-1));
					nodes.remove(nodes.size()-1);
				}
			}
			if(nodes.size() == 0){
				root = new TrieNode(maxElementsByNode, ' ', null);
				nodes.add(root);
			}

			lock.writeLock().unlock();
		}
		return true;
	}

	public boolean clearAllNodes(){
		activeLock.readLock().lock();
		if(active){
			activeLock.readLock().unlock();
			return false;
		}
		else{
			lock.writeLock().lock();
			activeLock.readLock().unlock();
			root = new TrieNode(maxElementsByNode, ' ', null);
			nodes = new Vector<TrieNode>(maxNodes);
			nodes.add(root);
			lock.writeLock().unlock();
		}
		return true;
	}
}