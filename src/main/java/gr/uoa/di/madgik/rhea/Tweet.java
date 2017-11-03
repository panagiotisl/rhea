package gr.uoa.di.madgik.rhea;

public class Tweet {

	private String time;
	private String user;
	private String tweet;
	
	Tweet(String time, String user, String tweet){
		this.time = time;
		this.user = user;
		this.tweet = tweet;
	}
	
	public String getTime() {
		return time;
	}
	public String getUser() {
		return user;
	}
	public String getTweet() {
		return tweet;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setTweet(String tweet) {
		this.tweet = tweet;
	}
	
	
	
}
