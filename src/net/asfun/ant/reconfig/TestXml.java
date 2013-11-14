package net.asfun.ant.reconfig;

import net.asfun.ant.reconfig.Operate.Operation;

public class TestXml {

	public static void main(String...strings) {
		Log.isDebugOn = true;
		XmlModifier xm = new XmlModifier("D:\\craft\\ant-recfg\\test\\in.xml.back");
		xm.modify("//root/appender-ref[@ref='STDOUT']", Operation.DELETE, 
				null, null);
		xm.writeTo("D:\\craft\\ant-recfg\\test\\out.xml");
	}
}
