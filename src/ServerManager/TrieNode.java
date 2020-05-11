package ServerManager;

import FileManager.FileDescription;
import utilities.Pair;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Vector;
import java.util.TreeSet;
import java.util.TreeMap;

public class TrieNode{

	private int maxElements;
	private TrieNode ancestor;
	private char nodeChar;
	private TreeMap<Character,TrieNode> childs;
	private TreeMap<FileDescription,Integer> fileDescriptionTree;
	private Vector<Pair<FileDescription,Integer>> fileDescriptionVector;
	private ReentrantReadWriteLock lock;

	
	public TrieNode(int maxElements, char nodeChar, TrieNode ancestor){
		this.maxElements = maxElements;
		this.nodeChar = nodeChar;
		this.ancestor = ancestor;
		childs = new TreeMap<Character,TrieNode>();
		fileDescriptionTree = new TreeMap<FileDescription,Integer>();
		fileDescriptionVector = new Vector<Pair<FileDescription,Integer>>();
		lock = new ReentrantReadWriteLock();
	}

	public void addChild(char c){
		lock.writeLock().lock();
		TrieNode node = childs.get(c);
		if(node == null)
		childs.put(c, new TrieNode(maxElements, c, this));
		lock.writeLock().unlock();
	}

	public void removeChild(char c){
		lock.writeLock().lock();
		childs.remove(c);
		lock.writeLock().unlock();
	}

	public char getChar(){
		lock.readLock().lock();
		char ans = nodeChar;
		lock.readLock().unlock();
		return ans;
	}

	public TrieNode getAncestor(){
		lock.readLock().lock();
		TrieNode ans = ancestor;
		lock.readLock().unlock();
		return ans;
	}

	public TrieNode getChild(char c){
		lock.readLock().lock();
		TrieNode ans = childs.get(c);
		lock.readLock().unlock();
		return ans;
	}

	public void addFileDescription(FileDescription fileDescription, int score){
		lock.writeLock().lock();
		if((fileDescriptionTree.get(fileDescription) == null) && (score >= 1)){
			if(fileDescriptionVector.size() < maxElements){
				fileDescriptionVector.add(new Pair<FileDescription,Integer>(fileDescription, score));
				fileDescriptionTree.put(fileDescription, fileDescriptionVector.size()-1);
			}
			else{
				if(maxElements >= 1){
					int pos = 0;
					for(int i=1; i<fileDescriptionVector.size(); i++){
						if(fileDescriptionVector.get(i).getSecond() < fileDescriptionVector.get(pos).getSecond())
						pos = i;
					}

					fileDescriptionTree.remove(fileDescriptionVector.get(pos).getFirst());
					fileDescriptionTree.put(fileDescription, pos);
					fileDescriptionVector.set(pos, new Pair<FileDescription,Integer>(fileDescription,score));
				}
			}
		}
		else if((fileDescriptionTree.get(fileDescription) != null) && (score == 0)){
			Integer pos = fileDescriptionTree.get(fileDescription);
			fileDescriptionVector.set(pos, fileDescriptionVector.get(fileDescriptionVector.size()-1));
			fileDescriptionVector.remove(fileDescriptionVector.size()-1);
			fileDescriptionTree.remove(fileDescription);
			if(pos < fileDescriptionVector.size() && fileDescriptionVector.size() >= 1)
			fileDescriptionTree.put(fileDescriptionVector.get(pos).getFirst(), pos);		
		}
		
		lock.writeLock().unlock();
	}

	public Vector<FileDescription> getFileDescriptionVector(){
		lock.readLock().lock();

		Vector<FileDescription> ans = new Vector<FileDescription>(fileDescriptionVector.size());
		for(int i=0; i<fileDescriptionVector.size(); i++)
		ans.add(fileDescriptionVector.get(i).getFirst());

		lock.readLock().unlock();
		return ans;
	}

	public int numberOfFileDescriptions(){
		lock.readLock().lock();
		int ans = fileDescriptionVector.size();
		lock.readLock().unlock();
		return ans;
	}
}