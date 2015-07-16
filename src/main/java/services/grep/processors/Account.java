package main.java.services.grep.processors;

import java.util.List;

import main.java.services.grep.utils.Constants;

import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
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

	private final String client_id;// 현재는 확인용에밖에 쓸 일이 없다.
	private final String client_secret;
	private final String access_token;
	Instagram instagram;
	// TODO: 차후에는 현시간 기준 1시간 이내 Query counting 하는 Data structure 만들고, 그를 통해 limit 실시간 관리 가능. 현재는 단순히 좀 차면 다시 쓰기 방식.
	private int rate_remaining;
	// real-time checking은 100퍼센트 정확하기도 애매하고, 외부에서의 query도 생각 안할 수 없고, 실시간 체크라는 부담 등의 이유로 주기적 check로 바꾸기로 했다.
	//private Queue query_box;// remaining은 query를 날려봐야 확인 가능하고, 실시간으로 날려서 remaining 갱신할 수 없으니, 여기서 thread로 집계/관리한다.
	private enum TaskStatus {UNAVAILABLE, FREE, RESERVED, WORKING};// 추후 monitoring시 .name()으로 쉽게 출력하기 위해 enum을 간단히 사용.
	private enum ProcessingType {NONE, SERIAL, PARALLEL, BOTH};// 추후 monitoring시 .name()으로 쉽게 출력하기 위해 enum을 간단히 사용.
	private TaskStatus task_status;
	private ProcessingType processing_type;
	
	public Account(String client_id, String client_secret, String access_token, ProcessingType a) {
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.access_token = access_token;
		
		this.processing_type = processing_type;
	}
	
	// account 사용 가능하게 하는 초기화.
	public void initInstagram() {
		Token token = new Token(access_token, client_secret);
		
		instagram = new Instagram(token);
	}
	
	// 초기 1회에 추정하기보다는 직접 구하는게 나을 것 같다. 그리고 main에서 callback 처리할 수 있도록 return해준다.
	public int getRateRemaining() {
		int rate_remaining = Constants.INT_NULL;
		
		try {
			MediaFeed mediaFeed = instagram.getUserFeeds();
			
			if(mediaFeed != null) {// 혹시 모르니 해준다.
				rate_remaining =  mediaFeed.getRemainingLimitStatus();// 여기서도 exception 날 수 있으니 값을 바로 return하지 않는다.
			}
		} catch (InstagramException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rate_remaining;
	}
	
	// limit 다 차거나, page가 없을 때까지 tag에 대한 feed list를 받는다.
	public List<MediaFeedData> getTagMediaList(String tag) {
		List<MediaFeedData> mediaList = null;
		
		try {
			TagMediaFeed mediaFeed = instagram.getRecentMediaTags(tag);
			
			mediaList = mediaFeed.getData();
			
            MediaFeed recentMediaNextPage = instagram.getRecentMediaNextPage(mediaFeed.getPagination());
            
            while (recentMediaNextPage.getRemainingLimitStatus() > 0 && recentMediaNextPage.getPagination() != null) {
                mediaList.addAll(recentMediaNextPage.getData());
                recentMediaNextPage = instagram.getRecentMediaNextPage(recentMediaNextPage.getPagination());
            }
		} catch (InstagramException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mediaList;
	}

}
