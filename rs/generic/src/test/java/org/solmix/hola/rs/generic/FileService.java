package org.solmix.hola.rs.generic;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author admin
 *
 */
public interface FileService {

	FileResponse download(String path);

	boolean download(String filePath, String localPath);

	boolean download(String filePath, OutputStream out);

	/**
	 * 处理上传
	 * 
	 * @param in
	 * @return
	 */
	String upload(String destPath, InputStream in);

	FileResponse getFile(FileRequest req);
}
