package FileManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.TreeSet;

public class FileWriter{
	private File file;
	private long fileSize, currentSize, pointer;		// tamaño que va tener el arhivo, numero de bytes escritos sin vacios, primer byte no seleccionado
	private int blockSize;
	private TreeSet<Long> assigned, pending;		// bloques que se asignaron, y bloques que estan pending (estuvieron asignados pero fueron cancelados)
	private ExecutorService threadPool;
	private ReentrantReadWriteLock lock;

	public FileWriter(File file, long fileSize, long currentSize, int blockSize){
		this.file = file;
		this.fileSize = fileSize;
		this.currentSize = currentSize;
		this.blockSize = blockSize;
		assigned = new TreeSet<Long>();
		pending = new TreeSet<Long>();
		pointer = currentSize;
		threadPool = Executors.newFixedThreadPool(4);

		lock = new ReentrantReadWriteLock();
	}

	public long size(){ return fileSize; }

	public boolean isFinished(){
		lock.readLock().lock();
		boolean ans = currentSize >= fileSize;
		lock.readLock().unlock();
		return ans;
	}

	public long next(){
		long ans = -1;
		lock.writeLock().lock();

		if(pending.isEmpty()){
			if(pointer < fileSize){
				ans = pointer;
				pointer += blockSize;
			}
		}
		else{
			ans = pending.first();
			pending.remove(ans);
		}
		if(ans != -1)
		assigned.add(ans);

		lock.writeLock().unlock();
		return ans;
	}

	public void cancel(long pos){
		lock.writeLock().lock();
		if(assigned.contains(pos)){
			assigned.remove(pos);
			pending.add(pos);
		}
		lock.writeLock().unlock();
	}

	public boolean write(byte[] array, long inicio){
		boolean ans = true;
		lock.writeLock().lock();
		if((inicio < 0) || (inicio >= fileSize) || ((inicio % blockSize) != 0))
		ans = false;
		if(!assigned.contains(inicio))
		ans = false;
		if(array.length != Math.min(fileSize - inicio, blockSize))
		ans = false;
		lock.writeLock().unlock();

		if(ans){
			try{
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.seek(inicio);
				raf.write(array);

				lock.writeLock().lock();
				assigned.remove(inicio);
				if(pending.isEmpty() && assigned.isEmpty())
				currentSize = pointer;
				else if(pending.isEmpty())
				currentSize = assigned.first();
				else if(assigned.isEmpty())
				currentSize = pending.first();
				else
				currentSize = Math.min(pending.first(), assigned.first());

				lock.writeLock().unlock();
			}
			catch(Exception e){ ans = false; }
		}

		return ans;
	}

	public boolean writeString(String fragment, long inicio){
		byte[] array = new byte[fragment.length()];
		for(int i=0; i<fragment.length(); i++){
			int c = fragment.charAt(i);
			if(c >= 128)
			c -= 256;
			array[i] = (byte)c;
		}
		//threadPool.execute(new FileFragmentWriter(this, array, inicio));
		//return true;
		return write(array, inicio);
	}

	public boolean writeStringConcurrent(String fragment, long inicio){
		threadPool.execute(new FileFragmentWriter(this, fragment, inicio));
		return true;
	}

}
