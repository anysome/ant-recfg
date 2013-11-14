package net.asfun.ant.reconfig;

import java.util.NoSuchElementException;

public class Operate {

	public static enum Operation {
		
		REPLACE,
		
		UPDATE,
		
		DELETE,
		
		ADD
	}
	
	public static Operation getOperation(String operation) {
		if ( "replace".equalsIgnoreCase(operation) ) {
			return Operation.REPLACE;
		} else if ( "update".equalsIgnoreCase(operation) ) {
			return Operation.UPDATE;
		} else if ( "delete".equalsIgnoreCase(operation) ) {
			return Operation.DELETE;
		} else if ( "add".equalsIgnoreCase(operation) || "append".equalsIgnoreCase(operation) ) {
			return Operation.ADD;
		}
		throw new NoSuchElementException("Unsupport operation : " + operation);
	}
}
