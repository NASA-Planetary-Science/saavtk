package edu.jhuapl.saavtk.util;

import java.net.URL;

// TODO this should extend Exception and thus be checked.
public class UnauthorizedAccessException extends RuntimeException {
	private static final long serialVersionUID = 7671006960310656926L;
	private final URL url;

	public UnauthorizedAccessException(String cause, URL url) {
		super(cause + url);
		this.url = url;
	}

	public UnauthorizedAccessException(Exception cause, URL url) {
		super(url != null ? url.toString() : "null", cause);
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	@Override
	public String toString() {
		return getMessage();
	}

}