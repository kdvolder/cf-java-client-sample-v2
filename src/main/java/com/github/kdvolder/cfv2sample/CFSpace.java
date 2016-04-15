package com.github.kdvolder.cfv2sample;

public class CFSpace {

	private String orgmane;
	private String spaceName;

	public CFSpace(String orgmane, String spaceName) {
		super();
		this.orgmane = orgmane;
		this.spaceName = spaceName;
	}

	public String getOrgmane() {
		return orgmane;
	}

	public void setOrgmane(String orgmane) {
		this.orgmane = orgmane;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}


}
