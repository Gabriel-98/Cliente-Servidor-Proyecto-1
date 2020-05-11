package ServerManager;

import java.net.InetSocketAddress;

public class IntermediaryConnectionDescription{
	private InetSocketAddress intermediaryAddress, nodeAddress;
	private String connectionId, connectionPasswod;

	public IntermediaryConnectionDescription(InetSocketAddress intermediaryAddress, InetSocketAddress nodeAddress, String connectionId, String connectionPassword){
		this.intermediaryAddress = intermediaryAddress;
		this.nodeAddress = nodeAddress;
		this.connectionId = connectionId;
		this.connectionPasswod = connectionPassword;
	}

	public InetSocketAddress getIntermediaryAddress(){ return intermediaryAddress; }
	public InetSocketAddress getNodeAddress(){ return nodeAddress; }
	public String getConnectionId(){ return connectionId; }
	public String getConnectionPassword(){ return connectionPasswod; }
}