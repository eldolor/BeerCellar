package com.cm.beer.db;

import java.io.Serializable;

public class Note implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public long id;
	public String beer = "";
	public String alcohol = "";
	public String price = "";
	public String style = "";
	public String brewery = "";
	public String breweryLink = "";
	public String state = "";
	public String country = "";
	public String notes = "";
	public String rating = "0.0";
	public String picture = "";
	public long created;
	public long updated;
	public String share = "";
	public String latitude = "0.0";
	public String longitude = "0.0";
	public String userId = "";
	public String userName = "";
	public String userLink = "";
	public String characteristics = "";
	public String currencySymbol = "";
	public String currencyCode = "";
}
