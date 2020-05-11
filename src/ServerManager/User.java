package ServerManager;

import java.net.InetSocketAddress;
import java.util.Vector;

public class User implements Comparable<User>{
	private String id, name, password;
	private InetSocketAddress address, listenerAddress;
	private boolean active;

	public User(String id, String name, String password, InetSocketAddress address){
		this.id = id;
		this.name = name;
		this.password = password;
		this.address = address;
		listenerAddress = null;
		active = true;
	}

	public String getId(){ return id; }
	public String getName(){ return name; }
	public String getPassword(){ return password; }
	public InetSocketAddress getAddress(){ return address; }
	public InetSocketAddress getListenerAddress(){ return listenerAddress; }
	public boolean isActive(){ return active; }

	public void setName(String name){ this.name = name; }
	public void setAddress(InetSocketAddress address){ this.address = address; }
	public void setListenerAddress(InetSocketAddress listenerAddress){ this.listenerAddress = listenerAddress; }
	public void setActive(boolean active){ this.active = active; }

	@Override
	public int compareTo(User user){ return id.compareTo(user.getId()); }
}
