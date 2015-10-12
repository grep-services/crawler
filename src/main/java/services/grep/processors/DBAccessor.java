package main.java.services.grep.processors;

/**
 * 
 * @author marine
 * @since 150706
 *
 */
public class DBAccessor {

	private static final DBAccessor dbAccessor = new DBAccessor();
	
	private DBAccessor() {
	}
	
	public static DBAccessor getInstance() {
		return dbAccessor;
	}
	
	public long getMaxItemId(String tableName) {
		return 100;
	}
	
	public long getMinItemId(String tableName) {
		return 0;
	}

}
