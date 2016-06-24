package com.weisen.xcxf.bean;

public class MessageReply {
	private String Id;
	private String uid;
	private String replyTxt;
	private String replyTime;
	
	public String getId() {
		return Id;
	}
	public void setRId(String Id) {
		this.Id = Id;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getReplyTime() {
		return replyTime;
	}
	public void setReplyTime(String replyTime) {
		this.replyTime = replyTime;
	}
	
	public String getReplyTxt() {
		return replyTxt;
	}
	public void setReplyTxt(String replyTxt) {
		this.replyTxt = replyTxt;
	}

}
