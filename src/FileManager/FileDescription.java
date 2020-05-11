package FileManager;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Vector;


public class FileDescription implements Comparable<FileDescription>{
	
	private String type;
	private String fileName;
	private long fileSize;
	private Vector<Tag> tags;
	private TreeMap<String,String> tagTree;

	public FileDescription(String type, String fileName, long fileSize, Vector<Tag> tags){
		if(type == null)
		this.type = "";
		else
		this.type = type;

		if(fileName == null)
		this.fileName = "";
		else
		this.fileName = fileName;

		this.fileSize = fileSize;
	
		tagTree = new TreeMap<String,String>();
		for(int i=0; i<tags.size(); i++){
			if(tags.get(i) != null)
			tagTree.put(tags.get(i).getName(), tags.get(i).getValue());
		}
		this.tags = new Vector<Tag>(tagTree.size());

		if(!tagTree.isEmpty()){
			Entry<String,String> entry = tagTree.firstEntry();
			while(entry != null){
				this.tags.add(new Tag(entry.getKey(), entry.getValue()));
				entry = tagTree.higherEntry(entry.getKey());
			}
		}
	}

	public String getType(){ return type; }
	public String getFileName(){ return fileName; }
	public long getFileSize(){ return fileSize; }
	public int numberOfTags(){ return tags.size(); }
	public Tag getTag(int index){ return tags.get(index); }
	public String getValue(String tagName){ return tagTree.get(tagName); }	

	public String convertToString(){
		if(type.length() >= 256 || fileName.length() >= 256 || tags.size() >= 256)
		return null;
		int size = 3 + type.length() + fileName.length() + 8 + 2 * tags.size();
		for(int i=0; i<tags.size(); i++){
			if(tags.get(i).getName().length() >= 256 ||  tags.get(i).getValue().length() >= 256)
			return null;
			size += tags.get(i).getName().length() + tags.get(i).getValue().length();
		}

		char[] array = new char[size];
		int pos = 0;

		// type
		array[pos] = (char)(type.length());
		pos++;
		for(int i=0; i<type.length(); i++,pos++)
		array[pos] = type.charAt(i);

		// fileName
		array[pos] = (char)(fileName.length());
		pos++;
		for(int i=0; i<fileName.length(); i++,pos++)
		array[pos] = fileName.charAt(i);

		// fileSize
		long temp = fileSize;
		for(int i=7; i>=0; i--){
			array[pos + i] = (char)(temp % 256);
			temp /= 256;
		}
		pos += 8;

		// tags
		array[pos] = (char)(tags.size());
		pos++;
		for(int i=0; i<tags.size(); i++){
			array[pos] = (char)(tags.get(i).getName().length());
			pos++;
			for(int j=0; j<tags.get(i).getName().length(); j++,pos++)
			array[pos] = tags.get(i).getName().charAt(j);

			array[pos] = (char)(tags.get(i).getValue().length());
			pos++;
			for(int j=0; j<tags.get(i).getValue().length(); j++,pos++)
			array[pos] = tags.get(i).getValue().charAt(j);
		}
		return new String(array);
	}

	public int compareTo(FileDescription fileDescription){
		if(fileDescription == null)
		return -1;
		if(!(type.equals(fileDescription.getType())))
		return type.compareTo(fileDescription.getType());
		if(!(fileName.equals(fileDescription.getFileName())))
		return fileName.compareTo(fileDescription.getFileName());
		if(fileSize != fileDescription.getFileSize()){
			if(fileSize < fileDescription.getFileSize())
			return -1;
			return 1;
		}
	
		for(int i=0; i<numberOfTags() && i<fileDescription.numberOfTags(); i++){
			if(!(tags.get(i).equals(fileDescription.getTag(i))));
			return tags.get(i).compareTo(fileDescription.getTag(i));
		}
		if(numberOfTags() == fileDescription.numberOfTags())
		return 0;
		if(numberOfTags() < fileDescription.numberOfTags())
		return -1;
		return 1;
	}

	public boolean equals(FileDescription fileDescription){
		return (this.compareTo(fileDescription) == 0);
	}
}