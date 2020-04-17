package ClientServerImplementation;

import java.util.Vector;

public abstract class Service{
	private String serviceName;

	public Service(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName(){ return serviceName; }

	public abstract Vector<String> solver(ClientInformation clientInformation, Vector<String> command);
}