package utilities;

import java.util.Enumeration;
import java.util.Vector;
import java.text.Normalizer;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class Utilities{
	
	public static Vector<String> getWords(String command){
		Vector<String> words = new Vector<String>();
		String word = "";
		for(int i=0; i<command.length(); i++){
			if(command.charAt(i) == ' '){
				if(word.length() > 0)
				words.add(word);
				word = "";
			}
			else
			word += command.charAt(i);
		}
		if(word.length() > 0)
		words.add(word);
		return words;
	}

	public static String getString(Vector<String> words){
		return getString(words, 0, words.size()-1);
	}

	public static String getString(Vector<String> words, int start, int fin){
		int size = 0, pos = 0;
		for(int i=start; i<=fin; i++){
			if(i > start)
			size++;
			size += words.get(i).length();
		}

		char[] wordsArray = new char[size];
		for(int i=start; i<=fin; i++){
			if(i > start){
				wordsArray[pos] = ' ';
				pos++;
			}
			for(int j=0; j<words.get(i).length(); j++,pos++)
			wordsArray[pos] = words.get(i).charAt(j);
		}
		return new String(wordsArray);
	}

	public static String unicodeToAscii(String command){
		String command2 = Normalizer.normalize(command, Normalizer.Form.NFKD);
		String ans = "";
		for(int i=1; i<command2.length(); i+=2) ans += command2.charAt(i);
		return ans;
	}

	public static String getPrivateIp(){
		String ipBase = "192.168.1";
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()){
				Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
				while(addresses.hasMoreElements()){
					String ip = addresses.nextElement().getHostAddress();
					int i;
					for(i=0; i<ipBase.length() && i<ip.length(); i++){
						if(ip.charAt(i) != ipBase.charAt(i))
						break;
					}
					if(i == ipBase.length())
					return ip;
				}
			}
		}
		catch(Exception e){}
		return null;	
	}
}