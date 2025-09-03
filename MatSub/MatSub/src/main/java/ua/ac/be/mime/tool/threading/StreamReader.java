package ua.ac.be.mime.tool.threading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class that implement a StreamReader that is used to read all the contents
 * coming from an InputStream.
 * 
 * @author Sandy Moens
 */
public class StreamReader implements MIMERunnable {

	private final BufferedReader bufferedReader;
	private boolean run;
	private boolean verbose = true;

	public StreamReader(InputStream inputStream) {
		this.bufferedReader = new BufferedReader(new InputStreamReader(
				inputStream));
	}

	public StreamReader(InputStream inputStream, boolean verbose) {
		this(inputStream);
		this.verbose = verbose;
	}

	@Override
	public void run() {
		this.run = true;
		int c;
		try {
			while ((c = this.bufferedReader.read()) != -1 && this.run) {
				if (this.verbose) {
					System.out.print((char) c);
					System.out.flush();
				}
				if (Thread.interrupted()) {
					stop();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		this.run = false;
	}
}
