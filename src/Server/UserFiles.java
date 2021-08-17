package Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserFiles {

	//private String path = System.getProperty("user.dir") +"\\";

	public String listAllFiles(String dir, String format) {
		String fullPath = "";

		fullPath = dir;

		try (Stream<Path> walk = Files.walk(Paths.get(fullPath))) {
			String allFiles = "+" + fullPath+"\r\n" ;

			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();

			List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());

			if(format.equals("F")) {
				for(int i = 0; i < result.size(); i++) {
					String file = result.get(i).replace(fullPath, "");
					allFiles = allFiles + file + "\r\n";
				}
			}
			else if(format.equals("V")) {
				for(int i = 0; i < listOfFiles.length; i++) {
					String file = result.get(i).replace(fullPath, "");

					BasicFileAttributes info = Files.readAttributes(Paths.get(listOfFiles[i].getPath()), BasicFileAttributes.class);

					DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
					String creationTime = "    file created time: " + df.format(info.creationTime().toMillis());
					String lastAccessTime = "    file last accessed time: " + df.format(info.lastAccessTime().toMillis());
					String lastModifiedTime = "    file last modified time: " + df.format(info.lastModifiedTime().toMillis());

					allFiles = allFiles + file + creationTime + lastAccessTime + lastModifiedTime + "\r\n";
				}
			}

			return allFiles;

		} catch (IOException e) {
			return "-Invalid directory";
		}
	}
}
