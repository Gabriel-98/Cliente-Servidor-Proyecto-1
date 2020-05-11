package utilities;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Comparator;

public class InetSocketAddressComparator implements Comparator<InetSocketAddress>{

	public long InetAddressToLong(InetAddress address){
		String ip = address.getHostAddress();
		long ans = 0, x = 0;
		for(int i=0; i<ip.length(); i++){
			if(ip.charAt(i) == '.'){
				ans = (ans * 256) + x;
				x = 0;
			}
			else
			x = (x * 10) + ip.charAt(i) - '0';
		}
		ans = (ans * 256) + x;
		return ans;
	}

	@Override
	public int compare(InetSocketAddress address1, InetSocketAddress address2){
		long codeIp1, codeIp2;
		codeIp1 = InetAddressToLong(address1.getAddress());
		codeIp2 = InetAddressToLong(address2.getAddress());
		if(codeIp1 < codeIp2)
		return -1;
		if(codeIp1 > codeIp2)
		return 1;		
		if(address1.getPort() < address2.getPort())
		return -1;
		if(address1.getPort() > address2.getPort())
		return 1;
		return 0;
	}
}