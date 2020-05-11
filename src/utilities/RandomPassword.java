package utilities;

import java.util.Random;

public class RandomPassword{

	public static String generatePassword(int size, boolean numbers, boolean lowerCase, boolean upperCase){
		Random random = new Random();
		char[] alp = new char[256];

		int numberOfCharacters = 0;
		if(numbers){
			for(int i=0; i<10; i++,numberOfCharacters++)
			alp[numberOfCharacters] = (char)('0' + i);
		}
		if(lowerCase){
			for(int i=0; i<26; i++,numberOfCharacters++)
			alp[numberOfCharacters] = (char)('a' + i);
		}
		if(upperCase){
			for(int i=0; i<26; i++,numberOfCharacters++)
			alp[numberOfCharacters] = (char)('A' + i);
		}
		
		char[] passwordArray = new char[size];
		for(int i=0; i<size; i++)
		passwordArray[i] = alp[random.nextInt(numberOfCharacters)];
		
		return new String(passwordArray);
	}
}
