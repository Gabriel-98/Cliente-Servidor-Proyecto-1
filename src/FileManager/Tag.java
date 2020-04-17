package FileManager;

public class Tag implements Comparable<Tag>{

	private String name;
	private String value;
	
	public Tag(String name, String value){
		if(name == null)
		this.name = "";
		else
		this.name = name;

		if(value == null)
		this.value = "";
		else
		this.value = value;
	}

	public String getName(){ return name; }
	public String getValue(){ return value; }

	@Override
	public int compareTo(Tag tag){
		if(tag == null)
		return -1;
		if(!(name.equals(tag.getName())))
		return name.compareTo(tag.getName());
		if(!(value.equals(tag.getValue())))
		return value.compareTo(tag.getValue());
		return 0;
	}

	public boolean equals(Tag tag){
		return (this.compareTo(tag) == 0);
	}
}