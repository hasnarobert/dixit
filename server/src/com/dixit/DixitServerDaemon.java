package com.dixit;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class DixitServerDaemon extends NanoHTTPD {
	public DixitServerDaemon() throws IOException {
		super(8080, new File("."));
	}

	public Response serve(String uri, String method, Properties header,
			Properties parms, Properties files) {
		System.out.println(method + " '" + uri + "' ");
		String msg = "<html><body><h1>Hello server</h1>\n";
		if (parms.getProperty("username") == null)
			msg += "<form action='?' method='get'>\n"
					+ "  <p>Your name: <input type='text' name='username'></p>\n"
					+ "</form>\n";
		else
			msg += "<p>Hello, " + parms.getProperty("username") + "!</p>";

		msg += "</body></html>\n";
		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
	}

	public static void main(String[] args) {
		try {
			new DixitServerDaemon();
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}
		System.out.println("Listening on port 8080. Hit Enter to stop.\n");
		try {
			System.in.read();
		} catch (Throwable t) {
		}
		;
	}
}
