package org.solmix.hola.rs.generic.two;

import org.solmix.hola.transport.netty.ThreadLocalChannel;

import io.netty.channel.Channel;

public class DefaultStationService implements StationService {

	Channel channel;
	@Override
	public String login(String token) {
		channel  = ThreadLocalChannel.get();
		return "aa";
	}
	

	public Channel getChannel() {
		return channel;
	}


	@Override
	public void logout(String token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportData(String data) {
		// TODO Auto-generated method stub

	}

}
