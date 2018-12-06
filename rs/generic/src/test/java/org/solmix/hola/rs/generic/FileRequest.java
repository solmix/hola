package org.solmix.hola.rs.generic;

import java.io.Serializable;

public class FileRequest implements Serializable {

	private String filePath;
	/** 是否支持断点续传，默认不支持 */
	private boolean breakpoint;
	/** 配合断点续传功能，表示文件的最近修改时间 */
	private long lastModified;
	/** 配合断点续传功能，请求数据的内容段 */
	private long[] range;
	/** 配合断点续传功能，可以用最近修改时间，或者md5，根据服务端而定 */
	private String etag;

	public FileRequest(String filePath) {
		this.filePath = filePath;
	}

	public FileRequest() {
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isBreakpoint() {
		return breakpoint;
	}

	public void setBreakpoint(boolean breakpoint) {
		this.breakpoint = breakpoint;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long[] getRange() {
		return range;
	}

	public void setRange(long[] range) {
		this.range = range;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

}
