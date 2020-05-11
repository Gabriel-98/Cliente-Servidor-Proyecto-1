package FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

public class FileReader{
	
	private File file;
	private long length;
	private String path, fileName, fileType;

	public FileReader(File file) throws Exception{
		this.file = file;
		try{
			FileInputStream fis = new FileInputStream(file);
			path = file.getAbsolutePath();
			fileName = file.getName();
			fileType = Files.probeContentType(file.toPath());
			length = file.length();	
		} catch(Exception e){ throw new Exception("Error al leer la informacion del archivo"); }
	}

	public long size(){ return length; }

	public byte[] read(long l, long r){
		if(l > r || l < 0 || r >= length)
		return null;
		try{
			FileInputStream fis = new FileInputStream(file);

			fis.skip(l);
			byte[] array = new byte[(int)(r+1-l)];
			fis.read(array);
			return array;
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			return null;
		}
	}

	public String readString(long l, long r){
		byte[] bytes = read(l,r);
		if(bytes == null)
		return null;
		char[] array = new char[bytes.length];
		for(int i=0; i<array.length; i++){
			int c = bytes[i];
			if(c < 0)
			c += 256;
			array[i] = (char)c;
		}
		return new String(array);
	}
}