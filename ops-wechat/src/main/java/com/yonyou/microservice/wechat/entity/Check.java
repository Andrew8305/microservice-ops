package com.yonyou.microservice.wechat.entity;
/**
 * 
 * @author Richard
 *
 */
public class Check {
	String signature;
	Long timestamp;
	Long nonce;
	String echostr;
	/**
	 * //全局返回码
	 */
	String errcode;
	/**
	 * //说明
	 */
	String errmsg;
	
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getNonce() {
		return nonce;
	}
	public void setNonce(Long nonce) {
		this.nonce = nonce;
	}
	public String getEchostr() {
		return echostr;
	}
	public void setEchostr(String echostr) {
		this.echostr = echostr;
	}
	public String getErrcode() {
		return errcode;
	}
	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
}
