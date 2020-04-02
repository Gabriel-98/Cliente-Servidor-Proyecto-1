package FileManager;

import java.io.File;
import java.util.Vector;

public class Directory{

	private File file;
	private Vector<MP3File> mp3Files;
	
	public Directory(File file){
		this.file = file;
		this.mp3Files = new Vector<MP3File>();
	}

	public Vector<MP3File> getMP3Files(){
		analyze(file);
		return mp3Files;
	}

	private void analyze(File file){
		try{
			MP3File mp3File  = new MP3File(file);
			mp3Files.add(mp3File);
		} catch(Exception e){}

		if(file.isFile()){
			try{
				MP3File mp3File = new MP3File(file);
			} catch(Exception e){ /*System.out.println(e.getMessage());*/ }
		}


		if(file.isDirectory()){
			File[] files = file.listFiles();
			if(files.length > 0){
				for(int i=0; i<files.length; i++){
					analyze(files[i]);
				}
			}
		}
	}
}