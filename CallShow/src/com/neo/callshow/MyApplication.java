package com.neo.callshow;

import android.app.Application;

public class MyApplication extends Application {
	public String PhoneNum = null;
	public String Location = null;
	public int Flag = 0;
	public int Load = 0;
	public int state = 0;

	public void setLocation(String location) {
		this.Location = location;
	}

	public String getLocation() {
		return this.Location;
	}

	public void setPhoneNum(String phonenum) {
		this.PhoneNum = phonenum;
	}

	public String getPhoneNum() {
		return this.PhoneNum;
	}

	public void setFlag(int flag) {
		this.Flag = flag;
	}

	public int getFlag() {
		return this.Flag;
	}

	public void setLoad(int load) {
		this.Load = load;
	}

	public int getLoad() {
		return this.Load;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return this.state;
	}
}
