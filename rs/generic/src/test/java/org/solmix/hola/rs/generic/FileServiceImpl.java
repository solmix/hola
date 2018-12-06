package org.solmix.hola.rs.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.solmix.runtime.io.CachedOutputStream;

public class FileServiceImpl implements FileService {

	@Override
	public FileResponse download(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);
			CachedOutputStream cache = new CachedOutputStream();
			IOUtils.copy(inputStream, cache);
			return new FileResponse(cache.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String upload(String destPath, InputStream input) {
		try {
			FileOutputStream out = new FileOutputStream("D:\\temp\\a.js");
			IOUtils.copy(input, out);
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "upload complete !";
	}

	@Override
	public boolean download(String filePath, String localPath) {

		return false;
	}

	@Override
	public boolean download(String filePath, OutputStream out) {
		// TODO Auto-generated method stub
		return false;
	}

	private static final int BLOCK = 1024 * 1024;

	@Override
	public FileResponse getFile(FileRequest req) {
		String location = req.getFilePath();
		File file = new File(location);
		FileResponse res = new FileResponse();
		if (!file.exists()) {
			res.setErrorCode(404);
			return res;
		} else if (file.isDirectory()) {
			res.setErrorCode(503);
			return res;
		}
		try {
			// RandomAccessFile in = new RandomAccessFile(file, "r");
			FileInputStream in = new FileInputStream(file);
			try {
				res.setContentLength(file.length());
				res.setLastModified(file.lastModified());
				long[] range = req.getRange();
				if (range == null || range.length != 2) {
					range = new long[2];
					range[0] = 0;
					range[1] = BLOCK;
				}
				int length = (int) (range[1] - range[0]);
				byte[] content = new byte[length];
				// in.seek(range[0]);
				in.skip(range[0]);
				in.read(content);
				res.setContentRange(range);
				res.setContent(content);
			} finally {
				IOUtils.closeQuietly(in);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

}
