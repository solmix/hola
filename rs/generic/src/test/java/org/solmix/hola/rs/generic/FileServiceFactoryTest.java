/*
 * Copyright 2015 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.hola.rs.generic;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.timer.StopWatch;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.rs.RemoteReference;
import org.solmix.hola.rs.RemoteRegistration;
import org.solmix.hola.rs.RemoteServiceFactory;
import org.solmix.hola.rs.RemoteServiceManager;
import org.solmix.runtime.Container;
import org.solmix.runtime.ContainerFactory;
import org.solmix.runtime.monitor.MonitorService;
import org.solmix.runtime.monitor.support.MonitorServiceImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年9月17日
 */

public class FileServiceFactoryTest extends Assert {

	public static final int PORT = NetUtils.getAvailablePort();
	static int i = 0;
	private static final Logger LOG = LoggerFactory.getLogger(FileServiceFactoryTest.class);

	@Test
	public void test() throws InterruptedException, ExecutionException, IOException {
		RemoteServiceManager rm = container.getExtension(RemoteServiceManager.class);
		assertNotNull(rm);
		RemoteServiceFactory rsf = rm.getRemoteServiceFactory("hola");
		assertNotNull(rsf);
		FileServiceImpl hs = new FileServiceImpl();
		RemoteRegistration<FileService> reg = rsf.register(FileService.class, hs, mockConfig());
		FileService hello = rsf.getService(reg.getReference());
		assertSame(hs, hello);
		Dictionary<String, Object> properties = mockConfig();

		RemoteReference<FileService> reference = rsf.getReference(FileService.class, properties);
		assertNotNull(reference);
		FileService remote = rsf.getService(reference);
		assertNotNull(remote);
		StopWatch sw = new StopWatch();
		download("F:\\迅雷下载\\1.rmvb", "D:\\temp\\1.rmvb", remote);
		LOG.info(sw.toString());
		// FileResponse input =
		// remote.download("C:\\Users\\admin\\Downloads\\HelloFont.win.1.1.0.0.zip");
		// File f = new File("D:\\temp\\HelloFont.win.1.1.0.0.zip");
		// // f.lastModified()
		// if (!f.exists()) {
		// f.createNewFile();
		// } else {
		// f.delete();
		// f.createNewFile();
		// }
		// FileOutputStream out = new FileOutputStream(f);
		// out.write(input.getContent());
		// out.flush();
		// IOUtils.closeQuietly(out);
		reference.destroy();
		reg.unregister();

	}

	private int BLOCK = 1024 * 1024;

	public boolean download(String filePath, String localPath, FileService service) throws IOException {
		String tmp = localPath + ".tmp";
		File f = new File(localPath);
		File tmpFile = new File(tmp);
		if (!tmpFile.exists()) {
			tmpFile.createNewFile();
		} else {
			tmpFile.delete();
			tmpFile.createNewFile();
		}
		if (f.exists()) {
			f.delete();
		}

		FileRequest request = new FileRequest(filePath);
		handle(tmpFile, request, service);
		tmpFile.renameTo(new File(localPath));
		return false;
	}

	private void handle(File f, FileRequest request, FileService service) throws IOException {

		FileResponse response = service.getFile(request);
		if (response == null) {
			throw new IOException("Can't Get file from server");
		}
		long len = response.getContentLength();
		long[] range = response.getContentRange();
		MonitorService ms = new MonitorServiceImpl();
		LOG.info("content length:{} from {} to {},memery {}", len, range[0], range[1],
				ms.getMonitorInfo().getFreeMemory());
		RandomAccessFile out = new RandomAccessFile(f, "rws");
		if (f.length() == 0) {
			out.setLength(len);
		}

		try {
			out.seek(range[0]);
			out.write(response.getContent());
		} finally {
			IOUtils.closeQuietly(out);
		}
		if (range[1] == len) {
			return;
		} else {
			long[] nrange = new long[2];
			nrange[0] = range[1];
			nrange[1] = range[1] + BLOCK > len ? (int) len : (int) range[1] + BLOCK;
			request.setRange(nrange);
			handle(f, request, service);

		}
	}

	/**
	 * @return
	 */
	private String getString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 1; i++) {
			sb.append("abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*(");
		}
		return sb.toString();
	}

	private Dictionary<String, Object> mockConfig() {
		Hashtable<String, Object> table = new Hashtable<String, Object>();
		table.put(HOLA.PATH_KEY, "/hola");
		table.put(HOLA.PORT_KEY, PORT);
		table.put(HOLA.TIMEOUT_KEY, 1000 * 600);
		// table.put(HOLA.TRANSPORTER_KEY, "local");
		table.put(HOLA.HOST_KEY, HOLA.LOCALHOST_VALUE);
		return table;
	}

	static Container container;

	@BeforeClass
	public static void setup() {
		ContainerFactory.setThreadDefaultContainer(null);
		container = ContainerFactory.getThreadDefaultContainer(true);
	}

	@AfterClass
	public static void tearDown() {
		if (container != null) {
			container.close();
		}
	}
}
