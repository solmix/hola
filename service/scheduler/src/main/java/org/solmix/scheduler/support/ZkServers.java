package org.solmix.scheduler.support;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.test.TestingServer;
import org.solmix.scheduler.exception.RegistryExceptionHandler;

public class ZkServers {
	private static ZkServers instance = new ZkServers();

	private static ConcurrentMap<Integer, TestingServer> nestedServers = new ConcurrentHashMap<Integer, TestingServer>();

	/**
	 * 获取单例实例.
	 * 
	 * @return 单例实例
	 */
	public static ZkServers getInstance() {
		return instance;
	}

	/**
	 * 启动内嵌的Zookeeper服务.
	 * 
	 * @param port
	 *            端口号
	 * 
	 *            <p>
	 *            如果该端口号的Zookeeper服务未启动, 则启动服务. 如果该端口号的Zookeeper服务已启动, 则不做任何操作.
	 *            </p>
	 */
	public synchronized void startServerIfNotStarted(final int port,
			final String dataDir) {
		if (!nestedServers.containsKey(port)) {
			TestingServer testingServer = null;
			try {
				testingServer = new TestingServer(port, new File(dataDir));
			} catch (final Exception ex) {
				RegistryExceptionHandler.handleException(ex);
			}
			nestedServers.putIfAbsent(port, testingServer);
		}
	}

	/**
	 * 关闭内嵌的Zookeeper服务.
	 * 
	 * @param port
	 *            端口号
	 */
	public void closeServer(final int port) {
		TestingServer nestedServer = nestedServers.get(port);
		if (null == nestedServer) {
			return;
		}
		try {
			nestedServer.close();
			nestedServers.remove(port);
		} catch (final IOException ex) {
			RegistryExceptionHandler.handleException(ex);
		}
	}
}
