package com.proyectodique.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="packets")
public class Packet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="srcip")
	private String srcip;
	
	@Column(name="srcport")
	private String srcport;
	
	@Column(name="dstip")
	private String dstip;
	
	@Column(name="dstport")
	private String dstport;
	
	@Column(name="protocol")
	private String protocol;

	@Column(name="type")
	private boolean type;
	
	@Column(name="time")
	private String time;
	
	public Packet() {
		super();
	}
	
	

	public Packet(String srcip, String srcport, String dstip, String dstport, String protocol, boolean type,
			String time) {
		super();
		this.srcip = srcip;
		this.srcport = srcport;
		this.dstip = dstip;
		this.dstport = dstport;
		this.protocol = protocol;
		this.type = type;
		this.time = time;
	}



	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSrcip() {
		return srcip;
	}

	public void setSrcip(String srcip) {
		this.srcip = srcip;
	}

	public String getSrcport() {
		return srcport;
	}

	public void setSrcport(String srcport) {
		this.srcport = srcport;
	}

	public String getDstip() {
		return dstip;
	}

	public void setDstip(String dstip) {
		this.dstip = dstip;
	}

	public String getDstport() {
		return dstport;
	}

	public void setDstport(String dstport) {
		this.dstport = dstport;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isType() {
		return type;
	}

	public void setType(boolean type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


	@Override
	public String toString() {
		return "Packet [id=" + id + ", srcip=" + srcip + ", srcport=" + srcport + ", dstip=" + dstip + ", dstport="
				+ dstport + ", protocol=" + protocol + ", type=" + type + ", time=" + time + "]";
	}



	

}
