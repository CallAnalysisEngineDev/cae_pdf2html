package org.cae.p2h.client;

import org.cae.p2h.TransformHandler;

public class TransformProxy {

	private TransformHandler handler =new TransformHandler();
	
	public void transform(){
		handler.handle();
	}
	
	public static void main(String[] args){
		new TransformProxy().transform();
	}
}
