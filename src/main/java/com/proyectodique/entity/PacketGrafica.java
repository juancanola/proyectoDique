package com.proyectodique.entity;

public class PacketGrafica {
	

	private String type;
	
	private String count;

	private String time;
	
	

	public PacketGrafica() {
		super();
	}

	public PacketGrafica(String type, String count, String time) {
		super();
		this.type = type;
		this.count = count;
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "PacketGrafica [type=" + type + ", count=" + count + ", time=" + time + "]";
	}
	

	

	
	



	

}
