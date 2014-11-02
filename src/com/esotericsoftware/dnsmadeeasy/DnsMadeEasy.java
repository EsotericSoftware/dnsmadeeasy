
package com.esotericsoftware.dnsmadeeasy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DnsMadeEasy {
	String user = "", pass = "", id = "", lastIP = "";
	int minutes = 30;
	final File configFile = new File(System.getProperty("user.home"), ".dnsmadeeasy/config.txt");
	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public DnsMadeEasy () throws IOException {
		loadConfig();

		new Timer("Timer").schedule(new TimerTask() {
			public void run () {
				System.out.print("Started.");
				try {
					update(user, pass, id);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}, 0, minutes * 60 * 1000);

		// Don't return so service isn't terminated.
		while (true) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	void update (String user, String pass, String id) throws IOException {
		String newIP = read("http://www.dnsmadeeasy.com/myip.jsp");
		if (newIP.equals(lastIP)) return;

		System.out.print(dateFormat.format(new Date()) + ", " + newIP + ", ");
		String result = read("http://www.dnsmadeeasy.com/servlet/updateip?username=" + user + "&password=" + pass + "&id=" + id
			+ "&ip=" + newIP);
		System.out.println(result);
		if (result.equals("success")) {
			lastIP = newIP;
			saveConfig();
		}
	}

	String read (String url) throws IOException {
		InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
		StringWriter writer = new StringWriter(128);
		char[] buffer = new char[1024];
		while (true) {
			int count = reader.read(buffer);
			if (count == -1) break;
			writer.write(buffer, 0, count);
		}
		return writer.toString();
	}

	void saveConfig () throws IOException {
		FileWriter writer = new FileWriter(configFile);
		writer.write("User: " + user + "\r\n");
		writer.write("Password: " + pass + "\r\n");
		writer.write("Record ID: " + id + "\r\n");
		writer.write("Minutes: " + minutes + "\r\n");
		writer.write("Last IP: " + lastIP);
		writer.close();
	}

	void loadConfig () throws IOException {
		if (!configFile.exists()) saveConfig();
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		try {
			user = value(reader.readLine());
			pass = value(reader.readLine());
			id = value(reader.readLine());
			minutes = Integer.parseInt(value(reader.readLine()));
			lastIP = value(reader.readLine());
		} catch (Exception ex) {
			throw new RuntimeException("Error reading config file: " + configFile.getAbsolutePath(), ex);
		}
		reader.close();
	}

	String value (String line) {
		int index = line.indexOf(":");
		if (index == -1) throw new RuntimeException("Invalid line: " + line);
		String value = line.substring(index + 1).trim();
		if (value.length() > 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
			value = value.substring(1, value.length() - 2);
		return value;
	}

	static class MultiplexOutputStream extends OutputStream {
		private final OutputStream[] streams;

		public MultiplexOutputStream (OutputStream... streams) {
			if (streams == null) throw new IllegalArgumentException("streams cannot be null.");
			this.streams = streams;
		}

		public void write (int b) throws IOException {
			for (int i = 0; i < streams.length; i++) {
				synchronized (streams[i]) {
					streams[i].write(b);
				}
			}
		}

		public void write (byte[] b, int off, int len) throws IOException {
			for (int i = 0; i < streams.length; i++) {
				synchronized (streams[i]) {
					streams[i].write(b, off, len);
				}
			}
		}
	}

	static public void main (final String[] args) throws Exception {
		try {
			File dir = new File(System.getProperty("user.home"), ".dnsmadeeasy");
			dir.mkdirs();
			FileOutputStream logFile = new FileOutputStream(new File(dir, "dnsmadeeasy.log"));
			System.setOut(new PrintStream(new MultiplexOutputStream(System.out, logFile), true));
			System.setErr(new PrintStream(new MultiplexOutputStream(System.err, logFile), true));
		} catch (Throwable ex) {
			System.out.println("Unable to write log file.");
			ex.printStackTrace();
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException (Thread thread, Throwable ex) {
				ex.printStackTrace();
				System.out.println("Uncaught exception, exiting.");
				System.exit(0);
			}
		});

		new DnsMadeEasy();
	}
}
