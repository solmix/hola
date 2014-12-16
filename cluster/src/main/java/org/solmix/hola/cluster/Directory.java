package org.solmix.hola.cluster;

import java.util.List;

import org.solmix.hola.common.identity.Identifiable;
import org.solmix.hola.rs.RemoteRequest;
import org.solmix.hola.rs.RemoteService;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年9月21日
 */
public interface Directory extends Identifiable{

	
	String[] getInterfaces();
	
	
	List<RemoteService> list(RemoteRequest request);
}
