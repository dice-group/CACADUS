package de.uni_leipzig.cacadus.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * The Class ZipUtils.
 * Saves a folder and it's files in a zip file
 */
public class ZipUtils {

	public static final int BUFFER_SIZE = 4096;
	
	public static void extractFromURL(String url, String file) throws IOException{
		extractFromURL(new URL(url), file);
	}
	
	public static void extractFromURL(URL url, String file) throws IOException{
		File f = new File(file);
		f.createNewFile();
		ZipInputStream zis = new ZipInputStream(url.openStream());
		zis.getNextEntry();
		extract(zis, file);
		zis.close();
	}
	
	private static void extract(ZipInputStream zis, String file) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		byte[] content = new byte[BUFFER_SIZE];
		int read;
		while((read = zis.read(content))!=-1){
			bos.write(content, 0, read);
		}
		bos.close();
	}
	
	public static int extractFilesWithSuffix(InputStream bytes, String destFolder, String suffix, String outputSuffix) throws IOException{
		File destDir = new File(destFolder);
		if(!destDir.exists())
			destDir.mkdirs();
		ZipInputStream zis = new ZipInputStream(bytes);
		ZipEntry entry = null;
		int ret=0;
		while((entry=zis.getNextEntry())!=null){
			if(!entry.isDirectory()){
				if(entry.getName().endsWith(suffix)){
					extract(zis, destFolder+File.separator+outputSuffix+entry.getName());
					ret++;
				}
			}
			zis.closeEntry();
		}
		zis.close();
		return ret;
	}
	
	public static void extractFilesWithSuffix(String zipFile, String destFolder, String suffix, String outputSuffix) throws IOException{
		extractFilesWithSuffix(new FileInputStream(zipFile), destFolder, suffix, outputSuffix);
		
		
	}
	
	/**
	 * Writes a folder to a zipFile.
	 *
	 * @param srcFolder the src folder
	 * @param destFolder the zip file
	 * @return destFolder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String folderToZip(String srcFolder, String destFolder)
			throws IOException {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destFolder);
		zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip);
		zip.flush();
		zip.close();
		return destFolder;
	}

	/**
	 * Adds a given folder to a zip file
	 *
	 * @param path the path in which the Folder should be saved in
	 * @param srcFolder the src folder
	 * @param zip the zipOutputStream to the zip file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void addFolderToZip(String path, String srcFolder,
			ZipOutputStream zip) throws IOException {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
						+ fileName, zip);
			}
		}
	}

	/**
	 * Adds a given file to the zipFile.
	 *
	 * @param path the path in which the given file should be saved
	 * @param srcFile the src file
	 * @param zip the zip stream in which the file should be saved
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void addFileToZip(String path, String srcFile,
			ZipOutputStream zip) throws IOException {
		File src = new File(srcFile);
		if (src.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + src.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

}
