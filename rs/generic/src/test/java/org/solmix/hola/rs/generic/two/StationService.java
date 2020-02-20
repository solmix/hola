package org.solmix.hola.rs.generic.two;

public interface StationService{

	
	String login(String token);
	void logout(String token);
	void reportData(String data);
}
