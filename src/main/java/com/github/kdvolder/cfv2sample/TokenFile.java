package com.github.kdvolder.cfv2sample;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenFile {

	final static Logger log = LoggerFactory.getLogger(TokenFile.class);
	
	private String refreshToken;

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public static TokenFile read(File file) {
		if (file.exists()) {
			ObjectMapper jackson = new ObjectMapper();
			try {
				return jackson.readValue(file, TokenFile.class);
			} catch (Exception e) {
				log.error("Token file exists, but couldn't read it!", e);
			}
		}
		return null;
	}

	public void write(File file) throws Exception {
		ObjectMapper jackson = new ObjectMapper();
		jackson.writeValue(file, this);
	}

}
