package ServerManager;

public class CharacterTranslator{
	
	public CharacterTranslator(){}

	public static char newChar(char c){
		if(c == 160) return 'A';
		if(c == 161) return 'I';
		if(c == 162) return 'O';
		if(c == 163) return 'U';
		if(c == 164 || c == 165) return (char)165;
		if(c <= 31 || c >= 127)
		return 32;
		if('a' <= c && c <= 'z')
		return c += 'A' - 'a';
		return c;
	}

	public static String newString(String s){
		char[] charArray = new char[s.length()];
		for(int i=0; i<s.length(); i++)
		charArray[i] = newChar(s.charAt(i));
		return new String(charArray);
	}
}
