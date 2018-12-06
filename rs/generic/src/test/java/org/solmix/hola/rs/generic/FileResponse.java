package org.solmix.hola.rs.generic;

import java.io.Serializable;

public class FileResponse implements Serializable {

	private static final long serialVersionUID = 7375471965521600038L;
	private byte[] content;
	private long[] contentRange;
	private long contentLength;
	private long lastModified;
	private int errorCode;

	public FileResponse() {
	}

	public FileResponse(byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public long[] getContentRange() {
		return contentRange;
	}

	public void setContentRange(long[] contentRange) {
		this.contentRange = contentRange;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
