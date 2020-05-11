package FileManager;

public class FileFragmentWriter implements Runnable{
	private String fragment;
	private long start;
	private FileWriter fw;

	public FileFragmentWriter(FileWriter fw, String fragment, long start){
		this.fw = fw;		
		this.fragment = fragment;
		this.start = start;
	}

	@Override
	public void run(){
		boolean ans = fw.writeString(fragment, start);
		if(!ans)
		fw.cancel(start);
	}
}
