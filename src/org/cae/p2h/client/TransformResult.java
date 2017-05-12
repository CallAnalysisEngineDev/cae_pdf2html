package org.cae.p2h.client;

import java.util.List;

public class TransformResult {

	private boolean successed;
	private List<String> failList;
	public TransformResult(){
		this.successed=true;
	}
	public TransformResult(List<String> failList){
		this.successed=false;
		this.failList=failList;
	}
	public boolean isSuccessed() {
		return successed;
	}
	public void setSuccessed(boolean successed) {
		this.successed = successed;
	}
	public List<String> getFailList() {
		return failList;
	}
	public void setFailList(List<String> failList) {
		this.failList = failList;
	}
	@Override
	public String toString() {
		if(successed){
			return "successed:"+successed+"\n";
		}
		else{
			String str="";
			for(int i=0;i<failList.size();i++){
				if(i==failList.size()-1){
					str+=failList.get(i);
				}
				else{
					str+=failList.get(i)+",";
				}
			}
			return "successed:"+successed+"\n"
					+ "failList:"+str+"\n";
		}
	}
	
}
