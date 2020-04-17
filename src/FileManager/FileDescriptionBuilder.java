package FileManager;

import java.util.Vector;

public class FileDescriptionBuilder{

	public static FileDescription createMP3FileDescription(String fileName, String fileSize, String title, String artist, String album){
		Vector<Tag> tags = new Vector<Tag>(4);
		tags.add(new Tag("fileSize", fileSize));
		tags.add(new Tag("title", title));
		tags.add(new Tag("artist", artist));
		tags.add(new Tag("album", album));

		String newFileName;
		if(title == null && artist == null && album == null)
		newFileName = fileName;
		else{
			newFileName = "";
			if(title != null && title.length() > 0)
			newFileName += title;
			if(artist != null && artist.length() > 0){
				if(newFileName.length() > 0)
				newFileName += "-";
				newFileName += artist;
			}
			if(album != null && album.length() > 0){
				if(newFileName.length() > 0)
				newFileName += "-";
				newFileName += album;
			}
		}
		return new FileDescription("mp3", newFileName, tags);
	}
}