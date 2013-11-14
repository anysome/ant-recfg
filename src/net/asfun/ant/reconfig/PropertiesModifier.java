package net.asfun.ant.reconfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import net.asfun.ant.reconfig.FileAlteration.AlterationIndicator;
import net.asfun.ant.reconfig.Operate.Operation;



public class PropertiesModifier implements ConfigModifier{

	private Properties props = new Properties();
	private boolean isLoaded = false;
	
	public PropertiesModifier() {
		
	}
	
	public PropertiesModifier(String file) {
		readFrom(file);
	}
	
	public void readFrom(String file) {
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
			props.load(fs);
			isLoaded = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if ( fs != null ) {
				try {
					fs.close();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	public void modify(AlterationIndicator ai) {
		modify(ai.location, ai.oper, ai.value);
	}
	
	public void modify(String key, Operation oper, String value) {
		if ( isLoaded ) {
			switch(oper) {
			case UPDATE :
				props.setProperty(key, formatProperty(value));
				Log.print("\tupdate `" + key +"` to value `" + value + "`");
				break;
			case DELETE :
				props.remove(key);
				Log.print("\tdelete `" + key +"`");
				break;
			case ADD :
				props.setProperty(key, formatProperty(value));
				Log.print("\tadd `" + key +"` with value `" + value + "`");
				break;
			}
		}
	}
	
	private String formatProperty(String kv) {
		try {
			return new String(kv.getBytes(), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		}
		return kv;
	}
	
	public void writeTo(String file) {
		if ( isLoaded ) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream( file );
				props.store(fos, "******* rebuild by ant-recfg. *******");
				fos.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			} finally {
				if ( fos != null ) {
					try {
						fos.close();
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}

}
