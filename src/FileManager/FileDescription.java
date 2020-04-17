package FileManager;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Vector;


public class FileDescription implements Comparable<FileDescription>{
	
	private String type;
	private String fileName;
	private Vector<Tag> tags;
	private TreeMap<String,String> tagTree;

	public FileDescription(String type, String fileName, Vector<Tag> tags){
		if(type == null)
		this.type = "";
		else
		this.type = type;

		if(fileName == null)
		this.fileName = "";
		else
		this.fileName = fileName;
	
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
	public int numberOfTags(){ return tags.size(); }
	public Tag getTag(int index){ return tags.get(index); }
	public String getValue(String tagName){ return tagTree.get(tagName); }	

	public int compareTo(FileDescription fileDescription){
		if(fileDescription == null)
		return -1;
		if(!(type.equals(fileDescription.getType())))
		return type.compareTo(fileDescription.getType());
		if(!(fileName.equals(fileDescription.getFileName())))
		return fileName.compareTo(fileDescription.getFileName());
		else{
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
	}

	public boolean equals(FileDescription fileDescription){
		return (this.compareTo(fileDescription) == 0);
	}
}