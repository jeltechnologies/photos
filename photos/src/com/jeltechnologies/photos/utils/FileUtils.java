package com.jeltechnologies.photos.utils;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final int MAX_BINARY_FILE_IN_MEMORY = 10485760; // 10 MB

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private static final int BINARY_READ_CHUNK_LENGTH = 4096;

    private static final int MAX_IMAGE_SIZE = 10 * 1048576; // 10 MB preventing
							    // memory leak

    public static final String NEWLINE = System.getProperty("line.separator");

    private final static String FILE_SEPERATOR = System.getProperty("file.separator");

    private static void makeFolderToStoreFileIfNeeded(String fileName) throws IOException {
	int lastSeperator = fileName.lastIndexOf(FILE_SEPERATOR);
	if (lastSeperator > 0) {
	    String folderName = fileName.substring(0, lastSeperator);
	    File folder = new File(folderName);
	    if (folder.isFile()) {
		throw new IOException("Cannot create folder " + folderName + " to store " + fileName + " because this is an existing file");
	    } else {
		if (!folder.exists()) {
		    boolean ok = folder.mkdirs();
		    if (!ok) {
			throw new IOException("Cannot create folder " + folderName + " to store " + fileName);
		    } else {
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Created folder " + folderName);
			}
		    }
		}
	    }
	}
    }

    public static LocalDateTime getLastModifiedTime(File file) throws IOException {
	BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
	FileTime time = attr.lastModifiedTime();
	return toLocalDateTime(time);
    }
    
    public static LocalDateTime getCreationTime(File file) throws IOException {
	BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
	FileTime time = attr.creationTime();
	return toLocalDateTime(time);
    }
    
    private static LocalDateTime toLocalDateTime(FileTime fileTime) {
	return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    public static void createFolderIfNotExists(File folder) {
	if (!folder.isDirectory()) {
	    boolean ok = folder.mkdirs();
	    if (!ok) {
		LOGGER.error("Could not create folder " + folder);
	    }
	}
    }

    private static void removeFolderThatContainedFileIfEmpty(String fileName) throws IOException {
	int lastSeperator = fileName.lastIndexOf(FILE_SEPERATOR);
	if (lastSeperator > 0) {
	    String folderName = fileName.substring(0, lastSeperator);
	    File folder = new File(folderName);
	    if (folder.isDirectory()) {
		String[] children = folder.list();
		if (children.length == 0) {
		    folder.delete();
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Successfully deleted folder " + folderName);
		    }
		}
	    }
	}
    }

    public static void moveFile(String fromAbsoluteFilePath, String toAbsoluteFilePath, boolean overwrite) throws IOException {
	moveFile(fromAbsoluteFilePath, toAbsoluteFilePath, overwrite, false);
    }

    public static void moveFile(File from, File to, boolean overwrite, boolean deleteEmptyFolder) throws IOException {
	moveFile(from.getAbsolutePath(), to.getAbsolutePath(), overwrite, deleteEmptyFolder);
    }

    public static void moveFile(String fromAbsoluteFilePath, String toAbsoluteFilePath, boolean overwrite, boolean deleteEmptyFolder) throws IOException {
	makeFolderToStoreFileIfNeeded(toAbsoluteFilePath);
	Path source = Paths.get(fromAbsoluteFilePath);
	Path destination = Paths.get(toAbsoluteFilePath);
	if (overwrite) {
	    Files.move(source, destination, REPLACE_EXISTING);
	} else {
	    Files.move(source, destination);
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Successfully moved [" + fromAbsoluteFilePath + "] to [" + toAbsoluteFilePath + "]");
	}
	if (deleteEmptyFolder) {
	    removeFolderThatContainedFileIfEmpty(fromAbsoluteFilePath);
	}
    }

    public static void deleteFileAndEmptyFolder(String absoluteFileName) throws IOException {
	deleteFileAndEmptyFolder(new File(absoluteFileName));
    }

    public static void deleteFileAndEmptyFolder(File file) throws IOException {
	file.delete();
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Successfully deleted file [" + file + "]");
	}
	removeFolderThatContainedFileIfEmpty(file.getAbsolutePath());
    }

    public static String readTextFile(String filePath) throws IOException {
	return readTextFile(filePath, null);
    }

    public static String readTextFile(String filePath, String charSetName) throws IOException {
	return readTextFile(filePath, true, charSetName);
    }

    public static String readTextFile(String filePath, boolean inClassPath, String charSetName) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("readFile " + filePath);
	}
	StringBuilder builder = new StringBuilder();
	Scanner scanner = null;
	InputStream in;
	try {
	    if (inClassPath) {
		in = FileUtils.class.getResourceAsStream("/" + filePath);
	    } else {
		in = new FileInputStream(new File(filePath));
	    }
	    if (charSetName != null) {
		scanner = new Scanner(in, charSetName);
	    } else {
		scanner = new Scanner(in);
	    }
	    while (scanner.hasNext()) {
		builder.append(scanner.nextLine());
	    }
	} finally {
	    if (scanner != null) {
		scanner.close();
	    }
	}
	return builder.toString();
    }

    public static List<String> readTextFileLines(String filePath) throws IOException {
	return readTextFileLines(filePath, true, null);
    }

    public static List<String> readTextFileLines(String filePath, boolean inClassPath) throws IOException {
	return readTextFileLines(filePath, inClassPath, null);
    }

    public static List<String> readTextFileLines(String filePath, String charSetName) throws IOException {
	return readTextFileLines(filePath, true, charSetName);
    }

    public static List<String> readTextFileLines(String filePath, boolean inClassPath, String charSetName) throws IOException {
	List<String> result = new ArrayList<String>();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("readTextFileLines " + filePath);
	}
	Scanner scanner = null;
	InputStream in;
	try {
	    if (inClassPath) {
		in = FileUtils.class.getResourceAsStream("/" + filePath);
		if (in == null) {
		    throw new FileNotFoundException("Cannot find " + filePath + " in classpath");
		}
	    } else {
		in = new FileInputStream(new File(filePath));
	    }
	    if (charSetName != null) {
		scanner = new Scanner(in, charSetName);
	    } else {
		scanner = new Scanner(in);
	    }
	    while (scanner.hasNext()) {
		result.add(scanner.nextLine());
	    }
	} finally {
	    if (scanner != null) {
		scanner.close();
	    }
	}
	return result;
    }

    public static void writeTextFile(String filePath, String text) throws IOException {
	writeTextFile(filePath, text, Charset.defaultCharset());
    }

    public static void writeTextFile(String filePath, List<String> lines) throws IOException {
	writeTextFile(filePath, lines, Charset.defaultCharset());
    }

    public static void writeTextFile(String filePath, String text, Charset charset) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("writeFile " + filePath);
	}
	makeFolderToStoreFileIfNeeded(filePath);
	Writer output = null;
	try {
	    output = new OutputStreamWriter(new FileOutputStream(filePath), charset);
	    output.write(text);
	} finally {
	    if (output != null) {
		output.close();
	    }
	}
    }

    public static void writeTextFile(String filePath, List<String> lines, Charset charset) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("writeFile " + filePath);
	}
	makeFolderToStoreFileIfNeeded(filePath);
	Writer output = null;
	try {
	    output = new OutputStreamWriter(new FileOutputStream(filePath), charset);
	    for (int i = 0; i < lines.size(); i++) {
		if (i > 0) {
		    output.write(NEWLINE);
		}
		output.write(lines.get(i));
	    }
	} finally {
	    if (output != null) {
		output.close();
	    }
	}
    }

    public static void writeBinaryFile(String filePath, byte[] data) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("writeBinaryFile " + filePath);
	}
	writeBinaryFile(new File(filePath), data);
    }
    
    public static void writeBinaryFile(File fileTo, byte[] data) throws IOException {
	if (fileTo.isFile()) {
	    LOGGER.info("File already exist on disk, writing is skipped for file [" + fileTo + "]");
	} else {
	    makeFolderToStoreFileIfNeeded(fileTo.getAbsolutePath());
	    FileOutputStream out = null;
	    try {
		out = new FileOutputStream(fileTo);
		out.write(data);
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }

    public static byte[] readBinaryFile(String filePath) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("readBinaryFile " + filePath);
	}
	File file = new File(filePath);
	return readBinaryFile(file);
    }

    public static byte[] readBinaryFile(File file) throws IOException {
	if (file.length() > MAX_BINARY_FILE_IN_MEMORY) {
	    throw new IOException("Cannot read binary files larger than " + MAX_BINARY_FILE_IN_MEMORY + " bytes");
	}
	byte[] fileData = new byte[(int) file.length()];
	DataInputStream dis = null;
	try {
	    dis = new DataInputStream(new FileInputStream(file));
	    dis.readFully(fileData);
	    return fileData;
	} finally {
	    if (dis != null) {
		dis.close();
	    }
	}
    }

    public static byte[] readImageFromClasspath(String localPath) throws IOException {
	ByteArrayOutputStream bais = new ByteArrayOutputStream();
	InputStream is = null;
	is = FileUtils.class.getResourceAsStream(localPath);
	if (is == null) {
	    throw new FileNotFoundException("Cannot find " + localPath);
	}
	byte[] byteChunk = new byte[BINARY_READ_CHUNK_LENGTH];
	int n;
	int totalBytesRead = 0;
	while ((n = is.read(byteChunk)) > 0) {

	    bais.write(byteChunk, 0, n);
	    totalBytesRead = totalBytesRead + n;
	    if (totalBytesRead > MAX_IMAGE_SIZE) {
		throw new IOException("Maximum image size is " + MAX_IMAGE_SIZE + " bytes");
	    }
	}
	return bais.toByteArray();
    }

    public static void writeObject(File file, Object object) throws IOException {
	FileOutputStream fos = null;
	ObjectOutputStream outputStream = null;
	try {
	    fos = new FileOutputStream(file);
	    outputStream = new ObjectOutputStream(fos);
	    outputStream.writeObject(object);
	} finally {
	    if (outputStream != null) {
		outputStream.close();
	    }
	    if (fos != null) {
		fos.close();
	    }
	}
    }

    public static Object readObject(File file) throws IOException, ClassNotFoundException {
	FileInputStream fis = null;
	ObjectInputStream inputStream = null;
	try {
	    fis = new FileInputStream(file);
	    inputStream = new ObjectInputStream(fis);
	    return inputStream.readObject();
	} finally {
	    if (inputStream != null) {
		inputStream.close();
	    }
	    if (fis != null) {
		fis.close();
	    }
	}
    }

    public static void copyFile(String source, String destination) throws IOException {
	copyFile(new File(source), new File(destination), true);
    }

    public static void copyFile(String source, String destination, boolean overwrite) throws IOException {
	copyFile(new File(source), new File(destination), overwrite);
    }

    public static void copyFile(File source, File destination, boolean overwrite) throws IOException {
	Path sourceFile = source.toPath();
	Path targetFile = destination.toPath();
	File targetFolder = destination.getParentFile();
	if (overwrite && !targetFolder.isDirectory()) {
	    boolean ok = targetFolder.mkdirs();
	    if (!ok) {
		LOGGER.warn("Could not create folder " + targetFolder);
	    }
	}
	if (overwrite) {
	    Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
	} else {
	    Files.copy(sourceFile, targetFile);
	}
    }

    public static void getFilesIterative(File folder, List<File> list, FileFilter fileFilter) throws InterruptedException {
	if (Thread.interrupted()) {
	    throw new InterruptedException("Interrupted getsFilesIterative");
	}
	File[] files = folder.listFiles(fileFilter);
	for (File file : files) {
	    if (file.isFile()) {
		list.add(file);
	    } else {
		if (file.isDirectory()) {
		    getFilesIterative(file, list, fileFilter);
		}
	    }
	}
    }

    public static void getFilesIterative(File folder, List<File> list, FilenameFilter fileNameFilter) throws InterruptedException {
	if (Thread.interrupted()) {
	    throw new InterruptedException("Interrupted getsFilesIterative");
	}
	File[] files = folder.listFiles(fileNameFilter);
	for (File file : files) {
	    if (file.isFile()) {
		list.add(file);
	    } else {
		if (file.isDirectory()) {
		    getFilesIterative(file, list, fileNameFilter);
		}
	    }
	}
    }

    public static String createMD5Checksum(File file) throws IOException, InterruptedException {
	MessageDigest md;
	try {
	    md = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    throw new IllegalStateException("Cannot find messagedigest");
	}
	try (InputStream fis = new FileInputStream(file)) {
	    byte[] buffer = new byte[1024];
	    int nread;
	    while ((nread = fis.read(buffer)) != -1) {
		md.update(buffer, 0, nread);
	    }
	}

	// bytes to hex
	StringBuilder result = new StringBuilder();
	for (byte b : md.digest()) {
	    result.append(String.format("%02x", b));
	}
	return result.toString();
    }
    
    public static String getNormalizedPath(File file) {
	return file.toPath().normalize().toString();
    }

}
