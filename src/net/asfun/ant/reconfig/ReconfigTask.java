package net.asfun.ant.reconfig;

import java.io.File;
import java.io.IOException;

import net.asfun.ant.reconfig.FileAlteration.AlterationIndicator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;


public class ReconfigTask extends Task{

	private String xfile;
	private String debug;
	private String projectName;
	private String workspace = null;
	private String projectDir = null;
	
	public void execute() throws BuildException {
		Log.print("xfile = " + xfile);
		if ( xfile == null || xfile.trim().length() == 0 ) {
			Log.print("do nothing");
			return;
		}
		workspace = getProject().getBaseDir().getAbsolutePath();
		Log.print("current path is :" + workspace);
		if ( ! new File(xfile).exists() ) {
			xfile = workspace + File.separator + xfile;
		}
		PublishXmlReader pxr = new PublishXmlReader(xfile);
		if ( projectName == null || projectName.trim().length() == 0) {
			for( String projId : pxr.listProjects() ) {
				rebuild(pxr, projId);
			}
		} else {
			rebuild(pxr, projectName);
		}
	}
	
	private void rebuild(PublishXmlReader pxr, String projName) {
		projectName = projName;
		ProjectAlteration pa = pxr.findIndicators(projectName);
		String fileName = pa.getProjectName();
		String fullFileName = workspace + File.separator + fileName;
		File pf = new File(fullFileName);
		if ( ! pf.exists() ) {
			return;
		}
		if ( pa.isFile() ) {
			if (pf.isDirectory() ) {
				System.err.println("make sure is a file : " + fileName);
				return;
			}
			Log.print("================ start rebuild " + projectName + " ==================");
			// 解压文件
			projectDir = fileName.substring(0, fileName.lastIndexOf("."));
			JarUtil.unjar(fullFileName, getLocation(null));
			backupFile(fullFileName, fileName);
			FileUtils.delete(new File(fullFileName));
		} else {
			if (pf.isFile() ) {
				System.err.println("make sure is a dir : " + fileName);
				return;
			}
			Log.print("================ start rebuild " + projectName + " ==================");
			projectDir = pa.getProjectName();
		}
		for(FileAlteration fa : pa.getFiles() ) {
			backupFile(fa.getFile());
			Log.print("rebuild file : " + fa.getFile());
			ConfigModifier cm = null;
			if ( "xml".equalsIgnoreCase(fa.getFileType()) ) {
				cm = new XmlModifier(getLocationBackFirst(fa.getFile()));
			}
			if ( "properties".equalsIgnoreCase(fa.getFileType()) ) {
				cm = new PropertiesModifier(getLocationBackFirst(fa.getFile()));
			}
			if ( cm != null ) {
				for(AlterationIndicator ai : fa.getIndicators() ) {
					cm.modify(ai);
				}
				cm.writeTo(getLocation(fa.getFile()));
			}
		}
		// 压缩文件
		if ( pa.isFile() ) {
			JarUtil.jar(getLocation(null), fullFileName);
			deleteDir(new File(getLocation(null)));
		}
		Log.print("================ finished rebuild " + projectName + " ==================");
		Log.print(" ");
	}
	
	private String getLocation(String file) {
		if ( file == null ) {
			return workspace + File.separator + projectDir;
		}
		return workspace + File.separator + projectDir + file;
	}
	
	private String getLocationBackFirst(String file) {
		if ( file == null ) {
			return workspace + File.separator + projectDir;
		}
		file = workspace + File.separator + projectDir + file;
		String back = file + ".back";
		if ( new File(back).exists() ) {
			return back;
		} else {
			return file;
		}
	}
	
	private void backupFile(String file) {
		file = workspace + File.separator + projectDir + file;
		String back = file + ".back";
		if ( new File(back).exists() ) {
			return;
		}
		FileUtils fUtils = FileUtils.getFileUtils();
		try {
			fUtils.copyFile(file, back);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void deleteDir(File dir) { 
	    if ( !dir.exists() || !dir.isDirectory()) 
	        return; // 检查参数 
	    for (File file : dir.listFiles()) { 
	        if (file.isFile()) 
	            file.delete(); // 删除所有文件 
	        else if (file.isDirectory()) 
	            deleteDir(file); // 递规的方式删除文件夹 
	    } 
	    dir.delete();// 删除目录本身 
	}
	
	private void backupFile(String file, String fileName) {
		FileUtils fUtils = FileUtils.getFileUtils();
		try {
			fUtils.copyFile(file, workspace + File.separator + "bak" + File.separator + fileName);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public String getXfile() {
		return xfile;
	}

	public void setXfile(String xfile) {
		this.xfile = xfile;
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
		Log.isDebugOn = "1".equals(debug) || 
				"true".equalsIgnoreCase(debug) || 
				"on".equalsIgnoreCase(debug);
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
