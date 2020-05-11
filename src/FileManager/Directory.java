package FileManager;

import utilities.Pair;

import java.io.File;
import java.nio.file.Files;
import java.util.Vector;

public class Directory{

	private File file;
	private Vector<Pair<FileDescription,File>> files;
	
	public Directory(File file){
		this.file = file;
		files = new Vector<Pair<FileDescription,File>>();
	}

	public Vector<Pair<FileDescription,File>> getFiles(){
		analyze(file);
		return files;
	}

	private void analyze(File file){
		if(file != null){
			if(file.isDirectory()){
				File[] files = file.listFiles();
				for(int i=0; i<files.length; i++)
				analyze(files[i]);				
			}
			else{
				try{
					String fileType = Files.probeContentType(file.toPath());
					FileDescription fd = null;
					if(fileType.equals("audio/mpeg")){
						MP3File mp3File  = new MP3File(file);
						fd = FileDescriptionBuilder.createMP3FileDescription(mp3File);
					}
					else if(fileType.equals("video/mp4"))
					fd = FileDescriptionBuilder.createMP4FileDescription(file.getName(), file.length());
					
					if(fd != null)
					files.add(new Pair<FileDescription,File>(fd, file));
				}
				catch(Exception e){}
			}
		}
	}
}