package net.asfun.ant.reconfig;

import java.util.LinkedList;
import java.util.List;

import net.asfun.ant.reconfig.Operate.Operation;


public class FileAlteration {

	private String file;
	private String fileType;
	private List<AlterationIndicator> alters;
	
	public FileAlteration() {
		alters = new LinkedList<AlterationIndicator>();
	}
	
	public FileAlteration(String file, String fileType) {
		this.file = file;
		this.fileType = fileType;
		alters = new LinkedList<AlterationIndicator>();
	}
	
	public void addIndicator(String location, Operation oper, String value, String spot) {
		AlterationIndicator ai =new AlterationIndicator();
		ai.location = location;
		ai.oper = oper;
		ai.spot = spot;
		ai.value = value;
		alters.add(ai);
	}
	
	public class AlterationIndicator {
		public String location;
		public String spot;
		public String value;
		public Operation oper;
		
		private AlterationIndicator(){}
	}

	public String getFile() {
		return file;
	}

	public List<AlterationIndicator> getIndicators() {
		return alters;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setAlters(List<AlterationIndicator> alters) {
		this.alters = alters;
	}
}
