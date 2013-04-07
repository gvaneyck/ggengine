package com.gvaneyck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtils {
	public static String readFile(File file) throws IOException {
		Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
		
		StringBuilder buff = new StringBuilder();
		int c;
		while ((c = in.read()) != -1)
			buff.append((char)c);
		in.close();
		
		return buff.toString();
	}
}
