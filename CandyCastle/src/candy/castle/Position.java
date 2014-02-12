package candy.castle;

public class Position {
	private long id;
	private String user;
	private int user_id;
	private Double lat;
	private Double lng;
	private Double alt;
	private Double bsl;
	private int act;
	private long time;
	private int game;
	private int send;

	public long getId () {
		return id;
	}

	public void setId (long id) {
		this.id = id;
	}
	
	public String getUser () {
		return user;
	}
	
	public void setUser (String user) {
		this.user = new String (""+user);
	}
	
	public int getUserId () {
		return user_id;
	}
	
	public void setUserId (int user_id) {
		this.user_id = user_id;
	}
	
	public Double getLat () {
		return lat;
	}

	public void setLat (Double lat) {
		this.lat = lat;
	}
	
	public Double getLng () {
		return lng;
	}

	public void setLng (Double lng) {
		this.lng = lng;
	}
	
	public Double getAlt () {
		return alt;
	}

	public void setAlt (Double alt) {
		this.alt = alt;
	}
	
	public Double getBsl () {
		return bsl;
	}

	public void setBsl (Double bsl) {
		this.bsl = bsl;
	}
	
	public int getAct () {
		return act;
	}

	public void setAct (int act) {
		this.act = act;
	}
	
	public long getTime () {
		return time;
	}

	public void setTime (long time) {
		this.time = time;
	}
	
	public int getGame () {
		return game;
	}

	public void setGame (int game) {
		this.game = game;
	}
	
	public int getSend () {
		return send;
	}

	public void setSend (int send) {
		this.send = send;
	}
}
