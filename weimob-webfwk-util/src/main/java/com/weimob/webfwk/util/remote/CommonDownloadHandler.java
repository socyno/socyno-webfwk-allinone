package com.weimob.webfwk.util.remote;

import java.io.IOException;
import java.io.InputStream;

public abstract class CommonDownloadHandler {
	public abstract void process(InputStream stream) throws IOException;
}
