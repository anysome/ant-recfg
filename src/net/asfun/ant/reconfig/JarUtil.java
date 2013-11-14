package net.asfun.ant.reconfig;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;

public class JarUtil {

	private static String mf = File.separator + "META-INF" + File.separator
			+ "MANIFEST.MF";

	public static void jar(String sourceDir, String jarFile) {
		File manifest = new File(sourceDir + mf);
		jarPackage(new File(sourceDir), manifest, "", new File(jarFile));
	}

	public static void jarPackage(File srcPath, File manifest,
			String manifestEncoding, File output) {
		Project prj = new Project();
		Jar jar = new Jar();
		jar.setProject(prj);
		jar.setDestFile(output);
		FileSet fileSet = new FileSet();
		fileSet.setProject(prj);
		fileSet.setDir(srcPath);
		fileSet.setIncludes("**/*.*");
		jar.addFileset(fileSet);
		jar.setManifest(manifest);
		// jar.setManifestEncoding(manifestEncoding);
		jar.execute();
	}

	public static void unjar(String jarFile, String destDir) {
		jarExpand(new File(jarFile), new File(destDir));
	}

	public static void jarExpand(File jarFile, File outputPath) {
		Project prj = new Project();
		Expand expand = new Expand();
		expand.setProject(prj);
		expand.setSrc(jarFile);
		expand.setOverwrite(true);
		expand.setDest(outputPath);
		expand.execute();
	}

}
