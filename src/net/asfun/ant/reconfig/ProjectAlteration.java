package net.asfun.ant.reconfig;

import java.util.LinkedList;
import java.util.List;

public class ProjectAlteration {

	private String projectName;
	private boolean isFile = false;
	List<FileAlteration> files;
	
	public ProjectAlteration() {
		files = new LinkedList<FileAlteration>();
	}
	
	public void addFileAlteration(FileAlteration fileAlteration) {
		files.add(fileAlteration);
	}
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public boolean isFile() {
		return isFile;
	}
	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	public List<FileAlteration> getFiles() {
		return files;
	}
	public void setFiles(List<FileAlteration> files) {
		this.files = files;
	}
	
}
