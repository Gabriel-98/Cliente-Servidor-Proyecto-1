package FileManager;

import java.util.Vector;

public class FileDescriptionBuilder{

	public static FileDescription createFileDescription(String fileDescriptionString){
		String type = "", fileName = "";
		long fileSize = 0;
		int pos = 0;

		// type
		if(fileDescriptionString.length() == 0 || (fileDescriptionString.length() <= (pos + fileDescriptionString.charAt(pos))))
		return null;
		type = fileDescriptionString.substring(pos+1, pos + 1 + fileDescriptionString.charAt(pos));
		pos += fileDescriptionString.charAt(pos) + 1;

		// fileName
		if(fileDescriptionString.length() <= (pos + fileDescriptionString.charAt(pos)))
		return null;
		fileName = fileDescriptionString.substring(pos+1, pos + 1 + fileDescriptionString.charAt(pos));
		pos += fileDescriptionString.charAt(pos) + 1;

		// fileSize
		if(fileDescriptionString.length() <= (pos + 7))
		return null;
		for(int i=0; i<8; i++,pos++){
			fileSize *= 256;
			fileSize += fileDescriptionString.charAt(pos);
		}

		// tags
		if(fileDescriptionString.length() <= pos)
		return null;
		int numberOfTags = fileDescriptionString.charAt(pos);
		pos++;

		Vector<Tag> tags = new Vector<Tag>(numberOfTags);
		for(int i=0; i<numberOfTags; i++){
			String tagName, tagValue;

			if(fileDescriptionString.length() <= (pos + fileDescriptionString.charAt(pos)))
			return null;
			tagName = fileDescriptionString.substring(pos+1, pos + 1 + fileDescriptionString.charAt(pos));
			pos += fileDescriptionString.charAt(pos) + 1;

			if(fileDescriptionString.length() <= (pos + fileDescriptionString.charAt(pos)))
			return null;
			tagValue = fileDescriptionString.substring(pos+1, pos + 1 + fileDescriptionString.charAt(pos));
			pos += fileDescriptionString.charAt(pos) + 1;

			tags.add(new Tag(tagName, tagValue));
		}

		if(pos != fileDescriptionString.length())
		return null;
		return new FileDescription(type, fileName, fileSize, tags);
	}

	public static FileDescription createMP3FileDescription(String fileName, long fileSize, String title, String artist, String album){
		Vector<Tag> tags = new Vector<Tag>(3);
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
			newFileName += ".mp3";
		}
		return new FileDescription("mp3", newFileName, fileSize, tags);
	}

	public static FileDescription createMP3FileDescription(MP3File mp3File){
		if(mp3File == null)
		return null;
		return createMP3FileDescription(mp3File.getFileName(), mp3File.getSize(), mp3File.getTitle(), mp3File.getArtist(), mp3File.getAlbum());
	}

	public static FileDescription createMP4FileDescription(String fileName, long fileSize){
		return new FileDescription("mp4", fileName, fileSize, new Vector<Tag>());
	}
}
