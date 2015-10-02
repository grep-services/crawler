package main.java.services.grep.processors;

import java.util.Iterator;
import java.util.List;

import main.java.services.grep.exceptions.InstagramLibraryException;
import main.java.services.grep.exceptions.PageNotFoundException;
import main.java.services.grep.exceptions.RateLimitExceedException;
import main.java.services.grep.utils.Constants;

import org.apache.commons.lang3.Range;
import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.tags.TagMediaFeed;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;

/**
 * 
 * AccountProvider inner class로 만들어도 크게 그 class field 쓸 것 없고
 * 특히 enum은 static이어야 되어서 inner class에서 사용하지 못한다는 점이 문제가 된다.
 * 그리고 앞으로 setter, getter 등 size 커질 것 생각하면 이렇게 따로 빼두는게 낫다.
 * 
 * @author marine
 * @since 150714
 * 
 */
public class Account {

	private final String account_name;// monitoring용
	private final String client_id;// 현재는 확인용에밖에 쓸 일이 없다.
	private final String client_secret;
	private final String access_token;
	Instagram instagram;
	//private Queue query_box;// remaining은 query를 날려봐야 확인 가능하고, 실시간으로 날려서 remaining 갱신할 수 없으니, 여기서 thread로 집계/관리한다.
	private ProcessingType processing_type;// none, serial, parallel, both
	private TaskStatus task_status;// unavailable, free, reserved, working - unavailable은 remaining에 의해 정해질 뿐 manually 정해지지는 않는다. 사용 안하고 싶으면 type을 none으로 둔다.

	public Account(String account_name, String client_id, String client_secret, String access_token, ProcessingType processing_type) {
		this.account_name = account_name;
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.access_token = access_token;
		this.processing_type = processing_type;
		
		initInstagram();
		
		try {
			updateTaskStatus();
		} catch (InstagramLibraryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// account 사용 가능하게 하는 초기화.
	public void initInstagram() {
		Token token = new Token(access_token, client_secret);
		
		instagram = new Instagram(token);
	}
	
	public String getAccountName() {
		return account_name;
	}
	
	public List<MediaFeedData> getTagMediaList(String tag) throws PageNotFoundException, RateLimitExceedException, InstagramLibraryException {
		return getTagMediaList(tag, null, null);// max 없이 recent를 받는 순간도 있을 것이다.
	}
	
	/*
	 * limit 다 차거나, page가 없을 때까지 tag에 대한 feed list를 받는다.
	 * max부터 받으며, min은 매 page의 min과 비교해 아직 크면 pass, 작으면 break. 
	 * 그리고, return(throw) 하기 전에는 remaining update를 하고 status까지 마무리되도록 한다.
	 */
	public List<MediaFeedData> getTagMediaList(String tag, String lower, String upper) throws PageNotFoundException, RateLimitExceedException, InstagramLibraryException {
		List<MediaFeedData> mediaList = null;
		
		try {
			// library가 object 구조를 좀 애매하게 해놓아서, 바로 loop 하기보다, 1 cycle은 직접 작성해주는 구조가 되었다.
			TagMediaFeed mediaFeed = instagram.getRecentMediaTags(tag, null, upper);// 충분히 null일 수 있고, 그것은 곧 recent부터이다.
			
			// 첫 list로 받긴 하지만, 처음부터도 filtering을 해야 한다. 만약 filtered되면, 바로 return한다.
			if(mediaFeed.getPagination().getNextMaxId().compareTo(lower) < 0) {
				mediaList = filterMediaList(lower, mediaList);
				
				return mediaList;
			} else {
				mediaList = mediaFeed.getData();
			}
			
			Pagination page = mediaFeed.getPagination();
			MediaFeed recentMediaNextPage = instagram.getRecentMediaNextPage(page);
            
            while(true) {
            	if(recentMediaNextPage.getPagination() == null) {// return도 필요하므로, throw에 value로 보낸다.
            		throw new PageNotFoundException(client_id, page.getNextMaxId(), mediaList);
            	}
            	
            	if(recentMediaNextPage.getRemainingLimitStatus() == 0) {// 현재까지의 list return.
            		throw new RateLimitExceedException(client_id, page.getNextMaxId(), mediaList);
            	}
            	
            	List<MediaFeedData> mediaData = recentMediaNextPage.getData();
            	
            	// range check. bottom보다 크면 통과. 작으면 bottom 까지만 남기고 자른다.
            	if(recentMediaNextPage.getPagination().getNextMaxTagId().compareTo(lower) < 0) {
            		mediaList.addAll(filterMediaList(lower, mediaData));
            		
            		break;
            	} else {
                    mediaList.addAll(mediaData);
                    
                    page = recentMediaNextPage.getPagination();
                    recentMediaNextPage = instagram.getRecentMediaNextPage(page);
            	}
            }
		} catch (InstagramException e) {
			throw new InstagramLibraryException(e.getMessage());
		}
		
		return mediaList;
	}
	
	/*
	 * 어차피 string으로 해야 되고, 그냥 min만 비교한다.
	 * 그리고, max값이 list의 last one의 id는 아닌 만큼, max < lower이라서 filter하려 했지만 list > lower 인 경우도 있을 수 있을지 모른다.
	 */
	public List<MediaFeedData> filterMediaList(String lower, List<MediaFeedData> mediaList) {
		for(Iterator<MediaFeedData> iterator = mediaList.iterator(); iterator.hasNext();) {
			MediaFeedData mediaData = iterator.next();
			
			if(mediaData.getId().compareTo(lower) < 0) {
				iterator.remove();
			}
		}
		
		return mediaList;
	}
	
	// 아마 0일 때는 exception 날 수도 있을 것 같다.
	private int getRateRemaining() throws InstagramLibraryException {
		int rate_remaining = Constants.INT_NULL;
		
		try {
			MediaFeed mediaFeed = instagram.getUserFeeds();
			
			if(mediaFeed != null) {// 혹시 모르니 해준다.
				rate_remaining =  mediaFeed.getRemainingLimitStatus();// 여기서도 exception 날 수 있으니 값을 바로 return하지 않는다.
			}
		} catch (InstagramException e) {
			throw new InstagramLibraryException(e.getMessage());
		}
		
		return rate_remaining;
	}
	
	public void updateTaskStatus() throws InstagramLibraryException {
		int rate_remaining = getRateRemaining();
		
		if(rate_remaining != Constants.INT_NULL) {
			if(rate_remaining < Constants.INSTAGRAM_RATE_LIMIT / 2) {
				setTaskStatus(TaskStatus.UNAVAILABLE);
			} else {
				setTaskStatus(TaskStatus.FREE);
			}
		}
	}
	
	public void setTaskStatus(TaskStatus task_status) {
		this.task_status = task_status;
	}

	public TaskStatus getTaskStatus() {
		return task_status;
	}
	
	public void setProcessingType(ProcessingType processing_type) {
		this.processing_type = processing_type;
	}
	
	public ProcessingType getProcessingType() {
		return processing_type;
	}

	public String getClientId() {
		return client_id;
	}

	public String getClientSecret() {
		return client_secret;
	}

	public String getAccessToken() {
		return access_token;
	}

}