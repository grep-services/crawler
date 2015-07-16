/**
 * 
 */
package main.java.services.grep.controllers;

import java.util.Date;
import java.util.List;

import org.jinstagram.Instagram;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.oauth.InstagramService;
import org.jinstagram.entity.common.Location;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.tags.TagMediaFeed;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;

/**
 * @author marine1079
 *
 */
public class Main {
	
	public static final String CLIENT_ID = "108f8113409a478b85876fe110df9582";
	public static final String CLIENT_SECRET = "7ae52b46ef0345d1b268c557684d8235";
	public static final String REDIRECT_URI = "http://grep.services";// 나중엔 aws ip로 바꾸고, query도 instagram에서 제공하는 대로 맞춰 받고(django 등으로) 테스트해본다.
	public static final String SCOPE = "basic";
	
	public static final int MAX_PAGE_SIZE = 6000;

	/**
	 * 
	 */
	public Main() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main().Function();
		//new Main().Function2();
	}
	
	private static final Token EMPTY_TOKEN = null;
	
	public static final String access_token = "1994185887.1fb234f.50784687fc344ebcb1cf340fc9117749";
	
	public void Function3() {
		InstagramService service = new InstagramAuthService()
	    .apiKey(CLIENT_ID)
	    .apiSecret(CLIENT_SECRET)
	    .callback(REDIRECT_URI) 
	    .scope(SCOPE)
	    .build();
	
		//String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		
		//System.out.println("Authorization URL : " + authorizationUrl);
		
		//Verifier verifier = new Verifier("verifier you get from the user");
		//Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		
		Token token = new Token(access_token, CLIENT_SECRET);
		
		Instagram instagram = new Instagram(token);
		
		//Instagram instagram = new Instagram(CLIENT_ID);
		
		String tagName = "snow";
		
		TagMediaFeed mediaFeed = null;
		
		int counter = 0;
		int mediaCount = 0;
		int cnt_loc = 0;
		int cnt_loc_gang = 0;
		
		try {
			mediaFeed = instagram.getRecentMediaTags(tagName);
			
			if(mediaFeed != null) {
				List<MediaFeedData> mediaList = mediaFeed.getData();
				
	            MediaFeed recentMediaNextPage = instagram.getRecentMediaNextPage(mediaFeed.getPagination());
	            
	            while (recentMediaNextPage.getPagination() != null && counter < 2) {
	                mediaList.addAll(recentMediaNextPage.getData());
	                recentMediaNextPage = instagram.getRecentMediaNextPage(recentMediaNextPage.getPagination());
	                counter++;
	            }
	            
	            mediaCount = mediaList.size();
	            
	            Location loc_gang = new Location();
	            loc_gang.setLatitude(37.497942);
	            loc_gang.setLongitude(127.027621);
	            
	            Location loc_abc = new Location();
	            loc_abc.setLatitude(37.4991525);
	            loc_abc.setLongitude(127.0336952);
	            
	
				
				for(MediaFeedData feed : mediaList) {
					if(feed.getLocation() != null) {
						cnt_loc++;
						Location loc = feed.getLocation();
						
						if(distance(loc, loc_gang) <= 5000) {
							cnt_loc_gang++;
						}
					}
				}
				
				System.out.println(mediaCount + ", " + cnt_loc + ", " + cnt_loc_gang + ", counter : " + counter);
			}
		} catch (InstagramException e) {
			e.printStackTrace();
		} finally {
			System.out.println(mediaCount + ", " + cnt_loc + ", " + cnt_loc_gang + ", counter : " + counter);
		}
	}
	
	public void Function() {
		InstagramService service = new InstagramAuthService()
		    .apiKey(CLIENT_ID)
		    .apiSecret(CLIENT_SECRET)
		    .callback(REDIRECT_URI) 
		    .scope(SCOPE)
		    .build();
		
		//String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		
		//System.out.println("Authorization URL : " + authorizationUrl);
		
		//Verifier verifier = new Verifier("verifier you get from the user");
		//Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		
		Token token = new Token(access_token, CLIENT_SECRET);
		
		Instagram instagram = new Instagram(token);
		
		//Instagram instagram = new Instagram(CLIENT_ID);
		
		String tagName = "ㅈㄷㅇ";
		
		TagMediaFeed mediaFeed = null;
		
		int counter = 0;
		int mediaCount = 0;
		int cnt_loc = 0;
		int cnt_loc_gang = 0;
		
		List<MediaFeedData> mediaList = null;
		
		try {
			//mediaFeed = instagram.getRecentMediaTags(tagName);
			//System.out.println(mediaFeed.getRemainingLimitStatus());
			Date date = new Date ();
			date.setTime(1288004669000l);
			System.out.println(date.toString());
			/*
			if(mediaFeed != null) {
				mediaList = mediaFeed.getData();
				
				Pagination page;
				MediaFeed recentMediaNextPage;
				
				while(true) {
					page = mediaFeed.getPagination();
					if(!page.hasNextPage()) break;
					
					recentMediaNextPage = instagram.getRecentMediaNextPage(page);
					mediaList.addAll(recentMediaNextPage.getData());
					
					counter++;
					
					System.out.println(recentMediaNextPage.getRemainingLimitStatus() + ", " + counter);
				}
			}
			*/
		} catch (/*Instagram*/Exception e) {
			e.printStackTrace();
		} finally {
			/*
			if(mediaList != null) {
	            mediaCount = mediaList.size();
	            
	            Location loc_gang = new Location();
	            loc_gang.setLatitude(37.497942);
	            loc_gang.setLongitude(127.027621);
	            
				for(MediaFeedData feed : mediaList) {
					if(feed.getLocation() != null) {
						cnt_loc++;
						Location loc = feed.getLocation();
						
						if(distance(loc, loc_gang) <= 5000) {
							cnt_loc_gang++;
						}
					}
				}
			}
			
			System.out.println(mediaCount + ", " + cnt_loc + ", " + cnt_loc_gang + ", counter : " + counter);
			*/
		}
		
	}
	
	public Date time(long time, int delay) {
		Date date = new Date ();
		date.setTime(time - delay * 24 * 60 * 60 * 1000);
		
		return date;
	}
	
	public void Function2() {
		InstagramService service = new InstagramAuthService()
		    .apiKey(CLIENT_ID)
		    .apiSecret(CLIENT_SECRET)
		    .callback(REDIRECT_URI) 
		    .scope(SCOPE)
		    .build();
		
		//String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		
		//System.out.println("Authorization URL : " + authorizationUrl);
		
		//Verifier verifier = new Verifier("verifier you get from the user");
		//Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		
		Token token = new Token(access_token, CLIENT_SECRET);
		
		Instagram instagram = new Instagram(token);
		
		//Instagram instagram = new Instagram(CLIENT_ID);
		
		String tagName = "먹스타그램";
		
        Location loc_gang = new Location();
        loc_gang.setLatitude(37.497942);
        loc_gang.setLongitude(127.027621);
		
		MediaFeed mediaFeed = null;
		
		int mediaCount = 0;
		
		try {
			int counter = 1;
			long cur = System.currentTimeMillis();
			Date max_date = time(cur, 0);
			Date min_date = time(cur, 7);
			List<MediaFeedData> mediaList = null;
			int cnt_tag = 0;
			int cnt_loc_tag = 0;
			while ( counter <= 52 * 4 ) { // 2달
				mediaFeed = instagram.searchMedia(loc_gang.getLatitude(), loc_gang.getLongitude(), max_date, min_date, 5000);
				
				List<MediaFeedData> temp = mediaFeed.getData();
				
				if(temp != null) {
					for(MediaFeedData data : temp) {
						if(data.getTags() != null) {
							List<String> tags = data.getTags();
							for(String tag : tags) {
								if(tag.contains(tagName)) {
									cnt_tag ++;
									if(data.getLocation() != null) {
										cnt_loc_tag ++;
									}
								}
							}
						}
					}
				}
				
				if(mediaList == null) mediaList = temp;
				else mediaList.addAll(temp);
				
				max_date = min_date;
				min_date = time(min_date.getTime(), 7);
				
				counter++;
			}
			mediaCount = mediaList.size();

            System.out.println(mediaCount + ", " + cnt_tag + ", " + cnt_loc_tag);
		} catch (InstagramException e) {
			e.printStackTrace();
		}
	}
	
	public double distance(Location startpoint, Location endpoint) {
		double d2r = Math.PI / 180;
		double distance = 0;

		try{
		    double dlong = (endpoint.getLongitude() - startpoint.getLongitude()) * d2r;
		    double dlat = (endpoint.getLatitude() - startpoint.getLatitude()) * d2r;
		    double a =
		        Math.pow(Math.sin(dlat / 2.0), 2)
		            + Math.cos(startpoint.getLatitude() * d2r)
		            * Math.cos(endpoint.getLatitude() * d2r)
		            * Math.pow(Math.sin(dlong / 2.0), 2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		    double d = 6367 * c;

		    distance = d;

		} catch(Exception e){
		    e.printStackTrace();
		}
		
		return distance * 1000;
	}

}
