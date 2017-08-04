package com.javapda.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipUtil {
	protected static Logger log = Logger.getLogger(ZipUtil.class);
	
	public static boolean isZipFile(File file) {
		return file!=null && isZipFile(file.getAbsolutePath());
	}
	
	public static boolean isZipFile(String filename) {
		return filename.endsWith(".zip");
	}
	
	public static List<NameAndLength> getFileNameAndLengths(File zipFile) {
		List<NameAndLength> fileNamesAndLengths = new ArrayList<>();
		try {
			try (FileSystem fs = FileSystems.newFileSystem(
					Paths.get(zipFile.getAbsolutePath()), null)) {

				Iterable<Path> dirs = fs.getRootDirectories();
				for (Path name : dirs) {
					// System.err.println(name);
					try (DirectoryStream<Path> stream = Files
							.newDirectoryStream(name)) {
						for (Path filePath : stream) {
							// System.out.println(filePath.getFileName());
							fileNamesAndLengths.add(NameAndLength.create(
									filePath.getFileName() + "",
									Files.size(filePath)));
						}
					} catch (IOException | DirectoryIteratorException x) {
						// IOException can never be thrown by the iteration.
						// In this snippet, it can only be thrown by
						// newDirectoryStream.
						System.err.println(x);
					}
				}
				// Files.copy(
				// fs.getPath(fileInZipName),
				// Paths.get(destFile.getAbsolutePath()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileNamesAndLengths;
	}

	/**
	 * http://docs.oracle.com/javase/tutorial/essential/io/dirs.html
	 * 
	 * @param zipFile
	 * @return
	 */
	public static List<String> getFileNames(File zipFile) {
		List<String> filenames = new ArrayList<>();
		try {
			try (FileSystem fs = FileSystems.newFileSystem(
					Paths.get(zipFile.getAbsolutePath()), null)) {

				Iterable<Path> dirs = fs.getRootDirectories();
				for (Path directory : dirs) {
					getFilesFromDirectory(directory,filenames);
				}
				// Files.copy(
				// fs.getPath(fileInZipName),
				// Paths.get(destFile.getAbsolutePath()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filenames;
	}

	private static void getFilesFromDirectory(Path directory,List<String> filenames) {
		try (DirectoryStream<Path> stream = Files
				.newDirectoryStream(directory)) {
			for (Path file : stream) {
				//System.out.println(file.getFileName() + ":"+file.getFileName().toString().endsWith("/"));
				if(file.getFileName().toString().endsWith("/")) {
					getFilesFromDirectory(file, filenames);
				} else {
					// System.out.println(file.getFileName());
					filenames.add(directory.toString() + file.getFileName() + "");
//					filenames.add(file.getFileName() + "");
				}
			}
		} catch (IOException | DirectoryIteratorException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can only be thrown by
			// newDirectoryStream.
			System.err.println(x);
		}
	}

	static boolean verbose = false;

	public static File getFileFromZip(File zipFile, String fileInZipName,
			File destFile) {
		try {
			try (FileSystem fs = FileSystems.newFileSystem(
					Paths.get(zipFile.getAbsolutePath()), null)) {

				if (verbose) {
					Iterable<Path> dirs = fs.getRootDirectories();
					for (Path name : dirs) {
						System.err.println(name);
						try (DirectoryStream<Path> stream = Files
								.newDirectoryStream(name)) {
							for (Path file : stream) {
								System.out.println(file.getFileName());
							}
						} catch (IOException | DirectoryIteratorException x) {
							// IOException can never be thrown by the iteration.
							// In this snippet, it can only be thrown by
							// newDirectoryStream.
							System.err.println(x);
						}
					}
				}
				Files.copy(fs.getPath(fileInZipName),
						Paths.get(destFile.getAbsolutePath()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return destFile;
	}

	/**
	 * http://www.java2novice.com/java-collections-and-util/zip/file-list/
	 * 
	 * @param zipFile
	 * @return
	 */
	public static List<String> getFileNamesOld(File zipFile) {
		List<String> filenames = new ArrayList<>();
		ZipFile z;
		try {
			z = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> en = z.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				filenames.add(ze.getName());
			}
			z.close();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filenames;
	}
	
	
	public static File buildZipFile(Collection<File> files) {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File zipFile = new File(tmpDir,String.format("%s_%d.zip",new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()), Math.abs(new Random(System.currentTimeMillis()).nextInt())));
		if(log.isDebugEnabled()) {
			log.debug(String.format("tmpDir:%s",tmpDir.getAbsolutePath()));
		}
		ZipOutputStream zos = null;
		try {
			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);
			for(File file : files) {
					FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
			}
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		try {
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return zipFile;
	}
	
}
