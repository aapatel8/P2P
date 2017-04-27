package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SampleClient {

	public static void main(String args[]) throws IOException {
		Scanner scanner = null;
		try {
			File initFile = new File("rfc115.txt");
			scanner = new Scanner(initFile);
			System.out.println(initFile.length());
			String text = scanner.useDelimiter("\\A").next();
//			out.println(text);
			
			File file = new File("rfc115copy.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(text);
			fileWriter.flush();
			fileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close(); // Put this call in a finally block
		}
	}
}
