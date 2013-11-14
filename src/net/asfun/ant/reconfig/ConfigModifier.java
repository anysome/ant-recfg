package net.asfun.ant.reconfig;

import net.asfun.ant.reconfig.FileAlteration.AlterationIndicator;

public interface ConfigModifier {

	void readFrom(String file);
	
	void modify(AlterationIndicator indicator);
	
	void writeTo(String file);
}
