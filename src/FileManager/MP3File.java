package FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.TreeMap;
import java.lang.Math;

public class MP3File{
	private final int bufferSize = 10000;
	
	private int tagSize;
	private TreeMap<String,String> tags;
	private File file;
	private String path, fileName;
	private String song, artist, album;
	private long size;

	public MP3File(File file) throws Exception{
		this.file = file;

		String fileType;
		try{ fileType = Files.probeContentType(file.toPath()); }
		catch(Exception e){ throw new Exception("Error al leer el tipo de archivo"); }
		
		if(!fileType.equals("audio/mpeg"))
		throw new Exception("Error no es un archivo de tipo audio/mpeg");		 

		String songTag, artistTag, albumTag;
		songTag = "TIT2";
		artistTag = "TPE2";
		albumTag = "TALB";

		tags = new TreeMap<String,String>();
		tags.put(songTag, null);
		tags.put(artistTag, null);
		tags.put(albumTag, null);
		byte[] array = new byte[10];
		FileInputStream fis;
		
		try{
			path = file.getAbsolutePath();
			fileName = file.getName();
		} catch(Exception e){ throw new Exception("Error al leer ruta del archivo"); }

		try{ fis = new FileInputStream(file); }
		catch(Exception e){ throw new Exception("Error al abrir el archivo"); }

		size = fis.available();
		if(size < 10)
		throw new Exception("Archivo incompleto");
		else{
			try{ fis.read(array); }
			catch(Exception e){ throw new Exception("Error al leer el archivo"); }
		
			String tagIdentifier="";
			tagSize = 0;
			
			for(int i=0; i<=2; i++){
				if(array[i] >= 0)
				tagIdentifier += (char)array[i];
				else
				tagIdentifier += (char)(array[i] + 256);
			}
			for(int i=6; i<=9; i++){
				int c = array[i];
				if(c < 0)
				c += 256;
				if(c >= 128)
				c -= 128;
			
				tagSize = (tagSize * 128) + c;
			}

			if(!tagIdentifier.equals("ID3"))
			throw new Exception("El identificador del TAG es diferente de ID3");
		}
		boolean ans = readFrames();
		if(!ans)
		throw new Exception("Error al leer el archivo");

		song = tags.get(songTag);
		artist = tags.get(artistTag);
		album = tags.get(albumTag);
	}

	private boolean readFrames(){
		String frameIdentifier = "";
		int frameSize=0;
		String description;
		char[] descriptionArray = new char[1];
		byte[] buffer = new byte[bufferSize];

		try{
			FileInputStream fis = new FileInputStream(file);
			fis.skip(10);
		
			for(int i=10, idItem=0, posBuffer=0, cont=0; i<tagSize+10 && i<size; i++){
				// Lee los siguientes bytes del archivo
				if(posBuffer == 0){
					if(i+bufferSize <= Math.min(tagSize+10, size))
					buffer = new byte[bufferSize];
					else{
						if(tagSize+10 < size)
						buffer = new byte[tagSize+10-i];
						else
						buffer = new byte[(int)(size-i)];
					}
					if(buffer.length == 0)
					break;

					fis.read(buffer);
				}

				// Verifica si es el inicio de un nuevo frame y que este no inicie con 0
				if(idItem == 0 && frameIdentifier.length() == 0 && buffer[posBuffer] == 0)
				break;

				// Siguiente byte
				int c = buffer[posBuffer];
				if(c < 0)
				c += 256;


				if(idItem == 0){	// identificador del frame
					frameIdentifier += (char)c;
					if(frameIdentifier.length() == 4){
						idItem = 1;
						frameSize = 0;
					}
				}
				else if(idItem == 1){	// tamaño de la descripcion del frame
					frameSize = (frameSize*256) + c;
					cont++;
					if(cont == 4){
						idItem = 2;
						cont = 0;
						descriptionArray = new char[frameSize];
					}
				}
				else if(idItem == 2){		// flags
					cont++;
					if(cont == 2){
						idItem = 3;
						cont = 0;
						if(frameSize == 0){
							frameIdentifier = "";
							idItem = 0;
						}
					}
				}
				else if(idItem == 3){		// descripcion
					descriptionArray[cont] = (char)c;

					cont++;
					if(cont == frameSize){
						idItem = 0;
						cont = 0;
						description = new String(descriptionArray);
						if(tags.containsKey(frameIdentifier))
						tags.put(frameIdentifier, description);

						frameIdentifier = "";
					}
				}
				posBuffer = (posBuffer + 1) % bufferSize;
			}
			return true;
		}
		catch(Exception e){ return false; }
	}

	public String getFileName(){ return fileName; }
	public String getPath(){ return path; }
	public String getSong(){ return song; }
	public String getArtist(){ return artist; }
	public String getAlbum(){ return album; }
	public long getSize(){ return size; }

	public byte[] read(int l, int r){
		if(l > r || l < 0 || r >= size)
		return null;
		try{
			FileInputStream fis = new FileInputStream(file);
			fis.skip(l);

			byte[] array = new byte[r+1-l];
			fis.read(array);

			return array;
		}
		catch(Exception e){ return null; }
	}
}