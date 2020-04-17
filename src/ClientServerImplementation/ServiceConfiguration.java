package ClientServerImplementation;

import java.util.Vector;

import utilities.Utilities;

public class ServiceConfiguration{
	private String serviceName;
	private boolean printDescriptionEnabled, printParametersEnabled;

	public ServiceConfiguration(String serviceName, boolean printDescriptionEnabled, boolean printParametersEnabled){
		this.serviceName = serviceName;
		this.printDescriptionEnabled = printDescriptionEnabled;
		this.printParametersEnabled = printParametersEnabled;
	}

	public String getServiceName(){ return serviceName; }

	public String getVisibleMessage(Vector<String> command){
		String ans = "";
		if(command.size() >= 1)
		ans += command.get(0);
		if(command.size() >= 2 && printDescriptionEnabled)
		ans += " " + command.get(1);
		if(command.size() >= 3 && printParametersEnabled)
		ans += " " + Utilities.getString(command, 2, command.size()-1);
		return ans;		
	}
}