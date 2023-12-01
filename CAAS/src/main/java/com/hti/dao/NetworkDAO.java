package com.hti.dao;

import java.util.List;

import com.hti.network.dto.NetworkEntry;

public interface NetworkDAO {
	public List<NetworkEntry> list();
}
