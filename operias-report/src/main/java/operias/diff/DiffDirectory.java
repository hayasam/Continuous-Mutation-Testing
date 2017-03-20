package operias.diff;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Contains the source diff information for a directory and all its sub directories
 * @author soosterwaal
 *
 */
public class DiffDirectory {

	/**
	 * Original directory name
	 */
	private String originalDirectoryName;
	
	/**
	 * Revised directory name
	 */
	private String revisedDirectoryName;
	
	/**
	 * List of changed files within the directory
	 */
	private List<DiffFile> files;
	
	/**
	 * List of changed files within the directory
	 */
	private List<DiffDirectory> directories;
	
	/**
	 * Diff state of the directory
	 */
	private SourceDiffState state;
	
	/**
	 * Construct a new direcotyr for in the diff report
	 * @param originalDirectoryName
	 */
	public DiffDirectory(String originalDirectoryName, String revisedDirectoryName, SourceDiffState state) {
		this.originalDirectoryName = originalDirectoryName;
		this.revisedDirectoryName = revisedDirectoryName;
		this.state = state;
		this.files = new ArrayList<DiffFile>();
		this.directories = new ArrayList<DiffDirectory>();
	}
	
	/**
	 * @return the directoryName
	 */
	public String getOriginalDirectoryName() {
		return originalDirectoryName;
	}
	
	public String getRevisedDirectoryName() {
		return revisedDirectoryName;
	}

	/**
	 * @return the files
	 */
	public List<DiffFile> getFiles() {
		return files;
	}
	
	/**
	 * @return the state
	 */
	public SourceDiffState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(SourceDiffState state) {
		this.state = state;
	}

	/**
	 * Add a new file to the files within this directory;
	 * @param file
	 */
	public void addFile(DiffFile file) {
		files.add(file);
	}
	
	/**
	 * Add a new directory to this directory
	 * @param directory
	 */
	public void addDirectory(DiffDirectory directory) {
		this.directories.add(directory);
	}
	
	/**
	 * 
	 * @param originalDirectory
	 * @param revisedDirectory
	 * @throws IOException 
	 */
	public static DiffDirectory compareDirectory(String originalDirectory, String revisedDirectory) throws IOException {
		
		// Short cuts 
		if (originalDirectory == null) {
			return fillDirectoryDiff(revisedDirectory, SourceDiffState.NEW);
		} else if (revisedDirectory == null) {
			return fillDirectoryDiff(originalDirectory, SourceDiffState.DELETED);
		} 
		
		// Both directories should exists, if not, throw exceptions
		DiffDirectory diffDirectory = new DiffDirectory(originalDirectory, revisedDirectory, SourceDiffState.SAME);
		
		File originalDirectoryFile = new File(originalDirectory);
		File newDirectoryFile = new File(revisedDirectory);
		
		if (!originalDirectoryFile.isDirectory()) {
			throw new InvalidParameterException("'" +originalDirectory + "' is not a valid directory");
		}
		
		if (!newDirectoryFile.isDirectory()) {
			throw new InvalidParameterException("'" +newDirectoryFile + "' is not a valid directory");
		}
		
		String[] newFiles = newDirectoryFile.list();
		Arrays.sort(newFiles);
		String[] originalFiles = originalDirectoryFile.list();
		Arrays.sort(originalFiles);
		
		List<String> filesWithinNewDirectory = Arrays.asList(newFiles);
		List<String> filesWithinOriginalDirectory = Arrays.asList(originalFiles);
		
		//First cmpare the original dirs with the new one to see changes and deleted file and directories
		for(String fileName : filesWithinOriginalDirectory) {
			File originalFile = new File(originalDirectoryFile, fileName);
			
			
			if (originalFile.isHidden()) {
				continue;
			}
			
			if (originalFile.isDirectory()) {
				if (filesWithinNewDirectory.indexOf(fileName) >= 0) {
					// Directory exists
					File newDir = new File(newDirectoryFile, fileName);
					diffDirectory.addDirectory(DiffDirectory.compareDirectory(originalFile.getAbsolutePath(), newDir.getAbsolutePath()));
				} else {
					// Directory was deleted
					diffDirectory.addDirectory(DiffDirectory.compareDirectory(originalFile.getAbsolutePath(), null));
				}
			} else {
				if (FilenameUtils.getExtension(originalFile.getName()).equals("java")) {
					if (filesWithinNewDirectory.indexOf(fileName) >= 0) {
						// File exists
						File newFile = new File(newDirectoryFile, fileName);
						diffDirectory.addFile(DiffFile.compareFile(originalFile.getAbsolutePath(), newFile.getAbsolutePath()));
					} else {
						// File was deleted
						diffDirectory.addFile(DiffFile.compareFile(originalFile.getAbsolutePath(), null));
					}
				}
			}
		}
		
		//Secondly check the new directory for any new files or directories
		for(String fileName : filesWithinNewDirectory) {
			File newFile = new File(revisedDirectory, fileName);

			if (newFile.isHidden()) {
				continue;
			}
			if (filesWithinOriginalDirectory.indexOf(fileName) < 0) {
				if (newFile.isDirectory()) {
					// directory is new
					diffDirectory.addDirectory(DiffDirectory.compareDirectory(null, newFile.getAbsolutePath()));		
				} else {
					// file is new

					if (FilenameUtils.getExtension(newFile.getName()).equals("java")) {
						diffDirectory.addFile(DiffFile.compareFile(null, newFile.getAbsolutePath()));		
					}
				} 
			}
		}
		
		//Check if we need to changed this directory status to changed
		diffDirectory.setNewStatusIfNeeded();
		
		
		return diffDirectory;
	}
	
	
	/**
	 * Set the status to CHANGED if any directory or file within this directory is changed
	 */
	private void setNewStatusIfNeeded() {
		for(DiffDirectory dir : directories ) {
			if (dir.getState() != SourceDiffState.SAME) {
				setState(SourceDiffState.CHANGED);
				return;
			}
		}
		
		for(DiffFile file : files ) {
			if (file.getSourceState() != SourceDiffState.SAME) {
				setState(SourceDiffState.CHANGED);
				return;
			}
		}
	}

	/**
	 * File a deleted directory with state deleted, only states DELETED or NEW are accepted
	 * @param fileDiffDirectory
	 * @return
	 * @throws IOException 
	 */
	private static DiffDirectory fillDirectoryDiff(String directory, SourceDiffState state) throws IOException {
	
		File dir = new File(directory);
		
		File[] files = dir.listFiles();
		Arrays.sort(files);
		
		List<File> filesWithinDirectory = Arrays.asList(files);
		
		// Create the instance
		DiffDirectory diffDirectory = new DiffDirectory(directory, "", state);
		
		// Switch the directory name is the state is NEW
		if (state.equals(SourceDiffState.NEW)) {
			diffDirectory = new DiffDirectory("", directory, state);
		}
		
		for(File file : filesWithinDirectory) {
			
			if (file.isHidden()) {
				continue;
			}
			
			if (file.isFile()) {
				if (state.equals(SourceDiffState.DELETED)) {
					diffDirectory.addFile(DiffFile.compareFile(file.getAbsolutePath(), null));
				} else {
					diffDirectory.addFile(DiffFile.compareFile(null, file.getAbsolutePath()));
				}
			} else {
				if (state.equals(SourceDiffState.DELETED)) {
					diffDirectory.addDirectory(DiffDirectory.compareDirectory(file.getAbsolutePath(), null));
				} else {
					diffDirectory.addDirectory(DiffDirectory.compareDirectory(null, file.getAbsolutePath()));
				}
			}
		}
		
		return diffDirectory;
	}
	
	/**
	 * Checks if the directory has no changes
	 * @return True if no changes were found in this directory, false otherwise
	 */
	public boolean isEmpty() {
		
		boolean isEmpty = files.size() == 0;
		
		for(DiffDirectory dir : directories) {
			isEmpty &= dir.isEmpty();
		}
		
		return isEmpty;
	}

	/**
	 * @return the directories
	 */
	public List<DiffDirectory> getDirectories() {
		return directories;
	}
	
	/**
	 * Get a directory by its name
	 * @param directoryName
	 * @return
	 */
	public DiffDirectory getDirectory(String directoryName) {
		for(DiffDirectory directory : directories) {
			if (directory.getOriginalDirectoryName().equals(directoryName) || directory.getRevisedDirectoryName().equals(directoryName)) {
				return directory;
			} else if (directoryName.startsWith(directory.getOriginalDirectoryName()) || directoryName.startsWith(directory.getRevisedDirectoryName())) {
				// Check sub directories
				
				DiffDirectory tempDir = directory.getDirectory(directoryName);
				
				if (tempDir != null) {
					return tempDir;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Get the file diff instance of the given file
	 * @param filename
	 * @return
	 */
	public DiffFile getFile(String filename) {
		
		// Search the files of this directory
		for(DiffFile file : files) {
			if (file.getOriginalFileName().equals(filename) || file.getRevisedFileName().equals(filename)) {
				return file;
			}
		}
		
		//Not found in file, search in directories
		for (DiffDirectory dir : directories) {
			DiffFile sFile = dir.getFile(filename);
			
			if (sFile != null) {
				return sFile;
			}
		}
		
		// file not found
		return null;
	}
}
