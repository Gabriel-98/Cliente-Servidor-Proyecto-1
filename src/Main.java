import java.io.File;
import java.util.Vector;

import FileManager.MP3File;
import FileManager.Directory;

public class Main{
	
	public static void main(String[] args){
		Directory directory = new Directory(new File("publico"));
		Vector<MP3File> mp3Files = directory.getMP3Files();

		for(int i=0; i<mp3Files.size(); i++){
			System.out.println("file " + i + ": ");
			System.out.println("file name: " + mp3Files.get(i).getFileName());
			System.out.println("path: " + mp3Files.get(i).getPath());
			System.out.println("song: " + mp3Files.get(i).getSong());
			System.out.println("artist: " + mp3Files.get(i).getArtist());
			System.out.println("album: " + mp3Files.get(i).getAlbum());
			System.out.println("size: " + mp3Files.get(i).getSize());
			System.out.println();
		}
	}
}