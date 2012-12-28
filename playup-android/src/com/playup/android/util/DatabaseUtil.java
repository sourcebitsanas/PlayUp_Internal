package com.playup.android.util;

import java.util.Hashtable;

import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.methods.Crypto;
import com.playup.android.database.DatabaseWrapper;
import com.playup.android.util.json.JsonUtil;

public class DatabaseUtil {

	private static DatabaseUtil databaseUtil;

	private DatabaseUtil() {
		this.databaseUtil = this;
	}

	public static DatabaseUtil getInstance() {
		if (databaseUtil == null) {
			new DatabaseUtil();
		}
		return databaseUtil;
	}

	// ( LENGTH(vLinkUrl) > 0 AND vLinkUrl = '"+vSectionUrl+"')

	// ////////////// USER SELF PROFILE TABLE RELATED QUERIES
	// //////////////////////////

	public String getNotification() {

		// TO DO write the query to fetch the notification

		// for time being
		return "1";
	}

	/**
	 * check if the user is annonymous or registered one.
	 * 
	 * @return false - if registered else true
	 */
	public boolean isUserAnnonymous() {
		Cursor c = null;

		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							" SELECT iUserId FROM user WHERE isAnonymousUser = '0' AND isPrimaryUser = \"1\" ");

			if (c != null && c.getCount() > 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}

	}

	public String getCompetitionIdFromContestId(String vContestId) {

		String vCompetitionId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vCompetitionId FROM contests WHERE 	vContestId = \""
						+ vContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionId = c
						.getString(c.getColumnIndex("vCompetitionId"));
			}

			c.close();
		}
		c = null;
		return vCompetitionId;
	}

	public String getRoundIdFromContestId(String vContestId) {

		String vCompetitionId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vRoundId FROM contests WHERE vContestId = \""
						+ vContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionId = c.getString(c.getColumnIndex("vRoundId"));
			}

			c.close();
		}

		c = null;
		return vCompetitionId;
	}

	/**
	 * checking if identifier is set or not
	 */
	public boolean isIdentifierProviderName(final String vProviderName) {

		if (vProviderName != null && vProviderName.trim().length() > 0) {
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vProviderName FROM providers WHERE vProviderName = \""
									+ vProviderName + "\" AND isSet = '1' ");

			if (count > 0) {

				return true;
			}
		}
		return false;
	}

	public boolean checkInFriends(String vProfileUrl) {

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vFriendId FROM my_friends mf LEFT JOIN user u ON u.iUserId = mf.vProfileId WHERE u.vSelfUrl = \""
								+ vProfileUrl + "\"  ");
		return (count > 0) ? true : false;
	}

	public String getFrienshipStatus(String vSelfUrl) {

		String status = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT status FROM user WHERE (LENGTH(vSelfUrl) > 0 AND vSelfUrl = \""
						+ vSelfUrl
						+ "\")  OR (LENGTH(vHrefUrl) > 0 AND vHrefUrl = \""
						+ vSelfUrl + "\")");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				status = c.getString(c.getColumnIndex("status"));
			}
			c.close();
		}
		c = null;
		return status;
	}

	public int getUnReadNotificationCount() {
		int notificationCount = 0;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT iNotificationUnReadCount FROM user WHERE isPrimaryUser = '1' ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				if (c.getString(c.getColumnIndex("iNotificationUnReadCount")) != null
						&& c.getString(
								c.getColumnIndex("iNotificationUnReadCount"))
								.trim().length() > 0)
					notificationCount = Integer.parseInt(c.getString(c
							.getColumnIndex("iNotificationUnReadCount")));
			}
			c.close();
		}
		c = null;
		return notificationCount;
	}

	public Hashtable<String, List<String>> getPrivateSelectedCompetitionData() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vHomeTeamId,vAwayTeamId,vSportType,dScheduledStartTime,dStartTime,dEndTime,iHasLiveUpdates, vContestId, vContestUrl,vContestHref, "
						+ "( SELECT vDisplayName  FROM teams WHERE vTeamId = vHomeTeamId ) AS vTeamName1,   "
						+ "( SELECT vDisplayName  FROM teams WHERE vTeamId = vAwayTeamId ) AS vTeamName2,  "
						+ "vSportsName,iTotal1,iTotal2,iWickets1,iWickets2,vOvers,vSummary,vSummary1,vSummary2 "
						+ "FROM contests c   "
						+ "LEFT JOIN competition cp ON cp.vCompetitionId = c.vCompetitionId "
						+ "WHERE cp.isFavourite = '1'  AND cp.iLiveNum > 0 AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL ) ");
	}

	public Hashtable<String, List<String>> getAssoicatedContestData(
			String vContestId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vContestId, vHomeTeamId,vAwayTeamId,vShortTitle, vSportType,dScheduledStartTime,dStartTime,dEndTime,iHasLiveUpdates, vContestId, vContestUrl,vContestHref, "
						+ "( SELECT vDisplayName  FROM associatedTeams WHERE vTeamId = vHomeTeamId ) AS vTeamName1,   "
						+ "( SELECT vDisplayName  FROM associatedTeams WHERE vTeamId = vAwayTeamId ) AS vTeamName2,  "
						+ "vSportsName,iTotal1,iTotal2,iWickets1,iWickets2,vOvers,vSummary,vSummary1,vSummary2 "
						+ "FROM associatedContestsData c    left join associatedContest ac on c.vContestId = ac.contestId "
						+ "where ac.associatedContestId = ( select associatedContestId from contests where vContestId = '"
						+ vContestId
						+ "')"
						+ "  ORDER BY ifnull(c.dScheduledStartTime, '2100-01-01T00:00:00Z') ASC,  ifnull(c.dStartTime,'2100-01-01T00:00:00Z') ASC");
	}

	public Hashtable<String, List<String>> getPrivateSelectedContestUrl(
			String vCompetitionUrl) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContestId,vContestUrl,vContestHref FROM contests c  LEFT JOIN competition cp ON "
						+

						"cp.vCompetitionId = c.vCompetitionId LEFT JOIN competition_live cl ON cp.vCompetitionLiveId = cl.vCompetitionLiveId WHERE "
						+ "((LENGTH(cl.vCompetitionLiveUrl) AND cl.vCompetitionLiveUrl = '"
						+ vCompetitionUrl
						+ "') OR "
						+ "(LENGTH(cl.vCompetitionLiveHref) AND cl.vCompetitionLiveHref = '"
						+ vCompetitionUrl
						+ "')) "
						+ "AND cp.isFavourite = '1'  AND cp.iLiveNum > 0 "
						+ "AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL )");

	}

	public int getUnReadDirectMessagesCount(String vUserId) {
		int unReadCount = 0;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT iUnReadCount FROM user_direct_conversation WHERE vUserId = \""
						+ vUserId + "\"");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				unReadCount = Integer.parseInt(c.getString(c
						.getColumnIndex("iUnReadCount")));
			}
			c.close();
		}
		c = null;
		return unReadCount;
	}

	public Hashtable<String, List<String>> getDirectConversation() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT c.vDirectConversationId,"
						+ " c.vDirectMessageId, c.iUnreadCount, c.vUserSelfUrl,c.vUserHrefUrl, c.vUserId, "
						+ "c.vDisplayName, c.vAvatarUrl, "
						+ "( SELECT vMessage "
						+

						"FROM direct_message_items dmi "
						+ "LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId "
						+ "WHERE dm.vDirectMessageId = c.vDirectMessageId ORDER BY dCreatedDate DESC ) AS vMessage, ( SELECT dCreatedDate "
						+ "FROM direct_message_items dmi "
						+ "LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId "
						+ "WHERE dm.vDirectMessageId = c.vDirectMessageId ORDER BY dCreatedDate DESC ) AS dCreatedDate "
						+ "FROM direct_conversation c "
						+ "LEFT JOIN user_direct_conversation udc ON udc.vDirectConversationId = c.vDirectConversationId "
						+ "WHERE udc.vUserId = \""
						+ getPrimaryUserId()
						+ "\"  AND "
						+ " ( SELECT vMessage FROM direct_message_items dmi LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId WHERE dm.vDirectMessageId = c.vDirectMessageId LIMIT 0,1  ) IS NOT NULL "
						+ "ORDER BY dCreatedDate DESC ");
	}

	public Hashtable<String, List<String>> getDirectConversationMessages(
			String vUserSelfUrl) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT dmi.rowid AS rowid, "
						+ "dmi.vDMessageId AS vDMessageId, dmi.vMessage AS vMessage,  "
						+ "dmi.dCreatedDate AS dCreatedDate, dmi.vUserSelfUrl AS vUserSelfUrl, "
						+ "dmi.vUserId AS vUserId, dmi.vDisplayName AS vDisplayName, dmi.vAvatarUrl  AS vAvatarUrl,"
						+ "dmi.vGapId AS vGapId, dmi.iGapSize AS iGapSize, dmi.vGapUrl AS vGapUrl, dmi.isGapLoading AS isGapLoading,"
						+ "dmi.vGapHrefUrl AS vGapHrefUrl,dmi.vUserHrefUrl AS vUserHrefUrl "
						+ "FROM direct_message_items dmi  "
						+ "LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId  "
						+ "LEFT JOIN direct_conversation dc ON dc.vDirectMessageId = dm.vDirectMessageId "
						+ " WHERE ((LENGTH(dc.vUserSelfUrl) > 0 AND dc.vUserSelfUrl = \""
						+ vUserSelfUrl
						+ "\") OR "
						+ "(LENGTH(dc.vUserHrefUrl) > 0 AND  dc.vUserHrefUrl = \""
						+ vUserSelfUrl + "\")) "
						+ "ORDER BY dCreatedDate DESC  ");
	}

	/*
	 * public String getDirectMessageUrl( String vUserSelfUrl ) {
	 * 
	 * String vDirectMessageUrl = null;
	 * 
	 * 
	 * 
	 * 
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT  dm.vDMessageUrl AS vDMessageUrl,dm.vDMessageHrefUrl  as vDMessageHrefUrl"
	 * + "FROM direct_messages dm " +
	 * "LEFT JOIN  direct_conversation dc ON dc.vDirectMessageId = dm.vDirectMessageId "
	 * + "WHERE dc.vUserSelfUrl = \"" + vUserSelfUrl + "\" " );
	 * 
	 * 
	 * if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vDirectMessageUrl =
	 * c.getString( c.getColumnIndex( "vDMessageUrl" ) ); } c.close(); } c =
	 * null; return vDirectMessageUrl;
	 * 
	 * }
	 */
	/**
	 * OPraveen :as per the href
	 */
	public Hashtable<String, Object> getDirectMessageUrl(String vUserSelfUrl) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT  dm.vDMessageUrl AS vDMessageUrl,dm.vDMessageHrefUrl  as vDMessageHrefUrl "
								+ " FROM direct_messages dm "
								+ " LEFT JOIN  direct_conversation dc ON dc.vDirectMessageId = dm.vDirectMessageId "
								+ " WHERE ((LENGTH(dc.vUserSelfUrl) > 0 AND dc.vUserSelfUrl = '"
								+ vUserSelfUrl
								+ "') OR "
								+ "(LENGTH(dc.vUserHrefUrl) > 0 AND dc.vUserHrefUrl = '"
								+ vUserSelfUrl + "'))");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("vDMessageHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vDMessageUrl"));
					if (url != null && url.trim().length() > 0) {
						result.put("url", url);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}

				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	public String getDirectMessageId(String vDirectMessageUrl) {

		String vDirectMessageId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT dc.vDirectMessageId AS vDirectMessageId "
								+ " FROM direct_conversation dc"
								+ " LEFT JOIN direct_messages dm ON dc.vDirectMessageId = dm.vDirectMessageId "
								+ " WHERE ( dm.vDMessageUrl = \""
								+ vDirectMessageUrl
								+ "\") OR ( dm.vDMessageHrefUrl = \""
								+ vDirectMessageUrl + "\") ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vDirectMessageId = c.getString(c
						.getColumnIndex("vDirectMessageId"));
			}
			c.close();
		}
		c = null;
		return vDirectMessageId;

	}

	public Hashtable<String, Object> getDirectMarkerUrl(
			String vDirectConversationUrl, String vUserSelfUrl) {

		String vMarkerUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT dm.vMarkerUrl AS vMarkerUrl, dm.vMarkerHrefUrl AS vMarkerHrefUrl  "
								+ " FROM direct_messages dm "
								+ " LEFT JOIN direct_conversation dc ON dc.vDirectMessageId = dm.vDirectMessageId  "
								+ " WHERE ( dc.vUserSelfUrl = \""
								+ vUserSelfUrl
								+ "\" ) OR ( dc.vUserHrefUrl = \""
								+ vUserSelfUrl + "\" )");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vMarkerUrl = c.getString(c.getColumnIndex("vMarkerHrefUrl"));
				if (vMarkerUrl != null && vMarkerUrl.trim().length() > 0) {
					result.put("url", vMarkerUrl);
					result.put("isHref", true);

				} else {

					vMarkerUrl = c.getString(c.getColumnIndex("vMarkerUrl"));
					result.put("url", vMarkerUrl);
					result.put("isHref", false);

				}

			}
			c.close();
		}
		c = null;
		return result;

	}

	/**
	 * checking if identifier is enabled or posting or not
	 */
	public boolean isIdentifierEnabledProviderName(final String vProviderName) {

		if (vProviderName != null && vProviderName.trim().length() > 0) {
			int count = PlayupLiveApplication
					.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vProviderName FROM providers WHERE vProviderName = \""
									+ vProviderName + "\" AND isEnabled = '1' ");
			if (count > 0) {
				return true;
			}
		}
		return false;
	}

	public String getDirectConversationIdFromUrl(String vDirectConversationUrl) {

		String vDirectConversationId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vDirectConversationId FROM "
								+ " user_direct_conversation WHERE ((LENGTH(vDirectConversationUrl)  > 0 AND vDirectConversationUrl = '"
								+ vDirectConversationUrl
								+ "') "
								+ " OR (LENGTH(vDirectConversationHrefUrl)  > 0 AND "
								+ " vDirectConversationHrefUrl = '"
								+ vDirectConversationUrl + "') ) ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vDirectConversationId = c.getString(c
						.getColumnIndex("vDirectConversationId"));
			}
			c.close();
		}
		c = null;
		return vDirectConversationId;
	}

	public String getDirectConversationUserIdFromUrl(
			String vDirectConversationUrl) {

		String vUserId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vUserId FROM "
								+ "user_direct_conversation WHERE ((LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl= '"
								+ vDirectConversationUrl
								+ "') OR  "
								+ "(LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl= '"
								+ vDirectConversationUrl + "'))");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vUserId = c.getString(c.getColumnIndex("vUserId"));
			}
			c.close();
		}
		c = null;
		return vUserId;
	}

	public Hashtable<String, Object> getDirectconversationUrlFromId(
			String vDirectConversationId) {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vDirectConversationUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT "
								+ "vDirectConversationUrl,vDirectConversationHrefUrl FROM user_direct_conversation WHERE "
								+ "vDirectConversationId = \""
								+ vDirectConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vDirectConversationUrl = c.getString(c
						.getColumnIndex("vDirectConversationHrefUrl"));

				if (vDirectConversationUrl != null
						&& vDirectConversationUrl.trim().length() > 0) {

					result.put("url", vDirectConversationUrl);
					result.put("isHref", true);

				} else {

					vDirectConversationUrl = c.getString(c
							.getColumnIndex("vDirectConversationUrl"));
					result.put("url", vDirectConversationUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	/*
	 * public String getPushNotificationUrl ( ) {
	 * 
	 * String url= null;
	 * 
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vPushNotificationUrl,vPushNotificationHrefUrl FROM user WHERE isPrimaryUser = '1' "
	 * ) ; if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); url = c.getString(
	 * c.getColumnIndex( "vPushNotificationUrl" ) ); } c.close(); } c = null;
	 * return url; }
	 */
	public Hashtable<String, Object> getPushNotificationUrl() {

		String url = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vPushNotificationUrl,vPushNotificationHrefUrl FROM user WHERE isPrimaryUser = '1' ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				url = c.getString(c.getColumnIndex("vPushNotificationHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {
					url = c.getString(c.getColumnIndex("vPushNotificationUrl"));

					if (url != null && url.trim().length() > 0) {
						result.put("url", url);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	/*
	 * public String getFriendshipStatusUrl ( String vProfileUrl ) {
	 * 
	 * String vFriendshipStatusUrl = null;
	 * 
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vFriendshipStatusUrl FROM user WHERE vSelfUrl = \"" +
	 * vProfileUrl + "\" " ); if ( c != null ) { if ( c.getCount() > 0 ) {
	 * c.moveToFirst(); vFriendshipStatusUrl = c.getString( c.getColumnIndex(
	 * "vFriendshipStatusUrl" ) ); } c.close(); } c = null; return
	 * vFriendshipStatusUrl; }
	 */

	public Hashtable<String, Object> getFriendshipStatusUrl(String vProfileUrl) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vFriendshipStatusUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vFriendshipStatusUrl,vFriendshipStatusHrefUrl FROM user WHERE ( LENGTH(vSelfUrl) > 0 AND vSelfUrl='"
								+ vProfileUrl
								+ "') OR ( LENGTH(vHrefUrl) > 0 AND vHrefUrl='"
								+ vProfileUrl + "')");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vFriendshipStatusUrl = c.getString(c
						.getColumnIndex("vFriendshipStatusHrefUrl"));
				if (vFriendshipStatusUrl != null
						&& vFriendshipStatusUrl.trim().length() > 0) {
					result.put("url", vFriendshipStatusUrl);
					result.put("isHref", true);
				} else {
					vFriendshipStatusUrl = c.getString(c
							.getColumnIndex("vFriendshipStatusUrl"));
					if (vFriendshipStatusUrl != null
							&& vFriendshipStatusUrl.trim().length() > 0) {
						result.put("url", vFriendshipStatusUrl);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getSportsIdFromCompetition(String vCompetitionId) {

		String vSportsId = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT s.vSportsId AS vSportsId FROM sports s LEFT JOIN sports_competition sc ON sc.vSportsId = s.vSportsId LEFT JOIN competition c ON c.vSportsCompetitionId = sc.vSportsCompetitionId WHERE c.vCompetitionId = \""
								+ vCompetitionId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSportsId = c.getString(c.getColumnIndex("vSportsId"));
			}
			c.close();
		}
		c = null;
		return vSportsId;
	}

	public String getContestUrlFromConversationId(String vConversationId) {

		String vContestUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vContestUrl FROM contests c LEFT JOIN match_conversation_node mcn ON mcn.vContestId = c.vContestId WHERE mcn.vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestUrl = c.getString(c.getColumnIndex("vContestUrl"));
			}
			c.close();
		}
		c = null;
		return vContestUrl;
	}

	public Hashtable<String, Object> getContestUrlFromContestId(
			String vContestId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		String vContestUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vContestUrl,vContestHref FROM contests WHERE vContestId = \""
						+ vContestId + "\"  ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestUrl = c.getString(c.getColumnIndex("vContestHref"));

				if (vContestUrl != null && vContestUrl.trim().length() > 0) {

					result.put("url", vContestUrl);
					result.put("isHref", true);

				} else {
					vContestUrl = c.getString(c.getColumnIndex("vContestUrl"));
					if (vContestUrl != null && vContestUrl.trim().length() > 0) {
						result.put("url", vContestUrl);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}

				}
			}

			c.close();
		}
		c = null;
		return result;
	}

	public String getContestUrlFromContestIdForRefresh(String vContestId) {

		String vContestUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vContestUrl,vContestHref FROM contests WHERE vContestId = \""
						+ vContestId + "\"  ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestUrl = c.getString(c.getColumnIndex("vContestUrl"));

				if (vContestUrl != null && vContestUrl.trim().length() > 0) {

				} else {
					vContestUrl = c.getString(c.getColumnIndex("vContestHref"));

				}
			}

			c.close();
		}
		c = null;
		return vContestUrl;
	}

	public String getContestId(String vContestUrl) {

		String vContestId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vContestId FROM contests WHERE vContestUrl = \""
						+ vContestUrl + "\"  ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestUrl = c.getString(c.getColumnIndex("vContestId"));
			}
			c.close();
		}
		c = null;
		return vContestUrl;
	}

	/*
	 * public Hashtable<String, List<String>> getUserNotificationData () {
	 * 
	 * return PlayupLiveApplication.getDatabaseWrapper().select( " SELECT " +
	 * "vNotificationId, vNotificationUrl, isRead, dTime, vStatus, vSubjectType, vUserSelfUrl, vUserId, vUserType, vDisplayName, "
	 * +
	 * "vAvatarUrl, vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, vConversationId, vConversationUrl, iGapSize, vGapId, vGapUrl "
	 * + "FROM notification " + "ORDER BY isRead ASC, dTime DESC " );
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public Hashtable<String, List<String>> getUserNotificationData() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT "
						+ "vNotificationId, vNotificationUrl,vNotificationHrefUrl, isRead, dTime, vStatus, vSubjectType, vUserSelfUrl,vUserHrefUrl, vUserId, vUserType, vDisplayName, "
						+ "vAvatarUrl, vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, vConversationId, vConversationUrl, vConversationHrefUrl,iGapSize, vGapId, vGapUrl,vGapHrefUrl "
						+ "FROM notification "
						+ "ORDER BY isRead ASC, dTime DESC ");

	}

	public String getContestIdFromConversationId(String vConversationId) {

		String vContestId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vContestId FROM match_conversation_node WHERE vConversationId = \""
						+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestId = c.getString(c.getColumnIndex("vContestId"));
			}
			c.close();
		}
		c = null;
		return vContestId;
	}

	/*
	 * public String getPlayUpFriendsUrl () {
	 * 
	 * String vPlayUpFriendsUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vPlayUpFriendUrl FROM user WHERE isPrimaryUser = '1' " );
	 * 
	 * if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vPlayUpFriendsUrl =
	 * c.getString( c.getColumnIndex( "vPlayUpFriendUrl" ) ); } c.close(); } c =
	 * null; return vPlayUpFriendsUrl; }
	 */
	public Hashtable<String, Object> getPlayUpFriendsUrl() {

		String vPlayUpFriendsUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vPlayUpFriendUrl,vPlayUpFriendHrefUrl FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vPlayUpFriendsUrl = c.getString(c
						.getColumnIndex("vPlayUpFriendHrefUrl"));

				if (vPlayUpFriendsUrl != null
						&& vPlayUpFriendsUrl.trim().length() > 0) {
					result.put("url", vPlayUpFriendsUrl);
					result.put("isHref", false);

				} else {

					vPlayUpFriendsUrl = c.getString(c
							.getColumnIndex("vPlayUpFriendUrl"));

					result.put("url", vPlayUpFriendsUrl);
					result.put("isHref", true);
				}

			}
			c.close();
		}
		c = null;
		return result;
	}

	public Hashtable<String, Object> getPlayUpFriendsUrlForApiCall() {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vPlayUpFriendsUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vPlayUpFriendUrl,vPlayUpFriendHrefUrl FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vPlayUpFriendsUrl = c.getString(c
						.getColumnIndex("vPlayUpFriendHrefUrl"));
				if (vPlayUpFriendsUrl != null
						&& vPlayUpFriendsUrl.trim().length() > 0) {

					result.put("url", vPlayUpFriendsUrl);
					result.put("isHref", true);

				} else {

					vPlayUpFriendsUrl = c.getString(c
							.getColumnIndex("vPlayUpFriendUrl"));
					result.put("url", vPlayUpFriendsUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	/**
	 * Getting the main ( primary ) color for the specific section
	 */
	public String getSectionMainColor(String vUid, String vSelfUrl) {

		String vMainColor = null;
		Cursor c = null;
		if (vUid != null && vUid.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper()
					.selectQuery(
							"SELECT vMainColor FROM color WHERE vUid = '"
									+ vUid + "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vMainColor = c.getString(c.getColumnIndex("vMainColor"));
					if (vMainColor != null && vMainColor.trim().length() > 0) {
						c.close();
						return vMainColor;

					}
				}
				c.close();
			}

		}
		if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vMainColor FROM color WHERE ((LENGTH(vSelfUrl) > 0 "
							+ "AND vSelfUrl = '" + vSelfUrl
							+ "') OR (LENGTH(vHref) > 0 AND vHref = '"
							+ vSelfUrl + "'))");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vMainColor = c.getString(c.getColumnIndex("vMainColor"));
					if (vMainColor != null && vMainColor.trim().length() > 0) {
						c.close();
						return vMainColor;

					}
				}
				c.close();
			}

		}
		return null;
	}

	public String getSectionSecondaryColor(String vUid, String vSelfUrl) {

		String vSecondaryColor = null;
		Cursor c = null;
		if (vUid != null && vUid.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vSecColor FROM color WHERE vUid = '" + vUid + "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vSecondaryColor = c
							.getString(c.getColumnIndex("vSecColor"));
					if (vSecondaryColor != null
							&& vSecondaryColor.trim().length() > 0) {
						c.close();
						return vSecondaryColor;

					}
				}
				c.close();
			}

		}
		if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vSecColor FROM color WHERE "
							+ "((LENGTH(vSelfUrl) > 0 AND vSelfUrl = '"
							+ vSelfUrl
							+ "') OR (LENGTH(vHref) > 0 AND vHref = '"
							+ vSelfUrl + "'))");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vSecondaryColor = c
							.getString(c.getColumnIndex("vSecColor"));
					if (vSecondaryColor != null
							&& vSecondaryColor.trim().length() > 0) {
						c.close();
						return vSecondaryColor;

					}
				}
				c.close();
			}

		}
		return null;
	}

	public String getSectionSecondaryTitleColor(String vUid, String vSelfUrl) {

		String vSecTitleColor = null;
		Cursor c = null;
		if (vUid != null && vUid.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vSecTitleColor FROM color WHERE vUid = '" + vUid
							+ "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vSecTitleColor = c.getString(c
							.getColumnIndex("vSecTitleColor"));
					if (vSecTitleColor != null
							&& vSecTitleColor.trim().length() > 0) {
						c.close();
						return vSecTitleColor;

					}
				}
				c.close();
			}

		}
		if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vSecTitleColor FROM color WHERE "
							+ "((LENGTH(vSelfUrl) > 0 AND vSelfUrl = '"
							+ vSelfUrl
							+ "') OR (LENGTH(vHref) > 0 AND vHref = '"
							+ vSelfUrl + "'))");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vSecTitleColor = c.getString(c
							.getColumnIndex("vSecTitleColor"));
					if (vSecTitleColor != null
							&& vSecTitleColor.trim().length() > 0) {
						c.close();
						return vSecTitleColor;

					}
				}
				c.close();
			}

		}
		return null;
	}

	/**
	 * Getting the title color for the specific section
	 */
	public String getSectionMainTitleColor(String vUid, String vSelfUrl) {
		Cursor c = null;
		String vMainTitleColor = null;
		if (vUid != null && vUid.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vMainTitleColor FROM color WHERE vUid = '" + vUid
							+ "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();

					vMainTitleColor = c.getString(c
							.getColumnIndex("vMainTitleColor"));
					if (vMainTitleColor != null
							&& vMainTitleColor.trim().length() > 0) {
						c.close();

						return vMainTitleColor;

					}
				}

				c.close();

			}
		}

		if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vMainTitleColor FROM color WHERE "
							+ "((LENGTH(vSelfUrl) > 0 AND vSelfUrl = '"
							+ vSelfUrl
							+ "') OR (LENGTH(vHref) > 0 AND vHref = '"
							+ vSelfUrl + "'))");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vMainTitleColor = c.getString(c
							.getColumnIndex("vMainTitleColor"));
					if (vMainTitleColor != null
							&& vMainTitleColor.trim().length() > 0) {
						c.close();

						return vMainTitleColor;
					}

				}
				c.close();
			}
		}

		return null;
	}

	public String getSectionMainColorForLeague(String vCompetitionId) {

		String vMainColor = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT "
								+ "vMainColor FROM color c LEFT JOIN competition comp ON c.vUid = comp.vSectionId  "
								+ "WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vMainColor = c.getString(c.getColumnIndex("vMainColor"));
				if (vMainColor != null && vMainColor.trim().length() > 0) {
					c.close();
					return vMainColor;
				}

			}

		}

		c.close();
		c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vMainColor FROM "
								+ "color c LEFT JOIN competition comp ON ((LENGTH(c.vSelfUrl) > 0 AND c.vSelfUrl = comp.vSectionUrl) OR "
								+ "(LENGTH(c.vHref)> 0 AND c.vHref = comp.vSectionHref)) "
								+ "WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vMainColor = c.getString(c.getColumnIndex("vMainColor"));
				if (vMainColor != null && vMainColor.trim().length() > 0) {
					c.close();
					return vMainColor;

				}

			}

		}

		c.close();
		return null;
	}

	/**
	 * Getting the title color for the specific section
	 */
	public String getSectionMainTitleColorForLeague(String vCompetitionId) {
		Cursor c = null;
		String vMainTitleColor = null;
		try {

			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vMainTitleColor FROM "
									+ "color c LEFT JOIN competition comp ON c.vUid = comp.vSectionId  WHERE "
									+ "comp.vCompetitionId = '"
									+ vCompetitionId + "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vMainTitleColor = c.getString(c
							.getColumnIndex("vMainTitleColor"));
					if (vMainTitleColor != null
							&& vMainTitleColor.trim().length() > 0) {
						c.close();
						return vMainTitleColor;

					}
				}
				c.close();
			}

			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vMainTitleColor FROM color c "
									+ "LEFT JOIN competition comp ON "
									+ "((LENGTH(c.vSelfUrl) > 0 AND c.vSelfUrl = comp.vSectionUrl) OR "
									+ "(LENGTH(c.vHref)> 0 AND c.vHref = comp.vSectionHref)) "
									+ "WHERE comp.vCompetitionId = '"
									+ vCompetitionId + "' ");

			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					vMainTitleColor = c.getString(c
							.getColumnIndex("vMainTitleColor"));

					if (vMainTitleColor != null
							&& vMainTitleColor.trim().length() > 0) {
						c.close();
						return vMainTitleColor;

					}
				}

			}
			return vMainTitleColor;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return vMainTitleColor;

		} finally {
			if (c != null) {
				c.close();

			}
		}
	}

	public String getSectionSecColorForLeague(String vCompetitionId) {

		String vSecColor = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT "
								+ "vSecColor FROM color c LEFT JOIN competition comp ON c.vUid = comp.vSectionId  "
								+ "WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSecColor = c.getString(c.getColumnIndex("vSecColor"));
				if (vSecColor != null && vSecColor.trim().length() > 0) {
					c.close();
					return vSecColor;
				}

			}

		}

		c.close();
		c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vSecColor FROM "
								+ "color c LEFT JOIN competition comp ON ((LENGTH(c.vSelfUrl) > 0 AND c.vSelfUrl = comp.vSectionUrl) OR "
								+ "(LENGTH(c.vHref)> 0 AND c.vHref = comp.vSectionHref)) "
								+ "WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSecColor = c.getString(c.getColumnIndex("vSecColor"));
				if (vSecColor != null && vSecColor.trim().length() > 0) {
					c.close();
					return vSecColor;

				}

			}

		}

		c.close();
		return null;
	}

	/**
	 * Getting the title color for the specific section
	 */
	public String getSectionSecTitleColorForLeague(String vCompetitionId) {

		String vSecTitleColor = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vSecTitleColor FROM "
								+ "color c LEFT JOIN competition comp ON c.vUid = comp.vSectionId  WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSecTitleColor = c
						.getString(c.getColumnIndex("vSecTitleColor"));
				if (vSecTitleColor != null
						&& vSecTitleColor.trim().length() > 0) {
					c.close();
					return vSecTitleColor;

				}
			}

		}

		c.close();
		c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vSecTitleColor FROM color c "
								+ "LEFT JOIN competition comp ON ((LENGTH(c.vSelfUrl) > 0 AND c.vSelfUrl = comp.vSectionUrl) OR "
								+ "(LENGTH(c.vHref)> 0 AND c.vHref = comp.vSectionHref)) "
								+ "WHERE comp.vCompetitionId = '"
								+ vCompetitionId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSecTitleColor = c
						.getString(c.getColumnIndex("vSecTitleColor"));

				if (vSecTitleColor != null
						&& vSecTitleColor.trim().length() > 0) {
					c.close();
					return vSecTitleColor;

				}
			}
			c.close();
		}

		return vSecTitleColor;
	}

	/*
	 * public Hashtable<String, List<String>> getFriendLobbyConversation () {
	 * 
	 * return PlayupLiveApplication.getDatabaseWrapper().select(
	 * "SELECT fc.vConversationId, fc.vConversationName, fc.vAccess, fc.isAccessPermitted, "
	 * + "fc.iTotalPresences, fc.iOrder, fcm.vConversationMessageId, " +
	 * "( SELECT vMessage FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1) AS vMessage,"
	 * +
	 * "( SELECT vDisplayName FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1 ) AS vDisplayName, "
	 * +
	 * "( SELECT vAvatarUrl FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId  ORDER BY vCreatedDate DESC LIMIT 0, 1 ) AS vAvatarUrl , "
	 * +
	 * "( SELECT vCreatedDate FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1) AS vCreatedDate, "
	 * + "fcm.iTotalMessages " + "FROM friendConversationMessage fcm " +
	 * "LEFT JOIN friendConversation fc ON fcm.vConversationId = fc.vConversationId "
	 * +
	 * "LEFT JOIN friendLobbyConversation flc ON fc.vLobbyConversationUrl = flc.vLobbyUrl "
	 * + "LEFT JOIN user u  ON u.vLobbyId = flc.vLobbyId " +
	 * "WHERE u.isPrimaryUser = 1 ORDER BY fc.iOrder ASC " ); }
	 */
	/**
	 * Praveen : modified as per href
	 */
	public Hashtable<String, List<String>> getFriendLobbyConversation() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT fc.vConversationId, "
						+ "fc.vConversationName, fc.vAccess, fc.isAccessPermitted, "
						+ "fc.iTotalPresences, fc.iOrder, fcm.vConversationMessageId, "
						+ "( SELECT vMessage FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1) AS vMessage,"
						+ "( SELECT vDisplayName FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1 ) AS vDisplayName, "
						+ "( SELECT vAvatarUrl FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId  ORDER BY vCreatedDate DESC LIMIT 0, 1 ) AS vAvatarUrl , "
						+ "( SELECT vCreatedDate FROM friendMessage WHERE vConversationMessageId = fcm.vConversationMessageId ORDER BY vCreatedDate DESC LIMIT 0, 1) AS vCreatedDate, "
						+ "fcm.iTotalMessages "
						+ "FROM friendConversationMessage fcm "
						+ "LEFT JOIN friendConversation fc ON fcm.vConversationId = fc.vConversationId "
						+ "LEFT JOIN friendLobbyConversation flc ON ((LENGTH(fc.vLobbyConversationUrl) > 0 AND fc.vLobbyConversationUrl = flc.vLobbyUrl) OR (LENGTH(fc.vLobbyConversationHrefUrl) > 0 AND fc.vLobbyConversationHrefUrl = flc.vLobbyHrefUrl)) "
						+ "LEFT JOIN user u  ON u.vLobbyId = flc.vLobbyId "
						+ "WHERE u.isPrimaryUser = 1 ORDER BY fc.iOrder ASC ");
	}

	public String getUserLobbyUrl() {

		String vLobbyUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vLobbyUrl,vLobbyHrefUrl FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vLobbyUrl = c.getString(c.getColumnIndex("vLobbyHrefUrl"));
				if (vLobbyUrl != null && vLobbyUrl.trim().length() > 0) {

				} else {
					vLobbyUrl = c.getString(c.getColumnIndex("vLobbyUrl"));
				}
			}
			c.close();
		}
		c = null;
		return vLobbyUrl;
	}

	public Hashtable<String, Object> getUserLobbyUrlForApiCall() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vLobbyUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vLobbyUrl,vLobbyHrefUrl FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vLobbyUrl = c.getString(c.getColumnIndex("vLobbyHrefUrl"));
				if (vLobbyUrl != null && vLobbyUrl.trim().length() > 0) {
					result.put("url", vLobbyUrl);
					result.put("isHref", true);

				} else {
					vLobbyUrl = c.getString(c.getColumnIndex("vLobbyUrl"));

					result.put("url", vLobbyUrl);
					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getLobbySubjectId() {

		String vLobbySubjectId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vLobbySubjectId FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vLobbySubjectId = c.getString(c
						.getColumnIndex("vLobbySubjectId"));
			}
			c.close();
		}
		c = null;
		return vLobbySubjectId;
	}

	/*
	 * public String getLobbySubjectUrl () {
	 * 
	 * String vLobbySubjectUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vLobbySubjectUrl FROM user WHERE isPrimaryUser = '1' " );
	 * 
	 * if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vLobbySubjectUrl =
	 * c.getString( c.getColumnIndex( "vLobbySubjectUrl" ) ); } c.close(); } c =
	 * null; return vLobbySubjectUrl; }
	 */

	public Hashtable<String, Object> getLobbySubjectUrl() {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vLobbySubjectUrl,vLobbySubjectHrefUrl FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			try {
				if (c.getCount() > 0) {
					c.moveToFirst();

					String url = c.getString(c
							.getColumnIndex("vLobbySubjectHrefUrl"));
					if (url != null && url.trim().length() > 0) {
						result.put("url", url);
						result.put("isHref", true);

					} else {

						url = c.getString(c.getColumnIndex("vLobbySubjectUrl"));

						if (url != null && url.trim().length() > 0) {
							result.put("url", url);
							result.put("isHref", false);
						} else {
							result.put("url", "");
							result.put("isHref", false);
						}

					}

				}
				c.close();
			} catch (Exception e) {
				Logs.show(e);
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}

				c = null;

			}
		}

		return result;
	}

	public Hashtable<String, Object> getUserLobbyConversationUrl() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vLobbyConversationUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vLobbyConversationUrl,vLobbyConversationHrefUrl FROM friendLobbyConversation flc LEFT JOIN user u ON u.vLobbyId = flc.vLobbyId WHERE u.isPrimaryUser = '1' LIMIT 0, 1 ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vLobbyConversationUrl = c.getString(c
						.getColumnIndex("vLobbyConversationHrefUrl"));

				if (vLobbyConversationUrl != null
						&& vLobbyConversationUrl.trim().length() > 0) {

					result.put("url", vLobbyConversationUrl);
					result.put("isHref", true);

				} else {

					vLobbyConversationUrl = c.getString(c
							.getColumnIndex("vLobbyConversationUrl"));

					result.put("url", vLobbyConversationUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getUserLobbyId() {

		String vLobbyId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vLobbyId FROM user WHERE isPrimaryUser = '1' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vLobbyId = c.getString(c.getColumnIndex("vLobbyId"));
			}
			c.close();
		}
		c = null;
		return vLobbyId;
	}

	/**
	 * Enabling the provider to post on social network
	 **/
	public void setIdentifierEnabledProviderName(final String vProviderName,
			final int isEnabled) {

		ContentValues values = new ContentValues();
		values.put("isEnabled", isEnabled);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "providers", values,
				" vProviderName = \"" + vProviderName + "\" ", null, false,
				true);

	}

	public boolean isPrimaryUser(final String iUserId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  iUserId FROM user WHERE iUserId = \"" + iUserId
						+ "\" AND isPrimaryUser = \"1\" ");
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	public String iIdvalue() {

		// int idValue =
		// PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		// " SELECT  iUserId FROM user WHERE iUserId = \"" + iUserId +
		// "\" AND isPrimaryUser = \"1\" " );
		String idprimaryUserValue = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT iId FROM user WHERE isPrimaryUser = \"1\" ");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				idprimaryUserValue = c.getString(c.getColumnIndex("iId"));
				// idprimaryUserValue = c.getColumnIndex("iId");

			}
			c.close();

		}
		c = null;
		return idprimaryUserValue;

	}

	public String urlReturn() {

		String vUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSelfUrl FROM user WHERE isPrimaryUser = \"1\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vUrl = c.getString(c.getColumnIndex("vSelfUrl"));
			}
			c.close();

		}
		c = null;
		return vUrl;

	}

	public String otherIdvalue(int tmpValue) {

		// int idValue =
		// PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		// " SELECT  iUserId FROM user WHERE iUserId = \"" + iUserId +
		// "\" AND isPrimaryUser = \"1\" " );
		String otherId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT iId FROM user WHERE iId =" + tmpValue);

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				otherId = c.getString(c.getColumnIndex("iId"));
				// idprimaryUserValue = c.getColumnIndex("iId");

			}
			c.close();

		}
		c = null;
		return otherId;

	}

	public String getConversationUrl(String vConversationId) {

		String vConversationUrl = null;
		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vConversationId FROM 	match_conversation_node WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (count == 0) {

			Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					" SELECT vSubjectUrl FROM 	recent WHERE vSubjectId = \""
							+ vConversationId + "\" ");
			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();
					vConversationUrl = c.getString(c
							.getColumnIndex("vSubjectUrl"));
				}
				c.close();
			}
			c = null;

		} else {
			Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					" SELECT vSelfUrl FROM 	match_conversation_node WHERE vConversationId = \""
							+ vConversationId + "\" ");
			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();
					vConversationUrl = c
							.getString(c.getColumnIndex("vSelfUrl"));
				}
				c.close();
			}
			c = null;
		}

		return vConversationUrl;
	}

	public Hashtable<String, Object> getConversationFriendsUrl(
			String vConversationId) {

		String vConversationFriendsUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vFriendsUrl,vFriendsHref FROM match_conversation_node WHERE vConversationId = \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vConversationFriendsUrl = c.getString(c
						.getColumnIndex("vFriendsUrl"));

				if (vConversationFriendsUrl != null
						&& vConversationFriendsUrl.trim().length() > 0) {
					result.put("url", vConversationFriendsUrl);
					result.put("isHref", false);

				} else {

					vConversationFriendsUrl = c.getString(c
							.getColumnIndex("vFriendsHref"));

					result.put("url", vConversationFriendsUrl);
					result.put("isHref", true);
				}

			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getConversationFriendsUrlForRefresh(String vConversationId) {

		String vConversationFriendsUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vFriendsUrl,vFriendsHref FROM match_conversation_node WHERE vConversationId = \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vConversationFriendsUrl = c.getString(c
						.getColumnIndex("vFriendsUrl"));

				if (vConversationFriendsUrl != null
						&& vConversationFriendsUrl.trim().length() > 0) {

				} else {

					vConversationFriendsUrl = c.getString(c
							.getColumnIndex("vFriendsHref"));

				}

			}
			c.close();
		}
		c = null;
		return vConversationFriendsUrl;
	}

	/*
	 * public String getPrivateLobbyFriendsUrl ( String vConversationId ) {
	 * 
	 * 
	 * String vConversationFriendsUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vFriendUrl FROM friendConversation WHERE vConversationId = \"" +
	 * vConversationId + "\" " ); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) {
	 * 
	 * c.moveToFirst(); vConversationFriendsUrl = c.getString( c.getColumnIndex(
	 * "vFriendUrl" ) ); } c.close(); } c = null; return
	 * vConversationFriendsUrl; }
	 */
	public Hashtable<String, Object> getPrivateLobbyFriendsUrl(
			String vConversationId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vFriendUrl,vFriendHrefUrl FROM friendConversation WHERE vConversationId = \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("vFriendHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vFriendUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}

			}
			c.close();
		}
		c = null;
		return result;
	}

	public Hashtable<String, Object> getProfileUrl(String iUserId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		String vSelfUrl = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSelfUrl,vHrefUrl FROM user WHERE iUserId = \""
						+ iUserId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSelfUrl = c.getString(c.getColumnIndex("vHrefUrl"));
				if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {

					result.put("url", vSelfUrl);

					result.put("isHref", true);

				} else {
					vSelfUrl = c.getString(c.getColumnIndex("vSelfUrl"));

					result.put("url", vSelfUrl);

					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getContestIdFromContestLobbyUrl(String vContestLobbyUrl) {

		String vContestId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vContestId FROM "
								+ "contest_lobby WHERE (LENGTH(vContestLobbyUrl) > 0 AND vContestLobbyUrl = \""
								+ vContestLobbyUrl
								+ "\" )"
								+ " OR (LENGTH(vContestLobbyHref) > 0 AND vContestLobbyHref = \""
								+ vContestLobbyUrl + "\" ) ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestId = c.getString(c.getColumnIndex("vContestId"));

			}
			c.close();
		}
		c = null;
		return vContestId;
	}

	public Hashtable<String, Object> getProfileUrlFromFriendId(String vFriendId) {

		String vSelfUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSelfUrl,vHrefUrl FROM user u LEFT JOIN my_friends mf on mf.vProfileId = u.iUserId WHERE mf.vFriendId =  \""
								+ vFriendId + "\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {

					vSelfUrl = c.getString(c.getColumnIndex("vHrefUrl"));
					if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {
						result.put("url", vSelfUrl);
						result.put("isHref", true);

					} else {

						vSelfUrl = c.getString(c.getColumnIndex("vSelfUrl"));
						result.put("url", vSelfUrl);
						result.put("isHref", false);

					}
				}
				c.close();
				c = null;
			} else {

				c.close();
				c = null;
				c = PlayupLiveApplication
						.getDatabaseWrapper()
						.selectQuery(
								" SELECT vSelfUrl,vHrefUrl FROM user u LEFT JOIN my_friends_live mf on mf.vProfileId = u.iUserId WHERE mf.vFriendId =  \""
										+ vFriendId + "\" ");
				if (c != null) {

					vSelfUrl = c.getString(c.getColumnIndex("vHrefUrl"));
					if (vSelfUrl != null && vSelfUrl.trim().length() > 0) {
						result.put("url", vSelfUrl);
						result.put("isHref", true);

					} else {

						vSelfUrl = c.getString(c.getColumnIndex("vSelfUrl"));
						result.put("url", vSelfUrl);
						result.put("isHref", false);

					}
					c.close();
				}
				c = null;

			}
		}

		return result;
	}

	public Hashtable<String, List<String>> getConversationFriendsData(
			String vConversationId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT mf.vFriendId AS vFriendId, "
						+ "vFriendAvatar, isOnline, iStatus,vSourceName,vSourceIconHref, "
						+ "vProfileId FROM recent_invite ri LEFT JOIN my_friends mf ON ri.vFriendId = mf.vFriendId "
						+ "WHERE ri.vConversationId = \"" + vConversationId
						+ "\" ");
	}

	public String getConversationMessagesUrl(String vConversationId) {

		String vConversationMessagesUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSelfUrl,vHrefUrl FROM conversation_message WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vConversationMessagesUrl = c.getString(c
						.getColumnIndex("vHrefUrl"));

				if (vConversationMessagesUrl != null
						&& vConversationMessagesUrl.trim().length() > 0) {

				} else {
					vConversationMessagesUrl = c.getString(c
							.getColumnIndex("vSelfUrl"));
				}
			}
			c.close();
		}
		c = null;
		return vConversationMessagesUrl;
	}

	public Hashtable<String, Object> getConversationMessagesUrlForApiCall(
			String vConversationId) {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vConversationMessagesUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSelfUrl,vHrefUrl FROM conversation_message WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vConversationMessagesUrl = c.getString(c
						.getColumnIndex("vHrefUrl"));

				if (vConversationMessagesUrl != null
						&& vConversationMessagesUrl.trim().length() > 0) {
					result.put("url", vConversationMessagesUrl);
					result.put("isHref", true);
				} else {
					vConversationMessagesUrl = c.getString(c
							.getColumnIndex("vSelfUrl"));
					result.put("url", vConversationMessagesUrl);
					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	/*
	 * public String getLobbyConversationMessagesUrl ( String vConversationId )
	 * {
	 * 
	 * String vConversationMessageUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vConversationMessageUrl " + "FROM friendConversationMessage fcm"
	 * +
	 * " LEFT JOIN friendConversation fc ON fc.vConversationId = fcm.vConversationId "
	 * + "WHERE fc.vConversationId = \"" + vConversationId + "\"  " );
	 * 
	 * if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst();
	 * 
	 * vConversationMessageUrl = c.getString( c.getColumnIndex(
	 * "vConversationMessageUrl" ) ); } c.close(); } c = null; return
	 * vConversationMessageUrl; }
	 */

	/**
	 * Praveeenm : as per the Href
	 */
	public Hashtable<String, Object> getLobbyConversationMessagesUrl(
			String vConversationId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vConversationMessageUrl,vConversationMessageHrefUrl "
								+ "FROM friendConversationMessage fcm"
								+ " LEFT JOIN friendConversation fc ON fc.vConversationId = fcm.vConversationId "
								+ "WHERE fc.vConversationId = \""
								+ vConversationId + "\"  ");
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				String url = c.getString(c
						.getColumnIndex("vConversationMessageHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c
							.getColumnIndex("vConversationMessageUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public Hashtable<String, Object> getFriendconversationUrlFromConversationId(
			String vConversationId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vConversationUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vConversationUrl,vConversationHrefUrl FROM friendConversation WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vConversationUrl = c.getString(c
						.getColumnIndex("vConversationHrefUrl"));

				if (vConversationUrl != null
						&& vConversationUrl.trim().length() > 0) {

					result.put("url", vConversationUrl);
					result.put("isHref", true);

				} else {
					vConversationUrl = c.getString(c
							.getColumnIndex("vConversationUrl"));
					result.put("url", vConversationUrl);
					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getFriendType(String vFriendId) {
		String vFriendType = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vFriendType FROM my_friends WHERE vFriendId = \""
						+ vFriendId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vFriendType = c.getString(c.getColumnIndex("vFriendType"));
			} else {
				c.close();
				c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
						" SELECT vFriendType FROM search_friends WHERE vFriendId = \""
								+ vFriendId + "\" ");
				if (c != null) {

					if (c.getCount() > 0) {
						c.moveToFirst();
						vFriendType = c.getString(c
								.getColumnIndex("vFriendType"));
					}
					c.close();
				}

			}
			c.close();
		}
		c = null;
		return vFriendType;
	}

	public Hashtable<String, List<String>> getRecentInviteFriends(
			String vConversationId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT "
						+ "ifnull ( ( SELECT iStatus FROM recent_invite WHERE vConversationId = \""
						+ vConversationId
						+ "\" AND vFriendId = r.vFriendId ), 0) AS iStatus, "
						+ "r.vFriendId, r.rowid, vFriendName,  vSourceName, vFriendAvatar, vSourceIconHref, isOnline, vFriendUserName "
						+ "FROM recent_invite r "
						+ "LEFT JOIN my_friends mf ON mf.vFriendId = r.vFriendId "
						+ "WHERE r.rowid IN ( SELECT  rowid FROM recent_invite GROUP BY vFriendId ) "
						+ "ORDER BY r.rowid DESC " + "LIMIT 0, 5");

	}

	public Hashtable<String, List<String>> getConversationFriends(
			String vConversationId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT mf.vFriendId AS vFriendId,  mf.vFriendName AS vFriendName, mf.vFriendAvatar AS vFriendAvatar, "
						+ "mf.vSourceName AS vSourceName, mf.vSourceIconHref AS vSourceIconHref,mf.vGapId as vGapId,"
						+ " mf.isOnline AS isOnline, mf.vFriendUserName AS vFriendUserName, mf.isAlreadyInvited AS isAlreadyInvited, "
						+ " ifnull( ri.iStatus, 0 ) AS iStatus FROM my_friends mf LEFT JOIN  recent_invite ri  "
						+ "ON mf.vFriendId = ri.vFriendId AND ri.vConversationId = \""
						+ vConversationId
						+ "\" WHERE mf.isSearch != 1 AND mf.isAlreadyInvited != 3"
						+ " ORDER BY ifnull(mf.vGapId,''),upper(mf.vFriendName)");

	}

	public Hashtable<String, Object> getConversationInvitationUrl(
			String vConversationId) {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vInvitationUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vInvitationUrl,vInvitationHref FROM match_conversation_node WHERE vConversationId =  \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vInvitationUrl = c.getString(c
						.getColumnIndex("vInvitationHref"));

				if (vInvitationUrl != null
						&& vInvitationUrl.trim().length() > 0) {

					result.put("url", vInvitationUrl);
					result.put("isHref", true);

				} else {

					vInvitationUrl = c.getString(c
							.getColumnIndex("vInvitationUrl"));
					result.put("url", vInvitationUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	public Hashtable<String, Object> getPrivateLobbyInvitationUrl(
			String vConversationId) {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vInvitationUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vInvitationUrl,vInvitationHrefUrl FROM friendConversation WHERE vConversationId =  \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vInvitationUrl = c.getString(c
						.getColumnIndex("vInvitationHrefUrl"));

				if (vInvitationUrl != null
						&& vInvitationUrl.trim().length() > 0) {
					result.put("url", vInvitationUrl);
					result.put("isHref", true);

				} else {
					vInvitationUrl = c.getString(c
							.getColumnIndex("vInvitationUrl"));
					result.put("url", vInvitationUrl);
					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	public String getCompetitionNameFromConversation(String vConversationId) {
		String vCompetitionName = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						""
								+ " SELECT c.vCompetitionName AS vCompetitionName FROM contests c LEFT JOIN match_conversation_node mcn "
								+ "ON c. vContestId = mcn.vContestId WHERE  mcn.vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			vCompetitionName = c
					.getString(c.getColumnIndex("vCompetitionName"));
		}

		if (c != null)
			c.close();

		c = null;
		return vCompetitionName;

	}

	public void setRecentInvite(String vFriendId, String vConversationId,
			int status, boolean queryMethod1, boolean inThread) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", vFriendId);
		values.put("vConversationId", vConversationId);
		values.put("iStatus", status);

		if (queryMethod1) {
			JsonUtil json = new JsonUtil();
			json.queryMethod1(Constants.QUERY_UPDATE, null, "recent_invite",
					values, " vConversationId = \"" + vConversationId
							+ "\" AND vFriendId = \"" + vFriendId + "\"  ",
					"vFriendId", true, inThread);

		} else {
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vFriendId FROM recent_invite WHERE vConversationId = \""
									+ vConversationId + "\" AND vFriendId = \""
									+ vFriendId + "\" ");

			if (count > 0) {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE,
						null,
						"recent_invite",
						values,
						" vConversationId = \"" + vConversationId
								+ "\" AND vFriendId = \"" + vFriendId + "\"  ");
			} else {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "recent_invite", values,
						null);
			}

		}

	}

	public void setFrindLobbyconversation(String vLobbyConversationId,
			String vLobbyConversationUrl, String vLobbyConversationHrefUrl,
			int iTotalCount, String vLobbyId, String vLobbyUrl,
			String vLobbyHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vLobbyConversationId", vLobbyConversationId);
		values.put("vLobbyConversationUrl", vLobbyConversationUrl);
		values.put("iTotalCount", iTotalCount);
		values.put("vLobbyId", vLobbyId);
		values.put("vLobbyUrl", vLobbyUrl);

		values.put("vLobbyConversationHrefUrl", vLobbyConversationHrefUrl);
		values.put("vLobbyHrefUrl", vLobbyHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vLobbyConversationId FROM friendLobbyConversation WHERE vLobbyConversationId = \""
								+ vLobbyConversationId + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"friendLobbyConversation",
					values,
					" vLobbyConversationId = \"" + vLobbyConversationId
							+ "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "friendLobbyConversation",
					values, null);
		}

	}

	/*
	 * public void setLobbySubject ( String vLobbySubjectId, String
	 * vLobbySubjectUrl ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vLobbySubjectId", vLobbySubjectId ); values.put( "vLobbySubjectUrl",
	 * vLobbySubjectUrl );
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user", values, " isPrimaryUser = '1'  " );
	 * 
	 * }
	 */

	// PRaveen : Changedas per the href element
	public void setLobbySubject(String vLobbySubjectId,
			String vLobbySubjectUrl, String vLobbySubjectHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vLobbySubjectId", vLobbySubjectId);
		values.put("vLobbySubjectUrl", vLobbySubjectUrl);

		values.put("vLobbySubjectHrefUrl", vLobbySubjectHrefUrl);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" isPrimaryUser = '1'  ");

	}

	/*
	 * public void setFriendConversation ( String vConversationId, String
	 * vConversationUrl, String vConversationName, String vLobbyConversationId,
	 * String vLobbyConversationUrl, String vInvitationUrl, String vAccess,
	 * boolean isAccessPermitted, String vPresenceUrl, int iTotalPresences,
	 * String vFriendId, String vFriendUrl, String vSubjectId, String
	 * vSubjectUrl, int iOrder,boolean editSubject ,boolean editName ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vConversationId", vConversationId ); values.put( "vConversationUrl",
	 * vConversationUrl ); values.put( "vConversationName", vConversationName );
	 * values.put( "vLobbyConversationId", vLobbyConversationId ); values.put(
	 * "vLobbyConversationUrl", vLobbyConversationUrl ); values.put(
	 * "vInvitationUrl", vInvitationUrl ); values.put( "vAccess", vAccess );
	 * values.put( "isAccessPermitted", isAccessPermitted ); values.put(
	 * "vPresenceUrl", vPresenceUrl ); values.put( "iTotalPresences",
	 * iTotalPresences ); values.put( "vFriendId", vFriendId ); values.put(
	 * "vFriendUrl", vFriendUrl ); values.put( "vSubjectId", vSubjectId );
	 * values.put( "vSubjectUrl", vSubjectUrl );
	 * 
	 * values.put( "editSubject", editSubject ); values.put( "editName",
	 * editName );
	 * 
	 * if(iOrder >= 0) values.put( "iOrder", iOrder );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vConversationId FROM friendConversation WHERE vConversationId = \""
	 * + vConversationId + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "friendConversation", values,
	 * " vConversationId = \"" + vConversationId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "friendConversation", values, null ); }
	 * 
	 * }
	 */
	/**
	 * Praveen : modified as per the href
	 */

	public void setFriendConversation(String vConversationId,
			String vConversationUrl, String vConversationHrefUrl,
			String vConversationName, String vLobbyConversationId,
			String vLobbyConversationUrl, String vLobbyConversationHrefUrl,
			String vInvitationUrl, String vInvitationHrefUrl, String vAccess,
			boolean isAccessPermitted, String vPresenceUrl,
			String vPresenceHrefUrl, int iTotalPresences, String vFriendId,
			String vFriendUrl, String vFriendHrefUrl, String vSubjectId,
			String vSubjectUrl, String vSubjectHrefUrl, int iOrder,
			boolean editSubject, boolean editName) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put("vConversationId", vConversationId);
		values.put("vConversationUrl", vConversationUrl);
		values.put("vConversationName", vConversationName);
		values.put("vLobbyConversationId", vLobbyConversationId);
		values.put("vLobbyConversationUrl", vLobbyConversationUrl);
		values.put("vInvitationUrl", vInvitationUrl);
		values.put("vAccess", vAccess);
		values.put("isAccessPermitted", isAccessPermitted);
		values.put("vPresenceUrl", vPresenceUrl);
		values.put("iTotalPresences", iTotalPresences);
		values.put("vFriendId", vFriendId);
		values.put("vFriendUrl", vFriendUrl);
		values.put("vSubjectId", vSubjectId);
		values.put("vSubjectUrl", vSubjectUrl);

		values.put("editSubject", editSubject);
		values.put("editName", editName);

		values.put("vConversationHrefUrl", vConversationHrefUrl);
		values.put("vLobbyConversationHrefUrl", vLobbyConversationHrefUrl);
		values.put("vInvitationHrefUrl", vInvitationHrefUrl);
		values.put("vPresenceHrefUrl", vPresenceHrefUrl);
		values.put("vFriendHrefUrl", vFriendHrefUrl);
		values.put("vSubjectHrefUrl", vSubjectHrefUrl);

		if (iOrder >= 0)
			values.put("iOrder", iOrder);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vConversationId FROM friendConversation WHERE vConversationId = \""
						+ vConversationId + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "friendConversation", values,
					" vConversationId = \"" + vConversationId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "friendConversation", values,
					null);
		}

	}

	public void setFriendConversationOrder(int iOrder) {
		ContentValues values = new ContentValues();
		values.put("iOrder", iOrder);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "friendConversation", values,
				null);

	}

	/*
	 * public void setFriendConversationMessage ( String vConversationMessageId,
	 * String vConversationMessageUrl, String vAdditionUrl, String vMarkerUrl,
	 * String vConversationId, String vConversationUrl, int iTotalMessages, int
	 * iVersion,boolean isConversationUrlHref ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vConversationMessageId", vConversationMessageId ); values.put(
	 * "vConversationMessageUrl", vConversationMessageUrl ); values.put(
	 * "vAdditionUrl", vAdditionUrl ); values.put( "vMarkerUrl", vMarkerUrl );
	 * values.put( "vConversationId", vConversationId );
	 * if(isConversationUrlHref){ values.put( "vConversationHrefUrl",
	 * vConversationUrl ); }else{ values.put( "vConversationUrl",
	 * vConversationUrl ); } values.put( "iTotalMessages", iTotalMessages );
	 * values.put( "iVersion", iVersion );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vConversationMessageId FROM friendConversationMessage WHERE vConversationMessageId = \""
	 * + vConversationMessageId + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "friendConversationMessage", values,
	 * " vConversationMessageId = \"" + vConversationMessageId + "\"  " ); }
	 * else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "friendConversationMessage", values, null
	 * ); }
	 * 
	 * }
	 */

	/**
	 * Praveen: modified as per the href element
	 * 
	 */
	public void setFriendConversationMessage(String vConversationMessageId,
			String vConversationMessageUrl, String vConversationMessageHrefUrl,
			String vAdditionUrl, String vAdditionHrefUrl, String vMarkerUrl,
			String vMarkerHrefUrl, String vConversationId,
			String vConversationUrl, int iTotalMessages, int iVersion,
			boolean isConversationUrlHref) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put("vConversationMessageId", vConversationMessageId);
		values.put("vConversationMessageUrl", vConversationMessageUrl);
		values.put("vAdditionUrl", vAdditionUrl);
		values.put("vMarkerUrl", vMarkerUrl);
		values.put("vConversationId", vConversationId);

		values.put("iTotalMessages", iTotalMessages);
		values.put("iVersion", iVersion);

		values.put("vConversationMessageHrefUrl", vConversationMessageHrefUrl);
		values.put("vAdditionHrefUrl", vAdditionHrefUrl);
		values.put("vMarkerHrefUrl", vMarkerHrefUrl);
		if (isConversationUrlHref)
			values.put("vConversationHrefUrl", vConversationUrl);
		else
			values.put("vConversationUrl", vConversationUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vConversationMessageId FROM friendConversationMessage WHERE vConversationMessageId = \""
								+ vConversationMessageId + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"friendConversationMessage",
					values,
					" vConversationMessageId = \"" + vConversationMessageId
							+ "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "friendConversationMessage",
					values, null);
		}
	}

	/*
	 * public void setFriendMessage ( String vMessageId, String vMessageUrl,
	 * String vMessage, String vSelfUrl, String vUserId, String vDisplayName,
	 * String vAvatarUrl, String vConversationMessageId, String
	 * vConversationMessageUrl, String vCreatedDate ,String vSubjectId,String
	 * vSubjectUrl,String vSubjectTitle ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vMessageId",
	 * vMessageId ); values.put( "vMessageUrl", vMessageUrl ); values.put(
	 * "vMessage", vMessage ); values.put( "vSelfUrl", vSelfUrl ); values.put(
	 * "vUserId", vUserId ); values.put( "vDisplayName", vDisplayName );
	 * values.put( "vAvatarUrl", vAvatarUrl ); values.put(
	 * "vConversationMessageId", vConversationMessageId ); values.put(
	 * "vConversationMessageUrl", vConversationMessageUrl ); values.put(
	 * "vCreatedDate", vCreatedDate );
	 * 
	 * values.put( "vSubjectId", vSubjectId ); values.put( "vSubjectUrl",
	 * vSubjectUrl ); values.put( "vSubjectTitle", vSubjectTitle );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vMessageId FROM friendMessage WHERE vMessageId = \"" +
	 * vMessageId + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "friendMessage", values, " vMessageId = \""
	 * + vMessageId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "friendMessage", values, null ); }
	 * 
	 * }
	 */

	public void setFriendMessage(String vMessageId, String vMessageUrl,
			String vMessageHrefUrl, String vMessage, String vSelfUrl,
			String vSelfHrefUrl, String vUserId, String vDisplayName,
			String vAvatarUrl, String vConversationMessageId,
			String vConversationMessageUrl, String vConversationMessageHrefUrl,
			String vCreatedDate, String vSubjectId, String vSubjectUrl,
			String subjectHrefUrl, String vSubjectTitle) {

		ContentValues values = new ContentValues();
		values.put("vMessageId", vMessageId);
		values.put("vMessageUrl", vMessageUrl);
		values.put("vMessage", vMessage);
		values.put("vSelfUrl", vSelfUrl);
		values.put("vUserId", vUserId);
		values.put("vDisplayName", vDisplayName);
		values.put("vAvatarUrl", vAvatarUrl);
		values.put("vConversationMessageId", vConversationMessageId);
		values.put("vConversationMessageUrl", vConversationMessageUrl);
		values.put("vCreatedDate", vCreatedDate);

		values.put("vSubjectId", vSubjectId);
		values.put("vSubjectUrl", vSubjectUrl);
		values.put("vSubjectTitle", vSubjectTitle);

		values.put("vMessageHrefUrl", vMessageHrefUrl);
		values.put("vHrefUrl", vSelfHrefUrl);
		values.put("vConversationMessageHrefUrl", vConversationMessageHrefUrl);
		values.put("subjectHrefUrl", subjectHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vMessageId FROM friendMessage WHERE vMessageId = \""
						+ vMessageId + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "friendMessage", values,
					" vMessageId = \"" + vMessageId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"friendMessage", values, null);
		}

	}

	public void setFriendMessageGap(String vGapId, int iGapSize,
			String vContentUrl, String vContentHrefUrl,
			String vConversationMessageId, String vConversationMessageUrl,
			String vConversationMessageHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vGapId", vGapId);
		values.put("iGapSize", iGapSize);
		values.put("vContentUrl", vContentUrl);
		values.put("vConversationMessageId", vConversationMessageId);
		values.put("vConversationMessageUrl", vConversationMessageUrl);

		values.put("vContentUrlHref", vContentHrefUrl);
		values.put("vConversationMessageHrefUrl", vConversationMessageHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vGapId FROM friendMessage WHERE vGapId = \"" + vGapId
						+ "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "friendMessage", values,
					" vGapId = \"" + vGapId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"friendMessage", values, null);
		}

	}

	public void setSearchUrl(String vFriendSearchUrl) {

		ContentValues values = new ContentValues();
		values.put("vFriendSearchUrl", vFriendSearchUrl);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" isPrimaryUser = '1' ");

	}

	public String getConversationName(String vConversationId) {
		String vConversationName = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vConversationName FROM match_conversation_node WHERE vConversationId =  \""
								+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vConversationName = c.getString(c
						.getColumnIndex("vConversationName"));
			}
			c.close();
		}
		c = null;
		return vConversationName;
	}

	public String getConversationIdFromPrivateLobbyGapId(String vGapId) {
		String vConversationId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT fcm.vConversationId "
								+ "FROM friendConversationMessage fcm "
								+ "LEFT JOIN  friendMessage fm ON fm.vConversationMessageId = fcm.vConversationMessageId "
								+ "WHERE fm.vGapId = \"" + vGapId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vConversationId = c.getString(c
						.getColumnIndex("vConversationId"));
			}
			c.close();
		}
		c = null;
		return vConversationId;
	}

	public String getConversationUrlFromPrivateLobbyGapId(String vGapId) {
		String vConversationUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT fcm.vConversationUrl,fcm.vConversationHrefUrl "
								+ "FROM friendConversationMessage fcm "
								+ "LEFT JOIN  friendMessage fm ON fm.vConversationMessageId = fcm.vConversationMessageId "
								+ "WHERE fm.vGapId = \"" + vGapId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vConversationUrl = c.getString(c
						.getColumnIndex("vConversationUrl"));
				if (vConversationUrl != null
						&& vConversationUrl.trim().length() > 0) {

				} else {
					vConversationUrl = c.getString(c
							.getColumnIndex("vConversationHrefUrl"));
				}
			}
			c.close();
		}
		c = null;
		return vConversationUrl;
	}

	public String getPrivateLobbyName(String vConversationId) {
		String vConversationName = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vConversationName FROM friendConversation WHERE vConversationId =  \""
						+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vConversationName = c.getString(c
						.getColumnIndex("vConversationName"));
			}
			c.close();
		}
		c = null;
		return vConversationName;

	}

	public Hashtable<String, Object> getAppInvitationUrl(String vFriendId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		String vAppInvitationUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vAppInvitationUrl,vAppInvitationHrefUrl FROM my_friends WHERE vFriendId = \""
								+ vFriendId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vAppInvitationUrl = c.getString(c
						.getColumnIndex("vAppInvitationHrefUrl"));

				if (vAppInvitationUrl != null
						&& vAppInvitationUrl.trim().length() > 0) {

					result.put("url", vAppInvitationUrl);
					result.put("isHref", true);

				} else {

					vAppInvitationUrl = c.getString(c
							.getColumnIndex("vAppInvitationUrl"));
					result.put("url", vAppInvitationUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	/**
	 * @return the play up name of the user
	 */
	public String getName() {

		Cursor c = null;

		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vName FROM user WHERE isPrimaryUser = \"1\" ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String userName = c.getString(c.getColumnIndex("vName"));
				c.close();
				c = null;
				return userName;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}

	}

	/**
	 * getting the cache time for messages
	 */
	/*
	 * public int getCacheTimeMessage ( final String vConversationId ) {
	 * 
	 * int cacheTime = -1; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT iCacheTime FROM conversation_message WHERE vConversationId = \""
	 * + vConversationId + "\" " ); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); cacheTime = Integer.parseInt(
	 * c.getString( c.getColumnIndex( "iCacheTime" ) ) ); } c.close(); } return
	 * cacheTime; }
	 *//**
	 * getting the cache time for friends
	 */
	/*
	 * public int getCacheTimeFriends ( final String vConversationId ) {
	 * 
	 * int cacheTime = -1; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT iCacheTime FROM conversation_friends WHERE vConversationId = \""
	 * + vConversationId + "\" " ); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); cacheTime = Integer.parseInt(
	 * c.getString( c.getColumnIndex( "iCacheTime" ) ) );
	 * 
	 * } c.close(); } return cacheTime; }
	 *//**
	 * getting the cache time for messages
	 */
	/*
	 * public int getCacheTimeRound ( final String vRoundId ) {
	 * 
	 * int cacheTime = -1; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT iCacheTime FROM rounds WHERE vRoundId = \"" + vRoundId + "\" "
	 * ); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); cacheTime = Integer.parseInt(
	 * c.getString( c.getColumnIndex( "iCacheTime" ) ) ); } c.close(); } return
	 * cacheTime; }
	 *//**
	 * getting the cache time for messages
	 */
	/*
	 * public int getCacheTimeSportsLive ( final String vSportsId ) {
	 * 
	 * int cacheTime = -1; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT iCacheTime FROM sports_live WHERE vSportsId = \"" + vSportsId +
	 * "\" " ); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); cacheTime = Integer.parseInt(
	 * c.getString( c.getColumnIndex( "iCacheTime" ) ) ); } c.close(); } return
	 * cacheTime; }
	 */

	/**
	 * getting user id on basis of self url
	 */
	public String getUserIdFromSelfUrl(final String vSelfUrl) {

		String vUserId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT iUserId FROM user WHERE  ( LENGTH(vSelfUrl)> 0 AND vSelfUrl = \""
						+ vSelfUrl
						+ "\" ) OR ( LENGTH(vHrefUrl)> 0 AND vHrefUrl = \""
						+ vSelfUrl + "\" )");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vUserId = c.getString(c.getColumnIndex("iUserId"));
			}
			c.close();
		}
		c = null;
		return vUserId;

	}

	/**
	 * @return gives the url of the user's avatar.
	 */
	public String getUserAvatarUrl() {

		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vUserAvatarUrl FROM user WHERE isPrimaryUser = \"1\" ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("vUserAvatarUrl"));
				c.close();
				return url;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * @return gives the url of the user's avatar.
	 */
	public String getUserName() {

		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vUserName FROM user WHERE isPrimaryUser = \"1\" ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("vUserName"));
				c.close();
				return url;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * @returns the contests data for the specific competition
	 */
	public Hashtable<String, List<String>> getContests(String vCompetitionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT c.vContestId AS vContestId,  c.dStartTime AS dStartTime,  c.dEndTime AS dEndTime, c.vTitle AS vTitle,  c.vShortTitle AS vShortTitle ,  c.iTotal1 AS iTotal1,  c.iTotal2 AS iTotal2,  (  SELECT  vShortName FROM teams WHERE vTeamId = c.vHomeTeamId ) AS vTeamName1,  (  SELECT  vShortName FROM teams WHERE vTeamId = c.vAwayTeamId ) AS vTeamName2 FROM contests c LEFT JOIN rounds r ON r.vRoundContestId = c.vRoundContestId LEFT JOIN competition_current_round ccr ON ccr.vCompetitionCurrentRoundId = vCompetitionRoundId WHERE ccr.vCompetitionId = \""
						+ vCompetitionId + "\" ");
	}

	/**
	 * @returns the contests data for the specific competition
	 */
	public Hashtable<String, List<String>> getContestsFromRoundId(
			String vRoundId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vSportType,vContestId,dScheduledStartTime,vStadiumName, "
						+ "dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2"
						+ ",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates, vLastEventName, vShortMessage, vLongMessage, iActive1,iActive2, "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName, "
						+ "(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl,"
						+ "(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl   "
						+ "FROM contests c "
						+ "LEFT JOIN rounds r ON r.vRoundContestId = c.vRoundContestId "
						+ "WHERE r.vRoundId = \""
						+ vRoundId
						+ "\"   ORDER BY ifnull(c.dScheduledStartTime, '2100-01-01T00:00:00Z') ASC,  ifnull(c.dStartTime,'2100-01-01T00:00:00Z') ASC ");
	}

	public Hashtable<String, List<String>> getTeamScheduleData(
			String vTeamScheduleId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vSportType,c.vContestId AS vContestId,dScheduledStartTime,vStadiumName, "
						+ "dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,c.vSummary AS vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2"
						+ ",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates, vLastEventName, vShortMessage, vLongMessage, iActive1,iActive2, "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeDisplayName, "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayDisplayName, "
						+ "( SELECT vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl,"
						+ "( SELECT vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl "
						+ "FROM contests c "
						+ " LEFT JOIN team_schedule_contests tsc on tsc.vContestId = c.vContestId LEFT JOIN team_schedule ts on ts.vTeamScheduleId = tsc.vTeamScheduleId "
						+ " where  ts.vTeamScheduleId= \""
						+ vTeamScheduleId
						+ "\" order by tsc.iOrder ASC ");
	}

	/**
	 * @return the sports name for sports id
	 */
	public String getSportsName(final String vSportsId) {

		String vName = "0";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vName FROM sports WHERE vSportsId = \"" + vSportsId
						+ "\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vName = c.getString(c.getColumnIndex("vName"));
			}
			c.close();
		}
		c = null;
		return vName;
	}

	/**
	 * @return the number of live matches for sports id
	 */
	public int getLiveMatches(final String vSportsId) {

		int liveMatches = 0;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT c.iLiveNum AS iLiveNum FROM  competition c LEFT JOIN sports_competition sc  ON sc.vSportsCompetitionId = c.vSportsCompetitionId WHERE vSportsId = \""
								+ vSportsId + "\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				int len = c.getCount();
				for (int i = 0; i < len; i++) {
					c.moveToPosition(i);
					liveMatches = liveMatches
							+ Integer.parseInt(c.getString(c
									.getColumnIndex("iLiveNum")));
				}
			}
			c.close();
		}

		c = null;
		return liveMatches;
	}

	public int getMyLeaguesLive() {

		int liveMatches = 0;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT iLiveNum AS iLiveNum FROM  competition WHERE isFavourite = 1");
		if (c != null) {
			if (c.getCount() > 0) {
				int len = c.getCount();
				for (int i = 0; i < len; i++) {
					c.moveToPosition(i);
					liveMatches = liveMatches
							+ Integer.parseInt(c.getString(c
									.getColumnIndex("iLiveNum")));
				}
			}
			c.close();
		}

		c = null;
		return liveMatches;
	}

	public Hashtable<String, List<String>> getRecentData(String userId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT r.vRecentId, r.vRecentName, r.vSubjectTitle,  r.vSubjectId, r.vSubjectUrl,r.vSubjectHref, r.iAccess, r.iAccessPermitted, r.iUnRead ,ur.vUserRecentId FROM recent r,user_recent ur WHERE ur.vUserRecentId=r.vUserRecentId and ur.vUserId=\""
						+ userId
						+ "\" ORDER BY isLastViewed DESC, r.rowid ASC LIMIT 0, 5 ");
	}

	/**
	 * @return the number of my live matches for sports id
	 */
	public int getMyLiveMatches() {

		int liveMatches = 0;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT c.iLiveNum AS iLiveNum FROM  competition c WHERE isFavourite = 1");
		if (c != null) {
			if (c.getCount() > 0) {
				int len = c.getCount();
				for (int i = 0; i < len; i++) {
					c.moveToPosition(i);
					liveMatches = liveMatches
							+ Integer.parseInt(c.getString(c
									.getColumnIndex("iLiveNum")));
				}
			}
			c.close();
		}
		c = null;
		return liveMatches;
	}

	/**
	 * Getting the sports competition url from the database for a specific sport
	 */
	public Hashtable<String, Object> getLeagueUrlFromSportsId(
			final String vSportsId) {

		Hashtable<String, Object> vSportsCompetitionUrl = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSportsCompetitionUrl,vSportsCompetitionHref FROM sports_competition WHERE vSportsId = \""
								+ vSportsId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				String vUrl = c.getString(c
						.getColumnIndex("vSportsCompetitionHref"));
				if (vUrl != null && vUrl.trim().length() > 0) {
					vSportsCompetitionUrl.put("url", vUrl);
					vSportsCompetitionUrl.put("isHref", true);

				} else {

					vUrl = c.getString(c
							.getColumnIndex("vSportsCompetitionUrl"));
					vSportsCompetitionUrl.put("url", vUrl);
					vSportsCompetitionUrl.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;

		return vSportsCompetitionUrl;
	}

	public String getLeagueUrlFromSportsIdForRefresh(final String vSportsId) {

		String vUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSportsCompetitionUrl,vSportsCompetitionHref FROM sports_competition WHERE vSportsId = \""
								+ vSportsId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vUrl = c.getString(c.getColumnIndex("vSportsCompetitionHref"));
				if (vUrl != null && vUrl.trim().length() > 0) {

				} else {

					vUrl = c.getString(c
							.getColumnIndex("vSportsCompetitionUrl"));

				}
			}
			c.close();
		}
		c = null;

		return vUrl;
	}

	/**
	 * Getting the current round url for the competition
	 */
	public Hashtable<String, Object> getRoundUrl(final String vRoundId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vRoundUrl,vRoundHref FROM rounds WHERE vRoundId = \""
						+ vRoundId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				String vRoundUrl = c.getString(c.getColumnIndex("vRoundHref"));
				if (vRoundUrl != null && vRoundUrl.trim().length() > 0) {
					result.put("url", vRoundUrl);
					result.put("isHref", true);

				} else {
					vRoundUrl = c.getString(c.getColumnIndex("vRoundUrl"));

					result.put("url", vRoundUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}

		c = null;
		return result;
	}

	public String getRoundUrlForRefresh(final String vRoundId) {

		String vRoundUrl = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vRoundUrl,vRoundHref FROM rounds WHERE vRoundId = \""
						+ vRoundId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vRoundUrl = c.getString(c.getColumnIndex("vRoundHref"));
				if (vRoundUrl != null && vRoundUrl.trim().length() > 0) {

				} else {
					vRoundUrl = c.getString(c.getColumnIndex("vRoundUrl"));

				}
			}
			c.close();
		}

		c = null;
		return vRoundUrl;
	}

	/**
	 * Getting the current round id for the competition
	 */
	public String getCurrentRoundId(final String vCompetitionId) {

		String vCompetitionCurrentRoundId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vCompetitionCurrentRoundId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vCompetitionCurrentRoundId = c.getString(c
						.getColumnIndex("vCompetitionCurrentRoundId"));
			}
			c.close();
		}

		c = null;
		return vCompetitionCurrentRoundId;
	}

	public String getCurrentSportsName(String vCompetitionId) {
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT s.vName AS vName FROM competition c LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId LEFT JOIN sports s ON s.vSportsId = sc.vSportsId WHERE c.vCompetitionId = \""
								+ vCompetitionId + "\"  ");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			String vName = c.getString(c.getColumnIndex("vName"));
			c.close();
			c = null;

			return vName;
		}
		c = null;
		return null;
	}

	public void removeConversationFriends(String vConversationId,
			boolean queryMethod1) {

		String vConversationFriendId = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vConversationFriendId FROM conversation_friends WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vConversationFriendId = c.getString(c
						.getColumnIndex("vConversationFriendId"));
			}
			c.close();
			c = null;
		}

		if (vConversationFriendId != null) {
			ContentValues values = new ContentValues();
			values.put("vConversationFriendId", "");

			if (queryMethod1) {
				JsonUtil json = new JsonUtil();
				json.queryMethod1(Constants.QUERY_UPDATE, null, "my_friends",
						values, " vConversationFriendId = \""
								+ vConversationFriendId + "\" ", null, false,
						false);

			} else {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE,
						null,
						"my_friends",
						values,
						" vConversationFriendId = \"" + vConversationFriendId
								+ "\" ");

			}

		}
	}

	public Hashtable<String, Object> getRoundcontestUrl(
			final String vRoundContestId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vRoundContestUrl,vRoundContestHref FROM round_contest WHERE vRoundContestId = \""
								+ vRoundContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				String vRoundContestUrl = c.getString(c
						.getColumnIndex("vRoundContestHref"));

				if (vRoundContestUrl != null
						&& vRoundContestUrl.trim().length() > 0) {
					result.put("url", vRoundContestUrl);
					result.put("isHref", true);

				} else {
					vRoundContestUrl = c.getString(c
							.getColumnIndex("vRoundContestUrl"));
					result.put("url", vRoundContestUrl);
					result.put("isHref", false);
				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public void deletePrivateLobbyGap(String vGapId) {

		JsonUtil json = new JsonUtil();
		json.json_method(null, -100, false, Constants.QUERY_DELETE, null,
				"friendMessage", null, "vGapId = \"" + vGapId + "\" ");

	}

	/**
	 * getting the contest lobby url from the contest id
	 */
	public Hashtable<String, Object> getContestLobbyUrl(final String vContestId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		String vContestLobbyUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vContestLobbyUrl,"
								+ " vContestLobbyHref FROM contest_lobby WHERE vContestId = \""
								+ vContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vContestLobbyUrl = c.getString(c
						.getColumnIndex("vContestLobbyHref"));
				if (vContestLobbyUrl != null
						&& vContestLobbyUrl.trim().length() > 0) {

					result.put("url", vContestLobbyUrl);
					result.put("isHref", true);

				} else {

					vContestLobbyUrl = c.getString(c
							.getColumnIndex("vContestLobbyUrl"));

					result.put("url", vContestLobbyUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public String getContestLobbyUrlForRefresh(final String vContestId) {

		String vContestLobbyUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vContestLobbyUrl,vContestLobbyHref FROM contest_lobby WHERE vContestId = \""
								+ vContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vContestLobbyUrl = c.getString(c
						.getColumnIndex("vContestLobbyUrl"));
				if (vContestLobbyUrl != null
						&& vContestLobbyUrl.trim().length() > 0) {

				} else {

					vContestLobbyUrl = c.getString(c
							.getColumnIndex("vContestLobbyHref"));

				}
			}
			c.close();
		}
		c = null;
		return vContestLobbyUrl;
	}

	/**
	 * returns the sports live url for the sports id
	 */
	public Hashtable<String, Object> getSportsLiveUrl(final String vSportsId) {

		Hashtable<String, Object> vSportsLiveUrl = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSportsLiveUrl,vSportsLiveHref FROM sports_live	 WHERE vSportsId = \""
						+ vSportsId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				String vUrl = c.getString(c.getColumnIndex("vSportsLiveHref"));

				if (vUrl != null && vUrl.trim().length() > 0) {

					vSportsLiveUrl.put("url", vUrl);

					vSportsLiveUrl.put("isHref", true);
				} else {
					vUrl = c.getString(c.getColumnIndex("vSportsLiveUrl"));

					vSportsLiveUrl.put("url", vUrl);

					vSportsLiveUrl.put("isHref", false);
				}

			}
			c.close();
		}
		c = null;
		return vSportsLiveUrl;
	}

	public String getSportsLiveUrlForRefresh(final String vSportsId) {

		String vSportsLiveUrl = "";

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSportsLiveUrl,vSportsLiveHref FROM sports_live	 WHERE vSportsId = \""
						+ vSportsId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vSportsLiveUrl = c.getString(c
						.getColumnIndex("vSportsLiveHref"));

				if (vSportsLiveUrl != null
						&& vSportsLiveUrl.trim().length() > 0) {

					return vSportsLiveUrl;
				} else {
					vSportsLiveUrl = c.getString(c
							.getColumnIndex("vSportsLiveUrl"));
					return vSportsLiveUrl;

				}

			}
			c.close();
		}
		c = null;
		return vSportsLiveUrl;
	}

	/*
	 * public int getContestLobbyConversationCacheTime ( String vContestId ) {
	 * 
	 * int cacheTime = -1; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT iCacheTime FROM contest_lobby WHERE vContestId = \"" +
	 * vContestId + "\" " );
	 * 
	 * if( c != null ) {
	 * 
	 * if ( c.getCount() > 0) {
	 * 
	 * c.moveToFirst(); cacheTime = c.getInt( c.getColumnIndex( "iCacheTime" )
	 * ); } c.close(); }
	 * 
	 * return cacheTime;
	 * 
	 * }
	 */

	/**
	 * returns the sports live url for the sports id
	 */
	public Hashtable<String, List<String>> getSportsLiveMatches(
			final String vSportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT cl.vCompetitionLiveUrl AS vCompetitionLiveUrl , cl.vCompetitionLiveId AS vCompetitionLiveId FROM competition_live cl LEFT JOIN competition c ON c.vCompetitionId = cl.vCompetitionId  LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId  WHERE c.iLiveNum > 0  AND sc.vSportsId = \""
						+ vSportsId + "\" ");
	}

	public String getRoundContestId(String vRoundId) {

		String vRoundContestId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT r.vRoundContestId AS vRoundContestId FROM round_contest rc LEFT JOIN rounds r ON r.vRoundContestId = rc.vRoundContestId WHERE vRoundId = \""
								+ vRoundId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vRoundContestId = c.getString(c
						.getColumnIndex("vRoundContestId"));
			}
			c.close();
		}

		c = null;
		return vRoundContestId;
	}

	public String getRoundContestUrl(String vRoundId) {

		String vRoundContestUrl = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT rc.vRoundContestUrl AS vRoundContestUrl FROM round_contest rc LEFT JOIN rounds r ON r.vRoundContestId = rc.vRoundContestId WHERE vRoundId = \""
								+ vRoundId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vRoundContestUrl = c.getString(c
						.getColumnIndex("vRoundContestUrl"));
			}
			c.close();
		}

		c = null;
		return vRoundContestUrl;
	}

	public Hashtable<String, List<String>> getWeekData(String vCompetitionId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vRoundId, vPeriod, vName, dStartDate, dEndDate FROM rounds r LEFT JOIN  competition_round cr ON cr.vCompetitionRoundId = r.vCompetitionRoundId WHERE cr.vCompetitionId = \""
						+ vCompetitionId
						+ "\" ORDER BY dStartDate ASC, dEndDate ASC ");
	}

	public int getWeekSelectedPosition(String vRoundId,
			Hashtable<String, List<String>> data) {
		int selectedPos = -1;
		if (data != null && data.get("vRoundId").size() > 0) {
			int len = data.get("vRoundId").size();
			for (int i = 0; i < len; i++) {
				if (vRoundId != null
						&& vRoundId.equalsIgnoreCase(data.get("vRoundId")
								.get(i))) {
					selectedPos = i;
					break;
				}
			}

		}

		return selectedPos;
	}

	/**
	 * returns the competition round url
	 */
	public String getCompetitionRoundUrl(final String vCompetitionId) {

		String vCompetitionRoundUrl = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vCompetitionRoundUrl FROM competition_round WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vCompetitionRoundUrl = c.getString(c
						.getColumnIndex("vCompetitionRoundUrl"));
			}
			c.close();
		}
		c = null;
		return vCompetitionRoundUrl;
	}

	/**
	 * @return gives the url of the user's avatar.
	 */
	public Hashtable<String, Object> getProfileURLFromRootResource() {

		Cursor c = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_url,resource_href FROM root_resource where resource_name = 'profile'");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("resource_href"));

				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("resource_url"));
					result.put("url", url);
					result.put("isHref", false);

				}

				c.close();
				return result;
			}
			return null;
		} catch (Exception e) {
			Logs.show(e);
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * @return gives the url of the all sports.
	 */
	public Hashtable<String, Object> getAllSportsURLFromRootResource() {

		Cursor c = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_url,resource_href FROM root_resource where resource_name = 'sports' ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("resource_href"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("resource_url"));
					result.put("url", url);
					result.put("isHref", false);

				}

				c.close();
				return result;

			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/*
	 * public String getAllSportsURL() {
	 * 
	 * Cursor c = null;
	 * 
	 * try { c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT resource_url,resource_href FROM root_resource where resource_name = 'sports' "
	 * ); if ( c != null && c.getCount() > 0) { c.moveToFirst(); String url =
	 * c.getString( c.getColumnIndex ( "resource_href" ) ); if(url != null &&
	 * url.trim().length() > 0){
	 * 
	 * }else{
	 * 
	 * url = c.getString( c.getColumnIndex ( "resource_url" ) );
	 * 
	 * 
	 * }
	 * 
	 * c.close(); return url;
	 * 
	 * } return null; }catch (Exception e) { return null; }finally{
	 * if(c!=null&&!c.isClosed()) c.close();
	 * 
	 * c = null; } }
	 */
	public Hashtable<String, Object> getAllSportsURL() {

		Cursor c = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_url,resource_href FROM root_resource where resource_name = 'sports' ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("resource_href"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("resource_url"));
					result.put("url", url);
					result.put("isHref", false);

				}

				c.close();
				return result;

			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * Fetching icon url based on provider name
	 */
	public String getProviderIconBroadcastUrl(final String vProviderName) {

		String vIconUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vIconBroadcastUrl FROM providers WHERE vProviderName = \""
						+ vProviderName + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vIconUrl = c.getString(c.getColumnIndex("vIconBroadcastUrl"));
			}
			c.close();
		}
		c = null;

		return vIconUrl;
	}

	/**
	 * Fetching sel icon url based on provider name
	 */
	public String getProviderSelIconBroadcastUrl(final String vProviderName) {

		String vIconUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vIconBroadcastHighLightUrl FROM providers WHERE vProviderName = \""
						+ vProviderName + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vIconUrl = c.getString(c
						.getColumnIndex("vIconBroadcastHighLightUrl"));
			}
			c.close();
		}

		c = null;
		return vIconUrl;
	}

	/**
	 * @return gives the url of the user's notification url.
	 */
	public String getNotificationURLFromRootResource() {

		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_name FROM root_resource WHERE resource_name = 'notifications_track_url'  ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("resource_name"));
				c.close();
				return url;
			}
			return null;
		} catch (Exception e) {

			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * @return gives the url of the user's notification url.
	 */
	/*
	 * 
	 * public String getUserNotificationUrl() {
	 * 
	 * Cursor c = null; try{ c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vNotificationUrl FROM user WHERE isPrimaryUser = '1'  "); if ( c
	 * != null && c.getCount() > 0) { c.moveToFirst(); String url = c.getString(
	 * c.getColumnIndex ( "vNotificationUrl" ) ); c.close(); return url; }
	 * return null; }catch (Exception e) {
	 * 
	 * return null; } finally { if ( c != null && !c.isClosed() ) c.close();
	 * 
	 * c = null; } }
	 */
	/**
	 * Praveen : sa per the href
	 */

	/**
	 * @return gives the url of the user's notification url.
	 */
	public Hashtable<String, Object> getUserNotificationUrl() {

		Cursor c = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vNotificationUrl,vNotificationHrefUrl FROM user WHERE isPrimaryUser = '1'  ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c
						.getColumnIndex("vNotificationHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vNotificationUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}

				c.close();
				return result;
			}
			return null;
		} catch (Exception e) {

			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/*
	 * public String getRecentActivityUrl() {
	 * 
	 * Cursor c = null; try{ c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vRecentActivityUrl FROM user WHERE isPrimaryUser = '1'  "); if (
	 * c != null && c.getCount() > 0) { c.moveToFirst(); String url =
	 * c.getString( c.getColumnIndex ( "vRecentActivityUrl" ) ); c.close();
	 * return url; } return null; }catch (Exception e) {
	 * 
	 * return null; } finally { if ( c != null && !c.isClosed() ) c.close();
	 * 
	 * c = null; } }
	 */

	/**
	 * Praveen : as per the href
	 */
	public Hashtable<String, Object> getRecentActivityUrl() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vRecentActivityUrl,vRecentActivityHrefUrl FROM user WHERE isPrimaryUser = '1'  ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c
						.getColumnIndex("vRecentActivityHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vRecentActivityUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}

				c.close();
				return result;
			}
			return result;
		} catch (Exception e) {

			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/*
	 * public String getDirectConversationUrl() {
	 * 
	 * Cursor c = null; try{ c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vDirectConversationUrl FROM user_direct_conversation udc LEFT JOIN user u ON udc.vUserId = u.iUserId WHERE u.isPrimaryUser = 1 "
	 * ); if ( c != null && c.getCount() > 0) { c.moveToFirst(); String url =
	 * c.getString( c.getColumnIndex ( "vDirectConversationUrl" ) ); c.close();
	 * return url; } return null; }catch (Exception e) { Logs.show( e ); return
	 * null; } finally { if ( c != null && !c.isClosed() ) c.close();
	 * 
	 * c = null; } }
	 */
	/**
	 * Praveen : as per the href
	 */
	public Hashtable<String, Object> getDirectConversationUrl() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							" SELECT vDirectConversationUrl,vDirectConversationHrefUrl FROM user_direct_conversation udc LEFT JOIN user u ON udc.vUserId = u.iUserId WHERE u.isPrimaryUser = 1 ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				// String url = c.getString( c.getColumnIndex (
				// "vDirectConversationUrl" ) );

				String url = c.getString(c
						.getColumnIndex("vDirectConversationHrefUrl"));

				// Log.e("123", "url-------before if---------->>>>"+url);
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c
							.getColumnIndex("vDirectConversationUrl"));
					// Log.e("123", "url--------else--------->>>>"+url);
					if (url != null && url.trim().length() > 0) {
						result.put("url", url);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}

				}

				c.close();

				return result;
			}
			return null;
		} catch (Exception e) {
			Logs.show(e);
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	public Hashtable<String, Object> getDirectConversationUrl(String vUserUrl) {

		Cursor c = null;
		try {
			Hashtable<String, Object> result = new Hashtable<String, Object>();

			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							" SELECT vDirectConversationUrl,vDirectConversationHrefUrl FROM user_direct_conversation udc LEFT JOIN user u ON udc.vUserId = u.iUserId WHERE  ( LENGTH(u.vSelfUrl)> 0 AND u.vSelfUrl = '"
									+ vUserUrl
									+ "' )OR ( LENGTH(u.vHrefUrl)> 0 AND u.vHrefUrl = '"
									+ vUserUrl + "' )");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				String url = c.getString(c
						.getColumnIndex("vDirectConversationHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c
							.getColumnIndex("vDirectConversationUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}
				c.close();
				return result;
			}
			return null;
		} catch (Exception e) {
			Logs.show(e);
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * getting the user token for some checking puporse.
	 */
	public String getUserToken() {

		// getting the data from the database
		Cursor c = null;
		String user_token = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vUserToken FROM user WHERE isPrimaryUser = \"1\" ");
			// checking for user token
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				user_token = c.getString(c.getColumnIndex("vUserToken"));
				c.close();
			}

			return user_token;
		} catch (Exception e) {
			return user_token;
		} finally {

			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}

	}

	/**
	 * getting the user token for some checking puporse.
	 */
	public String getUserId() {

		// getting the data from the database
		Cursor c = null;
		String user_token = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT iId FROM user WHERE isPrimaryUser = \"1\" ");
			// checking for user token
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				user_token = c.getString(c.getColumnIndex("iId"));
				c.close();
			}
			return user_token;
		} catch (Exception e) {
			return user_token;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	public String getPrimaryUserId() {

		// getting the data from the database
		Cursor c = null;
		String user_token = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT iUserId FROM user WHERE isPrimaryUser = \"1\" ");
			// checking for user token
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				user_token = c.getString(c.getColumnIndex("iUserId"));
				c.close();
			}
			return user_token;
		} catch (Exception e) {
			return user_token;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * getting the user token for some checking puporse.
	 */
	// public String getsignOutUrl () {
	//
	// // getting the data from the database
	// Cursor c = null;
	// String signOutUrl = null;
	// try{
	// c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	// "SELECT vSignOutUrl FROM user WHERE isPrimaryUser = \"1\" " );
	// // checking for user token
	// if ( c != null && c.getCount() > 0) {
	// c.moveToFirst();
	// signOutUrl = c.getString( c.getColumnIndex( "vSignOutUrl" ) );
	// c.close();
	// }
	// return signOutUrl;
	// }catch (Exception e) {
	// return signOutUrl;
	// }finally{
	// if(c!=null&&!c.isClosed())
	// c.close();
	//
	// c = null;
	// }
	// }

	public Hashtable<String, Object> getsignOutUrl() {

		// getting the data from the database
		Cursor c = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String signOutUrl = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT vSignOutUrl,vSignOutHrefUrl FROM user WHERE isPrimaryUser = \"1\" ");
			// checking for user token
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				signOutUrl = c.getString(c.getColumnIndex("vSignOutHrefUrl"));
				// Log.e("234",
				// "signOut------------signOutUrl before if----------->>>>>>>"+signOutUrl);
				if (signOutUrl != null && signOutUrl.trim().length() > 0) {
					result.put("url", signOutUrl);
					result.put("isHref", true);

				} else {

					signOutUrl = c.getString(c.getColumnIndex("vSignOutUrl"));
					// Log.e("234",
					// "signOut------------signOutUrl else---------->>>>>>>"+signOutUrl);

					if (signOutUrl != null && signOutUrl.trim().length() > 0) {
						result.put("url", signOutUrl);
						result.put("isHref", false);
					} else {
						result.put("url", "");
						result.put("isHref", false);
					}

				}

				c.close();
			}
			return result;
		} catch (Exception e) {
			return result;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	public boolean doesExistsMessage(final String message_id_pk) {
		/*
		 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
		 * " SELECT message_uid, message_timestamp FROM message WHERE message_uid = \""
		 * + iMessageId + "\" " ); if ( c != null ) { if ( c.getCount() > 0 ) {
		 * c.moveToFirst();
		 * 
		 * String timeStamp = c.getString( c.getColumnIndex( "message_timestamp"
		 * ) ); c.close(); int count =
		 * PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		 * " SELECT message_uid FROM message WHERE message_timestamp < '" +
		 * timeStamp + "' "); if ( count > 0 ) {
		 * 
		 * return true; } } c.close(); } /*int count = if ( count > 0 ) { count
		 * = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		 * " SELECT message_uid FROM message WHERE message_uid = \"" +
		 * iMessageId + "\" " ); return true; } else { return false; }
		 */

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vNextUrl FROM message WHERE message_id_pk = \""
						+ message_id_pk + "\" ");

		if (c != null) {
			if (c.getCount() > 0) {
				/*
				 * c.moveToFirst(); String vNextUrl = c.getString(
				 * c.getColumnIndex( "vNextUrl" ) ); if(vNextUrl.length()>1){
				 * return true; }else{ return false; }
				 */
				c.close();
				return true;
			}
			c.close();
		}
		c = null;
		return false;
	}

	/**
	 * getting the after id for making the next message call
	 */
	public String getAfterMessageId(final String message_timestamp,
			final String vConversationId) {

		String afterMessageId = "0";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT message_uid FROM conversation_message cm LEFT JOIN message m ON cm.vConversationMessageId = m.vConversationMessageId WHERE  cm.vConversationId  = \""
								+ vConversationId
								+ "\" AND message_timestamp < '"
								+ message_timestamp
								+ "' ORDER BY message_timestamp DESC LIMIT 0, 1");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				afterMessageId = c.getString(c.getColumnIndex("message_uid"));
				afterMessageId = afterMessageId.replace("messages-", "");
			}
			c.close();
		}
		c = null;
		return afterMessageId;
	}

	/**
	 * gettingthe self url for the message for given conversation
	 */
	public String getMessageSelfUrl(final String vConversationId) {

		String url = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vSelfUrl FROM conversation_message WHERE vConversationId  = \""
						+ vConversationId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				url = c.getString(c.getColumnIndex("vSelfUrl"));
			}
			c.close();
		}
		c = null;
		return url;

	}

	/**
	 * returns all sports data
	 */
	public Hashtable<String, List<String>> getAllSports() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT  DISTINCT(vSportsId) AS vSportsId, vName, vTileLogoUrl AS vLogoUrl, iLiveNum, vSportsCompetitionUid FROM sports ORDER BY iOrder ASC");
	}

	/**
	 * returns my sports data
	 */
	public Hashtable<String, List<String>> getMySports() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId, vCompetitonName, vLogoUrl FROM competition WHERE isFavourite = '1' ORDER BY vCompetitonName ASC ");
	}

	/**
	 * returns my sports data
	 */
	public Hashtable<String, List<String>> getFavouriteSports() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT c.vCompetitionId AS vCompetitionId , "
						+ "c.vCompetitionUrl AS vCompetitionUrl, "
						+ "c.vCompetitionHref AS vCompetitionHref, "
						+ "c.vCompetitonName AS vCompetitonName, "
						+ "c.iLiveNum AS iLiveNum, "
						+ "c.isFavourite AS isFavourite, "
						+ "c.vRegion AS vRegion, "
						+ "s.vSportType AS vSportType FROM competition c "
						+ "LEFT JOIN sports s on c.vSportsCompetitionId == s.vSportsCompetitionUid  "
						+ "WHERE c.isFavourite = '1'   ORDER BY c.iFavouriteTime ASC");
	}

	/**
	 * returns my sports data
	 */
	// public void setSelectedMySports ( String vCompetitionId ) {
	//
	// // remove selection from every other team.
	// ContentValues values = new ContentValues();
	// values.put( "isSelected", 0 );
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	// Constants.QUERY_UPDATE, null, "competition", values, null);
	//
	// // set the selection to that particular competition
	// values.put( "isSelected", 1 );
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	// Constants.QUERY_UPDATE, null, "competition", values,
	// " vCompetitionId = \"" + vCompetitionId + "\" " );
	// }
	//

	public void setSelectedMySports(String vCompetitionId) {
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						("SELECT isSelected FROM competition WHERE isSelected = (SELECT MAX(isSelected) FROM competition)"));
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				int isSelected = (c.getInt(c.getColumnIndex("isSelected")));
				isSelected = isSelected + 1;
				ContentValues values = new ContentValues();
				values.put("isSelected", isSelected);
				JsonUtil json = new JsonUtil();
				json.queryMethod1(Constants.QUERY_UPDATE, null, "competition",
						values,
						" vCompetitionId = \"" + vCompetitionId + "\" ", null,
						false, true);
			}
			c.close();

			c = null;
		}

	}

	public void deSelectMySports(String vCompetitionId) {
		ContentValues values = new ContentValues();
		values.put("isSelected", 0);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "competition", values,
				" vCompetitionId = \"" + vCompetitionId + "\" ", null, false,
				false);

		deselectPrivateCompetitions(vCompetitionId);

	}

	/**
	 * returns my sports data
	 */
	public Hashtable<String, List<String>> getSelectedMySports() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId, vCompetitonName, vLogoUrl FROM competition WHERE isFavourite = '1' ORDER BY isSelected DESC, vCompetitonName ASC ");
	}

	/**
	 * returns my sports data
	 */
	// public Hashtable<String, List < String > > getMySportsImage () {
	//
	// return PlayupLiveApplication.getDatabaseWrapper().select(
	// " SELECT s.vSportsId AS vSportsId, vFeatureLogoUrl,c.iLiveNum FROM sports s LEFT JOIN sports_competition sc ON sc.vSportsId = s.vSportsId LEFT JOIN competition c ON c.vSportsCompetitionId = sc.vSportsCompetitionId WHERE c.vCompetitionId = ( SELECT vCompetitionId  FROM competition WHERE isFavourite = '1' AND isSelected = '1' ORDER BY vCompetitonName ASC LIMIT 0, 1 ) "
	// );
	// }

	public Hashtable<String, List<String>> getMySportsImage() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT s.vSportsId AS vSportsId, vFeatureLogoUrl,c.iLiveNum FROM sports s LEFT JOIN sports_competition sc ON sc.vSportsId = s.vSportsId LEFT JOIN competition c ON c.vSportsCompetitionId = sc.vSportsCompetitionId WHERE c.vCompetitionId = ( SELECT vCompetitionId  FROM competition WHERE isFavourite = '1' AND isSelected = (SELECT MAX(isSelected) FROM competition) ORDER BY vCompetitonName ASC LIMIT 0, 1 ) ");
	}

	public String getTotalMySports() {

		String totalCount = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT COUNT(vCompetitionId) as totalCount  FROM competition WHERE isFavourite = '1'");

		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				totalCount = c.getString(c.getColumnIndex("totalCount"));
			}
			c.close();
		}
		c = null;

		return totalCount;
	}

	public Hashtable<String, List<String>> getLeagues(final String vSportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId, vCompetitionUrl,vCompetitionHref, vCompetitonName, vShortName, vRegion, vLogoUrl, "
						+ "iLiveNum, isFavourite"
						+ " FROM competition c "
						+ "LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId "
						+ "WHERE sc.vSportsId = \""
						+ vSportsId
						+ "\"  ORDER BY c.iOrder ASC ");
	}

	/*
	 * public String getSportsCompetitionUrl ( final String vSportsId ) {
	 * 
	 * String vSportsCompetitionUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vSportsCompetitionUrl FROM "+ "sports_competition " +
	 * "WHERE vSportsId = \"" + vSportsId + "\"  ");
	 * 
	 * if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vSportsCompetitionUrl =
	 * c.getString( c.getColumnIndex( "vSportsCompetitionUrl" ) ); } c.close();
	 * } c = null; return vSportsCompetitionUrl; }
	 */

	public Hashtable<String, Object> getSportsCompetitionUrl(
			final String vSportsId) {

		String vSportsCompetitionUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vSportsCompetitionUrl,vSportsCompetitionHref FROM "
						+ "sports_competition " + "WHERE vSportsId = \""
						+ vSportsId + "\"  ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSportsCompetitionUrl = c.getString(c
						.getColumnIndex("vSportsCompetitionHref"));
				if (vSportsCompetitionUrl != null
						&& vSportsCompetitionUrl.trim().length() > 0) {
					result.put("url", vSportsCompetitionUrl);
					result.put("isHref", true);

				} else {

					vSportsCompetitionUrl = c.getString(c
							.getColumnIndex("vSportsCompetitionUrl"));
					result.put("url", vSportsCompetitionUrl);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;
	}

	public int getLeaguesCount(final String vSportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vCompetitionId, vCompetitionUrl, vCompetitonName, vShortName, vRegion, vLogoUrl, "
								+ "iLiveNum, isFavourite"
								+ " FROM competition c "
								+ "LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId "
								+ "WHERE sc.vSportsId = \""
								+ vSportsId
								+ "\"  ");
	}

	public String getSingleCompetitionIdFromSportsId(String vSportsId) {

		String vCompetitionId = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vCompetitionId "
								+ " FROM competition c "
								+ "LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId "
								+ "WHERE sc.vSportsId = \"" + vSportsId
								+ "\"  ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionId = c
						.getString(c.getColumnIndex("vCompetitionId"));
			}
			c.close();
		}
		c = null;
		return vCompetitionId;
	}

	public Hashtable<String, List<String>> getMyLeagues() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId,isFavourite, vCompetitonName, "
						+ "vShortName, vRegion, vLogoUrl, iLiveNum FROM competition WHERE isFavourite = '1' ");
	}

	public Hashtable<String, List<String>> getMyLeagues(final String vSportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId, vSelfUrl, vCompetitonName, vShortName, vRoundId, vTeamId, isLive, isFavourite FROM sports_competition sc LEFT JOIN competition c ON sc.vSportsCompetitionId = c.vSportsCompetitionId WHERE sc.vSportsId = \""
						+ vSportsId + "\"  AND c.isFavourite = 1 ");
	}

	public Hashtable<String, List<String>> getSportsLiveContest(
			String vCompetitionLiveId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vSportType,vContestId,dScheduledStartTime,"
						+ "dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2"
						+ ",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates, vLastEventName, vShortMessage, vLongMessage,iActive1,iActive2,  "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, "
						+ "( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName, "
						+ "(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl,"
						+ "(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  "
						+ "FROM contests con WHERE con.vCompetitionLiveId = \""
						+ vCompetitionLiveId + "\" ");

	}

	public Hashtable<String, List<String>> getSportsLiveCompetition(
			String vSportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT  vCompetitionLiveId FROM competition_live cl "
						+ "LEFT JOIN sports_live sl ON cl.vSportsLiveId = sl.vSportsLiveId WHERE "
						+ "sl.vSportsId = \"" + vSportsId + "\" ");
	}

	public Hashtable<String, List<String>> getMyLiveCompetition() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT  vCompetitionLiveId,vCompetitonName,vRegion FROM "
						+ "competition c WHERE isFavourite = 1 AND iLiveNum > 0 ");
	}

	public String getCompetitionName(String vCompetitionId) {

		String vCompetitionName = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vCompetitonName FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vCompetitionName = c.getString(c
						.getColumnIndex("vCompetitonName"));
			}
			c.close();
		}
		c = null;
		return vCompetitionName;
	}

	public String getCompetitionRegion(String vCompetitionId) {

		String vRegion = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vRegion FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vRegion = c.getString(c.getColumnIndex("vRegion"));
			}
			c.close();
		}
		c = null;
		return vRegion;
	}

	public String getCompetitionLiveName(String vCompetitionLiveId) {

		String vCompetitionName = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vCompetitonName FROM competition c LEFT JOIN competition_live cl ON cl.vCompetitionId = c.vCompetitionId WHERE cl.vCompetitionLiveId = \""
								+ vCompetitionLiveId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vCompetitionName = c.getString(c
						.getColumnIndex("vCompetitonName"));

			}
			c.close();
		}
		c = null;
		return vCompetitionName;
	}

	public String getLatestMessageId(String vConversationId) {
		String id = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT message_uid FROM message m LEFT JOIN conversation_message cm ON cm.vConversationMessageId = m.vConversationMessageId WHERE cm.vConversationId = \""
								+ vConversationId
								+ "\" ORDER BY message_timestamp DESC LIMIT 0, 1");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				id = c.getString(c.getColumnIndex("message_uid"));
			}
			c.close();
		}

		c = null;
		return id;
	}

	public String getLatestPrivateLobbyMessageId(String vConversationId) {
		String id = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vMessageId FROM friendMessage m LEFT JOIN friendConversationMessage cm ON cm.vConversationMessageId = m.vConversationMessageId WHERE cm.vConversationId = \""
								+ vConversationId
								+ "\" ORDER BY vCreatedDate DESC LIMIT 0, 1");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				id = c.getString(c.getColumnIndex("vMessageId"));
			}
			c.close();
		}

		c = null;
		return id;
	}

	public String getLatestPrivateLobbyMessageUrl(String vConversationId) {
		String id = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vMessageUrl FROM friendMessage m LEFT JOIN friendConversationMessage cm ON cm.vConversationMessageId = m.vConversationMessageId WHERE cm.vConversationId = \""
								+ vConversationId
								+ "\" ORDER BY vCreatedDate DESC LIMIT 0, 1");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				id = c.getString(c.getColumnIndex("vMessageUrl"));
			}
			c.close();
		}

		c = null;
		return id;
	}

	public String getLatestDirectMessageId(String vDirectMarkerUrl) {
		String id = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vDMessageItemId FROM direct_message_items dmi LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId WHERE (dm.vMarkerUrl = \""
								+ vDirectMarkerUrl
								+ "\" ) OR (dm.vMarkerHrefUrl = \""
								+ vDirectMarkerUrl
								+ "\" ) ORDER BY dCreatedDate DESC LIMIT 0, 1 ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				id = c.getString(c.getColumnIndex("vDMessageItemId"));
			}
			c.close();
		}

		c = null;
		return id;
	}

	public String getLatestMessageUrl(String vConversationId) {
		String url = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT message_id_pk,vMessageHrefUrl FROM message m LEFT JOIN conversation_message cm ON cm.vConversationMessageId = m.vConversationMessageId WHERE cm.vConversationId = \""
								+ vConversationId
								+ "\" ORDER BY message_timestamp DESC LIMIT 0, 1");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				url = c.getString(c.getColumnIndex("message_id_pk"));

				if (url != null && url.trim().length() > 0) {

				} else {
					url = c.getString(c.getColumnIndex("vMessageHrefUrl"));
				}
			}
			c.close();
		}

		c = null;
		return url;
	}

	public String getLatestDirectMessageUrl(String vDirectMarkerUrl) {
		String url = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vDMessageItemUrl FROM direct_message_items dmi LEFT JOIN direct_messages dm ON dm.vDMessageId = dmi.vDMessageId WHERE ( dm.vMarkerUrl = \""
								+ vDirectMarkerUrl
								+ "\"  ) OR ( dm.vMarkerHrefUrl = \""
								+ vDirectMarkerUrl
								+ "\"  ) ORDER BY dCreatedDate DESC LIMIT 0, 1 ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				url = c.getString(c.getColumnIndex("vDMessageItemUrl"));
			}
			c.close();
		}

		c = null;
		return url;
	}

	public Hashtable<String, Object> getMarkerUrl(String vConversationId) {

		String vMarkerUrl = null;

		Hashtable<String, Object> result = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vMarkerUrl,vMarkerHref FROM conversation_message WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vMarkerUrl = c.getString(c.getColumnIndex("vMarkerHref"));

				if (vMarkerUrl != null && vMarkerUrl.trim().length() > 0) {

					result.put("url", vMarkerUrl);
					result.put("isHref", true);

				} else {

					vMarkerUrl = c.getString(c.getColumnIndex("vMarkerUrl"));
					result.put("url", vMarkerUrl);
					result.put("isHref", false);
				}
			}

			c.close();
		}

		c = null;
		return result;

	}

	public Hashtable<String, Object> getPrivateLobbyMarkerUrl(
			String vConversationId) {

		String vMarkerUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vMarkerUrl,vMarkerHrefUrl FROM friendConversationMessage WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vMarkerUrl = c.getString(c.getColumnIndex("vMarkerHrefUrl"));

				if (vMarkerUrl != null && vMarkerUrl.trim().length() > 0) {

					result.put("url", vMarkerUrl);
					result.put("isHref", true);

				} else {

					vMarkerUrl = c.getString(c.getColumnIndex("vMarkerUrl"));
					result.put("url", vMarkerUrl);
					result.put("isHref", false);
				}
			}

			c.close();
		}

		c = null;
		return result;

	}

	public String getCompetitionLiveRegion(String vCompetitionLiveId) {

		String vRegion = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vRegion FROM competition c LEFT JOIN competition_live cl ON cl.vCompetitionId = c.vCompetitionId WHERE cl.vCompetitionLiveId = \""
								+ vCompetitionLiveId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vRegion = c.getString(c.getColumnIndex("vRegion"));
			}
			c.close();
		}
		c = null;
		return vRegion;
	}

	/**
	 * getting the etag for the corresponding url
	 */
	public String getETag(final String url) {

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT eTag FROM eTag WHERE vUrl = \"" + url + "\" ");

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			String eTag = c.getString(c.getColumnIndex("eTag"));

			c.close();

			c = null;
			return eTag;
		} else {
			if (c != null) {

				c.close();
				c = null;
			}
			return null;
		}

	}

	/**
	 * getting the vCacheTime for the corresponding url
	 */
	public String getCacheTime(final String url) {

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vCacheTime FROM eTag WHERE vUrl = \"" + url + "\" ");

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			String vCacheTime = c.getString(c.getColumnIndex("vCacheTime"));

			c.close();
			c = null;
			if (vCacheTime == null) {
				return "-1";
			}
			if (vCacheTime.trim().length() == 0) {
				return "-1";
			}
			return vCacheTime;
		} else {
			if (c != null) {

				c.close();
				c = null;
			}
			return "-1";
		}

	}

	public String getCacheTimeForSection(final String url) {

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vCacheTime FROM eTag WHERE vUrl = \"" + url + "\" ");

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			String vCacheTime = c.getString(c.getColumnIndex("vCacheTime"));

			c.close();
			c = null;
			if (vCacheTime == null) {
				return "-1";
			}
			if (vCacheTime.trim().length() == 0) {
				return "-1";
			}
			return vCacheTime;
		} else {
			if (c != null) {

				c.close();
				c = null;
			}
			return "0";
		}

	}

	/**
	 * setting the etag for the url
	 */
	public void setETag(String url, int cacheTime) {

		ContentValues values = new ContentValues();
		values.put("vUrl", url);
		values.put("vCacheTime", cacheTime);
		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "eTag", values,
				" vUrl = \"" + url + "\" ", "eTag", true, false);

	}

	/**
	 * setting the etag for the url
	 */
	public void setETag(String url, String eTag) {

		ContentValues values = new ContentValues();
		values.put("vUrl", url);
		values.put("eTag", eTag);
		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "eTag", values,
				" vUrl = \"" + url + "\" ", "eTag", true, true);

	}

	/**
	 * getting the etag for the corresponding url
	 */
	public String getHeader(final String url) {

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vHeader FROM headers WHERE "
						+ "(LENGTH(vUrl) > 0 AND vUrl = \"" + url
						+ "\") OR (LENGTH(vHref) > 0 AND vHref = '" + url
						+ "')");

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();

			String headers = c.getString(c.getColumnIndex("vHeader"));
			c.close();
			c = null;
			return headers;
		} else {
			if (c != null) {
				c.close();
				c = null;
			}
			return null;
		}

	}

	/**
	 * setting the accept type headers for the url
	 */
	public void setHeader(String url, String header, boolean queryMethod1) {

		if (queryMethod1) {

			ContentValues values = new ContentValues();
			values.put("vHeader", header);

			JsonUtil json = new JsonUtil();
			json.queryMethod1(Constants.QUERY_UPDATE, null, "headers", values,
					" vUrl = \"" + url + "\" ", "vUrl", true, true);

		} else {
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vUrl FROM headers WHERE vUrl = \"" + url
									+ "\"  ");

			if (count > 0) {

				ContentValues values = new ContentValues();
				values.put("vHeader", header);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "headers", values,
						" vUrl = \"" + url + "\" ");

			} else {

				ContentValues values = new ContentValues();
				values.put("vUrl", url);
				values.put("vHeader", header);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "headers", values, null);

			}
		}

	}

	public void setHeader(String href, String url, String header,
			boolean queryMethod1) {

		if (queryMethod1) {

			ContentValues values = new ContentValues();
			values.put("vHeader", header);
			values.put("vHref", href);
			values.put("vUrl", url);

			JsonUtil json = new JsonUtil();

			json.queryMethod1(Constants.QUERY_UPDATE, null, "headers", values,
					" (LENGTH(vUrl) > 0 AND vUrl = \"" + url
							+ "\") OR (LENGTH(vHref) > 0 AND vHref = '" + href
							+ "')", "vUrl", true, true);

		} else {
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vUrl FROM headers WHERE (LENGTH(vUrl) > 0 AND vUrl = \""
									+ url + "\") OR "
									+ "(LENGTH(vHref) > 0 AND vHref = '" + href
									+ "')");

			if (count > 0) {

				ContentValues values = new ContentValues();
				values.put("vHeader", header);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE,
						null,
						"headers",
						values,
						" (LENGTH(vUrl) > 0 AND vUrl = \"" + url
								+ "\") OR (LENGTH(vHref) > 0 AND vHref = '"
								+ href + "')");

			} else {

				ContentValues values = new ContentValues();
				values.put("vHeader", header);
				values.put("vHref", href);
				values.put("vUrl", url);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "headers", values, null);

			}
		}

	}

	/**
	 * Praveen: Updated as per HREF setting the accept type headers for the url
	 */
	public void setColor(String vSelfUrl, String vHrefUrl, String vUid,
			String vMainColor, String vSecColor, String vMainTitleColor,
			String vSecTitleColor, String vHeaderImg) {

		if ((vHrefUrl != null && vHrefUrl.trim().length() > 0)
				|| (vSelfUrl != null && vSelfUrl.trim().length() > 0)) {
			int count = 0;
			if (vHrefUrl != null && vHrefUrl.trim().length() > 0)
				count = PlayupLiveApplication.getDatabaseWrapper()
						.getTotalCount(
								" SELECT vSelfUrl FROM color WHERE (LENGTH(vHref) > 0 AND vHref = \""
										+ vHrefUrl + "\") ");
			else
				count = PlayupLiveApplication.getDatabaseWrapper()
						.getTotalCount(
								" SELECT vSelfUrl FROM color WHERE (LENGTH(vSelfUrl) > 0 AND vSelfUrl = \""
										+ vSelfUrl + "\" )  ");

			if (count > 0) {

				ContentValues values = new ContentValues();
				values.put("vSelfUrl", vSelfUrl);
				values.put("vUid", vUid);
				values.put("vMainColor", vMainColor);
				values.put("vSecColor", vSecColor);
				values.put("vMainTitleColor", vMainTitleColor);
				values.put("vSecTitleColor", vSecTitleColor);
				values.put("vHeaderImg", vHeaderImg);
				values.put("vHref", vHrefUrl);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE,
						null,
						"color",
						values,
						" (LENGTH(vSelfUrl) > 0 AND vSelfUrl = \"" + vSelfUrl
								+ "\" ) OR (LENGTH(vHref) > 0 AND vHref = \""
								+ vHrefUrl + "\") ");

			} else {

				ContentValues values = new ContentValues();
				values.put("vSelfUrl", vSelfUrl);
				values.put("vUid", vUid);
				values.put("vMainColor", vMainColor);
				values.put("vSecColor", vSecColor);
				values.put("vMainTitleColor", vMainTitleColor);
				values.put("vSecTitleColor", vSecTitleColor);
				values.put("vHeaderImg", vHeaderImg);
				values.put("vHref", vHrefUrl);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "color", values, null);

			}

			return;
		}

		if (vUid != null && vUid.trim().length() > 0) {

			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vUid FROM color WHERE vUid = \"" + vUid
									+ "\"  ");

			if (count > 0) {

				ContentValues values = new ContentValues();
				values.put("vSelfUrl", vSelfUrl);
				values.put("vMainColor", vMainColor);
				values.put("vSecColor", vSecColor);
				values.put("vMainTitleColor", vMainTitleColor);
				values.put("vSecTitleColor", vSecTitleColor);
				values.put("vHeaderImg", vHeaderImg);
				values.put("vHref", vHrefUrl);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "color", values,
						" vUid = \"" + vUid + "\" ");

			} else {

				ContentValues values = new ContentValues();
				values.put("vSelfUrl", vSelfUrl);
				values.put("vUid", vUid);
				values.put("vMainColor", vMainColor);
				values.put("vSecColor", vSecColor);
				values.put("vMainTitleColor", vMainTitleColor);
				values.put("vSecTitleColor", vSecTitleColor);
				values.put("vHeaderImg", vHeaderImg);
				values.put("vHref", vHrefUrl);

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "color", values, null);

			}

		}

	}

	/**
	 * setting the accept type headers for the url
	 */
	/*
	 * public void setColor ( String vSelfUrl, String vUid, String vMainColor,
	 * String vSecColor, String vMainTitleColor, String vSecTitleColor, String
	 * vHeaderImg ) {
	 * 
	 * if(vSelfUrl != null && vSelfUrl.trim().length() > 0){
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vSelfUrl FROM color WHERE vSelfUrl = \"" + vSelfUrl + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vUid", vUid );
	 * values.put( "vMainColor", vMainColor ); values.put( "vSecColor",
	 * vSecColor ); values.put( "vMainTitleColor", vMainTitleColor );
	 * values.put( "vSecTitleColor", vSecTitleColor ); values.put( "vHeaderImg",
	 * vHeaderImg );
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_UPDATE, null, "color", values, " vSelfUrl = \"" +
	 * vSelfUrl + "\" ");
	 * 
	 * 
	 * 
	 * } else {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vSelfUrl",
	 * vSelfUrl ); values.put( "vUid", vUid ); values.put( "vMainColor",
	 * vMainColor ); values.put( "vSecColor", vSecColor ); values.put(
	 * "vMainTitleColor", vMainTitleColor ); values.put( "vSecTitleColor",
	 * vSecTitleColor ); values.put( "vHeaderImg", vHeaderImg );
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_INSERT, null, "color", values, null);
	 * 
	 * }
	 * 
	 * return; }
	 * 
	 * if(vUid != null && vUid.trim().length() > 0){
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vUid FROM color WHERE vUid = \"" + vUid + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vSelfUrl",
	 * vSelfUrl ); values.put( "vMainColor", vMainColor ); values.put(
	 * "vSecColor", vSecColor ); values.put( "vMainTitleColor", vMainTitleColor
	 * ); values.put( "vSecTitleColor", vSecTitleColor ); values.put(
	 * "vHeaderImg", vHeaderImg );
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "color", values, " vUid = \"" + vUid +
	 * "\" ");
	 * 
	 * 
	 * 
	 * } else {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vSelfUrl",
	 * vSelfUrl ); values.put( "vUid", vUid ); values.put( "vMainColor",
	 * vMainColor ); values.put( "vSecColor", vSecColor ); values.put(
	 * "vMainTitleColor", vMainTitleColor ); values.put( "vSecTitleColor",
	 * vSecTitleColor ); values.put( "vHeaderImg", vHeaderImg );
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "color", values, null);
	 * 
	 * }
	 * 
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * }
	 */

	/*
	 * public void setConversationFriends ( String vConversationId, String
	 * vConversationFriendId, String vConversationFriendUrl ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues(); values.put(
	 * "vConversationId", vConversationId ); values.put(
	 * "vConversationFriendId", vConversationFriendId ); values.put(
	 * "vConversationFriendUrl", vConversationFriendUrl );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vConversationFriendId FROM conversation_friends WHERE vConversationFriendId = \""
	 * + vConversationFriendId + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "conversation_friends", values,
	 * "  vConversationFriendId = \"" + vConversationFriendId + "\" "); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "conversation_friends", values, null); }
	 * 
	 * }
	 */
	/**
	 * Praveen: As per the href element
	 * 
	 * @param vConversationFriendHrefUrl
	 */
	public void setConversationFriends(String vConversationId,
			String vConversationFriendId, String vConversationFriendUrl,
			String vConversationFriendHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vConversationId", vConversationId);
		values.put("vConversationFriendId", vConversationFriendId);
		values.put("vConversationFriendUrl", vConversationFriendUrl);

		values.put("vConversationFriendHrefUrl", vConversationFriendHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vConversationFriendId FROM conversation_friends WHERE vConversationFriendId = \""
								+ vConversationFriendId + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"conversation_friends",
					values,
					"  vConversationFriendId = \"" + vConversationFriendId
							+ "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "conversation_friends",
					values, null);
		}

	}

	/**
	 * setting the accept type headers for the url
	 */
	public SQLiteDatabase getWritabeDatabase() {

		/*
		 * SQLiteDatabase sQLiteDatabase =
		 * PlayupLiveApplication.getDatabaseWrapper
		 * ().getWritableSQLiteDatabase(); sQLiteDatabase.setLockingEnabled (
		 * false );
		 */
		return PlayupLiveApplication.getDatabaseWrapper()
				.getWritableSQLiteDatabase();

	}

	/**
	 * Setting the sports live competition related data
	 */
	public void setCompetitionLiveData(String vSportsLiveId,
			String vCompetitionLiveId, String vCompetitionLiveUrl,
			String vCompetitionId, String name, String short_name,
			String region, String vLogoUrl, String vCompetitionLiveHref) {

		ContentValues values = new ContentValues();
		values.put("vCompetitionLiveId", vCompetitionLiveId);
		values.put("vCompetitionLiveUrl", vCompetitionLiveUrl);

		values.put("vCompetitionLiveHref", vCompetitionLiveHref);

		if (vCompetitionId != null && vCompetitionId.trim().length() > 0) {
			values.put("vCompetitionId", vCompetitionId);
		}
		values.put("vSportsLiveId", vSportsLiveId);

		// set the live competition data
		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vCompetitionLiveId FROM competition_live WHERE vCompetitionLiveId = \""
								+ vCompetitionLiveId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_live", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_live", values,
					" vCompetitionLiveId = \"" + vCompetitionLiveId + "\" ");
		}

		// set the competition data
		count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		values = new ContentValues();
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitonName", name);
		values.put("vShortName", short_name);
		values.put("vRegion", region);
		values.put("vLogoUrl", vLogoUrl);

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");
		}

	}

	/**
	 * getting the self url for checking purpose
	 */
	public String getUserSelfUrl() {

		// getting data from the self profile table
		Cursor c = null;
		String user_token = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					" SELECT vSelfUrl FROM user WHERE isPrimaryUser = \"1\"  ");

			// getting self url
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				user_token = c.getString(c.getColumnIndex("vSelfUrl"));
				c.close();
			}
			return user_token;
		} catch (Exception e) {
			return user_token;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	/**
	 * checknig conversation for duplicate entry
	 */
	private boolean checkForConversationExists(final String vConversationId) {

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vConversationId FROM match_conversation_node WHERE  vConversationId = \""
								+ vConversationId + "\" ");
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * checknig conversation message for duplicate entry
	 */
	private boolean checkForConversationMessageExists(
			final String vConversationId, final String vConversationMessageId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vConversationId FROM conversation_message WHERE  vConversationId = \""
						+ vConversationId
						+ "\" AND vConversationMessageId = \""
						+ vConversationMessageId + "\" ");
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * checking subject for duplicate entry
	 */
	/*
	 * private boolean checkForConversationSubjectExists ( final String
	 * vSubjectId ) {
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vSubjectId FROM match_home_node WHERE  vSubjectId = \"" +
	 * vSubjectId + "\" " ); if ( count > 0 ) { return true; } else { return
	 * false; } }
	 */

	/*	*//**
	 * Setting the Team data.
	 */
	public void setTeam(final String uid, final String self_url,
			final String name, final String shortName,
			final String displayName, final String vHeaderUrl,
			final String vCalendarUrl, final String vTeamHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vTeamId", uid);
		values.put("vSelfUrl", self_url);
		values.put("vTeamName", name);
		values.put("vShortName", shortName);
		values.put("vHeaderUrl", vHeaderUrl);
		values.put("vCalendarUrl", vCalendarUrl);
		values.put("vDisplayName", displayName);
		values.put("vHrefUrl", vTeamHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vTeamId FROM teams WHERE vTeamId = \"" + uid + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "teams", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "teams", values,
					" vTeamId = \"" + uid + "\" ");

		}

	}

	/**
	 * Praveen : as per the jref Setting the Team data.
	 */
	// public void setTeam ( final String uid, final String self_url,final
	// String hrefURl, final String name, final String shortName,final String
	// displayName, final String vHeaderUrl, final String vCalendarUrl ) {
	//
	// ContentValues values = new ContentValues();
	//
	// values.put( "vTeamId", uid );
	// values.put( "vSelfUrl", self_url );
	// values.put( "vTeamName", name );
	// values.put( "vShortName", shortName);
	// values.put( "vHeaderUrl", vHeaderUrl );
	// values.put( "vCalendarUrl", vCalendarUrl );
	// values.put("vDisplayName", displayName);
	//
	// values.put( "vHrefUrl", hrefURl );
	// int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	// "SELECT vTeamId FROM teams WHERE vTeamId = \"" + uid + "\" " );
	//
	// if ( count == 0 ) {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_INSERT, null, "teams", values, null );
	// } else {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_UPDATE, null, "teams", values, " vTeamId = \"" + uid +
	// "\" " );
	//
	// }
	//
	// }
	//
	/**
	 * Setting the setAssociatedTeam data.
	 */
	public void setAssociatedTeam(final String uid, final String self_url,
			final String name, final String shortName,
			final String displayName, final String vHeaderUrl,
			final String vCalendarUrl, final String vHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vTeamId", uid);
		values.put("vSelfUrl", self_url);
		values.put("vTeamName", name);
		values.put("vShortName", shortName);
		values.put("vHeaderUrl", vHeaderUrl);
		values.put("vCalendarUrl", vCalendarUrl);
		values.put("vDisplayName", displayName);
		values.put("vHrefUrl", vHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vTeamId FROM associatedTeams WHERE vTeamId = \"" + uid
						+ "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "associatedTeams", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "associatedTeams", values,
					" vTeamId = \"" + uid + "\" ");

		}

	}

	//
	//
	// /**
	// * Setting the contest lobby data.
	// */
	// public void setContestLobbyData ( final String vContestLobbyUid, final
	// String vContestLobbyUrl, final String vContestId ) {
	//
	//
	//
	//
	// ContentValues values = new ContentValues();
	//
	// values.put( "vContestLobbyUid", vContestLobbyUid );
	// values.put( "vContestLobbyUrl", vContestLobbyUrl );
	// values.put( "vContestId", vContestId );
	//
	// int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	// "SELECT vContestLobbyUid FROM contest_lobby WHERE vContestId = \"" +
	// vContestId + "\" " );
	//
	// if ( count == 0 ) {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_INSERT, null, "contest_lobby", values, null );
	// } else {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_UPDATE, null, "contest_lobby", values, " vContestId = \""
	// + vContestId + "\" " );
	//
	// }
	//
	// }

	public void setContestLobbyData(final String vContestLobbyUid,
			final String vContestLobbyUrl, final String vContestId,
			final String vContestLobbyHref) {

		ContentValues values = new ContentValues();

		values.put("vContestLobbyUid", vContestLobbyUid);
		values.put("vContestLobbyUrl", vContestLobbyUrl);
		values.put("vContestId", vContestId);

		values.put("vContestLobbyHref", vContestLobbyHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestLobbyUid FROM contest_lobby WHERE vContestId = \""
						+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"contest_lobby", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "contest_lobby", values,
					" vContestId = \"" + vContestId + "\" ");

		}

	}

	public void updateContestLobbyData(final String vTitle,
			final String vSubjectId, final String vBlockTileId) {

		ContentValues values = new ContentValues();

		values.put("VTitle", vTitle);
		values.put("vContestId", vSubjectId);
		values.put("vBlockTileId", vBlockTileId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestLobbyUid FROM contest_lobby WHERE vContestId = \""
						+ vSubjectId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"contest_lobby", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "contest_lobby", values,
					" vContestId = \"" + vSubjectId + "\" ");

		}

	}

	public void updateBlockContentLinkUrl(String vLinkUrl,
			String vContestLobbyUrl, String vContestLobbyHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vLinkUrl", vContestLobbyUrl);
		values.put("vLinkHrefUrl", vContestLobbyHrefUrl);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "blockContent", values,
				" vLinkUrl = \"" + vLinkUrl + "\" ");

	}

	/*
	 * public void setContestLobbyConversationData ( final String
	 * vConversationUid, final String vConversationSelfUrl, final String
	 * vContestLobbyUid ) {
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vConversationUid", vConversationUid ); values.put(
	 * "vConversationSelfUrl", vConversationSelfUrl ); values.put(
	 * "vContestLobbyUid", vContestLobbyUid );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vContestLobbyUid FROM contest_lobby_conversation WHERE vContestLobbyUid = \""
	 * + vContestLobbyUid + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "contest_lobby_conversation", values, null
	 * ); } else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "contest_lobby_conversation", values,
	 * " vContestLobbyUid = \"" + vContestLobbyUid + "\" " );
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public void setContestLobbyConversationData(final String vConversationUid,
			final String vConversationSelfUrl,
			final String vConversationHrefUrl, final String vContestLobbyUid) {

		ContentValues values = new ContentValues();

		values.put("vConversationUid", vConversationUid);
		values.put("vConversationSelfUrl", vConversationSelfUrl);
		values.put("vConversationHref", vConversationHrefUrl);
		values.put("vContestLobbyUid", vContestLobbyUid);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						"SELECT vContestLobbyUid FROM contest_lobby_conversation WHERE vContestLobbyUid = \""
								+ vContestLobbyUid + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "contest_lobby_conversation",
					values, null);
		} else {
			PlayupLiveApplication
					.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_UPDATE, null,
							"contest_lobby_conversation", values,
							" vContestLobbyUid = \"" + vContestLobbyUid + "\" ");

		}

	}

	/**
	 * Setting the sports live contest data
	 */
	public void setSportsLiveContest(final String vSportsLiveId,
			final String vContestId) {

		ContentValues values = new ContentValues();

		values.put("vSportsLiveId", vSportsLiveId);
		values.put("vContestId", vContestId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestId FROM sports_live_contests WHERE vContestId = \""
						+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sports_live_contests",
					values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sports_live_contests",
					values, " vContestId = \"" + vContestId + "\" ");

		}

	}

	/**
	 * Setting the contest data.
	 * 
	 * @param vEventsId
	 * @param vEventsUrl
	 * @param vEventsExtensionUrl
	 */
	/*
	 * 
	 * public void setContestData ( final String vContestId, final String
	 * vContestUrl, final String scheduled_start_time, String startTime, String
	 * endTime, String last_modified, String shortTitle, String title, int
	 * iTotal1, String vSummary1, String vHomeTeamId, int iTotal2, String
	 * vSummary2, String vAwayTeamId, int mins, int secs, int period, int
	 * innings, String innningsHalf, String vOvers, String vRunRate, String
	 * vLastBall, String summary, String vRoundContestId, String vCompetitionId,
	 * String vCompetitionLiveId , String competitionName,String
	 * roundName,String sportsName,String annotation, int iWickets1, String
	 * vPlayerFirstName1, String vPlayerLastName1, String vRole1, String
	 * vStats1, int iWickets2, String vPlayerFirstName2, String
	 * vPlayerLastName2, String vRole2, String vStats2, String
	 * vStrikerFirstName,String vStrikerLastName,String vStrikerStats, String
	 * vNonStrikerFirstName,String vNonStrikerLastName,String
	 * vNonStrikerStats,String annotation2, int runs1,int out1,int balls1,int
	 * strikes1,String base1,int runs2,int out2,int balls2,int strikes2,String
	 * base2, int super_goals1,int goals1,int behinds1,int super_goals2, int
	 * goals2,int behinds2, String vRoundId, String vShareUrl, int
	 * iHasLiveUpdates, String vLastEventName, String vShortMessage, String
	 * vLongMessage, String vSportType, int active1, int active2, String
	 * associatedContestId,String associatedContestUrl, String
	 * vEventsExtensionUrl, String vEventsUrl, String vEventsId, String
	 * vStadiumName, String vBackgroundImageUrl, String vContestHref,String
	 * vAssociatedContestHref,String vShareHref,String
	 * vEventsExtensionHref,String vEventsHref) {
	 * 
	 * // ,String vEventsUrl
	 * 
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vContestId", vContestId );
	 * 
	 * values.put( "vContestUrl", vContestUrl ); values.put(
	 * "dScheduledStartTime", scheduled_start_time ); values.put( "dStartTime",
	 * startTime ); values.put( "dEndTime", endTime ); values.put(
	 * "dLastModified", last_modified ); values.put( "vShortTitle", shortTitle
	 * ); values.put( "vTitle", title ); values.put( "iTotal1", iTotal1 );
	 * 
	 * values.put( "vSummary1", vSummary1 ); values.put( "vHomeTeamId",
	 * vHomeTeamId ); values.put( "iTotal2", iTotal2 ); values.put( "vSummary2",
	 * vSummary2 ); values.put( "vAwayTeamId", vAwayTeamId );
	 * 
	 * values.put( "iMins", mins ); values.put( "iSecs", secs ); values.put(
	 * "iPeriod", period );
	 * 
	 * values.put( "iInnnings", innings ); values.put( "vInnningsHalf",
	 * innningsHalf );
	 * 
	 * values.put( "vOvers", vOvers ); values.put( "vRunRate", vRunRate );
	 * values.put( "vLastBall", vLastBall );
	 * 
	 * values.put( "vCompetitionName", competitionName ); values.put(
	 * "vRoundName", roundName ); values.put( "vSportsName", sportsName );
	 * values.put( "vAnnotation", annotation );
	 * 
	 * values.put( "vSummary", summary );
	 * 
	 * 
	 * if ( vRoundContestId != null && vRoundContestId.trim().length() > 0 ) {
	 * values.put( "vRoundContestId", vRoundContestId ); }
	 * 
	 * if ( vCompetitionLiveId != null && vCompetitionLiveId.trim().length() > 0
	 * ) { values.put( "vCompetitionLiveId", vCompetitionLiveId ); } values.put(
	 * "vCompetitionId", vCompetitionId );
	 * 
	 * values.put( "iWickets1", iWickets1 ); values.put( "vPlayerFirstName1",
	 * vPlayerFirstName1 ); values.put( "vPlayerLastName1", vPlayerLastName1 );
	 * values.put( "vRole1", vRole1 ); values.put( "vStats1", vStats1 );
	 * 
	 * values.put( "iWickets2", iWickets2 ); values.put( "vPlayerFirstName2",
	 * vPlayerFirstName2 ); values.put( "vPlayerLastName2", vPlayerLastName2 );
	 * values.put( "vRole2", vRole2 ); values.put( "vStats2", vStats2 );
	 * 
	 * values.put( "vStrikerFirstName", vStrikerFirstName ); values.put(
	 * "vStrikerLastName", vStrikerLastName ); values.put( "vStrikerStats",
	 * vStrikerStats ); values.put( "vNonStrikerFirstName", vNonStrikerFirstName
	 * ); values.put( "vNonStrikerLastName", vNonStrikerLastName ); values.put(
	 * "vNonStrikerStats", vNonStrikerStats ); values.put("vAnnotation2",
	 * annotation2);
	 * 
	 * values.put("iRuns1", runs1); values.put("iOut1", out1);
	 * values.put("iBalls1", balls1); values.put("iStrikes1", strikes1);
	 * values.put("vBase1", base1);
	 * 
	 * values.put("iRuns2", runs2); values.put("iOut2", out2);
	 * values.put("iBalls2", balls2); values.put("iStrikes2", strikes2);
	 * values.put("vBase2", base2);
	 * 
	 * 
	 * values.put("iSuperGoals1", super_goals1); values.put("iGoals1", goals1);
	 * values.put("iBehinds1", behinds1);
	 * 
	 * values.put("iSuperGoals2", super_goals2); values.put("iGoals2", goals2);
	 * values.put("iBehinds2", behinds2);
	 * 
	 * values.put( "vRoundId", vRoundId );
	 * 
	 * values.put( "vShareUrl", vShareUrl ); values.put("iHasLiveUpdates",
	 * iHasLiveUpdates );
	 * 
	 * values.put("vLastEventName", vLastEventName );
	 * values.put("vShortMessage", vShortMessage ); values.put("vLongMessage",
	 * vLongMessage ); values.put("vSportType", vSportType );
	 * values.put("iActive1", active1 ); values.put("iActive2", active2 );
	 * 
	 * 
	 * values.put("associatedContestId", associatedContestId );
	 * values.put("associatedContestUrl", associatedContestUrl );
	 * values.put("vEventsUrl", vEventsUrl ); values.put("vEventsId", vEventsId
	 * ); values.put("vEventsExtensionUrl", vEventsExtensionUrl );
	 * 
	 * values.put("vStadiumName", vStadiumName );
	 * 
	 * 
	 * if(vBackgroundImageUrl != null && vBackgroundImageUrl.trim().length() >
	 * 0) values.put("vBackgroundImageUrl", vBackgroundImageUrl);
	 * 
	 * 
	 * values.put("vContestHref", vContestHref );
	 * values.put("vAssociatedContestHref", vAssociatedContestHref );
	 * values.put("vShareHref", vShareHref ); values.put("vEventsExtensionHref",
	 * vEventsExtensionHref ); values.put("vEventsHref", vEventsHref );
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vContestId FROM contests WHERE vContestId = \"" + vContestId +
	 * "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "contests", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "contests", values, " vContestId = \"" +
	 * vContestId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */
	// /**
	// * Praveen : as per href
	// */
	// /**
	// * Setting the contest data.
	// * @param vEventsId
	// * @param vEventsUrl
	// * @param vEventsExtensionUrl
	// */

	// public void setContestData ( final String vContestId, final String
	// vContestUrl, final String scheduled_start_time, String startTime, String
	// endTime,
	// String last_modified, String shortTitle, String title, int iTotal1,
	// String vSummary1, String vHomeTeamId, int iTotal2,
	// String vSummary2, String vAwayTeamId, int mins, int secs, int period, int
	// innings,
	// String innningsHalf, String vOvers, String vRunRate, String vLastBall,
	// String summary,
	// String vRoundContestId, String vCompetitionId, String vCompetitionLiveId
	// ,
	// String competitionName,String roundName,String sportsName,String
	// annotation,
	// int iWickets1, String vPlayerFirstName1, String vPlayerLastName1, String
	// vRole1, String vStats1,
	// int iWickets2, String vPlayerFirstName2, String vPlayerLastName2, String
	// vRole2, String vStats2,
	// String vStrikerFirstName,String vStrikerLastName,String vStrikerStats,
	// String vNonStrikerFirstName,String vNonStrikerLastName,String
	// vNonStrikerStats,String annotation2,
	// int runs1,int out1,int balls1,int strikes1,String base1,int runs2,int
	// out2,int balls2,int strikes2,String base2,
	// int super_goals1,int goals1,int behinds1,int super_goals2, int goals2,int
	// behinds2,
	// String vRoundId, String vShareUrl, int iHasLiveUpdates, String
	// vLastEventName,
	// String vShortMessage, String vLongMessage, String vSportType, int
	// active1, int active2,
	// String associatedContestId,String associatedContestUrl,
	// String vEventsExtensionUrl, String vEventsUrl, String vEventsId,String
	// vAssociatedContestHref,String vContestHref,
	// String vEventsExtensionHref,String vEventsHref,String vShareHref) {
	//
	//
	// // ,String vEventsUrl
	//
	//
	//
	//
	// ContentValues values = new ContentValues();
	//
	// values.put( "vContestId", vContestId );
	//
	// values.put( "vContestUrl", vContestUrl );
	// values.put( "dScheduledStartTime", scheduled_start_time );
	// values.put( "dStartTime", startTime );
	// values.put( "dEndTime", endTime );
	// values.put( "dLastModified", last_modified );
	// values.put( "vShortTitle", shortTitle );
	// values.put( "vTitle", title );
	// values.put( "iTotal1", iTotal1 );
	//
	// values.put( "vSummary1", vSummary1 );
	// values.put( "vHomeTeamId", vHomeTeamId );
	// values.put( "iTotal2", iTotal2 );
	// values.put( "vSummary2", vSummary2 );
	// values.put( "vAwayTeamId", vAwayTeamId );
	//
	// values.put( "iMins", mins );
	// values.put( "iSecs", secs );
	// values.put( "iPeriod", period );
	//
	// values.put( "iInnnings", innings );
	// values.put( "vInnningsHalf", innningsHalf );
	//
	// values.put( "vOvers", vOvers );
	// values.put( "vRunRate", vRunRate );
	// values.put( "vLastBall", vLastBall );
	//
	// values.put( "vCompetitionName", competitionName );
	// values.put( "vRoundName", roundName );
	// values.put( "vSportsName", sportsName );
	// values.put( "vAnnotation", annotation );
	//
	// values.put( "vSummary", summary );
	//
	//
	//
	// if ( vRoundContestId != null && vRoundContestId.trim().length() > 0 ) {
	// values.put( "vRoundContestId", vRoundContestId );
	// }
	//
	// if ( vCompetitionLiveId != null && vCompetitionLiveId.trim().length() > 0
	// ) {
	// values.put( "vCompetitionLiveId", vCompetitionLiveId );
	// }
	// values.put( "vCompetitionId", vCompetitionId );
	//
	// values.put( "iWickets1", iWickets1 );
	// values.put( "vPlayerFirstName1", vPlayerFirstName1 );
	// values.put( "vPlayerLastName1", vPlayerLastName1 );
	// values.put( "vRole1", vRole1 );
	// values.put( "vStats1", vStats1 );
	//
	// values.put( "iWickets2", iWickets2 );
	// values.put( "vPlayerFirstName2", vPlayerFirstName2 );
	// values.put( "vPlayerLastName2", vPlayerLastName2 );
	// values.put( "vRole2", vRole2 );
	// values.put( "vStats2", vStats2 );
	//
	// values.put( "vStrikerFirstName", vStrikerFirstName );
	// values.put( "vStrikerLastName", vStrikerLastName );
	// values.put( "vStrikerStats", vStrikerStats );
	// values.put( "vNonStrikerFirstName", vNonStrikerFirstName );
	// values.put( "vNonStrikerLastName", vNonStrikerLastName );
	// values.put( "vNonStrikerStats", vNonStrikerStats );
	// values.put("vAnnotation2", annotation2);
	//
	// values.put("iRuns1", runs1);
	// values.put("iOut1", out1);
	// values.put("iBalls1", balls1);
	// values.put("iStrikes1", strikes1);
	// values.put("vBase1", base1);
	//
	// values.put("iRuns2", runs2);
	// values.put("iOut2", out2);
	// values.put("iBalls2", balls2);
	// values.put("iStrikes2", strikes2);
	// values.put("vBase2", base2);
	//
	//
	// values.put("iSuperGoals1", super_goals1);
	// values.put("iGoals1", goals1);
	// values.put("iBehinds1", behinds1);
	//
	// values.put("iSuperGoals2", super_goals2);
	// values.put("iGoals2", goals2);
	// values.put("iBehinds2", behinds2);
	//
	// values.put( "vRoundId", vRoundId );
	//
	// values.put( "vShareUrl", vShareUrl );
	// values.put("iHasLiveUpdates", iHasLiveUpdates );
	//
	// values.put("vLastEventName", vLastEventName );
	// values.put("vShortMessage", vShortMessage );
	// values.put("vLongMessage", vLongMessage );
	// values.put("vSportType", vSportType );
	// values.put("iActive1", active1 );
	// values.put("iActive2", active2 );
	//
	//
	// values.put("associatedContestId", associatedContestId );
	// values.put("associatedContestUrl", associatedContestUrl );
	// values.put("vEventsUrl", vEventsUrl );
	// values.put("vEventsId", vEventsId );
	// values.put("vEventsExtensionUrl", vEventsExtensionUrl );
	//
	// values.put("associatedContestHref", vAssociatedContestHref );
	// values.put("vContestHref", vContestHref );
	// values.put("vEventsExtensionHref", vEventsExtensionHref );
	// values.put("vEventsHref", vEventsHref );
	// values.put("vShareHref", vShareHref );
	//
	//
	//
	//
	//
	//
	//
	//
	// int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	// "SELECT vContestId FROM contests WHERE vContestId = \"" + vContestId +
	// "\" " );
	//
	// if ( count == 0 ) {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_INSERT, null, "contests", values, null );
	// } else {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_UPDATE, null, "contests", values, " vContestId = \"" +
	// vContestId + "\" " );
	//
	// }
	//
	//
	// }

	public void setContestData(final String vContestId,
			final String vContestUrl, final String scheduled_start_time,
			String startTime, String endTime, String last_modified,
			String shortTitle, String title, int iTotal1, String vSummary1,
			String vHomeTeamId, int iTotal2, String vSummary2,
			String vAwayTeamId, int mins, int secs, int period, int innings,
			String innningsHalf, String vOvers, String vRunRate,
			String vLastBall, String summary, String vRoundContestId,
			String vCompetitionId, String vCompetitionLiveId,
			String competitionName, String roundName, String sportsName,
			String annotation, int iWickets1, String vPlayerFirstName1,
			String vPlayerLastName1, String vRole1, String vStats1,
			int iWickets2, String vPlayerFirstName2, String vPlayerLastName2,
			String vRole2, String vStats2, String vStrikerFirstName,
			String vStrikerLastName, String vStrikerStats,
			String vNonStrikerFirstName, String vNonStrikerLastName,
			String vNonStrikerStats, String annotation2, int runs1, int out1,
			int balls1, int strikes1, String base1, int runs2, int out2,
			int balls2, int strikes2, String base2, int super_goals1,
			int goals1, int behinds1, int super_goals2, int goals2,
			int behinds2, String vRoundId, String vShareUrl,
			int iHasLiveUpdates, String vLastEventName, String vShortMessage,
			String vLongMessage, String vSportType, int active1, int active2,
			String associatedContestId, String associatedContestUrl,
			String vEventsExtensionUrl, String vEventsUrl, String vEventsId,
			String vStadiumName, String vBackgroundImageUrl,
			String vAssociatedContestHref, String vContestHref,
			String vEventsExtensionHref, String vEventsHref, String vShareHref) {

		// ,String vEventsUrl

		ContentValues values = new ContentValues();

		values.put("vContestId", vContestId);

		values.put("vContestUrl", vContestUrl);
		values.put("dScheduledStartTime", scheduled_start_time);
		values.put("dStartTime", startTime);
		values.put("dEndTime", endTime);
		values.put("dLastModified", last_modified);
		values.put("vShortTitle", shortTitle);
		values.put("vTitle", title);
		values.put("iTotal1", iTotal1);

		values.put("vSummary1", vSummary1);
		values.put("vHomeTeamId", vHomeTeamId);
		values.put("iTotal2", iTotal2);
		values.put("vSummary2", vSummary2);
		values.put("vAwayTeamId", vAwayTeamId);

		values.put("iMins", mins);
		values.put("iSecs", secs);
		values.put("iPeriod", period);

		values.put("iInnnings", innings);
		values.put("vInnningsHalf", innningsHalf);

		values.put("vOvers", vOvers);
		values.put("vRunRate", vRunRate);
		values.put("vLastBall", vLastBall);

		values.put("vCompetitionName", competitionName);
		values.put("vRoundName", roundName);
		values.put("vSportsName", sportsName);
		values.put("vAnnotation", annotation);

		values.put("vSummary", summary);

		if (vRoundContestId != null && vRoundContestId.trim().length() > 0) {
			values.put("vRoundContestId", vRoundContestId);
		}

		if (vCompetitionLiveId != null
				&& vCompetitionLiveId.trim().length() > 0) {
			values.put("vCompetitionLiveId", vCompetitionLiveId);
		}
		values.put("vCompetitionId", vCompetitionId);

		values.put("iWickets1", iWickets1);
		values.put("vPlayerFirstName1", vPlayerFirstName1);
		values.put("vPlayerLastName1", vPlayerLastName1);
		values.put("vRole1", vRole1);
		values.put("vStats1", vStats1);

		values.put("iWickets2", iWickets2);
		values.put("vPlayerFirstName2", vPlayerFirstName2);
		values.put("vPlayerLastName2", vPlayerLastName2);
		values.put("vRole2", vRole2);
		values.put("vStats2", vStats2);

		values.put("vStrikerFirstName", vStrikerFirstName);
		values.put("vStrikerLastName", vStrikerLastName);
		values.put("vStrikerStats", vStrikerStats);
		values.put("vNonStrikerFirstName", vNonStrikerFirstName);
		values.put("vNonStrikerLastName", vNonStrikerLastName);
		values.put("vNonStrikerStats", vNonStrikerStats);
		values.put("vAnnotation2", annotation2);

		values.put("iRuns1", runs1);
		values.put("iOut1", out1);
		values.put("iBalls1", balls1);
		values.put("iStrikes1", strikes1);
		values.put("vBase1", base1);

		values.put("iRuns2", runs2);
		values.put("iOut2", out2);
		values.put("iBalls2", balls2);
		values.put("iStrikes2", strikes2);
		values.put("vBase2", base2);

		values.put("iSuperGoals1", super_goals1);
		values.put("iGoals1", goals1);
		values.put("iBehinds1", behinds1);

		values.put("iSuperGoals2", super_goals2);
		values.put("iGoals2", goals2);
		values.put("iBehinds2", behinds2);

		values.put("vRoundId", vRoundId);

		values.put("vShareUrl", vShareUrl);
		values.put("iHasLiveUpdates", iHasLiveUpdates);

		values.put("vLastEventName", vLastEventName);
		values.put("vShortMessage", vShortMessage);
		values.put("vLongMessage", vLongMessage);
		values.put("vSportType", vSportType);
		values.put("iActive1", active1);
		values.put("iActive2", active2);

		values.put("associatedContestId", associatedContestId);
		values.put("associatedContestUrl", associatedContestUrl);
		values.put("vEventsUrl", vEventsUrl);
		values.put("vEventsId", vEventsId);
		values.put("vEventsExtensionUrl", vEventsExtensionUrl);

		values.put("vStadiumName", vStadiumName);

		if (vBackgroundImageUrl != null
				&& vBackgroundImageUrl.trim().length() > 0)
			values.put("vBackgroundImageUrl", vBackgroundImageUrl);
		values.put("vContestHref", vContestHref);
		// values.put("vContestHref", vContestUrl );
		values.put("vAssociatedContestHref", vAssociatedContestHref);
		values.put("vShareHref", vShareHref);
		values.put("vEventsExtensionHref", vEventsExtensionHref);
		values.put("vEventsHref", vEventsHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestId FROM contests WHERE vContestId = \""
						+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "contests", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "contests", values,
					" vContestId = \"" + vContestId + "\" ");

		}

	}

	/**
	 * Setting the associated contest data.
	 * 
	 * @param vEventsId
	 * @param vEventsUrl
	 * @param vEventsExtensionUrl
	 */

	// public void setContestDataForAssociatedContests ( final String
	// vContestId, final String vContestUrl, final String scheduled_start_time,
	// String startTime, String endTime,
	// String last_modified, String shortTitle, String title, int iTotal1,
	// String vSummary1, String vHomeTeamId, int iTotal2,
	// String vSummary2, String vAwayTeamId, int mins, int secs, int period, int
	// innings,
	// String innningsHalf, String vOvers, String vRunRate, String vLastBall,
	// String summary,
	// String vRoundContestId, String vCompetitionId, String vCompetitionLiveId
	// ,
	// String competitionName,String roundName,String sportsName,String
	// annotation,
	// int iWickets1, String vPlayerFirstName1, String vPlayerLastName1, String
	// vRole1, String vStats1,
	// int iWickets2, String vPlayerFirstName2, String vPlayerLastName2, String
	// vRole2, String vStats2,
	// String vStrikerFirstName,String vStrikerLastName,String vStrikerStats,
	// String vNonStrikerFirstName,String vNonStrikerLastName,String
	// vNonStrikerStats,String annotation2,
	// int runs1,int out1,int balls1,int strikes1,String base1,int runs2,int
	// out2,int balls2,int strikes2,String base2,
	// int super_goals1,int goals1,int behinds1,int super_goals2, int goals2,int
	// behinds2,
	// String vRoundId, String vShareUrl, int iHasLiveUpdates, String
	// vLastEventName,
	// String vShortMessage, String vLongMessage, String vSportType, int
	// active1, int active2,
	// String associatedContestId,String associatedContestUrl, String
	// vEventsExtensionUrl, String vEventsUrl, String vEventsId) {
	//
	// // ,String vEventsUrl
	//
	//
	//
	//
	// ContentValues values = new ContentValues();
	//
	// values.put( "vContestId", vContestId );
	//
	// values.put( "vContestUrl", vContestUrl );
	// values.put( "dScheduledStartTime", scheduled_start_time );
	// values.put( "dStartTime", startTime );
	// values.put( "dEndTime", endTime );
	// values.put( "dLastModified", last_modified );
	// values.put( "vShortTitle", shortTitle );
	// values.put( "vTitle", title );
	// values.put( "iTotal1", iTotal1 );
	//
	// values.put( "vSummary1", vSummary1 );
	// values.put( "vHomeTeamId", vHomeTeamId );
	// values.put( "iTotal2", iTotal2 );
	// values.put( "vSummary2", vSummary2 );
	// values.put( "vAwayTeamId", vAwayTeamId );
	//
	// values.put( "iMins", mins );
	// values.put( "iSecs", secs );
	// values.put( "iPeriod", period );
	//
	// values.put( "iInnnings", innings );
	// values.put( "vInnningsHalf", innningsHalf );
	//
	// values.put( "vOvers", vOvers );
	// values.put( "vRunRate", vRunRate );
	// values.put( "vLastBall", vLastBall );
	//
	// values.put( "vCompetitionName", competitionName );
	// values.put( "vRoundName", roundName );
	// values.put( "vSportsName", sportsName );
	// values.put( "vAnnotation", annotation );
	//
	// values.put( "vSummary", summary );
	//
	//
	// if ( vRoundContestId != null && vRoundContestId.trim().length() > 0 ) {
	// values.put( "vRoundContestId", vRoundContestId );
	// }
	//
	// if ( vCompetitionLiveId != null && vCompetitionLiveId.trim().length() > 0
	// ) {
	// values.put( "vCompetitionLiveId", vCompetitionLiveId );
	// }
	// values.put( "vCompetitionId", vCompetitionId );
	//
	// values.put( "iWickets1", iWickets1 );
	// values.put( "vPlayerFirstName1", vPlayerFirstName1 );
	// values.put( "vPlayerLastName1", vPlayerLastName1 );
	// values.put( "vRole1", vRole1 );
	// values.put( "vStats1", vStats1 );
	//
	// values.put( "iWickets2", iWickets2 );
	// values.put( "vPlayerFirstName2", vPlayerFirstName2 );
	// values.put( "vPlayerLastName2", vPlayerLastName2 );
	// values.put( "vRole2", vRole2 );
	// values.put( "vStats2", vStats2 );
	//
	// values.put( "vStrikerFirstName", vStrikerFirstName );
	// values.put( "vStrikerLastName", vStrikerLastName );
	// values.put( "vStrikerStats", vStrikerStats );
	// values.put( "vNonStrikerFirstName", vNonStrikerFirstName );
	// values.put( "vNonStrikerLastName", vNonStrikerLastName );
	// values.put( "vNonStrikerStats", vNonStrikerStats );
	// values.put("vAnnotation2", annotation2);
	//
	// values.put("iRuns1", runs1);
	// values.put("iOut1", out1);
	// values.put("iBalls1", balls1);
	// values.put("iStrikes1", strikes1);
	// values.put("vBase1", base1);
	//
	// values.put("iRuns2", runs2);
	// values.put("iOut2", out2);
	// values.put("iBalls2", balls2);
	// values.put("iStrikes2", strikes2);
	// values.put("vBase2", base2);
	//
	//
	// values.put("iSuperGoals1", super_goals1);
	// values.put("iGoals1", goals1);
	// values.put("iBehinds1", behinds1);
	//
	// values.put("iSuperGoals2", super_goals2);
	// values.put("iGoals2", goals2);
	// values.put("iBehinds2", behinds2);
	//
	// values.put( "vRoundId", vRoundId );
	//
	// values.put( "vShareUrl", vShareUrl );
	// values.put("iHasLiveUpdates", iHasLiveUpdates );
	//
	// values.put("vLastEventName", vLastEventName );
	// values.put("vShortMessage", vShortMessage );
	// values.put("vLongMessage", vLongMessage );
	// values.put("vSportType", vSportType );
	// values.put("iActive1", active1 );
	// values.put("iActive2", active2 );
	//
	//
	// values.put("associatedContestId", associatedContestId );
	// values.put("associatedContestUrl", associatedContestUrl );
	// values.put("vEventsUrl", vEventsUrl );
	// values.put("vEventsId", vEventsId );
	// values.put("vEventsExtensionUrl", vEventsExtensionUrl );
	//
	//
	//
	//
	//
	// int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	// "SELECT vContestId FROM associatedContestsData WHERE vContestId = \"" +
	// vContestId + "\" " );
	//
	// if ( count == 0 ) {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_INSERT, null, "associatedContestsData", values, null );
	// } else {
	// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	// Constants.QUERY_UPDATE, null, "associatedContestsData", values,
	// " vContestId = \"" + vContestId + "\" " );
	//
	// }
	//
	//
	// }

	/**
	 * Setting the associated contest data.
	 * 
	 * @param vEventsId
	 * @param vEventsUrl
	 * @param vEventsExtensionUrl
	 */

	public void setContestDataForAssociatedContests(final String vContestId,
			final String vContestUrl, final String scheduled_start_time,
			String startTime, String endTime, String last_modified,
			String shortTitle, String title, int iTotal1, String vSummary1,
			String vHomeTeamId, int iTotal2, String vSummary2,
			String vAwayTeamId, int mins, int secs, int period, int innings,
			String innningsHalf, String vOvers, String vRunRate,
			String vLastBall, String summary, String vRoundContestId,
			String vCompetitionId, String vCompetitionLiveId,
			String competitionName, String roundName, String sportsName,
			String annotation, int iWickets1, String vPlayerFirstName1,
			String vPlayerLastName1, String vRole1, String vStats1,
			int iWickets2, String vPlayerFirstName2, String vPlayerLastName2,
			String vRole2, String vStats2, String vStrikerFirstName,
			String vStrikerLastName, String vStrikerStats,
			String vNonStrikerFirstName, String vNonStrikerLastName,
			String vNonStrikerStats, String annotation2, int runs1, int out1,
			int balls1, int strikes1, String base1, int runs2, int out2,
			int balls2, int strikes2, String base2, int super_goals1,
			int goals1, int behinds1, int super_goals2, int goals2,
			int behinds2, String vRoundId, String vShareUrl,
			int iHasLiveUpdates, String vLastEventName, String vShortMessage,
			String vLongMessage, String vSportType, int active1, int active2,
			String associatedContestId, String associatedContestUrl,
			String vEventsExtensionUrl, String vEventsUrl, String vEventsId,
			String vAssociatedContestHref, String vContestHref,
			String vEventsExtensionHref, String vEventsHref, String vShareHref) {

		// ,String vEventsUrl

		ContentValues values = new ContentValues();

		values.put("vContestId", vContestId);

		values.put("vContestUrl", vContestUrl);
		values.put("dScheduledStartTime", scheduled_start_time);
		values.put("dStartTime", startTime);
		values.put("dEndTime", endTime);
		values.put("dLastModified", last_modified);
		values.put("vShortTitle", shortTitle);
		values.put("vTitle", title);
		values.put("iTotal1", iTotal1);

		values.put("vSummary1", vSummary1);
		values.put("vHomeTeamId", vHomeTeamId);
		values.put("iTotal2", iTotal2);
		values.put("vSummary2", vSummary2);
		values.put("vAwayTeamId", vAwayTeamId);

		values.put("iMins", mins);
		values.put("iSecs", secs);
		values.put("iPeriod", period);

		values.put("iInnnings", innings);
		values.put("vInnningsHalf", innningsHalf);

		values.put("vOvers", vOvers);
		values.put("vRunRate", vRunRate);
		values.put("vLastBall", vLastBall);

		values.put("vCompetitionName", competitionName);
		values.put("vRoundName", roundName);
		values.put("vSportsName", sportsName);
		values.put("vAnnotation", annotation);

		values.put("vSummary", summary);

		if (vRoundContestId != null && vRoundContestId.trim().length() > 0) {
			values.put("vRoundContestId", vRoundContestId);
		}

		if (vCompetitionLiveId != null
				&& vCompetitionLiveId.trim().length() > 0) {
			values.put("vCompetitionLiveId", vCompetitionLiveId);
		}
		values.put("vCompetitionId", vCompetitionId);

		values.put("iWickets1", iWickets1);
		values.put("vPlayerFirstName1", vPlayerFirstName1);
		values.put("vPlayerLastName1", vPlayerLastName1);
		values.put("vRole1", vRole1);
		values.put("vStats1", vStats1);

		values.put("iWickets2", iWickets2);
		values.put("vPlayerFirstName2", vPlayerFirstName2);
		values.put("vPlayerLastName2", vPlayerLastName2);
		values.put("vRole2", vRole2);
		values.put("vStats2", vStats2);

		values.put("vStrikerFirstName", vStrikerFirstName);
		values.put("vStrikerLastName", vStrikerLastName);
		values.put("vStrikerStats", vStrikerStats);
		values.put("vNonStrikerFirstName", vNonStrikerFirstName);
		values.put("vNonStrikerLastName", vNonStrikerLastName);
		values.put("vNonStrikerStats", vNonStrikerStats);
		values.put("vAnnotation2", annotation2);

		values.put("iRuns1", runs1);
		values.put("iOut1", out1);
		values.put("iBalls1", balls1);
		values.put("iStrikes1", strikes1);
		values.put("vBase1", base1);

		values.put("iRuns2", runs2);
		values.put("iOut2", out2);
		values.put("iBalls2", balls2);
		values.put("iStrikes2", strikes2);
		values.put("vBase2", base2);

		values.put("iSuperGoals1", super_goals1);
		values.put("iGoals1", goals1);
		values.put("iBehinds1", behinds1);

		values.put("iSuperGoals2", super_goals2);
		values.put("iGoals2", goals2);
		values.put("iBehinds2", behinds2);

		values.put("vRoundId", vRoundId);

		values.put("vShareUrl", vShareUrl);
		values.put("iHasLiveUpdates", iHasLiveUpdates);

		values.put("vLastEventName", vLastEventName);
		values.put("vShortMessage", vShortMessage);
		values.put("vLongMessage", vLongMessage);
		values.put("vSportType", vSportType);
		values.put("iActive1", active1);
		values.put("iActive2", active2);

		values.put("associatedContestId", associatedContestId);
		values.put("associatedContestUrl", associatedContestUrl);
		values.put("vEventsUrl", vEventsUrl);
		values.put("vEventsId", vEventsId);
		values.put("vEventsExtensionUrl", vEventsExtensionUrl);

		values.put("associatedContestHref", vAssociatedContestHref);
		values.put("vContestHref", vContestHref);
		values.put("vEventsExtensionHref", vEventsExtensionHref);
		values.put("vEventsHref", vEventsHref);
		values.put("vShareHref", vShareHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestId FROM associatedContestsData WHERE vContestId = \""
						+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "associatedContestsData",
					values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "associatedContestsData",
					values, " vContestId = \"" + vContestId + "\" ");

		}

	}

	public void setContestData(ContentValues values, String vContestId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContestId FROM contests WHERE vContestId = \""
						+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "contests", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "contests", values,
					" vContestId = \"" + vContestId + "\" ");

		}

	}

	/**
	 * setting leader board data
	 */
	public void setLeaderBoardData(String vContestId, String vTeamId,
			String vSummary, int iPosition, String vPositionSummary) {

		ContentValues values = new ContentValues();
		values.put("vContestId", vContestId);
		values.put("vTeamId", vTeamId);
		values.put("vSummary", vSummary);
		values.put("iPosition", iPosition);
		values.put("vPositionSummary", vPositionSummary);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vTeamId FROM leaderBoard WHERE vContestId = \""
						+ vContestId + "\" AND  vTeamId =\"" + vTeamId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "leaderBoard", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"leaderBoard",
					values,
					"vContestId = \"" + vContestId + "\" AND  vTeamId =\""
							+ vTeamId + "\"");

		}

	}

	public void setSummariesData(String vTeamId, String vContestId,
			String summary, int position) {

		ContentValues values = new ContentValues();
		values.put("vContestId", vContestId);
		values.put("vTeamId", vTeamId);
		values.put("vSummary", summary);
		values.put("iPosition", position);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vTeamId FROM summaries WHERE vContestId = \""
						+ vContestId + "\" AND  vTeamId =\"" + vTeamId
						+ "\" AND  iPosition =\"" + position + "\"");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "summaries", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"summaries",
					values,
					"vContestId = \"" + vContestId + "\" AND  vTeamId =\""
							+ vTeamId + "\" AND  iPosition =\"" + position
							+ "\"");

		}

	}

	public void setPollingUrl(final String vUrl, final String vPollingUrl) {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					int count = PlayupLiveApplication.getDatabaseWrapper()
							.getTotalCount(
									"SELECT 	vPollingUrl FROM poll WHERE vUrl = \""
											+ vUrl + "\" ");

					if (count == 0) {

						count = PlayupLiveApplication.getDatabaseWrapper()
								.getTotalCount(
										"SELECT vPollingUrl FROM poll WHERE vPollingUrl = \""
												+ vUrl + "\" ");

						if (count == 0) {

							ContentValues values = new ContentValues();
							values.put("vPollingUrl", vPollingUrl);
							values.put("vUrl", vUrl);

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_INSERT, null, "poll",
									values, null);
						} else {

							ContentValues values = new ContentValues();
							values.put("vPollingUrl", vPollingUrl);

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_UPDATE, null, "poll",
									values, " vPollingUrl = \"" + vUrl + "\" ");
						}
					} else {
						ContentValues values = new ContentValues();
						values.put("vPollingUrl", vPollingUrl);
						JsonUtil json = new JsonUtil();
						json.json_method(null, -100, false,
								Constants.QUERY_UPDATE, null, "poll", values,
								" vUrl = \"" + vUrl + "\" ");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}
			}

		};
		Thread th = new Thread(r);
		th.start();

	}

	public String getConversationMessagePollingUrl(String vConversationId) {

		String vPollingUrl = null;

		String vUrl = getConversationMessagesUrl(vConversationId);

		vPollingUrl = vUrl;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vPollingUrl FROM poll WHERE vUrl = \"" + vUrl + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vPollingUrl = c.getString(c.getColumnIndex("vPollingUrl"));
				if (vPollingUrl == null) {
					vPollingUrl = vUrl;
				}
			}
			c.close();
		}
		c = null;
		return vPollingUrl;
	}

	public void setSportsCompetition(final String vSportsId,
			final String vSportsCompetitionId,
			final String vSportsCompetitionUrl,
			final String vSportsCompetitionHref) {

		ContentValues values = new ContentValues();

		values.put("vSportsId", vSportsId);
		values.put("vSportsCompetitionId", vSportsCompetitionId);
		values.put("vSportsCompetitionUrl", vSportsCompetitionUrl);

		values.put("vSportsCompetitionHref", vSportsCompetitionHref);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						"SELECT v"
								+ "SportsCompetitionId FROM sports_competition WHERE vSportsCompetitionId = \""
								+ vSportsCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sports_competition", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(
							Constants.QUERY_UPDATE,
							null,
							"sports_competition",
							values,
							" vSportsCompetitionId = \"" + vSportsCompetitionId
									+ "\" ");
		}

	}

	public void setSportsLive(final String vSportsId,
			final String vSportsLiveId, final String vSportsLiveUrl,
			final String vSportsLiveHref) {

		ContentValues values = new ContentValues();

		values.put("vSportsId", vSportsId);
		values.put("vSportsLiveId", vSportsLiveId);
		values.put("vSportsLiveUrl", vSportsLiveUrl);
		values.put("vSportsLiveHref", vSportsLiveHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vSportsLiveId FROM sports_live WHERE vSportsLiveId = \""
						+ vSportsLiveId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sports_live", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sports_live", values,
					" vSportsLiveId = \"" + vSportsLiveId + "\" ");
		}

	}

	/*	*//**
	 * Setting the round data.
	 */
	/*
	 * public void setRound ( final String uid, final String self_url ) {
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vRoundId", uid ); values.put( "vSelfUrl", self_url );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vRoundId FROM rounds WHERE vRoundId = \"" + uid + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_INSERT, null, "rounds", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_UPDATE, null, "rounds", values, " vRoundId = \"" + uid +
	 * "\" " ); } }
	 */

	/**
	 * Setting the Team data.
	 */
	public void setTeam(final String uid, final String self_url) {

		ContentValues values = new ContentValues();

		values.put("vTeamId", uid);
		values.put("vSelfUrl", self_url);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vTeamId FROM teams WHERE vTeamId = \"" + uid + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "teams", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "teams", values,
					" vTeamId = \"" + uid + "\" ");
		}

	}

	public void setSports(int iOrder, final String uid, String type,
			final String self_url, final String name,
			final String vFeatureLogoUrl, final String vTileLogoUrl,
			final int live_contests, final String vSportsCompetitionUid,
			final String vSportsLiveUid, final String vSportsHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("iOrder", iOrder);
		values.put("vSportType", type);
		values.put("vSportsId", uid);
		values.put("vSelfUrl", self_url);
		values.put("vName", name);

		values.put("vFeatureLogoUrl", vFeatureLogoUrl);
		values.put("vTileLogoUrl", vTileLogoUrl);

		values.put("iLiveNum", live_contests);
		values.put("vSportsCompetitionUid", vSportsCompetitionUid);
		values.put("vSportsLiveUid", vSportsLiveUid);
		values.put("vSportsHrefUrl", vSportsHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vSportsId FROM sports WHERE vSportsId = \"" + uid
						+ "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sports", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sports", values,
					" vSportsId = \"" + uid + "\" ");
		}

	}

	/*	*//**
	 * Setting the Competition data.
	 */
	/*
	 * 
	 * public void setCompetition (int iOrder, final String vCompetitionId,
	 * final String vCompetitionUrl, final String name, final String shortName,
	 * final String region, final String vLogoUrl, final int iLiveNum, final
	 * String vCompetitionLiveId, final String vCompetitionRoundId, final String
	 * vCompetitionCurrentRoundId, final String vCompetitionTeamId, final String
	 * vSportsCompetitionUid ,final String vSectionId, final String
	 * vSectionUrl,final String vCompetitionHref,final String vSectionHref) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues(); values.put( "iOrder", iOrder
	 * ); values.put( "vCompetitionId", vCompetitionId ); values.put(
	 * "vCompetitionUrl", vCompetitionUrl ); values.put( "vCompetitonName", name
	 * ); values.put( "vShortName", shortName ); values.put( "vRegion", region
	 * ); values.put( "vLogoUrl", vLogoUrl ); values.put( "iLiveNum", iLiveNum
	 * ); values.put( "vCompetitionLiveId", vCompetitionLiveId ); values.put(
	 * "vCompetitionRoundId", vCompetitionRoundId ); values.put(
	 * "vCompetitionCurrentRoundId", vCompetitionCurrentRoundId );
	 * 
	 * values.put( "vCompetitionTeamId", vCompetitionTeamId ); values.put(
	 * "vSportsCompetitionId", vSportsCompetitionUid ); values.put(
	 * "vSectionId", vSectionId); values.put( "vSectionUrl", vSectionUrl);
	 * 
	 * values.put( "vCompetitionHref", vCompetitionHref);
	 * 
	 * values.put( "vSectionHref", vSectionHref);
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vCompetitionId FROM competition WHERE vCompetitionId = \"" +
	 * vCompetitionId + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "competition", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "competition", values,
	 * " vCompetitionId = \"" + vCompetitionId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href Setting the Competition data.
	 */

	public void setCompetition(int iOrder, final String vCompetitionId,
			final String vCompetitionUrl, final String vCompetitionHrefUrl,
			final String name, final String shortName, final String region,
			final String vLogoUrl, final int iLiveNum,
			final String vCompetitionLiveId, final String vCompetitionRoundId,
			final String vCompetitionCurrentRoundId,
			final String vCompetitionTeamId,
			final String vSportsCompetitionUid, final String vSectionId,
			final String vSectionUrl, final String sectionHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("iOrder", iOrder);
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionUrl", vCompetitionUrl);
		values.put("vCompetitonName", name);
		values.put("vShortName", shortName);
		values.put("vRegion", region);
		values.put("vLogoUrl", vLogoUrl);
		values.put("iLiveNum", iLiveNum);
		values.put("vCompetitionLiveId", vCompetitionLiveId);
		values.put("vCompetitionRoundId", vCompetitionRoundId);
		values.put("vCompetitionCurrentRoundId", vCompetitionCurrentRoundId);

		values.put("vCompetitionTeamId", vCompetitionTeamId);
		values.put("vSportsCompetitionId", vSportsCompetitionUid);
		values.put("vSectionId", vSectionId);
		values.put("vSectionUrl", vSectionUrl);

		values.put("vCompetitionHref", vCompetitionHrefUrl);
		values.put("vSectionHref", sectionHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	/*
	 * public void setCompetition ( final String vCompetitionId, final String
	 * vCompetitionUrl, final String name, final String shortName, final String
	 * region, final String vLogoUrl, final int iLiveNum, final String
	 * vCompetitionLiveId, final String vCompetitionRoundId, final String
	 * vCompetitionCurrentRoundId, final String vCompetitionTeamId,final String
	 * vSectionId, final String vSectionUrl,final String vCompetitionHref,final
	 * String vSectionHref) {
	 * 
	 * ContentValues values = new ContentValues(); values.put( "vCompetitionId",
	 * vCompetitionId ); values.put( "vCompetitionUrl", vCompetitionUrl );
	 * values.put( "vCompetitonName", name ); values.put( "vShortName",
	 * shortName ); values.put( "vRegion", region ); values.put( "vLogoUrl",
	 * vLogoUrl ); values.put( "iLiveNum", iLiveNum ); values.put(
	 * "vCompetitionLiveId", vCompetitionLiveId ); values.put(
	 * "vCompetitionRoundId", vCompetitionRoundId ); values.put(
	 * "vCompetitionCurrentRoundId", vCompetitionCurrentRoundId ); values.put(
	 * "vCompetitionTeamId", vCompetitionTeamId ); values.put( "vSectionId",
	 * vSectionId ); values.put( "vSectionUrl", vSectionUrl );
	 * 
	 * values.put( "vCompetitionHref", vCompetitionHref ); values.put(
	 * "vSectionHref", vSectionHref );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vCompetitionId FROM competition WHERE vCompetitionId = \"" +
	 * vCompetitionId + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "competition", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "competition", values,
	 * " vCompetitionId = \"" + vCompetitionId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */
	/**
	 * Praveen: as per the href
	 */
	public void setCompetition(final String vCompetitionId,
			final String vCompetitionUrl, final String name,
			final String shortName, final String region, final String vLogoUrl,
			final int iLiveNum, final String vCompetitionLiveId,
			final String vCompetitionRoundId,
			final String vCompetitionCurrentRoundId,
			final String vCompetitionTeamId, final String vSectionId,
			final String vSectionUrl, final String vCompetitionHrefUrl,
			final String sectionHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionUrl", vCompetitionUrl);
		values.put("vCompetitonName", name);
		values.put("vShortName", shortName);
		values.put("vRegion", region);
		values.put("vLogoUrl", vLogoUrl);
		values.put("iLiveNum", iLiveNum);
		values.put("vCompetitionLiveId", vCompetitionLiveId);
		values.put("vCompetitionRoundId", vCompetitionRoundId);
		values.put("vCompetitionCurrentRoundId", vCompetitionCurrentRoundId);
		values.put("vCompetitionTeamId", vCompetitionTeamId);
		values.put("vSectionId", vSectionId);
		values.put("vSectionUrl", vSectionUrl);

		values.put("vCompetitionHref", vCompetitionHrefUrl);
		values.put("vSectionHref", sectionHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public void setCompetition(final String vCompetitionId,
			final String vCompetitionUrl, final String name,
			final String shortName, final String region, final String vLogoUrl) {

		ContentValues values = new ContentValues();
		;
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionUrl", vCompetitionUrl);
		values.put("vCompetitonName", name);
		values.put("vShortName", shortName);
		values.put("vRegion", region);
		values.put("vLogoUrl", vLogoUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	/*
	 * public void setSectionData ( final String vSectionId, final String
	 * vSectionUrl) {
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vSectionId", vSectionId ); values.put( "vSectionUrl",
	 * vSectionUrl );
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vSectionId FROM sections WHERE vSectionId = \"" + vSectionId +
	 * "\" " );
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "sections", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "sections", values, " vSectionId = \"" +
	 * vSectionId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public void setSectionData(final String vSectionId,
			final String vSectionUrl, final String vHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vSectionId", vSectionId);
		values.put("vSectionUrl", vSectionUrl);

		values.put("vSectionHrefUrl", vHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vSectionId FROM sections WHERE vSectionId = \""
						+ vSectionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sections", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sections", values,
					" vSectionId = \"" + vSectionId + "\" ");

		}

	}

	/*
	 * Praveen: Modified as per HREF link : vSectionUrl
	 */

	public void setSectionData(final String vSectionId,
			final String vSectionUrl, final String vSectionHrefUrl,
			final String vBlockId, final int iBlockCount, final String vTitle) {

		ContentValues values = new ContentValues();

		values.put("vSectionId", vSectionId);
		values.put("vSectionUrl", vSectionUrl);
		values.put("vBlockId", vBlockId);
		values.put("iBlockCount", iBlockCount);
		values.put("vTitle", vTitle);
		// values.put( "vHref", vSectionHrefUrl );
		values.put("vSectionHrefUrl", vSectionHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vSectionId FROM sections WHERE vSectionId = \""
						+ vSectionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sections", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sections", values,
					" vSectionId = \"" + vSectionId + "\" ");

		}

	}

	/*
	 * public void setSectionData ( final String vSectionId, final String
	 * vSectionUrl, final String vBlockId, final int iBlockCount,final String
	 * vTitle ) { ======= public void setSectionData ( final String vSectionId,
	 * final String vSectionUrl, final String vBlockId, final int
	 * iBlockCount,final String vTitle,String vHref ) { >>>>>>> Stashed changes
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vSectionId", vSectionId ); values.put( "vSectionUrl",
	 * vSectionUrl ); values.put( "vBlockId", vBlockId ); values.put(
	 * "iBlockCount", iBlockCount ); values.put( "vTitle", vTitle ); values.put(
	 * "vHref", vHref );
	 * 
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vSectionId FROM sections WHERE vSectionId = \"" + vSectionId +
	 * "\" " );
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "sections", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "sections", values, " vSectionId = \"" +
	 * vSectionId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	public void deleteSectionBlockContent(String vSectionId) {

		// Cursor c = (Cursor)
		// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
		// Constants.QUERY_RAW,
		// " DELETE FROM blockContent WHERE vBlockContentId = " +
		// "( SELECT  b.vBlockFeatureId FROM blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE s.vSectionId = \""
		// + vSectionId + "\" ) " +
		// "OR   vBlockContentId = ( SELECT  b.vBlockTileId FROM blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE s.vSectionId = \""
		// + vSectionId + "\"  )" +
		// "OR   vBlockContentId = ( SELECT  b.vBlockStackId FROM blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE s.vSectionId = \""
		// + vSectionId + "\"  ) ", null, null, null );
		//
		// if ( c != null ) {
		// Logs.show(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> inside deletesectionBlockContent"+c.getCount());
		// c.close();
		// }

		int c = (Integer) PlayupLiveApplication
				.getDatabaseWrapper()
				.queryMethod2(
						Constants.QUERY_DELETE,
						null,
						"blockContent",
						null,
						"vBlockContentId IN "
								+ "( SELECT  b.vBlockItemId FROM blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE s.vSectionId = \""
								+ vSectionId + "\" ) ");

	}

	public void setBlockFeature(final String vBlockId,
			final String vBlockFeatureId, final int iBlockFeaturesCount) {

		ContentValues values = new ContentValues();

		values.put("vBlockId", vBlockId);
		values.put("vBlockFeatureId", vBlockFeatureId);
		values.put("iBlockFeaturesCount", iBlockFeaturesCount);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vBlockId " + "FROM blocks WHERE vBlockId = \""
						+ vBlockId + "\" AND vBlockFeatureId = \""
						+ vBlockFeatureId + "\"");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blocks", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blocks",
					values,
					" vBlockId = \"" + vBlockId + "\" AND vBlockFeatureId = \""
							+ vBlockFeatureId + "\"");

		}

	}

	public void setBlockTile(final String vBlockId, final String vBlockTileId,
			final int iBlockTilesCount) {

		ContentValues values = new ContentValues();

		values.put("vBlockId", vBlockId);
		values.put("vBlockTileId", vBlockTileId);
		values.put("iBlockTilesCount", iBlockTilesCount);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vBlockId " + "FROM blocks WHERE vBlockId = \""
						+ vBlockId + "\" AND vBlockTileId = \"" + vBlockTileId
						+ "\"");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blocks", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blocks",
					values,
					" vBlockId = \"" + vBlockId + "\" AND vBlockTileId = \""
							+ vBlockTileId + "\"");

		}

	}

	public void setBlockStack(final String vBlockId,
			final String vBlockStackId, final int iBlockStackCount) {

		ContentValues values = new ContentValues();

		values.put("vBlockId", vBlockId);
		values.put("vBlockStackId", vBlockStackId);
		values.put("iBlockStackCount", iBlockStackCount);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vBlockId FROM blocks WHERE vBlockId = \"" + vBlockId
						+ "\" AND vBlockStackId = \"" + vBlockStackId + "\"");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blocks", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blocks",
					values,
					" vBlockId = \"" + vBlockId + "\" AND vBlockStackId = \""
							+ vBlockStackId + "\"");

		}

	}

	/**
	 * storing the section blocks in blocks table
	 */
	public void setBlockItems(String vBlockId, String vBlockItemId,
			int iBlockItemCount, String vBlockItemType, int iOrder) {
		ContentValues values = new ContentValues();

		values.put("vBlockId", vBlockId);
		values.put("vBlockItemId", vBlockItemId);
		values.put("iBlockItemCount", iBlockItemCount);
		values.put("vBlockItemType", vBlockItemType);
		values.put("iOrder", iOrder);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vBlockId FROM blocks WHERE vBlockId = \"" + vBlockId
						+ "\" AND vBlockItemId = \"" + vBlockItemId + "\"");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blocks", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blocks",
					values,
					" vBlockId = \"" + vBlockId + "\" AND vBlockItemId = \""
							+ vBlockItemId + "\"");

		}

	}

	/*
	 * public void setUserNotification ( String vNotificationId, String
	 * vNotificationUrl, int isRead, String dDate, String vSubjectType, String
	 * vUserSelfUrl, String vUserId, String vUserType, String vUserName, String
	 * vAvatarUrl, String vDetailTitle, String vDetailSubTitle, String
	 * vDetailType, String vDetailMessage, String vConversationId, String
	 * vConversationUrl, String vStatus, int iGapSize, String vGapId, String
	 * vGapUrl ) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values.put( "vNotificationId", vNotificationId ); values.put(
	 * "vNotificationUrl", vNotificationUrl ); values.put( "isRead", isRead );
	 * values.put( "dTime", dDate ); values.put( "vStatus", vStatus );
	 * 
	 * values.put( "vSubjectType", vSubjectType ); values.put( "vUserSelfUrl",
	 * vUserSelfUrl ); values.put( "vUserId", vUserId ); values.put(
	 * "vUserType", vUserType ); values.put( "vDisplayName", vUserName );
	 * values.put( "vAvatarUrl", vAvatarUrl );
	 * 
	 * values.put( "vDetailTitle", vDetailTitle ); values.put(
	 * "vDetailSubTitle", vDetailSubTitle ); values.put( "vDetailType",
	 * vDetailType ); values.put( "vDetailMessage", vDetailMessage );
	 * 
	 * values.put( "vConversationId", vConversationId ); values.put(
	 * "vConversationUrl", vConversationUrl );
	 * 
	 * values.put( "iGapSize", iGapSize ); values.put( "vGapId", vGapId );
	 * values.put( "vGapUrl", vGapUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vNotificationId FROM notification WHERE vNotificationId = \"" +
	 * vNotificationId + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "notification", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "notification", values,
	 * " vNotificationId = \"" + vNotificationId + "\" " );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	public void setUserNotification(String vNotificationId,
			String vNotificationUrl, String vNotificationHrefUrl, int isRead,
			String dDate, String vSubjectType, String vUserSelfUrl,
			String vUserHrefUrl, String vUserId, String vUserType,
			String vUserName, String vAvatarUrl, String vDetailTitle,
			String vDetailSubTitle, String vDetailType, String vDetailMessage,
			String vConversationId, String vConversationUrl,
			String vConversationHrefUrl, String vStatus, int iGapSize,
			String vGapId, String vGapUrl, String vGapHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vNotificationId", vNotificationId);
		values.put("vNotificationUrl", vNotificationUrl);
		values.put("isRead", isRead);
		values.put("dTime", dDate);
		values.put("vStatus", vStatus);

		values.put("vSubjectType", vSubjectType);
		values.put("vUserSelfUrl", vUserSelfUrl);
		values.put("vUserId", vUserId);
		values.put("vUserType", vUserType);
		values.put("vDisplayName", vUserName);
		values.put("vAvatarUrl", vAvatarUrl);

		values.put("vDetailTitle", vDetailTitle);
		values.put("vDetailSubTitle", vDetailSubTitle);
		values.put("vDetailType", vDetailType);
		values.put("vDetailMessage", vDetailMessage);

		values.put("vConversationId", vConversationId);
		values.put("vConversationUrl", vConversationUrl);

		values.put("iGapSize", iGapSize);
		values.put("vGapId", vGapId);
		values.put("vGapUrl", vGapUrl);

		values.put("vNotificationHrefUrl", vNotificationHrefUrl);
		values.put("vUserHrefUrl", vUserHrefUrl);
		values.put("vConversationHrefUrl", vConversationHrefUrl);
		values.put("vGapHrefUrl", vGapHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vNotificationId FROM notification WHERE vNotificationId = \""
						+ vNotificationId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "notification", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "notification", values,
					" vNotificationId = \"" + vNotificationId + "\" ");

		}

	}

	public void setCompetitionLiveData(final String vCompetitionId,
			final String vCompetitionLiveId, final String vCompetitionLiveUrl,
			final String vCompetitionLiveHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionLiveId", vCompetitionLiveId);
		values.put("vCompetitionLiveUrl", vCompetitionLiveUrl);

		values.put("vCompetitionLiveHref", vCompetitionLiveHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionLiveId FROM competition_live WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_live", values,
					null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_live", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public void setDirectMessageGapLoading(String vGapUrl, int isGapLoading) {

		ContentValues values = new ContentValues();
		values.put("isGapLoading", isGapLoading);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "direct_message_items", values,
				" vGapUrl = \"" + vGapUrl + "\" ");
	}

	public void setDirectMessageGapDelete(String vGapUrl) {

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_DELETE, null, "direct_message_items", null,
				" vGapUrl = \"" + vGapUrl + "\" ");
	}

	public void setCompetitionRoundData(final String vCompetitionId,
			final String vCompetitionRoundId,
			final String vCompetitionRoundUrl,
			final String vCompetitionRoundHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionRoundId", vCompetitionRoundId);
		values.put("vCompetitionRoundUrl", vCompetitionRoundUrl);
		values.put("vCompetitionRoundHref", vCompetitionRoundHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionRoundId FROM competition_round WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_round", values,
					null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_round", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	/**
	 * Setting the Competition current round data.
	 */
	public void setCompetitionCurrentRoundData(final String vCompetitionId,
			final String vCompetitionCurrentRoundId,
			final String vCompetitionCurrentRoundUrl) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vRoundId", vCompetitionCurrentRoundId);
		values.put("vCompetitionCurrentRoundUrl", vCompetitionCurrentRoundUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRoundId FROM competition_current_round WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_current_round",
					values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_current_round",
					values, " vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public void setPrivateLobbyGapLoading(String vGapId, int flag) {

		ContentValues values = new ContentValues();
		values.put("isGapLoading", flag);

		JsonUtil json = new JsonUtil();
		json.json_method(null, -100, false, Constants.QUERY_UPDATE, null,
				"friendMessage", values, " vGapId = \"" + vGapId + "\" ");
	}

	public void setCompetitionTeamData(final String vCompetitionId,
			final String vCompetitionTeamId, final String vCompetitionTeamUrl,
			final String vCompetitionTeamHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionTeamId", vCompetitionTeamId);
		values.put("vCompetitionTeamUrl", vCompetitionTeamUrl);
		values.put("vCompetitionTeamHref", vCompetitionTeamHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionTeamId FROM competition_team WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_team", values,
					null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_team", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	/**
	 * Setting the Competition team data.
	 */
	public void setRoundContestData(final String vRoundContestId,
			final String vRoundContestUrl, final String vRoundContestHref) {

		ContentValues values = new ContentValues();

		values.put("vRoundContestId", vRoundContestId);
		values.put("vRoundContestUrl", vRoundContestUrl);
		values.put("vRoundContestHref", vRoundContestHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRoundContestId FROM round_contest WHERE vRoundContestId = \""
						+ vRoundContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"round_contest", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "round_contest", values,
					" vRoundContestId = \"" + vRoundContestId + "\" ");

		}

	}

	public void setLastViewed(String vConversationId) {

		ContentValues values = new ContentValues();
		values.put("isLastViewed", 0);
		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "recent", values, null,
				null, false, true);

		values = new ContentValues();
		values.put("isLastViewed", 1);
		json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "recent", values,
				" vSubjectId = \"" + vConversationId + "\" ", null, false, true);

	}

	public void setAlreadyInvited(String vFriendId, int isAlreadyInvited) {

		ContentValues values = new ContentValues();
		values.put("isAlreadyInvited", isAlreadyInvited);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "my_friends", values,
				" vFriendId = \"" + vFriendId + "\" ", null, false, true);
		json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "search_friends",
				values, " vFriendId = \"" + vFriendId + "\" ", null, false,
				true);

	}

	/**
	 * Setting round data.
	 */
	public void setRoundData(final String vRoundId, final String vRoundUrl,
			final String period, final String name, final String dStartDate,
			final String dEndDate, final String vRoundContestId,
			final String vCompetitionRoundId, final String vRoundHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vRoundId", vRoundId);
		values.put("vRoundUrl", vRoundUrl);
		values.put("vPeriod", period);
		values.put("vName", name);
		values.put("dStartDate", dStartDate);
		values.put("dEndDate", dEndDate);
		values.put("vRoundContestId", vRoundContestId);

		if (vCompetitionRoundId != null) {
			values.put("vCompetitionRoundId", vCompetitionRoundId);
		}

		values.put("vRoundHref", vRoundHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRoundId FROM rounds WHERE vRoundId = \"" + vRoundId
						+ "\" ");
		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "rounds", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "rounds", values,
					" vRoundId = \"" + vRoundId + "\" ");

		}

	}

	public void setRoundData(final String vRoundId, final String vRoundUrl,
			final String vRoundHref) {

		ContentValues values = new ContentValues();

		values.put("vRoundId", vRoundId);
		values.put("vRoundUrl", vRoundUrl);
		values.put("vRoundHref", vRoundHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRoundId FROM rounds WHERE vRoundId = \"" + vRoundId
						+ "\" ");
		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "rounds", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "rounds", values,
					" vRoundId = \"" + vRoundId + "\" ");

		}

	}

	/**
	 * Setting round data.
	 */
	public void setRoundData(final String vRoundId, ContentValues values) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRoundId FROM rounds WHERE vRoundId = \"" + vRoundId
						+ "\" ");
		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "rounds", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "rounds", values,
					" vRoundId = \"" + vRoundId + "\" ");

		}

	}

	public void setConversationNotificationRead(String vConversationId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vNotificationId, vNotificationUrl,vNotificationHrefUrl FROM notification WHERE vConversationId = \""
								+ vConversationId + "\" ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				String vNotificationId = c.getString(c
						.getColumnIndex("vNotificationId"));

				ContentValues values = new ContentValues();
				values.put("isRead", 1);
				JsonUtil json = new JsonUtil();
				json.queryMethod1(Constants.QUERY_UPDATE, null, "notification",
						values, " vNotificationId = \"" + vNotificationId
								+ "\" ", null, false, true);

				JSONObject jObj = new JSONObject();

				/*
				 * String vNotificationUrl = c.getString( c.getColumnIndex(
				 * "vNotificationUrl" ) );
				 */
				String vNotificationUrl;
				Boolean isHrefUrl = false;
				String url = c.getString(c
						.getColumnIndex("vNotificationHrefUrl"));

				if (url != null && url.length() > 0) {
					vNotificationUrl = url;
					isHrefUrl = true;

				} else {
					vNotificationUrl = c.getString(c
							.getColumnIndex("vNotificationUrl"));
				}

				try {
					jObj.put(":type", getHeader(vNotificationUrl));
					jObj.put(":uid", vNotificationId);
					jObj.put("read", true);
				} catch (Exception e) {

				}

				new Util().setNotificationConfirm(vNotificationUrl, isHrefUrl,
						jObj.toString(), null);

				// new Util().setNotificationConfirm (
				// vNotificationUrl,jObj.toString(), null );

			}
			c.close();
			c = null;

		}

	}

	public boolean checkForLeague(String vSportsId) {

		boolean exists = false;

		Hashtable<String, List<String>> data = PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT c.vCompetitionId AS vCompetitionId FROM competition c  LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId WHERE sc.vSportsId = \""
						+ vSportsId + "\" ");
		if (data != null && data.get("vCompetitionId").size() > 0) {
			if (data.get("vCompetitionId").get(0) != null) {
				return true;
			}
		}

		return exists;

	}

	/*
	*//**
	 * Setting the Competition data.
	 */
	/*
	 * 
	 * public void setCompetition ( final String vCompetitionId, final String
	 * vCompetitionUrl ) {
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put( "vCompetitionId", vCompetitionId ); values.put(
	 * "vCompetitionUrl", vCompetitionUrl );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vCompetitionId FROM competition WHERE vCompetitionId = \"" +
	 * vCompetitionId + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "competition", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "competition", values,
	 * " vCompetitionId = \"" + vCompetitionId + "\" " );
	 * 
	 * }
	 * 
	 * }
	 */

	public void setCompetition(final String vCompetitionId,
			final String vCompetitionUrl, String vCompetitionHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionUrl", vCompetitionUrl);

		values.put("vCompetitionHref", vCompetitionHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public String getContestLobbyUid(String vContestId) {
		String vContestLobbyUid = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vContestLobbyUid from contest_lobby where vContestId = '"
						+ vContestId + "' ");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vContestLobbyUid = c.getString(c
						.getColumnIndex("vContestLobbyUid"));
			}
			c.close();
		}
		c = null;
		return vContestLobbyUid;

	}

	/**
	 * Setting the conversation data.
	 */
	public void setConversation(final String contestLobbyUid,
			final String vConversationId, final String vSelfUrl,
			final String name, final String access, final String invitationUrl,
			final String presenceUrl, final String totalPresences,
			final String friendsUrl, final String friendsUid,
			final String vContestId, int iOrder, boolean isOrderSet,
			final String vHref, final String vInvitationHref,
			final String vPresenceHref, final String vFriendsHref) {

		int isPrivate = 1;
		if (access != null && !access.trim().equalsIgnoreCase("public")) {
			isPrivate = 0;
		}

		ContentValues values = new ContentValues();
		values.put("vConversationId", vConversationId);
		values.put("vSelfUrl", vSelfUrl);
		values.put("vConversationName", name);
		values.put("isPrivate", isPrivate);

		values.put("vInvitationUrl", invitationUrl);
		values.put("vPresenceUrl", presenceUrl);
		values.put("iTotalPresences", totalPresences);

		values.put("vFriendsUid", friendsUid);
		values.put("vFriendsUrl", friendsUrl);

		values.put("vContestLobbyUid", contestLobbyUid);
		values.put("vContestId", vContestId);

		if (isOrderSet) {
			values.put("iOrder", iOrder);
		}

		values.put("vHref", vHref);
		values.put("vInvitationHref", vInvitationHref);

		values.put("vPresenceHref", vPresenceHref);
		values.put("vFriendsHref", vFriendsHref);

		// if already exists then update the data else insert into database
		if (checkForConversationExists(vConversationId)) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "match_conversation_node",
					values, " vConversationId = \"" + vConversationId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "match_conversation_node",
					values, null);
		}

	}

	/**
	 * Setting the conversation data.
	 */
	public void setConversation(ContentValues values, String vConversationId) {

		// if already exists then update the data else insert into database
		if (checkForConversationExists(vConversationId)) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "match_conversation_node",
					values, " vConversationId = \"" + vConversationId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "match_conversation_node",
					values, null);
		}

	}

	/**
	 * setting the total number of messages inside any conversation ...
	 */
	public void setConversationSize(final String vConversationId, final int size) {

		ContentValues values = new ContentValues();
		values.put("iTotalMessagePosts", size);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "match_conversation_node",
				values, " vConversationId = \"" + vConversationId + "\" ");

	}

	/*
	*//**
	 * Setting the conversation message data.
	 */
	/*
	 * public void setConversationMessageData ( final String vConversationId,
	 * final String vConversationMessageId, final String vSelfUrl, String
	 * vMarkerUrl, String vAdditionUrl ) {
	 * 
	 * ContentValues values = new ContentValues(); values.put(
	 * "vConversationId", vConversationId ); values.put(
	 * "vConversationMessageId", vConversationMessageId ); values.put(
	 * "vSelfUrl", vSelfUrl ); values.put( "vAdditionUrl", vAdditionUrl );
	 * values.put( "vMarkerUrl", vMarkerUrl );
	 * 
	 * // if already exists then update the data else insert into database if (
	 * checkForConversationMessageExists ( vConversationId,
	 * vConversationMessageId ) ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "conversation_message", values,
	 * " vConversationId = \"" + vConversationId +
	 * "\" AND vConversationMessageId = \"" + vConversationMessageId + "\" "); }
	 * else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "conversation_message", values, null ); }
	 * 
	 * }
	 */

	public void setConversationMessageData(final String vConversationId,
			final String vConversationMessageId, final String vSelfUrl,
			String vMarkerUrl, String vAdditionUrl, final String vHrefUrl,
			String vMarkerHref, String vAdditionHref) {

		ContentValues values = new ContentValues();
		values.put("vConversationId", vConversationId);
		values.put("vConversationMessageId", vConversationMessageId);
		values.put("vSelfUrl", vSelfUrl);
		values.put("vAdditionUrl", vAdditionUrl);
		values.put("vMarkerUrl", vMarkerUrl);

		values.put("vHrefUrl", vHrefUrl);
		values.put("vMarkerHref", vMarkerHref);
		values.put("vAdditionHref", vAdditionHref);

		// if already exists then update the data else insert into database
		if (checkForConversationMessageExists(vConversationId,
				vConversationMessageId)) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"conversation_message",
					values,
					" vConversationId = \"" + vConversationId
							+ "\" AND vConversationMessageId = \""
							+ vConversationMessageId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "conversation_message",
					values, null);
		}

	}

	/**
	 * Setting the conversation subject data.
	 */
	/*
	 * public void setConversationSubject ( final String vSubjectId, final
	 * String vSelfUrl ) {
	 * 
	 * ContentValues values = new ContentValues(); values.put( "vSelfUrl",
	 * vSelfUrl ); values.put( "vSubjectId", vSubjectId );
	 * 
	 * 
	 * // if already exists then update the data else insert into database if (
	 * checkForConversationSubjectExists ( vSubjectId ) ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_UPDATE, null, "match_home_node", values,
	 * " vSubjectId = \"" + vSubjectId + "\" "); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_INSERT, null, "match_home_node", values, null ); } }
	 */

	/**
	 * Setting the annonymous data into the self profile table .
	 */
	/*
	 * public void setUserOrAnnonymousUserData1 ( final String token, final
	 * String user, boolean isAnnonymous, final String iUserId ) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values .put( "vUserToken", token); values .put( "vSelfUrl", user); values
	 * .put( "isAnonymousUser", ( isAnnonymous)? 1 : 0); values .put( "iUserId",
	 * user.replace(token, ""));
	 * 
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "user", values, null);
	 * 
	 * 
	 * }
	 */

	/**
	 * Setting the Root Resources data into the root_resource .
	 */

	public void setRootResources(String resourceName, String resourceUrl,
			boolean clearOldData, String resource_id, String resource_href) {

		try {
			if (clearOldData)
				PlayupLiveApplication.getDatabaseWrapper().emptyTable(
						"root_resource");

			ContentValues values = new ContentValues();
			values.put("resource_name", resourceName);
			values.put("resource_url", resourceUrl);
			values.put("resource_id", resource_id);

			values.put("resource_href", resource_href);
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"root_resource", values, null);
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	public void updaeRootResource(String rootName, String rootResourceUrl) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT resource_name FROM root_resource WHERE resource_name = \""
						+ rootName + "\" ");

		ContentValues values = new ContentValues();
		values.put("resource_name", rootName);
		values.put("resource_url", rootResourceUrl);

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"root_resource", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "root_resource", values,
					" resource_name = \"" + rootName + "\" ");
		}

	}

	/*	*//**
	 * sets the user data into the database
	 */
	/*
	 * public void setUserData ( final String name, final String userName, final
	 * String avatar_url, final String uid, final int id, final boolean
	 * isAnnonymous, final String selfUrl, final boolean isPrimary ,final String
	 * signOutUrl, final String vFriendshipStatusId, final String
	 * vFriendshipStatusUrl, String status, String vPushNotificationUrl, String
	 * vPlayUpFriendUrl, String vLobbyId, String vLobbyUrl ) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values .put( "vSelfUrl", selfUrl); values.put( "iUserId", uid );
	 * values.put( "iId", id ); values.put( "isAnonymousUser", ( isAnnonymous)?
	 * 1 : 0 ); values.put( "vUserName", userName); values.put( "vName", name);
	 * values.put( "vUserAvatarUrl", avatar_url); values.put( "vSignOutUrl",
	 * signOutUrl);
	 * 
	 * values.put( "vFriendshipStatusId", vFriendshipStatusId ); values.put(
	 * "vFriendshipStatusUrl", vFriendshipStatusUrl); values.put( "status",
	 * status ); values.put( "vPushNotificationUrl", vPushNotificationUrl );
	 * values.put( "vPlayUpFriendUrl", vPlayUpFriendUrl ); values.put(
	 * "vLobbyId", vLobbyId ); values.put( "vLobbyUrl", vLobbyUrl );
	 * 
	 * 
	 * if ( isPrimary ) {
	 * 
	 * Log.e("123","update rows >>>>>>>>>>>>>>>>>>"+PlayupLiveApplication.
	 * getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "user",
	 * values, " isPrimaryUser = \"1\" "));
	 * 
	 * 
	 * } else { int count =
	 * PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT iUserId FROM user WHERE vSelfUrl = \"" + selfUrl + "\" "); if (
	 * count > 0 ) {
	 * Log.e("123","update rows >>>>>>>>>>>>>>>>>>"+PlayupLiveApplication
	 * .getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "user",
	 * values, " vSelfUrl = \"" + selfUrl + "\" ")); } else {
	 * Log.e("123","inserted rows >>>>>>>>>>>>>>>> "
	 * +PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "user", values, null)); } }
	 * 
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href sets the user data into the database
	 */
	public void setUserData(final String name, final String userName,
			final String avatar_url, final String uid, final int id,
			final boolean isAnnonymous, final String selfUrl,
			final String hrefUrl, final boolean isPrimary,
			final String signOutUrl, final String signOutHrefURL,
			final String vFriendshipStatusId,
			final String vFriendshipStatusUrl,
			final String vFriendshipStatusHrefUrl, String status,
			String vPushNotificationUrl, String vPushNotificationHrefUrl,
			String vPlayUpFriendUrl, String vPlayUpFriendHrefUrl,
			String vLobbyId, String vLobbyUrl, String vLobbyHrefUrl) {

		ContentValues values = new ContentValues();

		values.put("vSelfUrl", selfUrl);
		values.put("iUserId", uid);
		values.put("iId", id);
		values.put("isAnonymousUser", (isAnnonymous) ? 1 : 0);
		values.put("vUserName", userName);
		values.put("vName", name);
		values.put("vUserAvatarUrl", avatar_url);
		values.put("vSignOutUrl", signOutUrl);

		values.put("vFriendshipStatusId", vFriendshipStatusId);
		values.put("vFriendshipStatusUrl", vFriendshipStatusUrl);
		values.put("status", status);
		values.put("vPushNotificationUrl", vPushNotificationUrl);
		values.put("vPlayUpFriendUrl", vPlayUpFriendUrl);
		values.put("vLobbyId", vLobbyId);
		values.put("vLobbyUrl", vLobbyUrl);

		values.put("vHrefUrl", hrefUrl);
		values.put("vSignOutHrefUrl", signOutHrefURL);
		values.put("vFriendshipStatusHrefUrl", vFriendshipStatusHrefUrl);
		values.put("vPlayUpFriendHrefUrl", vPlayUpFriendHrefUrl);
		values.put("vPushNotificationHrefUrl", vPushNotificationHrefUrl);
		values.put("vLobbyHrefUrl", vLobbyHrefUrl);

		if (isPrimary) {

			Log.e("123",
					"update rows >>>>>>>>>>>>>>>>>>"
							+ PlayupLiveApplication.getDatabaseWrapper()
									.queryMethod2(Constants.QUERY_UPDATE, null,
											"user", values,
											" isPrimaryUser = \"1\" "));

		} else {
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							" SELECT iUserId FROM user WHERE vSelfUrl = \""
									+ selfUrl + "\" ");
			if (count > 0) {
				Log.e("123",
						"update rows >>>>>>>>>>>>>>>>>>"
								+ PlayupLiveApplication.getDatabaseWrapper()
										.queryMethod2(
												Constants.QUERY_UPDATE,
												null,
												"user",
												values,
												" vSelfUrl = \"" + selfUrl
														+ "\" "));
			} else {
				Log.e("123",
						"inserted rows >>>>>>>>>>>>>>>> "
								+ PlayupLiveApplication.getDatabaseWrapper()
										.queryMethod2(Constants.QUERY_INSERT,
												null, "user", values, null));
			}
		}

	}

	/**
	 * sets the user data into the database
	 */
	public void setUserData(ContentValues values, String iUserId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT iUserId FROM user WHERE iUserId = \"" + iUserId
						+ "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "user", values,
					" iUserId = \"" + iUserId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user", values, null);
		}

	}

	/**
	 * saving the recent activty url
	 */
	/*
	 * public void setRecentAvtivityUrl ( final String recentActivityUrl, final
	 * String iUserId ) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values .put( "vRecentActivityUrl", recentActivityUrl );
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user", values, " iUserId = \"" + iUserId +
	 * "\" ");
	 * 
	 * 
	 * }
	 */
	/**
	 * Praveeen : as per the Href saving the recent activty url
	 */
	public void setRecentAvtivityUrl(final String recentActivityUrl,
			final String recentActivityHrefUrl, final String iUserId) {

		ContentValues values = new ContentValues();

		values.put("vRecentActivityUrl", recentActivityUrl);
		values.put("vRecentActivityHrefUrl", recentActivityHrefUrl);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" iUserId = \"" + iUserId + "\" ");

	}

	public boolean setPrivateSelectContest(String vContestId,
			String vContestUrl, String vConversationId, boolean isHref) {

		boolean isAlreadyFollowing = false;

		ContentValues values = new ContentValues();
		values.put("vSubjectId", vContestId);
		if (isHref)
			values.put("vSubjectHrefUrl", vContestUrl);
		else
			values.put("vSubjectUrl", vContestUrl);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
				Constants.QUERY_UPDATE, null, "friendConversation", values,
				" vConversationId = \"" + vConversationId + "\"");

		// Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
		// "SELECT fm.vSubjectId " +
		// "FROM friendMessage fm " +
		// "LEFT JOIN user u ON u.iUserId = fm.vUserId " +
		// "LEFT JOIN friendConversationMessage fcm ON fcm.vConversationMessageId = fm.vConversationMessageId "
		// +
		// "LEFT JOIN friendConversation fc ON fc.vConversationId = fcm.vConversationId "
		// +
		// "WHERE fm.vSubjectId = \"" + vContestId +
		// "\"  AND u.isPrimaryUser = '1' AND fc.vConversationId = \"" +
		// vConversationId + "\" " +
		// "LIMIT 0, 1  " );

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT fm.vSubjectId FROM friendMessage fm "
								+ "LEFT JOIN user u ON u.iUserId = fm.vUserId LEFT JOIN friendConversationMessage fcm ON "
								+ "fcm.vConversationMessageId = fm.vConversationMessageId LEFT JOIN "
								+ "friendConversation fc ON fc.vConversationId = fcm.vConversationId WHERE LENGTH(fm.vSubjectId) > 0 AND u.isPrimaryUser = '1' "
								+ "AND fc.vConversationId =  '"
								+ vConversationId
								+ "' ORDER BY fm.vCreatedDate DESC LIMIT 0, 1");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				if (c.getString(c.getColumnIndex("vSubjectId")) != null
						&& c.getString(c.getColumnIndex("vSubjectId")).trim()
								.length() > 0
						&& c.getString(c.getColumnIndex("vSubjectId"))
								.equalsIgnoreCase(vContestId))

					isAlreadyFollowing = true;
			}
			c.close();
		}
		return isAlreadyFollowing;
	}

	public void setPrivateLobbySelectContest(String vContestId) {

		ContentValues values = new ContentValues();
		values.put("vLobbySubjectId", vContestId);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
				Constants.QUERY_UPDATE, null, "user", values,
				" isPrimaryUser = '1' ");
	}

	public void deselectPrivateCompetitions(String vCompetitionId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"  SELECT  c.vContestId AS  vContestId "
								+ "FROM contests c "
								+ "LEFT JOIN competition com ON com.vCompetitionId = c.vCompetitionId "
								+ "WHERE  c.vCompetitionId = \""
								+ vCompetitionId + "\"   ");

		if (c != null) {

			if (c.getCount() > 0) {
				int len = c.getCount();
				for (int i = 0; i < len; i++) {
					c.moveToPosition(i);
					ContentValues values = new ContentValues();
					values = new ContentValues();
					// values.putNull( "vSubjectId" );

					// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					// Constants.QUERY_UPDATE, null, "friendConversation",
					// values, " vSubjectId = \"" + c.getString(
					// c.getColumnIndex( "vContestId" ) ) + "\" " );

					values = new ContentValues();

					// values.putNull( "vLobbySubjectId" );
					// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					// Constants.QUERY_UPDATE, null, "user", values,
					// " isPrimaryUser = '1' AND vLobbySubjectId = \"" +
					// c.getString( c.getColumnIndex( "vContestId" ) ) + "\" "
					// );

				}
			}
			c.close();
		}

	}

	/**
	 * saving the recent room of the user
	 */
	/*
	 * public void setRecentAvtivity ( String vRecentId, String vRecentName,
	 * String vSubjectTitle, String vSubjectId, String vSubjectUrl, int iAccess,
	 * int iAccessPermitted, int iUnRead ,String vUserRecentId) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put( "vRecentId",
	 * vRecentId ); values .put( "vRecentName", vRecentName ); values .put(
	 * "vSubjectTitle", vSubjectTitle ); values .put( "vSubjectId", vSubjectId
	 * ); values .put( "vSubjectUrl", vSubjectUrl ); values .put( "iAccess",
	 * iAccess ); values .put( "iAccessPermitted", iAccessPermitted ); values
	 * .put( "iUnRead", iUnRead ); values .put( "vUserRecentId", vUserRecentId
	 * );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vRecentName FROM recent WHERE vRecentId = \"" + vRecentId +
	 * "\" AND vUserRecentId = '"+vUserRecentId+"'" );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "recent", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "recent", values, " vRecentId = \"" +
	 * vRecentId + "\" AND vUserRecentId = '"+vUserRecentId+"'" ); }
	 * 
	 * }
	 */

	/**
	 * saving the recent room of the user
	 */
	public void setRecentAvtivity(String vRecentId, String vRecentName,
			String vSubjectTitle, String vSubjectId, String vSubjectUrl,
			String vSubjectHrefUrl, int iAccess, int iAccessPermitted,
			int iUnRead, String vUserRecentId) {

		ContentValues values = new ContentValues();
		values.put("vRecentId", vRecentId);
		values.put("vRecentName", vRecentName);
		values.put("vSubjectTitle", vSubjectTitle);
		values.put("vSubjectId", vSubjectId);
		values.put("vSubjectUrl", vSubjectUrl);
		values.put("iAccess", iAccess);
		values.put("iAccessPermitted", iAccessPermitted);
		values.put("iUnRead", iUnRead);
		values.put("vUserRecentId", vUserRecentId);
		values.put("vSubjectHref", vSubjectHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vRecentName FROM recent WHERE vRecentId = \""
						+ vRecentId + "\" AND vUserRecentId = '"
						+ vUserRecentId + "'");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "recent", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"recent",
					values,
					" vRecentId = \"" + vRecentId + "\" AND vUserRecentId = '"
							+ vUserRecentId + "'");
		}

	}

	public void setClearRecentAvtivity(String vUserRecentId) {
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_DELETE, null, "recent", null,
				"vUserRecentId = \"" + vUserRecentId + "\" ");

	}

	/*	*//**
	 * saving the recent activty url
	 */
	/*
	 * public void setNotificationUrl ( final String notificationUrl, final
	 * String iUserId ) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values .put( "vNotificationUrl", notificationUrl );
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user", values, " iUserId = \"" + iUserId +
	 * "\" ");
	 * 
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href saving the recent activty url
	 */
	public void setNotificationUrl(final String notificationUrl,
			final String notificationHrefUrl, final String iUserId) {

		ContentValues values = new ContentValues();

		values.put("vNotificationUrl", notificationUrl);

		values.put("vNotificationHrefUrl", notificationHrefUrl);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" iUserId = \"" + iUserId + "\" ");

	}

	/**
	 * saving the friends url
	 */
	/*
	 * public void setFriendsUrl ( final String friendsUrl, final String iUserId
	 * ) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values .put( "vFriendsUrl", friendsUrl );
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user", values, " iUserId = \"" + iUserId +
	 * "\" ");
	 * 
	 * 
	 * }
	 */
	/**
	 * saving the friends url
	 */
	public void setFriendsUrl(final String friendsUrl,
			final String friendsHrefUrl, final String iUserId) {

		ContentValues values = new ContentValues();

		values.put("vFriendsUrl", friendsUrl);

		values.put("vFriendsHrefUrl", friendsHrefUrl);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" iUserId = \"" + iUserId + "\" ");

	}

	/**
	 * sets the user data into the database
	 */
	public void setUserToken(final String token, boolean queryMethod1) {

		Log.e("123",
				"map.containsKey( Constants.AUTHORIZATION_TOKEN_KEY ) !Constants.isLoggedIn setUserToken>>>>>>>>>>>>>>>>>>>>>>>>>>");

		if (queryMethod1) {

			Runnable r = new Runnable() {

				@Override
				public void run() {

					try {
						ContentValues values = new ContentValues();
						values.put("vUserToken", token);

						int count = PlayupLiveApplication
								.getDatabaseWrapper()
								.getTotalCount(
										" SELECT vUserToken FROM user WHERE isPrimaryUser = \"1\" ");

						if (count == 0) {
							values.put("isAnonymousUser", "1");
							values.put("isPrimaryUser", "1");

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_INSERT, null, "user",
									values, null);

						} else {

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_UPDATE, null, "user",
									values, null);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}

				}

			};
			Thread th = new Thread(r);
			th.start();

		} else {
			ContentValues values = new ContentValues();
			values.put("vUserToken", token);

			int count = PlayupLiveApplication
					.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vUserToken FROM user WHERE isPrimaryUser = \"1\" ");

			if (count == 0) {
				values.put("isAnonymousUser", "1");
				values.put("isPrimaryUser", "1");

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "user", values, null);
			} else {

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "user", values, null);
			}

		}

	}

	/**
	 * sets the user region into the database
	 */
	public void setPrimaryUserRegion(final String region, boolean queryMethod1) {

		if (queryMethod1) {

			Runnable r = new Runnable() {

				@Override
				public void run() {

					try {
						ContentValues values = new ContentValues();
						values.put("vUserRegion", region);

						int count = PlayupLiveApplication
								.getDatabaseWrapper()
								.getTotalCount(
										" SELECT vUserRegion FROM user WHERE isPrimaryUser = \"1\" ");

						if (count == 0) {
							values.put("isAnonymousUser", "1");
							values.put("isPrimaryUser", "1");

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_INSERT, null, "user",
									values, null);

						} else {

							JsonUtil json = new JsonUtil();
							json.json_method(null, -100, false,
									Constants.QUERY_UPDATE, null, "user",
									values, null);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}

				}

			};
			Thread th = new Thread(r);
			th.start();

		} else {
			ContentValues values = new ContentValues();
			values.put("vUserRegion", region);

			int count = PlayupLiveApplication
					.getDatabaseWrapper()
					.getTotalCount(
							" SELECT vUserRegion FROM user WHERE isPrimaryUser = \"1\" ");

			if (count == 0) {

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "user", values, null);
			} else {

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "user", values, null);
			}

		}

	}

	public void dropTables() {

		Log.e("123", "######################## TABLES DROPED");
		Hashtable<String, Object> result = getRegionUrlFromRoot();
		String vRegionUrl = (String) result.get("url");

		DatabaseWrapper.getWritableSQLiteDatabase().delete("sections", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase()
				.delete("blocks", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("blockContent",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase()
				.delete("sports", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("eTag",
				"vUrl != '" + vRegionUrl + "'", null);
		// DatabaseWrapper.getWritableSQLiteDatabase().delete( "competition",
		// null, null);

		ContentValues values = new ContentValues();
		values.put("vBlockTileId", "");
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "contest_lobby", values, null);

	}

	public void dropTablesForExpirationScenario() {

		DatabaseWrapper.getWritableSQLiteDatabase().delete("providerTokens",
				null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete("user", null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete("contest_lobby",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"contest_lobby_conversation", null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete("context", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"conversation_friends", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"conversation_message", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"direct_conversation", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"direct_message_items", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("direct_messages",
				null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"friendConversation", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"friendConversationMessage", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"friendLobbyConversation", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("friendMessage",
				null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete("gap_info", null,
				null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"match_conversation_node", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("message", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("my_friends", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("my_friends_live",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("notification",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("poll", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("providers", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"push_notifications", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase()
				.delete("recent", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("recent_invite",
				null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete("search_friends",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("teams", null, null);

		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"user_direct_conversation", null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("user_notification",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("user_recent", null,
				null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete("playup_friends",
				null, null);
		DatabaseWrapper.getWritableSQLiteDatabase().delete(
				"friendConversationHeaders", null, null);

	}

	/**
	 * sets the user data into the database
	 */
	public void setUserTokenLogin(final String token) {

		Runnable r = new Runnable() {

			@Override
			public void run() {

				try {
					ContentValues values = new ContentValues();
					values.put("vUserToken", token);

					int count = PlayupLiveApplication
							.getDatabaseWrapper()
							.getTotalCount(
									" SELECT vUserToken FROM user WHERE isPrimaryUser = \"1\" ");

					if (count == 0) {
						values.put("isAnonymousUser", "0");
						values.put("isPrimaryUser", "1");
						JsonUtil json = new JsonUtil();
						json.queryMethod1(Constants.QUERY_INSERT, null, "user",
								values, null, null, false, true);
					} else {
						values.put("isAnonymousUser", "0");
						values.put("isPrimaryUser", "1");
						JsonUtil json = new JsonUtil();
						json.queryMethod1(Constants.QUERY_UPDATE, null, "user",
								values, null, null, false, true);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}

			}

		};
		Thread th = new Thread(r);
		th.start();

	}

	public void setUrbanPushNotificationId(int id) {

		ContentValues values = new ContentValues();
		values.put("vUrbanAirShipNotificationId", id);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_INSERT, null,
				"push_notification_ids", values, null, null, false, true);
	}

	public void removeUrbanPushNotification(int id) {

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_DELETE, null,
				"push_notification_ids", null,
				" vUrbanAirShipNotificationId = '" + id + "' ", null, false,
				true);
	}

	public Hashtable<String, List<String>> getUrbanPushNotificationId() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vUrbanAirShipNotificationId FROM push_notification_ids ");
	}

	/**
	 * sets the user data into the database
	 */
	public void setUserTokenPrimary1(final String token) {

		ContentValues values = new ContentValues();
		values.put("vUserToken", token);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" isPrimaryUser = \"1\"  ");

	}

	/**
	 * 
	 * set room conversation details
	 * 
	 */
	public void setUserConversation(ContentValues values) {
		Cursor c = null;
		try {

			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							" SELECT vNextUrl,vNextHref FROM message "
									+ "WHERE (LENGTH(message_id_pk) > 0 AND message_id_pk = \""
									+ values.getAsString("message_id_pk")
									+ "\")"
									+ " OR (LENGTH(vMessageHrefUrl) > 0 AND vMessageHrefUrl = \""
									+ values.getAsString("vMessageHrefUrl")
									+ "\")");
			if (c != null && c.getCount() > 0) {

				c.moveToFirst();
				values.put("isFromContestLobby", 0);
				String vNext = c.getString(0);
				values.put("vNextUrl", vNext);
				values.put("vNextHref", c.getString(1));
				PlayupLiveApplication
						.getDatabaseWrapper()
						.queryMethod2(
								Constants.QUERY_UPDATE,
								null,
								"message",
								values,
								" (LENGTH(message_id_pk) > 0 AND message_id_pk = \""
										+ values.getAsString("message_id_pk")
										+ "\")"
										+ " OR (LENGTH(vMessageHrefUrl) > 0 AND vMessageHrefUrl = \""
										+ values.getAsString("vMessageHrefUrl")
										+ "\")");

				c.close();
				c = null;
			} else {

				if (c != null) {
					c.close();
				}
				c = PlayupLiveApplication
						.getDatabaseWrapper()
						.selectQuery(
								" SELECT * FROM message WHERE "
										+ " (LENGTH(message_id_pk) > 0 AND message_id_pk = \""
										+ values.getAsString("message_id_pk")
										+ "\")"
										+ " OR (LENGTH(vMessageHrefUrl) > 0 AND vMessageHrefUrl = \""
										+ values.getAsString("vMessageHrefUrl")
										+ "\")");
				if (c != null && c.getCount() > 0) {
					values.put("isFromContestLobby", 0);
				}

				PlayupLiveApplication
						.getDatabaseWrapper()
						.queryMethod2(
								Constants.QUERY_DELETE,
								null,
								"message",
								null,
								" (LENGTH(message_id_pk) > 0 AND message_id_pk = \""
										+ values.getAsString("message_id_pk")
										+ "\")"
										+ " OR (LENGTH(vMessageHrefUrl) > 0 AND vMessageHrefUrl = \""
										+ values.getAsString("vMessageHrefUrl")
										+ "\")");
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "message", values, null);

				c.close();
				c = null;
			}

			c = null;

		} catch (Exception e) {
			Logs.show(e);
			if (c != null && !c.isClosed())
				c.close();

			c = null;

		}
	}

	/**
	 * sets the next url data into the database
	 */

	public void setNextUrl(ContentValues values, String vNextUrl,
			String vNextHrefUrl) {

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vNextUrl FROM"
								+ " message WHERE (LENGTH(vNextUrl) > 0 AND vNextUrl = \""
								+ vNextUrl
								+ "\") OR  (LENGTH(vNextHref) > 0 AND vNextHref = \""
								+ vNextHrefUrl + "\")");

		if (count > 0) {
			// PlayupLiveApplication.getDatabaseWrapper().queryMethod(
			// Constants.QUERY_UPDATE, null, "message",
			// cValues,"vNextUrl = \""+vNextUrl+"\" ");
		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "message", values, null);
		}

	}

	/**
	 * sets the next url data into the database
	 */
	public boolean isNextUrlForConversationId_Present(
			String vConversationMessageId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vNextUrl FROM message WHERE vConversationMessageId = \""
						+ vConversationMessageId + "\" AND vNextUrl != \"\"");
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * sets the gap information to DB, next url data into the database
	 */
	/*
	 * public void setGapSize ( final String gap_Uid ,final String gapUrl,final
	 * int gapSize) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "gap_url",
	 * gapUrl); values.put( "gap_size", gapSize); values.put( "gap_uid",
	 * gap_Uid); int count =
	 * PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT gap_url FROM gap_info WHERE gap_url = \""+gapUrl+"\" " );
	 * if(count==0) PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "gap_info", values, null); else{
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "gap_info", values,
	 * "gap_url = \""+gapUrl+"\" ");
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href sets the gap information to DB, next url data
	 * into the database
	 */
	public void setGapSize(final String gap_Uid, final String gapUrl,
			final String gapHrefUrl, final int gapSize) {

		ContentValues values = new ContentValues();
		values.put("gap_url", gapUrl);
		values.put("gap_size", gapSize);
		values.put("gap_uid", gap_Uid);
		values.put("gap_href_url", gapHrefUrl);
		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT gap_url FROM gap_info WHERE  (LENGTH(gap_url) > 0 AND  gap_url = \""
								+ gapUrl
								+ "\") or (LENGTH(gap_href_url) > 0 AND  gap_href_url = \""
								+ gapHrefUrl + "\" ) ");
		if (count == 0)
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "gap_info", values, null);
		else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "gap_info", values,
					"gap_url = \"" + gapUrl + "\" ");

		}

	}

	/**
	 * 
	 * Getting the Gap Size
	 * 
	 * 
	 */

	public int getGapSize(String nextURL, String nextHrefUrl) {

		Hashtable<String, List<String>> data = new Hashtable<String, List<String>>();

		if (nextHrefUrl != null && nextHrefUrl.trim().length() > 0) {

			data = PlayupLiveApplication
					.getDatabaseWrapper()
					.select("SELECT gap_size FROM "
							+ "gap_info WHERE (LENGTH(gap_href_url) > 0 AND  gap_href_url = \""
							+ nextHrefUrl + "\" ) ");
			if (data != null && data.containsKey("gap_size")
					&& data.get("gap_size").size() > 0) {

				int gapSize = Integer.parseInt(data.get("gap_size").get(0));
				data.clear();
				data = null;
				return gapSize;
			} else {
				JsonUtil json = new JsonUtil();
				json.queryMethod1(Constants.QUERY_DELETE, null, "message",
						null, " vNextHref = \"" + nextHrefUrl + "\" ", null,
						false, false);
			}
			if (data != null) {
				data.clear();
				data = null;
			}
		}

		if (nextURL != null && nextURL.trim().length() > 0) {
			data = PlayupLiveApplication
					.getDatabaseWrapper()
					.select("SELECT gap_size "
							+ "FROM gap_info WHERE (LENGTH(gap_url) > 0 AND gap_url = \""
							+ nextURL + "\") ");
			if (data != null && data.containsKey("gap_size")
					&& data.get("gap_size").size() > 0) {

				int gapSize = Integer.parseInt(data.get("gap_size").get(0));
				data.clear();
				data = null;
				return gapSize;
			} else {
				JsonUtil json = new JsonUtil();
				json.queryMethod1(Constants.QUERY_DELETE, null, "message",
						null, " vNextUrl = \"" + nextURL + "\" ", null, false,
						false);
			}
			if (data != null) {
				data.clear();
				data = null;
			}
		}

		return 0;

	}

	/**
	 * 
	 * Remove the Gap Size
	 * 
	 * 
	 */

	public void removeGapSizeEntry(String nextURL) {
		try {
			JsonUtil json = new JsonUtil();
			json.queryMethod1(
					Constants.QUERY_DELETE,
					null,
					"gap_info",
					null,
					" (LENGTH(gap_url) > 0 AND gap_url = "
							+ "\""
							+ nextURL
							+ "\" ) OR (LENGTH(gap_href_url) > 0 AND gap_href_url = "
							+ "\"" + nextURL + "\" ) ", null, false, false);

		} catch (Exception e) {

		}

	}

	/*
	 * set subject for conversation
	 */
	/*
	 * public void setSubjectForConversation ( ContentValues cValues, String
	 * vSubjectId) {
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vSubjectId FROM match_home_node WHERE vSubjectId = \"" +
	 * vSubjectId + "\" " ); if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_INSERT, null, "match_home_node", cValues, null); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod(
	 * Constants.QUERY_UPDATE, null, "match_home_node", cValues,
	 * " vSubjectId = \"" + vSubjectId + "\" " ); }
	 * 
	 * }
	 */

	// ////////////// CONTEXT TABLE RELATED QUERIES //////////////////////////

	/**
	 * inserts the context url into the database.
	 */
	public void setContext(final String contextUrl) {

		ContentValues values = new ContentValues();
		values.put("vUrl", contextUrl);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_INSERT, null, "context", values, null);

	}

	public List getContextUrls() {

		Hashtable<String, List<String>> data = PlayupLiveApplication
				.getDatabaseWrapper().select("SELECT vUrl FROM context");
		if (data != null && data.containsKey("vUrl")
				&& data.get("vUrl").size() > 0) {
			int len = data.get("vUrl").size();
			return data.get("vUrl");
		}
		return null;
	}

	/**
	 * deletes the context url from the context table.
	 */
	public void deleteContextUrls() {

		PlayupLiveApplication.getDatabaseWrapper().directDelete("context",
				null, null, -1);
	}

	/**
	 * inserts the list of providers into the database.
	 */
	public void setProvider(final String providerName, final String loginUrl,
			final String successUrl, final String failureUrl,
			final String vIconLoginUrl, String vIconLoginDisabledUrl,
			String vIconLoginHighLightUrl, String vIconBroadcastUrl,
			String vIconBroadcastDisableUrl, String vIconBroadcastHighLightUrl,
			final String vUserName, final String vAvatarUrl, final int isSet) {

		ContentValues values = new ContentValues();
		values.put("vProviderName", providerName);
		values.put("vLoginUrl", loginUrl);
		values.put("vSuccessUrl", successUrl);
		values.put("vFailureUrl", failureUrl);

		values.put("vIconLoginUrl", vIconLoginUrl);
		values.put("vIconLoginDisabledUrl", vIconLoginDisabledUrl);
		values.put("vIconHightLightUrl", vIconLoginHighLightUrl);

		values.put("vIconBroadcastUrl", vIconBroadcastUrl);
		values.put("vIconBroadcastDisabledUrl", vIconBroadcastDisableUrl);
		values.put("vIconBroadcastHighLightUrl", vIconBroadcastHighLightUrl);

		values.put("vUserName", vUserName);
		values.put("vAvatarUrl", vAvatarUrl);
		values.put("isSet", isSet);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vProviderName FROM providers WHERE vProviderName = \""
						+ providerName + "\" ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "providers", values,
					" vProviderName = \"" + providerName + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "providers", values, null);
		}

	}

	/**
	 * gets the provider's list
	 */
	public Hashtable<String, List<String>> getProviders() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vProviderName, vLoginUrl, vSuccessUrl, vFailureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconHightLightUrl, vIconBroadcastUrl, vIconBroadcastDisabledUrl, vIconBroadcastHighLightUrl, vUserName, vAvatarUrl, isSet FROM providers ");

	}

	/**
	 * gets the login provider details
	 */
	public Hashtable<String, List<String>> getLoginProviders() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vProviderName, vLoginUrl, vSuccessUrl, vFailureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconHightLightUrl, vIconBroadcastUrl, vIconBroadcastDisabledUrl, vIconBroadcastHighLightUrl, vUserName, vAvatarUrl, isSet FROM providers WHERE isSet = '1' ");

	}

	/**
	 * gets the provider's list
	 */
	public Hashtable<String, List<String>> getProviderUrls(String vProviderName) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vProviderName, vLoginUrl, vSuccessUrl, vFailureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconHightLightUrl, vIconBroadcastUrl, vIconBroadcastDisabledUrl, vIconBroadcastHighLightUrl, vUserName, vAvatarUrl, isSet FROM providers WHERE vProviderName = \""
						+ vProviderName + "\" ");

	}

	/**
	 * gets the provider's list with only requested fields
	 */
	public Hashtable<String, List<String>> getProviders(String fields) {

		return PlayupLiveApplication.getDatabaseWrapper().select(
				" SELECT DISTINCT " + fields + " FROM providers");

	}

	/**
	 * gets the provider's list with only requested fields in some order
	 */
	public Hashtable<String, List<String>> getProvidersSortOrder(String fields,
			String selectedUrl) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT DISTINCT   "
						+ fields
						+ " FROM providers WHERE vAvatarUrl != '' AND vAvatarUrl IS NOT NULL ORDER BY vAvatarUrl = '"
						+ selectedUrl + "' desc");

	}

	/**
	 * gets the provider's list
	 */
	public Hashtable<String, List<String>> getProviderByName(String providerName) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT DISTINCT vProviderName, vLoginUrl, vSuccessUrl, vFailureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconHightLightUrl, vIconBroadcastUrl, vIconBroadcastDisabledUrl, vIconBroadcastHighLightUrl FROM providers WHERE vProviderName = \""
						+ providerName + "\" ");

	}

	public String getCompetitionUrl(final String vCompetitionId) {
		String vCompetitionUrl = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSelfUrl FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionUrl = c.getString(c.getColumnIndex("vSelfUrl"));
			}
			c.close();

		}
		c = null;
		return vCompetitionUrl;
	}

	public Hashtable<String, List<String>> getCompetitionUrl() {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vCompetitionUrl FROM competition WHERE isFavourite = 1 ");
	}

	public Hashtable<String, List<String>> getCompetitionLiveUrl() {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT cl.vCompetitionLiveUrl AS vCompetitionLiveUrl,"
						+ "cl.vCompetitionLiveHref AS vCompetitionLiveHref FROM competition_live cl LEFT JOIN competition c ON cl.vCompetitionLiveId = c.vCompetitionLiveId WHERE c.isFavourite = 1 AND c.iLiveNum > 0");
	}

	/*
	 * public String getCompetitionRoundsUrl ( final String vCompetitionId ) {
	 * String vCompetitionUrl = null; Cursor c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT r.vSelfUrl AS vSelfUrl FROM competition c LEFT JOIN rounds r ON r.vRoundId = c.vRoundId WHERE c.vCompetitionId = \""
	 * + vCompetitionId + "\" "); if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vCompetitionUrl = c.getString(
	 * c.getColumnIndex( "vSelfUrl" ) ); } c.close(); } return vCompetitionUrl;
	 * }
	 */

	public String getCompetitionTeamsUrl(final String vCompetitionId) {
		String vCompetitionUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT t.vSelfUrl AS vSelfUrl FROM competition c LEFT JOIN teams t ON t.vTeamId = c.vTeamId WHERE c.vCompetitionId = \""
								+ vCompetitionId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionUrl = c.getString(c.getColumnIndex("vSelfUrl"));
			}
			c.close();
		}
		c = null;
		return vCompetitionUrl;
	}

	/**
	 * Selecting/ De-selecting match for competition
	 */
	public void setCompetitionFavourite(final String vCompetitionId,
			final int isFavourite) {

		ContentValues values = new ContentValues();
		values.put("isFavourite", isFavourite);
		values.put("iFavouriteTime", System.currentTimeMillis());
		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "competition", values,
				" vCompetitionId = \"" + vCompetitionId + "\" ", null, false,
				false);

	}

	/**
	 * delete all the data in provider table.
	 */
	public void emptyProvider() {
		// empty the provider table for the latest data.
		PlayupLiveApplication.getDatabaseWrapper().directDelete("providers",
				null, null, -1);

	}

	// searching listview items

	public Hashtable<String, List<String>> getLeagueNames(
			final String search_text, final String sportsId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vCompetitionId, vCompetitionUrl,vCompetitionHref, vCompetitonName, vShortName, vRegion, vLogoUrl, "
						+ "iLiveNum, isFavourite"
						+ " FROM competition c "
						+ "LEFT JOIN sports_competition sc ON sc.vSportsCompetitionId = c.vSportsCompetitionId "
						+ "WHERE sc.vSportsId = \""
						+ sportsId
						+ "\"  AND "
						+ "(vCompetitonName LIKE \"%"
						+ search_text
						+ "%\"  OR c.vShortName LIKE \"%"
						+ search_text
						+ "%\""
						+ " OR c.vRegion LIKE '%" + search_text + "%')");
	}

	public Hashtable<String, List<String>> getMyLeagueNames(
			final String search_text) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT isFavourite, vCompetitionId, vCompetitonName, "
						+ "vShortName, vRegion, vLogoUrl, iLiveNum FROM competition WHERE isFavourite = '1' AND ( vCompetitonName LIKE \"%"
						+ search_text
						+ "%\"  OR vShortName LIKE \"%"
						+ search_text + "%\" ) ");
	}

	public void setUserRecent(String vUserRecentId, String iUserId) {
		ContentValues values = new ContentValues();
		values.put("vUserRecentId", vUserRecentId);
		values.put("vUserId", iUserId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vUserRecentId FROM user_recent WHERE vUserId = \""
						+ iUserId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user_recent", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "user_recent", values,
					" vUserId = \"" + iUserId + "\" ");
		}

	}

	// Used for searching
	public Hashtable<String, List<String>> getWeekDetails(
			String vCompetitionId, String searchValue) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT vRoundId, vPeriod, vName, dStartDate, dEndDate FROM rounds r LEFT JOIN  competition_round cr ON cr.vCompetitionRoundId = r.vCompetitionRoundId WHERE cr.vCompetitionId = \""
						+ vCompetitionId
						+ "\"  AND ( vName LIKE \"%"
						+ searchValue
						+ "%\"  OR vPeriod LIKE \"%"
						+ searchValue
						+ "%\" )   ORDER BY dStartDate ASC, dEndDate ASC ");
	}

	public void setFriends(ContentValues values, String vFriendId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM my_friends WHERE vFriendId = \""
						+ vFriendId + "\" ");

		if (count == 0) {

			values.put("isAlreadyInvited", 3);

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "my_friends", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "my_friends", values,
					" vFriendId = \"" + vFriendId + "\" ");
		}

	}

	public void setSearchFriends(ContentValues values, String vFriendId) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM search_friends WHERE vFriendId = \""
						+ vFriendId + "\" ");

		if (count == 0) {
			values.put("isAlreadyInvited", 3);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "search_friends", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "search_friends", values,
					" vFriendId = \"" + vFriendId + "\" ");
		}

	}

	/**
	 * setting the Friends
	 */
	/*
	 * public void setFriends ( String friendUid,String friendName,String
	 * friendAvatar, String sourceName,String sourceIconHref,String
	 * appInvitationUrl, String appInvitationType, int alreadyInvited,boolean
	 * isOnline, String vProfileId, String friendUserName, String
	 * friendType,String profileUrl,String directConversationUrl,boolean
	 * fromSearch) { ContentValues values = new ContentValues (); values.put(
	 * "vFriendId", friendUid ); values.put( "vFriendName", friendName );
	 * values.put( "vFriendAvatar", friendAvatar ); values.put( "vSourceName",
	 * sourceName ); values.put( "vSourceIconHref", sourceIconHref );
	 * values.put("vAppInvitationUrl", appInvitationUrl); values.put(
	 * "vAppInvitationType", appInvitationType ); values.put( "isOnline",
	 * isOnline ); values.put("vProfileId", vProfileId ); values.put(
	 * "vFriendUserName", friendUserName ); values.put("vGapId","");
	 * values.put("vFriendType", friendType ); values.put("vUserSelfUrl",
	 * profileUrl ); values.put("vDirectConversationUrl", directConversationUrl
	 * );
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vFriendId FROM my_friends WHERE vFriendId = \"" + friendUid +
	 * "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * if(!fromSearch){ values.put("isSearch",fromSearch);
	 * values.put("isAlreadyInvited", alreadyInvited ); }
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("my_friends",
	 * values," vFriendId = \"" + friendUid + "\" ",null, -1);
	 * 
	 * } else { values.put("isSearch",fromSearch);
	 * values.put("isAlreadyInvited", alreadyInvited );
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("my_friends",
	 * values, null, -1);
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Praveem
	 * 
	 */

	public void setFriends(String friendUid, String friendName,
			String friendAvatar, String sourceName, String sourceIconHref,
			String appInvitationUrl, String appInvitationHrefUrl,
			String appInvitationType, int alreadyInvited, boolean isOnline,
			String vProfileId, String friendUserName, String friendType,
			String profileUrl, String vProfileHrefUrl,
			String directConversationUrl, String directConversationHrefUrl,
			boolean fromSearch) {
		// TODO Auto-generated method stub

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUid);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIconHref", sourceIconHref);
		values.put("vAppInvitationUrl", appInvitationUrl);
		values.put("vAppInvitationType", appInvitationType);
		values.put("isOnline", isOnline);
		values.put("vProfileId", vProfileId);
		values.put("vFriendUserName", friendUserName);
		values.put("vGapId", "");
		values.put("vFriendType", friendType);
		values.put("vUserSelfUrl", profileUrl);
		values.put("vDirectConversationUrl", directConversationUrl);

		values.put("vAppInvitationHrefUrl", appInvitationHrefUrl);
		values.put("vUserHrefUrl", vProfileHrefUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM my_friends WHERE vFriendId = \""
						+ friendUid + "\"  ");

		if (count > 0) {

			if (!fromSearch) {
				values.put("isSearch", fromSearch);
				values.put("isAlreadyInvited", alreadyInvited);
			}

			PlayupLiveApplication.getDatabaseWrapper().directUpdate(
					"my_friends", values,
					" vFriendId = \"" + friendUid + "\" ", null, -1);

		} else {
			values.put("isSearch", fromSearch);
			values.put("isAlreadyInvited", alreadyInvited);
			PlayupLiveApplication.getDatabaseWrapper().directInsert(
					"my_friends", values, null, -1);

		}

	}

	/*
	 * public void setPlayupFriendsData ( String friendUid,String
	 * friendName,String friendAvatar,String friendUserName, String
	 * sourceName,String sourceIconHref,String vProfileId,boolean
	 * isOnline,String onlineSince,String lastActivitySince, int access,int
	 * unread, String lastActivityUid ,String roomName,String subjectTitle
	 * ,String subjectUid ,String subjectUrl , String access_permitted,String
	 * directConversationUrl, String vUserSelfUrl ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vFriendId",
	 * friendUid ); values.put( "vFriendName", friendName ); values.put(
	 * "vFriendAvatar", friendAvatar ); values.put( "vSourceName", sourceName );
	 * values.put( "vSourceIconHref", sourceIconHref ); values.put("vProfileId",
	 * vProfileId ); values.put( "vFriendUserName", friendUserName );
	 * values.put("vGapId","");
	 * 
	 * 
	 * values.put( "vDirectConversationUrl", directConversationUrl );
	 * values.put( "isOnline", isOnline ); values.put( "dOnlineSince",
	 * onlineSince ); values.put( "vLastActivityId", lastActivityUid );
	 * values.put( "vRoomName", roomName ); values.put("vSubjectTitle",
	 * subjectTitle ); values.put( "vSubjectId", subjectUid );
	 * values.put("vSubjectUrl",subjectUrl);
	 * 
	 * 
	 * values.put( "iAccess", access ); values.put( "iAccessPermitted",
	 * access_permitted ); values.put( "vLastActivitySince", lastActivitySince
	 * ); values.put( "iUnreadCount", unread );
	 * 
	 * values.put( "vUserSelfUrl", vUserSelfUrl );
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vFriendId FROM playup_friends WHERE vFriendId = \"" + friendUid
	 * + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("playup_friends",
	 * values," vFriendId = \"" + friendUid + "\" ",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("playup_friends",
	 * values, null, -1);
	 * 
	 * }
	 * 
	 * }
	 */

	public void setPlayupFriendsData(String friendUid, String friendName,
			String friendAvatar, String friendUserName, String sourceName,
			String sourceIconHref, String vProfileId, boolean isOnline,
			String onlineSince, String lastActivitySince, int access,
			int unread, String lastActivityUid, String roomName,
			String subjectTitle, String subjectUid, String subjectUrl,
			String subjectHrefUrl, String access_permitted,
			String directConversationUrl, String directConversationHrefUrl,
			String vUserSelfUrl, String vProfileHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUid);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIconHref", sourceIconHref);
		values.put("vProfileId", vProfileId);
		values.put("vFriendUserName", friendUserName);
		values.put("vGapId", "");

		values.put("vDirectConversationUrl", directConversationUrl);
		values.put("isOnline", isOnline);
		values.put("dOnlineSince", onlineSince);
		values.put("vLastActivityId", lastActivityUid);
		values.put("vRoomName", roomName);
		values.put("vSubjectTitle", subjectTitle);
		values.put("vSubjectId", subjectUid);
		values.put("vSubjectUrl", subjectUrl);

		values.put("iAccess", access);
		values.put("iAccessPermitted", access_permitted);
		values.put("vLastActivitySince", lastActivitySince);
		values.put("iUnreadCount", unread);

		values.put("vUserSelfUrl", vUserSelfUrl);

		values.put("vSubjectHrefUrl", subjectHrefUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);
		values.put("vUserHrefUrl", vProfileHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM playup_friends WHERE vFriendId = \""
						+ friendUid + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate(
					"playup_friends", values,
					" vFriendId = \"" + friendUid + "\" ", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert(
					"playup_friends", values, null, -1);

		}

	}

	/*
	 * public void setSearchPlayupFriendsData ( String friendUid,String
	 * friendName,String friendAvatar,String friendUserName, String
	 * sourceName,String sourceIconHref,String vProfileId,boolean
	 * isOnline,String onlineSince,String lastActivitySince, int access,int
	 * unread, String lastActivityUid ,String roomName,String subjectTitle
	 * ,String subjectUid ,String subjectUrl , String access_permitted,String
	 * directConversationUrl, String vUserSelfUrl ) {
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vFriendId",
	 * friendUid ); values.put( "vFriendName", friendName ); values.put(
	 * "vFriendAvatar", friendAvatar ); values.put( "vSourceName", sourceName );
	 * values.put( "vSourceIconHref", sourceIconHref ); values.put("vProfileId",
	 * vProfileId ); values.put( "vFriendUserName", friendUserName );
	 * values.put("vGapId","");
	 * 
	 * 
	 * values.put( "vDirectConversationUrl", directConversationUrl );
	 * values.put( "isOnline", isOnline ); values.put( "dOnlineSince",
	 * onlineSince ); values.put( "vLastActivityId", lastActivityUid );
	 * values.put( "vRoomName", roomName ); values.put("vSubjectTitle",
	 * subjectTitle ); values.put( "vSubjectId", subjectUid );
	 * values.put("vSubjectUrl",subjectUrl);
	 * 
	 * 
	 * values.put( "iAccess", access ); values.put( "iAccessPermitted",
	 * access_permitted ); values.put( "vLastActivitySince", lastActivitySince
	 * ); values.put( "iUnreadCount", unread );
	 * 
	 * values.put( "vUserSelfUrl", vUserSelfUrl );
	 * 
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vFriendId FROM search_playup_friends WHERE vFriendId = \"" +
	 * friendUid + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate(
	 * "search_playup_friends", values," vFriendId = \"" + friendUid +
	 * "\" ",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert(
	 * "search_playup_friends", values, null, -1);
	 * 
	 * }
	 * 
	 * }
	 */

	/**
	 * Praveen : moduifired as per the href
	 * 
	 * @param vProfileHrefUrl
	 * @param
	 */
	public void setSearchPlayupFriendsData(String friendUid, String friendName,
			String friendAvatar, String friendUserName, String sourceName,
			String sourceIconHref, String vProfileId, boolean isOnline,
			String onlineSince, String lastActivitySince, int access,
			int unread, String lastActivityUid, String roomName,
			String subjectTitle, String subjectUid, String subjectUrl,
			String subjectHrefUrl, String access_permitted,
			String directConversationUrl, String directConversationHrefUrl,
			String vUserSelfUrl, String vUserHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUid);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIconHref", sourceIconHref);
		values.put("vProfileId", vProfileId);
		values.put("vFriendUserName", friendUserName);
		values.put("vGapId", "");

		values.put("vDirectConversationUrl", directConversationUrl);
		values.put("isOnline", isOnline);
		values.put("dOnlineSince", onlineSince);
		values.put("vLastActivityId", lastActivityUid);
		values.put("vRoomName", roomName);
		values.put("vSubjectTitle", subjectTitle);
		values.put("vSubjectId", subjectUid);
		values.put("vSubjectUrl", subjectUrl);

		values.put("iAccess", access);
		values.put("iAccessPermitted", access_permitted);
		values.put("vLastActivitySince", lastActivitySince);
		values.put("iUnreadCount", unread);

		values.put("vUserSelfUrl", vUserSelfUrl);

		values.put("vSubjectHrefUrl", subjectHrefUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);
		values.put("vUserHrefUrl", vUserHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM search_playup_friends WHERE vFriendId = \""
						+ friendUid + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate(
					"search_playup_friends", values,
					" vFriendId = \"" + friendUid + "\" ", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert(
					"search_playup_friends", values, null, -1);

		}

	}

	/**
	 * setting the Friends
	 */
	/*
	 * public void setSearchFriends ( String friendUid,String friendName,String
	 * friendAvatar, String sourceName,String sourceIconHref,String
	 * appInvitationUrl, String appInvitationType, int alreadyInvited,boolean
	 * isOnline, String profileUid,String friendUserName, String onlineSince,
	 * String lastActivityUid, String roomName, String subjectTitle, String
	 * subjectUid, String subjectUrl, int access, int access_permitted, String
	 * lastActivitySince,String profileUrl,String directConversationUrl,String
	 * vFriendType) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vFriendId",
	 * friendUid ); values.put( "vFriendName", friendName ); values.put(
	 * "vFriendAvatar", friendAvatar ); values.put( "vSourceName", sourceName );
	 * values.put( "vSourceIconHref", sourceIconHref );
	 * values.put("vAppInvitationUrl", appInvitationUrl); values.put(
	 * "vAppInvitationType", appInvitationType ); values.put("isAlreadyInvited",
	 * alreadyInvited ); values.put( "isOnline", ( isOnline )? 1 : 0 );
	 * values.put( "vProfileId", profileUid ); values.put("vFriendUserName",
	 * friendUserName);
	 * 
	 * values.put("dOnlineSince",onlineSince); values.put( "vLastActivityId",
	 * lastActivityUid ); values.put("vRoomName", roomName ); values.put(
	 * "vSubjectTitle", subjectTitle ); values.put( "vSubjectId", subjectUid );
	 * values.put("vSubjectUrl", subjectUrl); values.put("iAccess",access);
	 * values.put( "vGapId", "");
	 * values.put("iAccessPermitted",access_permitted);
	 * values.put("dLastActivitySince",lastActivitySince);
	 * values.put("vUserSelfUrl", profileUrl );
	 * values.put("vDirectConversationUrl", directConversationUrl );
	 * values.put("vFriendType", vFriendType );
	 * 
	 * 
	 * // int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vFriendId FROM search_friends WHERE vFriendId = \"" + friendUid
	 * + "\"  " );
	 * 
	 * 
	 * 
	 * 
	 * JsonUtil json = new JsonUtil(); json.queryMethod1 (
	 * Constants.QUERY_INSERT, null, "search_friends", values, " vFriendId = \""
	 * + friendUid + "\" " , "vFriendId", true, false );
	 * 
	 * 
	 * 
	 * if ( count > 0 ) {
	 * 
	 * JsonUtil json = new JsonUtil(); json.queryMethod1(
	 * Constants.QUERY_UPDATE, null, "search_friends", values, " vFriendId = \""
	 * + friendUid + "\" " );
	 * 
	 * } else {
	 * 
	 * JsonUtil json = new JsonUtil(); json.queryMethod1 (
	 * Constants.QUERY_INSERT, null, "search_friends", values, null );
	 * 
	 * }
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href
	 */
	/**
	 * setting the Friends
	 */
	public void setSearchFriends(String friendUid, String friendName,
			String friendAvatar, String sourceName, String sourceIconHref,
			String appInvitationUrl, String appInvitationHrefUrl,
			String appInvitationType, int alreadyInvited, boolean isOnline,
			String profileUid, String friendUserName, String onlineSince,
			String lastActivityUid, String roomName, String subjectTitle,
			String subjectUid, String subjectUrl, String subjectHrefUrl,
			int access, int access_permitted, String lastActivitySince,
			String profileUrl, String vProfileHrefUrl,
			String directConversationUrl, String directConversationHrefUrl,
			String vFriendType) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUid);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIconHref", sourceIconHref);
		values.put("vAppInvitationUrl", appInvitationUrl);
		values.put("vAppInvitationType", appInvitationType);
		values.put("isAlreadyInvited", alreadyInvited);
		values.put("isOnline", (isOnline) ? 1 : 0);
		values.put("vProfileId", profileUid);
		values.put("vFriendUserName", friendUserName);

		values.put("dOnlineSince", onlineSince);
		values.put("vLastActivityId", lastActivityUid);
		values.put("vRoomName", roomName);
		values.put("vSubjectTitle", subjectTitle);
		values.put("vSubjectId", subjectUid);
		values.put("vSubjectUrl", subjectUrl);
		values.put("iAccess", access);
		values.put("vGapId", "");
		values.put("iAccessPermitted", access_permitted);
		values.put("dLastActivitySince", lastActivitySince);
		values.put("vUserSelfUrl", profileUrl);
		values.put("vDirectConversationUrl", directConversationUrl);
		values.put("vFriendType", vFriendType);

		values.put("vAppInvitationHrefUrl", appInvitationHrefUrl);
		values.put("vSubjectHrefUrl", subjectHrefUrl);
		values.put("vUserHrefUrl", vProfileHrefUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);

		// int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		// " SELECT vFriendId FROM search_friends WHERE vFriendId = \"" +
		// friendUid + "\"  " );

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_INSERT, null, "search_friends",
				values, " vFriendId = \"" + friendUid + "\" ", "vFriendId",
				true, false);

		/*
		 * if ( count > 0 ) {
		 * 
		 * JsonUtil json = new JsonUtil(); json.queryMethod1(
		 * Constants.QUERY_UPDATE, null, "search_friends", values,
		 * " vFriendId = \"" + friendUid + "\" " );
		 * 
		 * } else {
		 * 
		 * JsonUtil json = new JsonUtil(); json.queryMethod1 (
		 * Constants.QUERY_INSERT, null, "search_friends", values, null );
		 * 
		 * }
		 */

	}

	public void setSearchFriendsGap(String gap_uid) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", "");
		values.put("vFriendName", "");
		values.put("vFriendAvatar", "");
		values.put("vSourceName", "");
		values.put("vSourceIconHref", "");
		values.put("vAppInvitationUrl", "");
		values.put("vAppInvitationType", "");
		values.put("isAlreadyInvited", "");
		values.put("isOnline", "");
		values.put("vProfileId", "");
		values.put("vFriendUserName", "");

		values.put("dOnlineSince", "");
		values.put("vLastActivityId", "");
		values.put("vRoomName", "");
		values.put("vSubjectTitle", "");
		values.put("vSubjectId", "");
		values.put("vSubjectUrl", "");
		values.put("iAccess", "");

		values.put("iAccessPermitted", "");
		values.put("dLastActivitySince", "");

		values.put("vAppInvitationHrefUrl", "");
		values.put("vSubjectHrefUrl", "");
		values.put("vUserHrefUrl", "");
		values.put("vDirectConversationHrefUrl", "");

		values.put("vGapId", gap_uid);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vGapId FROM search_friends WHERE vGapId = \""
						+ gap_uid + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "search_friends", values,
					" vGapId = \"" + gap_uid + "\" ");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "search_friends", values,
					null);

		}

		updateGapInfo(gap_uid, "", "", 0);

	}

	/**
	 * setting the Friends
	 */
	public void setFriendsGap(String gap_uid) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", "");
		values.put("vFriendName", "");
		values.put("vFriendAvatar", "");
		values.put("vSourceName", "");
		values.put("vSourceIconHref", "");
		values.put("vAppInvitationUrl", "");
		values.put("vAppInvitationType", "");
		values.put("isAlreadyInvited", "");
		values.put("isOnline", "");
		values.put("vProfileId", "");
		values.put("vFriendUserName", "");

		values.put("vFriendType", "");

		values.put("vAppInvitationHrefUrl", "");

		values.put("vUserHrefUrl", "");
		values.put("vDirectConversationHrefUrl", "");

		values.put("vGapId", gap_uid);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vGapId FROM my_friends WHERE vGapId = \"" + gap_uid
						+ "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "my_friends", values,
					" vGapId = \"" + gap_uid + "\" ");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "my_friends", values, null);

		}
		updateGapInfo(gap_uid, "", "", 0);

	}

	public void setPlayupFriendsGap(String gap_uid) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", "");
		values.put("vFriendName", "");
		values.put("vFriendAvatar", "");
		values.put("vSourceName", "");
		values.put("vSourceIconHref", "");
		values.put("vProfileId", "");
		values.put("vFriendUserName", "");

		values.put("vDirectConversationUrl", "");
		values.put("isOnline", "");
		values.put("dOnlineSince", "");
		values.put("vLastActivityId", "");
		values.put("vRoomName", "");
		values.put("vSubjectTitle", "");
		values.put("vSubjectId", "");
		values.put("vSubjectUrl", "");

		values.put("iAccess", "");
		values.put("iAccessPermitted", "");
		values.put("vLastActivitySince", "");
		values.put("iUnreadCount", "");

		values.put("vUserSelfUrl", "");

		values.put("vGapId", gap_uid);

		values.put("vSubjectHrefUrl", "");

		values.put("vDirectConversationHrefUrl", "");

		values.put("vUserHrefUrl", "");

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vGapId FROM playup_friends WHERE vGapId = \""
						+ gap_uid + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "playup_friends", values,
					" vGapId = \"" + gap_uid + "\" ");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "playup_friends", values,
					null);

		}
		updateGapInfo(gap_uid, "", "", 0);

	}

	public void setSearchPlayupFriendsGap(String gap_uid) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", "");
		values.put("vFriendName", "");
		values.put("vFriendAvatar", "");
		values.put("vSourceName", "");
		values.put("vSourceIconHref", "");
		values.put("vProfileId", "");
		values.put("vFriendUserName", "");
		values.put("vDirectConversationUrl", "");
		values.put("isOnline", "");
		values.put("dOnlineSince", "");
		values.put("vLastActivityId", "");
		values.put("vRoomName", "");
		values.put("vSubjectTitle", "");
		values.put("vSubjectId", "");
		values.put("vSubjectUrl", "");

		values.put("iAccess", "");
		values.put("iAccessPermitted", "");
		values.put("vLastActivitySince", "");
		values.put("iUnreadCount", "");

		values.put("vUserSelfUrl", "");

		values.put("vSubjectHrefUrl", "");
		values.put("vDirectConversationHrefUrl", "");
		values.put("vUserHrefUrl", "");

		values.put("vGapId", gap_uid);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vGapId FROM search_playup_friends WHERE vGapId = \""
						+ gap_uid + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "search_playup_friends",
					values, " vGapId = \"" + gap_uid + "\" ");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "search_playup_friends",
					values, null);

		}

		updateGapInfo(gap_uid, "", "", 0);

	}

	/*
	 * public void updateGapInfo(String gap_uid,String gap_url,int gapSize) {
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * values.put( "gap_uid", gap_uid); values.put("gap_url", gap_url);
	 * values.put("gap_size", gapSize);
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT gap_uid FROM gap_info WHERE gap_uid = \"" + gap_uid + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(Constants.
	 * QUERY_UPDATE, null,"gap_info", values,"gap_uid = \"" + gap_uid + "\" ");
	 * 
	 * 
	 * 
	 * } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(Constants
	 * .QUERY_INSERT, null,"gap_info", values, null);
	 * 
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * }
	 */
	public void updateGapInfo(String gap_uid, String gap_url,
			String gap_href_url, int gapSize) {

		ContentValues values = new ContentValues();

		values.put("gap_uid", gap_uid);
		values.put("gap_url", gap_url);
		values.put("gap_size", gapSize);

		values.put("gap_href_url", gap_href_url);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT gap_uid FROM gap_info WHERE gap_uid = \"" + gap_uid
						+ "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "gap_info", values,
					"gap_uid = \"" + gap_uid + "\" ");

		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "gap_info", values, null);

		}

	}

	public String getFriendsUrl() {
		String friendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vFriendsUrl,vFriendsHrefUrl FROM user WHERE isPrimaryUser = '1'");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				friendsUrl = c.getString(c.getColumnIndex("vFriendsHrefUrl"));

				if (friendsUrl != null && friendsUrl.trim().length() > 0) {

				} else {
					friendsUrl = c.getString(c.getColumnIndex("vFriendsUrl"));

				}
			}
			c.close();

		}
		c = null;

		return friendsUrl;
	}

	public Hashtable<String, Object> getFriendsUrlForApiCall() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String friendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vFriendsUrl,vFriendsHrefUrl FROM user WHERE isPrimaryUser = '1'");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				friendsUrl = c.getString(c.getColumnIndex("vFriendsHrefUrl"));

				if (friendsUrl != null && friendsUrl.trim().length() > 0) {

					result.put("url", friendsUrl);

					result.put("isHref", true);
				} else {
					friendsUrl = c.getString(c.getColumnIndex("vFriendsUrl"));

					result.put("url", friendsUrl);

					result.put("isHref", false);

				}
			}
			c.close();

		}
		c = null;

		return result;
	}

	public String getPlayupFriendsUrl() {
		String playupFriendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vPlayUpFriendUrl,vPlayUpFriendHrefUrl FROM user WHERE isPrimaryUser = '1'");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				playupFriendsUrl = c.getString(c
						.getColumnIndex("vPlayUpFriendHrefUrl"));

				if (playupFriendsUrl != null
						&& playupFriendsUrl.trim().length() > 0) {

				} else {
					playupFriendsUrl = c.getString(c
							.getColumnIndex("vPlayUpFriendUrl"));
				}
			}
			c.close();

		}
		c = null;

		return playupFriendsUrl;
	}

	public String getPlayupFriendsSearchUrl() {
		String playupFriendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vPlayupFriendSearchUrl FROM user WHERE isPrimaryUser = '1'");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				playupFriendsUrl = c.getString(c
						.getColumnIndex("vPlayupFriendSearchUrl"));

			}
			c.close();

		}
		c = null;

		return playupFriendsUrl;
	}

	public void setPlayupFriendsData(String playupFriendsSearchUrl,
			int playupFriendsCount) {

		ContentValues values = new ContentValues();
		values.put("vPlayupFriendSearchUrl", playupFriendsSearchUrl);
		values.put("vPlayupFriendCount", playupFriendsCount);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vPlayupFriendSearchUrl from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	public Hashtable<String, Object> getFriendSearchUrl() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vFriendSearchUrl = "";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vFriendSearchUrl FROM user WHERE isPrimaryUser = '1' ");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				// vFriendSearchUrl =
				// c.getString(c.getColumnIndex("vFriendsHrefUrl"));

				// if(vFriendSearchUrl != null &&
				// vFriendSearchUrl.trim().length() > 0){

				// result.put("url",vFriendSearchUrl);
				// result.put("isHref",true);

				// }else{

				vFriendSearchUrl = c.getString(c
						.getColumnIndex("vFriendSearchUrl"));
				if (vFriendSearchUrl != null
						&& vFriendSearchUrl.trim().length() > 0) {
					result.put("url", vFriendSearchUrl);
					result.put("isHref", false);
				} else {
					result.put("url", "");
					result.put("isHref", false);
				}
				/*
				 * result.put("url",vFriendSearchUrl);
				 * result.put("isHref",false);
				 */

				// }
			}
			c.close();

		}
		c = null;

		return result;
	}

	public Hashtable<String, List<String>> getOnlineFriendsData() {

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT * FROM my_friends_live");

	}

	public Hashtable<String, List<String>> getAllFriendsData() {

		/*
		 * return PlayupLiveApplication.getDatabaseWrapper().select(
		 * "SELECT vFriendId, vGapId, vFriendName, vSourceName," +
		 * " vFriendAvatar, vAppInvitationUrl, vSourceIconHref, isOnline, vFriendUserName,"
		 * +
		 * " isAlreadyInvited FROM my_friends WHERE isAlreadyInvited != 3 AND isSearch != 1 ORDER BY vGapId,upper(vFriendName)"
		 * );
		 */
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vFriendId, vGapId, vFriendName, vSourceName,"
						+ " vFriendAvatar, vAppInvitationUrl,vAppInvitationHrefUrl, vSourceIconHref, isOnline, vFriendUserName,"
						+ " isAlreadyInvited FROM my_friends WHERE isAlreadyInvited != 3 AND isSearch != 1 ORDER BY vGapId,upper(vFriendName)");

	}

	public Hashtable<String, List<String>> getPlayupFriendsData() {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vUserSelfUrl,vUserHrefUrl, vDirectConversationUrl,vDirectConversationHrefUrl, vFriendId ,vFriendName,vFriendAvatar,"
						+ "vSourceIconHref,vFriendUserName,vProfileId,isOnline,"
						+ "vGapId FROM playup_friends ORDER BY vGapId,upper(vFriendName)");
	}

	public Hashtable<String, List<String>> getGapUrl(String gapUid) {

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT gap_url,gap_size,gap_href_url  FROM gap_info WHERE gap_uid = '"
						+ gapUid + "'");

	}

	public void removeFriendGapEntry(String gapUid) {
		try {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "gap_info", null,
					"gap_uid = \"" + gapUid + "\" ");

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "my_friends", null,
					" vGapId = \"" + gapUid + "\" ");

		} catch (Exception e) {
			// TODO: handle exception

		}
	}

	public void removePlayupFriendGapEntry(String gapUid) {
		try {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "gap_info", null,
					"gap_uid = \"" + gapUid + "\" ");

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "playup_friends", null,
					" vGapId = \"" + gapUid + "\" ");

		} catch (Exception e) {
			// TODO: handle exception

		}
	}

	/*
	 * public void setLiveFriends(String friendUID, String friendName, String
	 * friendAvatar, String sourceName, String sourceIconHref, boolean isOnline,
	 * String profileUid, String userName,String onlineSince, String
	 * lastActivityUid ,String roomName,String subjectTitle ,String subjectUid
	 * ,String subjectUrl , int access, String access_permitted, String
	 * lastActivitySince,String profileUrl,String directConversationUrl) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put( "vFriendId",
	 * friendUID ); values.put( "vFriendName", friendName ); values.put(
	 * "vFriendAvatar", friendAvatar ); values.put( "vSourceName", sourceName );
	 * values.put( "vSourceIcon", sourceIconHref );
	 * 
	 * values.put( "isOnline", isOnline ); values.put("vProfileId", profileUid
	 * ); values.put("vFriendUserName", userName);
	 * values.put("dOnlineSince",onlineSince);
	 * 
	 * 
	 * values.put( "vLastActivityId", lastActivityUid ); values.put("vRoomName",
	 * roomName ); values.put( "vSubjectTitle", subjectTitle ); values.put(
	 * "vSubjectId", subjectUid ); values.put("vSubjectUrl", subjectUrl);
	 * values.put("iAccess",access);
	 * values.put("iAccessPermitted",access_permitted);
	 * values.put("vLastActivitySince", lastActivitySince);
	 * 
	 * 
	 * values.put("vUserSelfUrl", profileUrl );
	 * values.put("vDirectConversationUrl", directConversationUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vFriendId FROM my_friends_live WHERE vFriendId = \"" + friendUID
	 * + "\"  " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate
	 * ("my_friends_live", values," vFriendId = \"" + friendUID + "\" ",null,
	 * -1); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("my_friends_live"
	 * , values, null, -1); } }
	 */

	/**
	 * Praveen
	 * 
	 * @param directConversationHrefUrl
	 * @param directConversationUrl2
	 * @param subjectHrefUrl
	 */
	public void setLiveFriends(String friendUID, String friendName,
			String friendAvatar, String sourceName, String sourceIconHref,
			boolean isOnline, String profileUid, String userName,
			String onlineSince, String lastActivityUid, String roomName,
			String subjectTitle, String subjectUid, String subjectUrl,
			String subjectHrefUrl, int access, String access_permitted,
			String lastActivitySince, String profileUrl,
			String vProfileHrefUrl, String directConversationUrl,
			String directConversationHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUID);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIcon", sourceIconHref);

		values.put("isOnline", isOnline);
		values.put("vProfileId", profileUid);
		values.put("vFriendUserName", userName);
		values.put("dOnlineSince", onlineSince);

		values.put("vLastActivityId", lastActivityUid);
		values.put("vRoomName", roomName);
		values.put("vSubjectTitle", subjectTitle);
		values.put("vSubjectId", subjectUid);
		values.put("vSubjectUrl", subjectUrl);
		values.put("iAccess", access);
		values.put("iAccessPermitted", access_permitted);
		values.put("vLastActivitySince", lastActivitySince);

		values.put("vUserSelfUrl", profileUrl);
		values.put("vDirectConversationUrl", directConversationUrl);

		values.put("vSubjectHrefUrl", subjectHrefUrl);
		values.put("vUserHrefUrl", vProfileHrefUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM my_friends_live WHERE vFriendId = \""
						+ friendUID + "\"  ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().directUpdate(
					"my_friends_live", values,
					" vFriendId = \"" + friendUID + "\" ", null, -1);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().directInsert(
					"my_friends_live", values, null, -1);
		}
	}

	public void setSearchLiveFriends(String friendUID, String friendName,
			String friendAvatar, String sourceName, String sourceIconHref,
			boolean isOnline, String profileSelf, String profileType,
			String profileUid, String userName, String onlineSince,
			String lastActivityUid, String roomName, String subjectTitle,
			String subjectUid, String subjectUrl, String access,
			String access_permitted, String lastActivitySince) {

		ContentValues values = new ContentValues();
		values.put("friendUid", friendUID);
		values.put("friendName", friendName);
		values.put("friendAvatar", friendAvatar);
		values.put("sourceName", sourceName);
		values.put("sourceIcon", sourceIconHref);

		values.put("isOnline", isOnline);
		values.put("profileSelf", profileSelf);
		values.put("profileType", profileType);
		values.put("profileUid", profileUid);
		values.put("friendUserName", userName);
		values.put("onlineSince", onlineSince);

		values.put("lastActivityUid", lastActivityUid);
		values.put("roomName", roomName);
		values.put("subjectTitle", subjectTitle);
		values.put("subjectUid", subjectUid);
		values.put("subjectUrl", subjectUrl);
		values.put("access", access);

		values.put("accessPermitted", access_permitted);
		values.put("lastActivitySince", lastActivitySince);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT friendUid FROM search_friends_live WHERE friendUid = \""
						+ friendUID + "\"  ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate(
					"search_friends_live", values,
					" friendUid = \"" + friendUID + "\" ", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert(
					"search_friends_live", values, null, -1);

		}

	}

	public Hashtable<String, List<String>> searchonlineFriendsData() {

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT * FROM search_friends_live");
	}

	public Hashtable<String, List<String>> searchAllFriendsData() {
		return PlayupLiveApplication.getDatabaseWrapper()
				.select("SELECT * FROM search_friends "
						+ "ORDER BY vGapId,vFriendName");
	}

	public Hashtable<String, List<String>> searchPlayupFriendsData() {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vUserSelfUrl, vSubjectHrefUrl,vDirectConversationHrefUrl,vUserHrefUrl,vDirectConversationUrl, vFriendId ,vFriendName,vFriendAvatar,"
						+ "vSourceIconHref,vFriendUserName,vProfileId,isOnline,"
						+ "vGapId FROM search_playup_friends ORDER BY vGapId ");
	}

	public Hashtable<String, List<String>> searchConversationAllFriendsData(
			String vConversationId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" "
						+ "SELECT mf.vFriendId AS vFriendId,  mf.vFriendName AS vFriendName, mf.vFriendAvatar AS vFriendAvatar, "
						+ "mf.vSourceName AS vSourceName,mf.vGapId AS vGapId, "
						+ "mf.vSourceIconHref AS vSourceIconHref, mf.isOnline AS isOnline, mf.vFriendUserName AS vFriendUserName, "
						+ "mf.isAlreadyInvited AS isAlreadyInvited, "
						+ "ifnull( ri.iStatus, 0 ) AS iStatus FROM search_friends mf LEFT JOIN  "
						+ "recent_invite ri  ON mf.vFriendId = ri.vFriendId AND ri.vConversationId = \""
						+ vConversationId + "\"  "
						+ "ORDER BY ifnull(mf.vGapId,''),upper(mf.vFriendName)");
	}

	public void updatePushNotification(final String vConversationId,
			final String vPushId) {

		ContentValues values = new ContentValues();
		values.put("vConversationId", vConversationId);
		values.put("vPushId", vPushId);
		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vPushId FROM push_notifications WHERE  vPushId = \""
						+ vPushId + "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "push_notifications", values,
					" vPushId = \"" + vPushId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "push_notifications", values,
					null);
		}

	}

	/**
	 * Updating recently selected competition id in sports
	 **/
	public void setRecentSelectedCompetetion(String vCompetitionId,
			String vSportsId) {

		ContentValues values = new ContentValues();
		values.put("vCompetitionId", vCompetitionId);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "sports", values,
				" vSportsId = \"" + vSportsId + "\" ", null, false, true);

	}

	/**
	 * Getting the current round id for the competition
	 */
	public String getRecentCompetitionId(final String vSportsId) {

		String vCompetitionId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT c.vCompetitionId AS vCompetitionId FROM competition c LEFT JOIN sports s ON s.vCompetitionId = c.vCompetitionId WHERE s.vSportsId = \""
								+ vSportsId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				vCompetitionId = c
						.getString(c.getColumnIndex("vCompetitionId"));
			}
			c.close();
		}
		c = null;
		return vCompetitionId;
	}

	public int getLeagueSelectedPosition(String vCompetitionId,
			Hashtable<String, List<String>> data) {

		int selectedPos = -1;
		if (data != null && data.get("vCompetitionId").size() > 0) {
			int len = data.get("vCompetitionId").size();

			for (int i = 0; i < len; i++) {
				if (vCompetitionId != null
						&& vCompetitionId.equalsIgnoreCase(data.get(
								"vCompetitionId").get(i))) {
					selectedPos = i;
				}
			}

		}

		return selectedPos;
	}

	public void emptySearchFriends() {
		JsonUtil json = new JsonUtil();

		json.queryMethod1(Constants.QUERY_DELETE, null, "search_friends", null,
				null, null, false, false);

	}

	public void emptySearchPlayupFriends() {
		JsonUtil json = new JsonUtil();

		json.queryMethod1(Constants.QUERY_DELETE, null,
				"search_playup_friends", null, null, null, false, false);

	}

	/*
	 * public void setLivefriendsUrl(String liveFriendsUrl) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vLiveFriendsUrl", liveFriendsUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vLiveFriendsUrl from user where isPrimaryUser = 1" );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
	 * values,"isPrimaryUser = 1",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("user", values,
	 * null, -1);
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Praveen: mod as per href
	 * 
	 * @param liveFriendsHrefUrl
	 * 
	 * 
	 */
	public void setLivefriendsUrl(String liveFriendsUrl,
			String liveFriendsHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vLiveFriendsUrl", liveFriendsUrl);
		values.put("vLiveFriendsHrefUrl", liveFriendsHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vLiveFriendsUrl from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	/*
	 * public void setPlayupLivefriendsUrl(String liveFriendsUrl) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vPlayupLiveFriendsUrl", liveFriendsUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vPlayupLiveFriendsUrl from user where isPrimaryUser = 1" );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
	 * values,"isPrimaryUser = 1",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("user", values,
	 * null, -1);
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Pra veen: mod as per the href
	 * 
	 * @param liveFriendsHrefUrl
	 */
	public void setPlayupLivefriendsUrl(String liveFriendsUrl,
			String liveFriendsHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vPlayupLiveFriendsUrl", liveFriendsUrl);
		values.put("vPlayupLiveFriendsHrefUrl", liveFriendsHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vPlayupLiveFriendsUrl,vPlayupLiveFriendsHrefUrl from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	/*
	 * public void setPlayupUpdatefriendsUrl(String updateFriendsUrl) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vPlayupUpdateFriendsUrl", updateFriendsUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vPlayupUpdateFriendsUrl from user where isPrimaryUser = 1" );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
	 * values,"isPrimaryUser = 1",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("user", values,
	 * null, -1);
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Praveen : mpd as per the href
	 * 
	 * @param updateFriendsHrefUrl
	 * @param updateFriendsUrl2
	 * 
	 */

	public void setPlayupUpdatefriendsUrl(String updateFriendsUrl,
			String updateFriendsHrefUrl) {

		ContentValues values = new ContentValues();
		values.put("vPlayupUpdateFriendsUrl", updateFriendsUrl);
		values.put("vPlayupUpdateFriendsHrefUrl", updateFriendsHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vPlayupUpdateFriendsUrl,vPlayupUpdateFriendsHrefUrl from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	public int getFriendsCount() {

		return PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vFriendId from my_friends ");

	}

	public int getPlayupFriendsSize() {

		return PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vFriendId from playup_friends ");

	}

	public Hashtable<String, Object> getLiveFriendsUrl() {
		String liveFriendsUrl = "";
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT  vLiveFriendsUrl,vLiveFriendsHrefUrl from user where isPrimaryUser = 1");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();

				String url = c.getString(c
						.getColumnIndex("vLiveFriendsHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vLiveFriendsUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	/*
	 * public String getPlayupLiveFriendsUrl() { String liveFriendsUrl = "";
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT  vPlayupLiveFriendsUrl from user where isPrimaryUser = 1" );
	 * 
	 * if( c != null){ if(c.getCount() > 0){ c.moveToFirst(); liveFriendsUrl =
	 * c.getString(c.getColumnIndex("vPlayupLiveFriendsUrl")); } c.close(); } c
	 * = null; return liveFriendsUrl;
	 * 
	 * }
	 */
	/**
	 * Praveen:
	 */
	public Hashtable<String, Object> getPlayupLiveFriendsUrl() {
		String liveFriendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT  vPlayupLiveFriendsUrl,vPlayupLiveFriendsHrefUrl from user where isPrimaryUser = 1");
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c
						.getColumnIndex("vPlayupLiveFriendsHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vPlayupLiveFriendsUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}

			}
			c.close();
		}
		c = null;
		return result;

	}

	public Hashtable<String, Object> getPlayupUpdateFriendsUrl() {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT  vPlayupUpdateFriendsUrl,vPlayupUpdateFriendsHrefUrl from user where isPrimaryUser = 1");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();

				String url = c.getString(c
						.getColumnIndex("vPlayupUpdateFriendsHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c
							.getColumnIndex("vPlayupUpdateFriendsUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	public void setTotalFriendsCount(String friendsCount) {

		ContentValues values = new ContentValues();
		values.put("vFriendsCount", friendsCount);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vFriendsCount from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	public String getTotalFriendsCount() {
		String friendsCount = "";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT  vFriendsCount from user where isPrimaryUser = 1");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				friendsCount = c.getString(c.getColumnIndex("vFriendsCount"));
			}
			c.close();
		}
		c = null;
		return friendsCount;

	}

	public String getPlayupFriendsCount() {
		String friendsCount = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT COUNT(*) AS vPlayupFriendCount FROM playup_friends WHERE LENGTH( vGapId ) == 0 ");

		// Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
		// " SELECT  vPlayupFriendCount from user where isPrimaryUser = 1" );

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				friendsCount = c.getString(c
						.getColumnIndex("vPlayupFriendCount"));
			}
			c.close();
		}
		c = null;

		return friendsCount;

	}

	/*
	 * public void setUpdateFriendsUrl(String updateFriendsUrl) { ContentValues
	 * values = new ContentValues (); values.put( "vUpdateFriendsUrl",
	 * updateFriendsUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vUpdateFriendsUrl from user where isPrimaryUser = 1" );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
	 * values,"isPrimaryUser = 1",null, -1);
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().directInsert("user", values,
	 * null, -1);
	 * 
	 * }
	 * 
	 * }
	 */
	/**
	 * Pravee:Mod as per the href
	 * 
	 * @param updateFriendsHrefUrl
	 */
	public void setUpdateFriendsUrl(String updateFriendsUrl,
			String updateFriendsHrefUrl) {
		ContentValues values = new ContentValues();
		values.put("vUpdateFriendsUrl", updateFriendsUrl);
		values.put("vUpdateFriendsHrefUrl", updateFriendsHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vUpdateFriendsUrl from user where isPrimaryUser = 1");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().directUpdate("user",
					values, "isPrimaryUser = 1", null, -1);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().directInsert("user",
					values, null, -1);

		}

	}

	/*
	*//**
	 * setting the direct conversation url and id
	 */
	/*
	 * public void setUserDirectConversation ( String vDirectConversationId,
	 * String vDirectConversationUrl, int total_count, int unread_count, String
	 * vUserId ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put(
	 * "vDirectConversationId", vDirectConversationId ); values .put(
	 * "vDirectConversationUrl", vDirectConversationUrl ); values .put(
	 * "iTotalCount", total_count ); values .put( "iUnReadCount" , unread_count
	 * ); values.put( "vUserId", vUserId );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vUserId =  \""
	 * + vUserId + "\" " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user_direct_conversation", values,
	 * " vUserId =  \"" + vUserId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "user_direct_conversation", values, null );
	 * }
	 * 
	 * }
	 */

	/**
	 * Praveen : as per the href setting the direct conversation url and id
	 */
	public void setUserDirectConversation(String vDirectConversationId,
			String vDirectConversationUrl, String vDirectConversationHrefUrl,
			int total_count, int unread_count, String vUserId) {

		ContentValues values = new ContentValues();
		values.put("vDirectConversationId", vDirectConversationId);
		values.put("vDirectConversationUrl", vDirectConversationUrl);
		values.put("iTotalCount", total_count);
		values.put("iUnReadCount", unread_count);
		values.put("vUserId", vUserId);

		values.put("vDirectConversationHrefUrl", vDirectConversationHrefUrl);

		String query = " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vUserId =  \""
				+ vUserId + "\" ";
		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				query);

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "user_direct_conversation",
					values, " vUserId =  \"" + vUserId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user_direct_conversation",
					values, null);
		}

	}

	/*	*//**
	 * setting the direct conversation url and id
	 */
	/*
	 * public void setUserDirectConversation ( String vDirectConversationId,
	 * String vDirectConversationUrl,String vUserId ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * 
	 * 
	 * if ( vDirectConversationId != null &&
	 * vDirectConversationId.trim().length() > 0 ) { values .put(
	 * "vDirectConversationId", vDirectConversationId ); } values .put(
	 * "vDirectConversationUrl", vDirectConversationUrl ); values.put(
	 * "vUserId", vUserId );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDirectConversationId FROM user_direct_conversation   WHERE ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
	 * + vDirectConversationUrl +
	 * "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
	 * + vDirectConversationUrl + "\") " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user_direct_conversation", values,
	 * " vDirectConversationUrl =  \"" + vDirectConversationUrl + "\"  " ); }
	 * else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "user_direct_conversation", values, null );
	 * }
	 * 
	 * }
	 */

	public void setUserDirectConversation(String vDirectConversationUrl,
			String vUserId) {

		ContentValues values = new ContentValues();
		values.put("vDirectConversationUrl", vDirectConversationUrl);
		values.put("vUserId", vUserId);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vDirectConversationId FROM user_direct_conversation  WHERE ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
								+ vDirectConversationUrl
								+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
								+ vDirectConversationUrl + "\")");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"user_direct_conversation",
					values,
					" vDirectConversationUrl =  \"" + vDirectConversationUrl
							+ "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user_direct_conversation",
					values, null);
		}

	}

	/**
	 * Praveen: modified as per the href element
	 * 
	 * @param vDirectConversationId
	 * @param directConversationUrl
	 * @param directConversationHrefUrl
	 * @param isHref
	 * @param vUserId
	 */
	public void setUserDirectConversation(String vDirectConversationId,
			String directConversationUrl, String directConversationHrefUrl,
			boolean isHref, String vUserId) {

		// ContentValues values = new ContentValues ();
		// values .put( "vDirectConversationId", vDirectConversationId );
		// values .put( "vDirectConversationUrl", directConversationUrl );
		// values .put( "vDirectConversationHrefUrl", directConversationHrefUrl
		// );
		// values.put( "vUserId", vUserId );

		ContentValues values = new ContentValues();
		values.put("vDirectConversationId", vDirectConversationId);
		values.put("vDirectConversationUrl", directConversationUrl);
		values.put("vDirectConversationHrefUrl", directConversationHrefUrl);
		values.put("vUserId", vUserId);

		// ( LENGTH(vLinkUrl) > 0 AND vLinkUrl = '"+vSectionUrl+"')
		/*
		 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		 * " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vDirectConversationUrl =  \""
		 * + vDirectConversationUrl + "\" " );
		 * 
		 * if ( count > 0 ) {
		 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
		 * Constants.QUERY_UPDATE, null, "user_direct_conversation", values,
		 * " vDirectConversationUrl =  \"" + vDirectConversationUrl + "\"  " );
		 * } else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
		 * Constants.QUERY_INSERT, null, "user_direct_conversation", values,
		 * null ); }
		 */
		String query = " SELECT  vDirectConversationId FROM user_direct_conversation WHERE ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
				+ directConversationUrl
				+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
				+ directConversationHrefUrl + "\")";
		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				query);

		if (count > 0) {
			PlayupLiveApplication
					.getDatabaseWrapper()
					.queryMethod2(
							Constants.QUERY_UPDATE,
							null,
							"user_direct_conversation",
							values,
							"( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
									+ directConversationUrl
									+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
									+ directConversationHrefUrl + "\")");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user_direct_conversation",
					values, null);
		}

	}

	//
	public void setUserDirectConversation(String vDirectConversationId,
			String vDirectConversationUrl, String vUserId,
			String vDirectMessageId, boolean isDirectConverrsationUrlHref) {

		ContentValues values = new ContentValues();

		if (vDirectConversationId != null
				&& vDirectConversationId.trim().length() > 0) {
			values.put("vDirectConversationId", vDirectConversationId);
		}
		if (vDirectMessageId != null && vDirectMessageId.trim().length() > 0) {
			values.put("vDirectMessageId", vDirectMessageId);
		}

		if (isDirectConverrsationUrlHref) {
			values.put("vDirectConversationHrefUrl", vDirectConversationUrl);
		} else {
			values.put("vDirectConversationUrl", vDirectConversationUrl);
		}

		values.put("vUserId", vUserId);

		int count = 0;
		if (vDirectMessageId == null) {

			count = PlayupLiveApplication
					.getDatabaseWrapper()
					.getTotalCount(
							" SELECT  vDirectConversationId FROM user_direct_conversation  WHERE ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
									+ vDirectConversationUrl
									+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
									+ vDirectConversationUrl + "\") ");

			// } else {
			// count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
			// " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vDirectConversationUrl =  \""
			// + vDirectConversationUrl + "\" AND vDirectMessageId = \"" +
			// vDirectMessageId + "\"  " );
			// }

			if (count > 0) {
				PlayupLiveApplication
						.getDatabaseWrapper()
						.queryMethod2(
								Constants.QUERY_UPDATE,
								null,
								"user_direct_conversation",
								values,
								"  ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
										+ vDirectConversationUrl
										+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
										+ vDirectConversationUrl + "\") ");
			} else {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null,
						"user_direct_conversation", values, null);
			}

		}
	}

	/*	*//**
	 * Praveen
	 * 
	 */
	/*
	 * public void setUserDirectConversation ( String vDirectConversationId,
	 * String vDirectConversationUrl,String vUserId, String vDirectMessageId ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues ();
	 * 
	 * 
	 * 
	 * if ( vDirectConversationId != null &&
	 * vDirectConversationId.trim().length() > 0 ) { values .put(
	 * "vDirectConversationId", vDirectConversationId ); } if ( vDirectMessageId
	 * != null && vDirectMessageId.trim().length() > 0 ) { values .put(
	 * "vDirectMessageId", vDirectMessageId ); } values .put(
	 * "vDirectConversationUrl", vDirectConversationUrl ); values.put(
	 * "vUserId", vUserId );
	 * 
	 * 
	 * int count = 0; if ( vDirectMessageId == null ) { int count =
	 * PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vDirectConversationUrl =  \""
	 * + vDirectConversationUrl + "\" " ); // } else { // count =
	 * PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vDirectConversationUrl =  \""
	 * + vDirectConversationUrl + "\" AND vDirectMessageId = \"" +
	 * vDirectMessageId + "\"  " ); // }
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "user_direct_conversation", values,
	 * " vDirectConversationUrl =  \"" + vDirectConversationUrl + "\"  " ); }
	 * else { PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "user_direct_conversation", values, null );
	 * }
	 * 
	 * }
	 */

	/**
	 * Praveen
	 * 
	 */
	public void setUserDirectConversation(String vDirectConversationId,
			String vDirectConversationUrl, String vDirectConversationHrefUrl,
			String vUserId, String vDirectMessageId) {

		ContentValues values = new ContentValues();

		if (vDirectConversationId != null
				&& vDirectConversationId.trim().length() > 0) {
			values.put("vDirectConversationId", vDirectConversationId);
		}
		if (vDirectMessageId != null && vDirectMessageId.trim().length() > 0) {
			values.put("vDirectMessageId", vDirectMessageId);
		}

		values.put("vDirectConversationUrl", vDirectConversationUrl);

		values.put("vDirectConversationHrefUrl", vDirectConversationHrefUrl);
		values.put("vUserId", vUserId);

		/*
		 * int count = 0; if ( vDirectMessageId == null ) {
		 */

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vDirectConversationId FROM user_direct_conversation WHERE ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
								+ vDirectConversationUrl
								+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
								+ vDirectConversationHrefUrl + "\")  ");

		// } else {
		// count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
		// " SELECT  vDirectConversationId FROM user_direct_conversation WHERE vDirectConversationUrl =  \""
		// + vDirectConversationUrl + "\" AND vDirectMessageId = \"" +
		// vDirectMessageId + "\"  " );
		// }

		if (count > 0) {
			PlayupLiveApplication
					.getDatabaseWrapper()
					.queryMethod2(
							Constants.QUERY_UPDATE,
							null,
							"user_direct_conversation",
							values,
							" ( LENGTH(vDirectConversationUrl) > 0 AND vDirectConversationUrl =  \""
									+ vDirectConversationUrl
									+ "\" ) OR ( LENGTH(vDirectConversationHrefUrl) > 0 AND vDirectConversationHrefUrl =  \""
									+ vDirectConversationHrefUrl + "\")   ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "user_direct_conversation",
					values, null);
		}

	}

	/*
	 * public void setDirectConversation ( String vDirectMessageUrl, String
	 * vDirectMessageId, String vDirectConversationId, int unread_count, String
	 * vUserSelfUrl, String vUserId, String vDisplayName, String vAvatarUrl ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put(
	 * "vDirectMessageUrl", vDirectMessageUrl ); values .put(
	 * "vDirectMessageId", vDirectMessageId ); values .put(
	 * "vDirectConversationId", vDirectConversationId );
	 * 
	 * values .put( "iUnReadCount" , unread_count );
	 * 
	 * values .put( "vUserSelfUrl", vUserSelfUrl ); values.put( "vUserId",
	 * vUserId ); values.put( "vDisplayName", vDisplayName ); values.put(
	 * "vAvatarUrl", vAvatarUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDirectMessageId FROM direct_conversation WHERE vDirectMessageId =  \""
	 * + vDirectMessageId + "\" " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "direct_conversation", values,
	 * " vDirectMessageId =  \"" + vDirectMessageId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "direct_conversation", values, null ); }
	 * 
	 * }
	 */

	/**
	 * Praveen: As per the href
	 */

	public void setDirectConversation(String vDirectMessageUrl,
			String vDirectMessageHrefUrl, String vDirectMessageId,
			String vDirectConversationId, int unread_count,
			String vUserSelfUrl, String vUserHrefUrl, String vUserId,
			String vDisplayName, String vAvatarUrl) {

		ContentValues values = new ContentValues();
		values.put("vDirectMessageUrl", vDirectMessageUrl);
		values.put("vDirectMessageId", vDirectMessageId);
		values.put("vDirectConversationId", vDirectConversationId);

		values.put("iUnReadCount", unread_count);

		values.put("vUserSelfUrl", vUserSelfUrl);
		values.put("vUserId", vUserId);
		values.put("vDisplayName", vDisplayName);
		values.put("vAvatarUrl", vAvatarUrl);

		values.put("vDirectMessageHrefUrl", vDirectMessageHrefUrl);
		values.put("vUserHrefUrl", vUserHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vDirectMessageId FROM direct_conversation WHERE vDirectMessageId =  \""
								+ vDirectMessageId + "\" ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "direct_conversation",
					values,
					" vDirectMessageId =  \"" + vDirectMessageId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "direct_conversation",
					values, null);
		}

	}

	/*
	 * public void setDirectMessages ( String vDMessageUrl, String vDMessageId,
	 * String vAdditionUrl, String vMarkerUrl, int total_count, int version,
	 * String vDirectMessageId ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put( "vDMessageUrl",
	 * vDMessageUrl ); values .put( "vDMessageId", vDMessageId ); values .put(
	 * "vAdditionUrl", vAdditionUrl );
	 * 
	 * values .put( "vMarkerUrl" , vMarkerUrl );
	 * 
	 * values .put( "iTotalCount", total_count ); values.put( "iVersion",
	 * version ); values.put( "vDirectMessageId", vDirectMessageId );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDMessageId FROM direct_messages WHERE vDMessageId =  \"" +
	 * vDMessageId + "\" " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "direct_messages", values,
	 * " vDMessageId =  \"" + vDMessageId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "direct_messages", values, null ); }
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public void setDirectMessages(String vDMessageUrl, String vDMessageHrefUrl,
			String vDMessageId, String vAdditionUrl, String vAdditionHrefUrl,
			String vMarkerUrl, String vMarkerHrefUrl, int total_count,
			int version, String vDirectMessageId) {

		ContentValues values = new ContentValues();
		values.put("vDMessageUrl", vDMessageUrl);
		values.put("vDMessageId", vDMessageId);
		values.put("vAdditionUrl", vAdditionUrl);

		values.put("vMarkerUrl", vMarkerUrl);

		values.put("iTotalCount", total_count);
		values.put("iVersion", version);
		values.put("vDirectMessageId", vDirectMessageId);

		values.put("vDMessageHrefUrl", vDMessageHrefUrl);
		values.put("vAdditionHrefUrl", vAdditionHrefUrl);
		values.put("vMarkerHrefUrl", vMarkerHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vDMessageId FROM direct_messages WHERE vDMessageId =  \""
						+ vDMessageId + "\" ");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "direct_messages", values,
					" vDMessageId =  \"" + vDMessageId + "\"  ");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "direct_messages", values,
					null);
		}

	}

	/*
	 * public void setDirectMessageItem ( String vDMessageId, String
	 * vDMessageItemId, String vDMessageItemUrl, String vMessage, String
	 * dCreatedDate, String vUserSelfUrl, String vUserId, String vDisplayName,
	 * String vAvatarUrl ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put( "vDMessageId",
	 * vDMessageId ); values .put( "vDMessageItemId", vDMessageItemId ); values
	 * .put( "vDMessageItemUrl", vDMessageItemUrl );
	 * 
	 * values .put( "vMessage" , vMessage );
	 * 
	 * values .put( "dCreatedDate", dCreatedDate ); values.put( "vUserSelfUrl",
	 * vUserSelfUrl ); values.put( "vUserId", vUserId ); values.put(
	 * "vDisplayName", vDisplayName ); values.put( "vAvatarUrl", vAvatarUrl );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vDMessageItemId FROM direct_message_items WHERE vDMessageItemId =  \""
	 * + vDMessageItemId + "\" " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "direct_message_items", values,
	 * " vDMessageItemId =  \"" + vDMessageItemId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "direct_message_items", values, null ); }
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public void setDirectMessageItem(String vDMessageId,
			String vDMessageItemId, String vDMessageItemUrl,
			String vDMessageItemHrefUrl, String vMessage, String dCreatedDate,
			String vUserSelfUrl, String vUserHrefUrl, String vUserId,
			String vDisplayName, String vAvatarUrl) {

		ContentValues values = new ContentValues();
		values.put("vDMessageId", vDMessageId);
		values.put("vDMessageItemId", vDMessageItemId);
		values.put("vDMessageItemUrl", vDMessageItemUrl);

		values.put("vMessage", vMessage);

		values.put("dCreatedDate", dCreatedDate);
		values.put("vUserSelfUrl", vUserSelfUrl);
		values.put("vUserId", vUserId);
		values.put("vDisplayName", vDisplayName);
		values.put("vAvatarUrl", vAvatarUrl);

		values.put("vDMessageItemHrefUrl", vDMessageItemHrefUrl);
		values.put("vUserHrefUrl", vUserHrefUrl);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT  vDMessageItemId FROM direct_message_items WHERE vDMessageItemId =  \""
								+ vDMessageItemId + "\" ");

		if (count > 0) {
			PlayupLiveApplication
					.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_UPDATE, null,
							"direct_message_items", values,
							" vDMessageItemId =  \"" + vDMessageItemId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "direct_message_items",
					values, null);
		}

	}

	/*
	 * public void setDirectMessageGapItem ( String vGapId, int iGapSize, String
	 * vGapUrl, String vDMessageId ) {
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values .put( "vGapId",
	 * vGapId ); values .put( "iGapSize", iGapSize ); values .put( "vGapUrl",
	 * vGapUrl );
	 * 
	 * values .put( "vDMessageId" , vDMessageId );
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT  vGapId FROM direct_message_items WHERE vGapId =  \"" + vGapId +
	 * "\" " );
	 * 
	 * if ( count > 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "direct_message_items", values,
	 * " vGapId =  \"" + vGapId + "\"  " ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "direct_message_items", values, null ); }
	 * 
	 * }
	 */
	/**
	 * Praveen : as per the href
	 */
	public void setDirectMessageGapItem(String vGapId, int iGapSize,
			String vGapUrl, String vGapHrefUrl, String vDMessageId) {

		ContentValues values = new ContentValues();
		values.put("vGapId", vGapId);
		values.put("iGapSize", iGapSize);
		values.put("vGapUrl", vGapUrl);

		values.put("vDMessageId", vDMessageId);

		values.put("vGapHrefUrl", vGapHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT  vGapId FROM direct_message_items WHERE vGapId =  \""
						+ vGapId + "\" ");

		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "direct_message_items",
					values, " vGapId =  \"" + vGapId + "\"  ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "direct_message_items",
					values, null);
		}

	}

	public String getConversationId(String vPushId) {
		String conversationId = "";
		String sqlQuery = "SELECT vConversationId FROM push_notifications"
				+ " WHERE vPushId = \"" + vPushId + "\"";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				sqlQuery);

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				conversationId = c.getString(c
						.getColumnIndex("vConversationId"));
			}
			c.close();
		}
		c = null;
		return conversationId;
	}

	public String getConversationIdFromConversationUrl(String vConversationUrl) {
		String conversationId = "";
		String sqlQuery = "SELECT vConversationId FROM match_conversation_node "
				+ " WHERE (LENGTH(vSelfUrl) > 0 AND vSelfUrl = \""
				+ vConversationUrl
				+ "\" ) OR (LENGTH(vHref) > 0 AND vHref = \""
				+ vConversationUrl + "\" )";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				sqlQuery);

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				conversationId = c.getString(c
						.getColumnIndex("vConversationId"));
			}
			c.close();
		}
		c = null;
		return conversationId;
	}

	public Hashtable<String, List<String>> getDirectConversationData(
			String vPushId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vDirectConversationUrl, "
						+ "vUserSelfUrl,vShortUrl FROM push_notifications WHERE vPushId = \""
						+ vPushId + "\"");

	}

	public void setConversationId(String conversationUrl, String vPushId) {
		String vConversationId = "", vUserSelfUrl = "";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vConversationId FROM match_conversation_node "
						+ " WHERE ((LENGTH(vSelfUrl)  > 0 AND vSelfUrl = '"
						+ conversationUrl + "') OR ("
						+ "LENGTH(vHref)  > 0 AND vHref = '" + conversationUrl
						+ "'))");

		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();
				vConversationId = c.getString(c
						.getColumnIndex("vConversationId"));
				updatePushNotification(vConversationId, vPushId);
				setPushType(vPushId, 1);

			} else {
				c.close();
				c = PlayupLiveApplication
						.getDatabaseWrapper()
						.selectQuery(
								"SELECT dc.vUserSelfUrl AS vUserSelfUrl ,dc.vUserHrefUrl AS vUserHrefUrl FROM direct_conversation dc LEFT JOIN user_direct_conversation udc ON udc.vDirectMessageId = dc.vDirectMessageId WHERE "
										+ " ((LENGTH(udc.vDirectConversationUrl) > 0 AND  udc.vDirectConversationUrl= '"
										+ conversationUrl
										+ "') OR "
										+ "(LENGTH(udc.vDirectConversationHrefUrl) > 0 AND  udc.vDirectConversationHrefUrl = '"
										+ conversationUrl + "'))");

				if (c.getCount() > 0) {
					c.moveToFirst();
					vUserSelfUrl = c
							.getString(c.getColumnIndex("vUserHrefUrl"));
					if (vUserSelfUrl != null
							&& vUserSelfUrl.trim().length() > 0) {
						updateDirectConversationPushNotification(
								conversationUrl, vUserSelfUrl, vPushId);
					} else {
						vUserSelfUrl = c.getString(c
								.getColumnIndex("vUserSelfUrl"));
						updateDirectConversationPushNotification(
								conversationUrl, vUserSelfUrl, vPushId);
					}

					setPushType(vPushId, 0);

				} else {
					c.close();
					c = PlayupLiveApplication
							.getDatabaseWrapper()
							.selectQuery(
									"SELECT vConversationId FROM friendConversation WHERE "
											+ " ((LENGTH(vConversationUrl)  > 0 AND vConversationUrl = '"
											+ conversationUrl
											+ "') OR (LENGTH(vConversationHrefUrl)  > 0 AND vConversationHrefUrl = '"
											+ conversationUrl + "'))");

					if (c.getCount() > 0) {
						c.moveToFirst();
						vConversationId = c.getString(c
								.getColumnIndex("vConversationId"));
						updatePushNotification(vConversationId, vPushId);
						setPushType(vPushId, 3);

					}

				}

			}
		}
		c = null;
	}

	public void setFriendInvitation(String vPushId, String vSender,
			boolean isFriends) {

		ContentValues values = new ContentValues();
		values.put("vPushId", vPushId);
		values.put("vShortUrl", "");
		values.put("isFriends", isFriends);
		values.put("vSender", vSender);

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_UPDATE, null, "push_notifications",
				values, " vPushId = \"" + vPushId + "\" ", "vPushId", true,
				true);

	}

	public void setPushNotification(String vPushId, String vShortUrl,
			String vSender, int pushType, int notificationId, int isRead) {

		ContentValues values = new ContentValues();

		values.put("vPushId", vPushId);
		values.put("vShortUrl", vShortUrl);
		values.put("iPushType", pushType);
		values.put("vSender", vSender);
		values.put("iNotificationId", notificationId);
		values.put("isRead", isRead);

		Log.e("234", "vPushId=======================>>>" + vPushId);
		Log.e("234", "vShortUrl=====================>>>" + vShortUrl);
		Log.e("234", "pushType======================>>>" + pushType);
		Log.e("234", "vSender=======================>>>" + vSender);
		Log.e("234", "notificationId================>>>" + notificationId);
		Log.e("234", "isRead========================>>>" + isRead);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vPushId FROM push_notifications WHERE  vPushId = \""
						+ vPushId + "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "push_notifications", values,
					" vPushId = \"" + vPushId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "push_notifications", values,
					null);
		}

	}

	public void setIsRead(String vShortUrl) {
		ContentValues values = new ContentValues();
		values.put("isRead", 1);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "push_notifications", values,
				" vShortUrl = \"" + vShortUrl + "\" ");

	}

	public void setPushType(String vPushId, int pushType) {

		ContentValues values = new ContentValues();

		values.put("vPushId", vPushId);

		values.put("iPushType", pushType);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vPushId FROM push_notifications WHERE  vPushId = \""
						+ vPushId + "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "push_notifications", values,
					" vPushId = \"" + vPushId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "push_notifications", values,
					null);
		}

	}

	public Hashtable<String, List<String>> getNotificationId(String vShortUrl) {

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT iNotificationId FROM push_notifications WHERE  vShortUrl = \""
						+ vShortUrl + "\" AND isREAD = 0");

	}

	public int getPushType(String vPushId) {

		int pushType = -1;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT  iPushType from push_notifications where vPushId = '"
						+ vPushId + "'");
		if (c != null) {

			if (c.getCount() > 0) {

				c.moveToFirst();

				pushType = c.getInt(c.getColumnIndex("iPushType"));

			}
			c.close();

		}

		return pushType;

	}

	public void updateDirectConversationPushNotification(
			String vDirectConversationUrl, String vUserSelfUrl, String vPushId) {

		ContentValues values = new ContentValues();

		values.put("vPushId", vPushId);
		values.put("vDirectConversationUrl", vDirectConversationUrl);
		values.put("vUserSelfUrl", vUserSelfUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vPushId FROM push_notifications WHERE  vPushId = \""
						+ vPushId + "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "push_notifications", values,
					" vPushId = \"" + vPushId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "push_notifications", values,
					null);
		}

	}

	/**
	 * Praveen : as per the href
	 * 
	 * @param vDirectConversationUrl
	 * @param vUserSelfUrl
	 * @param vUserHrefUrl
	 * @param vPushId
	 */

	public void updateDirectConversationPushNotification(
			String vDirectConversationUrl, String vUserSelfUrl,
			String vUserHrefUrl, String vPushId) {

		ContentValues values = new ContentValues();

		values.put("vPushId", vPushId);
		values.put("vDirectConversationUrl", vDirectConversationUrl);
		values.put("vUserSelfUrl", vUserSelfUrl);
		values.put("vUserHrefUrl", vUserHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vPushId FROM push_notifications WHERE  vPushId = \""
						+ vPushId + "\" ");
		if (count > 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "push_notifications", values,
					" vPushId = \"" + vPushId + "\" ");
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "push_notifications", values,
					null);
		}

	}

	public void updateUserUnreadCount() {
		int unReadCount = 0;
		String userId = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT  SUM(dc.iUnreadCount) AS iUnReadCount,u.iUserId FROM direct_conversation dc LEFT JOIN "
								+ "user_direct_conversation udc ON dc.vDirectConversationId = udc.vDirectConversationId LEFT JOIN user u ON udc.vUserId = u.iUserId "
								+ "WHERE  u.isPrimaryUser = 1");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				unReadCount = c.getInt(c.getColumnIndex("iUnReadCount"));
				userId = c.getString(c.getColumnIndex("iUserId"));
			}
			c.close();
		}
		c = null;

		ContentValues values = new ContentValues();
		values.put("iUnReadCount", unReadCount);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user_direct_conversation",
				values, " vUserId = \"" + userId + "\" ");

	}

	public void updateConversationUnreadCount(String vDirectConversationUrl) {

		String vDirectMessageId = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT dc.vDirectMessageId from direct_conversation dc Left JOIN "
								+ " user_direct_conversation udc ON dc.vDirectMessageId = udc.vDirectMessageId WHERE ( udc.vDirectConversationUrl = '"
								+ vDirectConversationUrl
								+ "') OR ( udc.vDirectConversationHrefUrl = '"
								+ vDirectConversationUrl + "')");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();

				vDirectMessageId = c.getString(c
						.getColumnIndex("vDirectMessageId"));
			}
			c.close();
		}

		ContentValues values = new ContentValues();
		values.put("iUnReadCount", 0);

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "direct_conversation", values,
				" vDirectMessageId = \"" + vDirectMessageId + "\" ");

	}

	public void updateConversationUnreadCount2(String vUserSelfUrl) {
		ContentValues values = new ContentValues();
		values.put("iUnReadCount", 0);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE,
				null,
				"direct_conversation",
				values,
				"( vUserSelfUrl = \"" + vUserSelfUrl
						+ "\" ) OR ( vUserHrefUrl = \"" + vUserSelfUrl
						+ "\" ) ");

	}

	public Hashtable<String, Object> getUpdateFriendsUrl() {
		String updateFriendsUrl = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT  vUpdateFriendsUrl,vUpdateFriendsHrefUrl from user where isPrimaryUser = 1");
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();

				String url = c.getString(c
						.getColumnIndex("vUpdateFriendsHrefUrl"));
				if (url != null && url.trim().length() > 0) {
					result.put("url", url);
					result.put("isHref", true);

				} else {

					url = c.getString(c.getColumnIndex("vUpdateFriendsUrl"));
					result.put("url", url);
					result.put("isHref", false);

				}
			}
			c.close();
		}
		c = null;
		return result;

	}

	public void emptyLiveFriends() {

		PlayupLiveApplication.getDatabaseWrapper().directDelete(
				"my_friends_live", null, null, -1);
	}

	public void updatePlayupFriends() {
		ContentValues values = new ContentValues();
		values.put("isOnline", 0);

		PlayupLiveApplication.getDatabaseWrapper().directUpdate(
				"playup_friends", values, null, null, -1);
	}

	public void removeSearchFriendGapEntry(String gapUid) {
		try {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "gap_info", null,
					"gap_uid = \"" + gapUid + "\" ");

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "search_friends", null,
					"vGapId = \"" + gapUid + "\" ");

		} catch (Exception e) {
			// TODO: handle exception

		}

	}

	public void removePlayupSearchFriendGapEntry(String gapUid) {
		try {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "gap_info", null,
					"gap_uid = \"" + gapUid + "\" ");

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_DELETE, null, "search_playup_friends",
					null, "vGapId = \"" + gapUid + "\" ");

		} catch (Exception e) {
			// TODO: handle exception

		}

	}

	public void updateFriends(String friendUid, String friendName,
			String friendAvatar, String sourceName, String sourceIconHref,
			boolean isOnline, String profileUid, String userName) {

		ContentValues values = new ContentValues();
		values.put("vFriendId", friendUid);
		values.put("vFriendName", friendName);
		values.put("vFriendAvatar", friendAvatar);
		values.put("vSourceName", sourceName);
		values.put("vSourceIconHref", sourceIconHref);

		values.put("isOnline", isOnline);
		values.put("vProfileId", profileUid);
		values.put("vFriendUserName", userName);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vFriendId FROM my_friends WHERE vFriendId = \""
						+ friendUid + "\"  ");

		if (count > 0) {
			Constants.friendsUpdated = true;
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "my_friends", values,
					" vFriendId = \"" + friendUid + "\" ");
		}

	}

	public void removeEtag(String searchUrl) {

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_DELETE, null, "eTag", null, "vUrl='"
				+ searchUrl + "'", null, false, false);

	}

	public void removeLobbySubject() {

		ContentValues values = new ContentValues();
		values.putNull("vLobbySubjectId");
		values.putNull("vLobbySubjectUrl");
		values.putNull("vLobbySubjectHrefUrl");
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "user", values,
				" isPrimaryUser = '1' ");

	}

	public void removePushNotification(String vPushId) {

		JsonUtil json = new JsonUtil();
		json.queryMethod1(Constants.QUERY_DELETE, null, "push_notifications",
				null, "vPushId='" + vPushId + "'", null, false, false);

	}

	public Hashtable<String, Object> getContestShareUrl(String vContestId) {

		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vShareUrl = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vShareUrl,vShareHref FROM contests WHERE 	vContestId = \""
						+ vContestId + "\" ");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vShareUrl = c.getString(c.getColumnIndex("vShareHref"));
				if (vShareUrl != null && vShareUrl.trim().length() > 0) {
					result.put("url", vShareUrl);
					result.put("isHref", true);
				} else {

					vShareUrl = c.getString(c.getColumnIndex("vShareUrl"));

					result.put("url", vShareUrl);
					result.put("isHref", false);

				}
			}

			c.close();
		}
		c = null;
		return result;
	}

	/**
	 * inserting base section data
	 * 
	 * @param vBaseSectionId
	 * @param vBaseSectionTargetId
	 * @param vBaseSectionTitle
	 * @param vBaseSectionImageUrl
	 * @param vBaseSectionTargetUrl
	 */
	/*
	 * public void setBaseSectionData(String vBaseSectionId, String
	 * vBaseSectionTargetId, String vBaseSectionTitle,String
	 * vBaseSectionImageUrl,String vBaseSectionTargetUrl, int iOrderId,final
	 * String vBaseSectionTargetHref) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues (); values.put(
	 * "vBaseSectionId", vBaseSectionId ); values.put( "vBaseSectionTargetId",
	 * vBaseSectionTargetId ); values.put( "vBaseSectionTitle",
	 * vBaseSectionTitle ); values.put( "vBaseSectionImageUrl",
	 * vBaseSectionImageUrl ); values.put( "vBaseSectionTargetUrl",
	 * vBaseSectionTargetUrl ); values.put( "iOrderId", iOrderId ); values.put(
	 * "vBaseSectionTargetHref", vBaseSectionTargetHref );
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vBaseSectionId FROM baseSection WHERE vBaseSectionId = \"" +
	 * vBaseSectionId + "\"" );
	 * 
	 * if ( count > 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "baseSection", values,"vBaseSectionId = \""
	 * + vBaseSectionId + "\"" ); }else{
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "baseSection", values, null ); }
	 * 
	 * 
	 * }
	 */

	/**
	 * Praveen :as per the href
	 */
	/**
	 * inserting base section data
	 * 
	 * @param vBaseSectionId
	 * @param vBaseSectionTargetId
	 * @param vBaseSectionTitle
	 * @param vBaseSectionImageUrl
	 * @param vBaseSectionTargetUrl
	 */
	public void setBaseSectionData(String vBaseSectionId,
			String vBaseSectionTargetId, String vBaseSectionTitle,
			String vBaseSectionImageUrl, String vBaseSectionTargetUrl,
			String vBaseSectionTargetHrefUrl, int iOrderId) {

		ContentValues values = new ContentValues();
		values.put("vBaseSectionId", vBaseSectionId);
		values.put("vBaseSectionTargetId", vBaseSectionTargetId);
		values.put("vBaseSectionTitle", vBaseSectionTitle);
		values.put("vBaseSectionImageUrl", vBaseSectionImageUrl);
		values.put("vBaseSectionTargetUrl", vBaseSectionTargetUrl);

		values.put("iOrderId", iOrderId);

		values.put("vBaseSectionTargetHrefUrl", vBaseSectionTargetHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vBaseSectionId FROM baseSection WHERE vBaseSectionId = \""
						+ vBaseSectionId + "\"");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "baseSection", values,
					"vBaseSectionId = \"" + vBaseSectionId + "\"");
		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "baseSection", values, null);
		}

	}

	public void setContetData(String vBlockContentId, String vContentId,
			ContentValues values) {

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vContentId FROM blockContent WHERE vContentId = \""
						+ vContentId + "\"  AND vBlockContentId = '"
						+ vBlockContentId + "'");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blockContent",
					values,
					" vContentId = \"" + vContentId
							+ "\"  AND vBlockContentId = '" + vBlockContentId
							+ "'");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blockContent", values, null);

		}

	}

	/**
	 * Praveen: Modified as per the Href element vHrefUrl
	 * 
	 * @param vBlockContentId
	 * @param vContentId
	 * @param vContentType
	 * @param vContentUrl
	 * @param vName
	 * @param vDisplayUrl
	 * @param vDisplayType
	 * @param vLinkUrl
	 * @param vLinkType
	 * @param vImage
	 * @param vFooterTitle
	 * @param vFooterSubtitle
	 * @param iLive
	 * @param vBackgroundColor
	 * @param vBackgroundImage
	 * @param vSummary
	 * @param vTitle
	 * @param vSource
	 * @param vSourceIcon
	 * @param vSocialIcon
	 * @param vTimeStamp
	 * @param vHighlightUrl
	 * @param vHighlightType
	 * @param vHighLightId
	 * @param iOrderId
	 * @param vAccessory
	 * @param vSubtitle
	 * @param vDisplayCount
	 * @param vRadioBackground
	 * @param vContentTitle
	 * @param vContentSubTitle
	 * @param vContentIcon
	 * @param vHrefUrl
	 */
	public void setContetData(String vBlockContentId, String vContentId,
			String vContentType, String vContentUrl, String vContentHrefUrl,
			String vName, String vDisplayUrl, String vDisplayHref,
			String vDisplayType, String vLinkUrl, String vLinkType,
			String vImage, String vFooterTitle, String vFooterSubtitle,
			int iLive, String vBackgroundColor, String vBackgroundImage,
			String vSummary, String vTitle, String vSource, String vSourceIcon,
			String vSocialIcon, String vTimeStamp, String vHighlightUrl,
			String vHighlightHrefUrl, String vHighlightType,
			String vHighLightId, int iOrderId, String vAccessory,
			String vSubtitle, String vLinkHrefUrl, String vContentIcon,
			String vContentSubTitle, String vContentTitle,
			String vRadioBackground, int vDisplayCount) {

		ContentValues values = new ContentValues();
		values.put("vBlockContentId", vBlockContentId);
		values.put("vContentId", vContentId);
		values.put("vContentType", vContentType);
		values.put("vContentUrl", vContentUrl);
		values.put("vName", vName);

		values.put("vDisplayUrl", vDisplayUrl);
		values.put("vDisplayType", vDisplayType);

		if (vLinkUrl != null && vLinkUrl.trim().length() > 0)
			values.put("vLinkUrl", vLinkUrl);

		if (vLinkType != null && vLinkType.trim().length() > 0)
			values.put("vLinkType", vLinkType);
		values.put("vImageUrl", vImage);
		values.put("vFooterTitle", vFooterTitle);
		values.put("vFooterSubtitle", vFooterSubtitle);
		values.put("iLive", iLive);

		values.put("vBackgroundColor", vBackgroundColor);
		values.put("vBackgroundImage", vBackgroundImage);
		values.put("vSummary", vSummary);

		values.put("vTitle", vTitle);
		values.put("vSource", vSource);
		values.put("vSourceIcon", vSourceIcon);
		values.put("vSocialIcon", vSocialIcon);
		values.put("vTimeStamp", vTimeStamp);

		values.put("vHighlightUrl", vHighlightUrl);
		values.put("vHighlightType", vHighlightType);
		values.put("vHighlightId", vHighLightId);
		if (iOrderId != -1)
			values.put("iOrderId", iOrderId);

		values.put("vAccessory", vAccessory);
		values.put("vSubtitle", vSubtitle);

		values.put("vContentHrefUrl", vContentHrefUrl);
		values.put("vHighlightHrefUrl", vHighlightHrefUrl);
		values.put("vDisplayHrefUrl", vDisplayHref);

		// added line
		values.put("vLinkHrefUrl", vLinkHrefUrl);

		values.put("vRadioBackground", vRadioBackground);
		values.put("vContentIcon", vContentIcon);
		values.put("vContentSubTitle", vContentSubTitle);
		values.put("vContentTitle", vContentTitle);
		values.put("vDisplayCount", vDisplayCount);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vContentId FROM blockContent WHERE vContentId = \""
						+ vContentId + "\"  AND vBlockContentId = '"
						+ vBlockContentId + "'");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"blockContent",
					values,
					" vContentId = \"" + vContentId
							+ "\"  AND vBlockContentId = '" + vBlockContentId
							+ "'");

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blockContent", values, null);

		}

	}

	public String getSectionId(String vCompetitionId) {

		String vSectionId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSectionId FROM competition WHERE vCompetitionId = '"
						+ vCompetitionId + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionId = c.getString(c.getColumnIndex("vSectionId"));
			} else {
				c.close();
				c = PlayupLiveApplication
						.getDatabaseWrapper()
						.selectQuery(
								" SELECT vBaseSectionTargetId  FROM baseSection  WHERE vBaseSectionTargetId = '"
										+ vCompetitionId + "'");
				if (c != null) {

					if (c.getCount() > 0) {
						c.moveToFirst();
						vSectionId = c.getString(c
								.getColumnIndex("vBaseSectionTargetId"));
					}

					c.close();
				}

			}

			c.close();
		}
		c = null;
		return vSectionId;

	}

	public String getSectionUrl(String vSectionId) {

		String vSectionUrl = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSectionUrl FROM sections WHERE vSectionId = '"
						+ vSectionId + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionUrl = c.getString(c.getColumnIndex("vSectionUrl"));
			}

			c.close();
		}
		c = null;
		return vSectionUrl;

	}

	/*
	 * public String getSectionUrlForLeagueLobby(String vCompetitionId) {
	 * 
	 * String vSectionUrl = null;
	 * 
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * " SELECT vSectionUrl,vCompetitionHref FROM competition c WHERE c.vCompetitionId = '"
	 * +vCompetitionId+"'" ) ; if ( c != null ) {
	 * 
	 * if ( c.getCount() > 0 ) { c.moveToFirst(); vSectionUrl = c.getString(
	 * c.getColumnIndex( "vCompetitionHref" ) );
	 * 
	 * if(vSectionUrl != null && vSectionUrl.trim().length() > 0){
	 * 
	 * return vSectionUrl;
	 * 
	 * }else{
	 * 
	 * vSectionUrl = c.getString( c.getColumnIndex( "vSectionUrl" ) ); } }
	 * 
	 * c.close(); } c = null; return vSectionUrl;
	 * 
	 * 
	 * }
	 */
	public Hashtable<String, Object> getSectionUrlForLeagueLobby(
			String vCompetitionId) {

		String vSectionUrl = null;
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSectionUrl,vSectionHref FROM competition c WHERE c.vCompetitionId = '"
								+ vCompetitionId + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionUrl = c.getString(c.getColumnIndex("vSectionHref"));

				if (vSectionUrl != null && vSectionUrl.trim().length() > 0) {
					result.put("url", vSectionUrl);
					result.put("isHref", true);

				} else {

					vSectionUrl = c.getString(c.getColumnIndex("vSectionUrl"));
					result.put("url", vSectionUrl);
					result.put("isHref", false);

				}
			}

			c.close();
		}
		c = null;
		return result;

	}

	public int getFeaturesCount(String vSectionId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" "
								+ "SELECT count(*) AS totalCount FROM blockContent bc LEFT JOIN blocks b ON "
								+ "bc.vBlockContentId = b.vBlockFeatureId LEFT JOIN "
								+ "sections s ON b.vBlockId = s.vBlockId WHERE s.vSectionId = '"
								+ vSectionId + "'"
								+ " AND (bc.vContentType = '"
								+ Types.CONTENT_DATA_TYPE
								+ "'  ) AND LENGTH(b.vBlockFeatureId) > 0");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				int iBlockFeaturesCount = c.getInt(c
						.getColumnIndex("totalCount"));
				c.close();
				return iBlockFeaturesCount;
			}

			c.close();
		}
		c = null;

		return 0;
	}

	public int getFeaturesCountForLeagueLobby(String vCompetitionId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" "
								+ "SELECT count(*) AS totalCount FROM blockContent bc LEFT JOIN blocks b ON "
								+ "bc.vBlockContentId = b.vBlockFeatureId LEFT JOIN "
								+ "sections s ON b.vBlockId = s.vBlockId LEFT JOIN "
								+ "competition c ON c.vSectionId = s.VsectionId WHERE c.vCompetitionId = '"
								+ vCompetitionId + "'"
								+ " AND (bc.vContentType = '"
								+ Types.CONTENT_DATA_TYPE
								+ "'  ) AND LENGTH(b.vBlockFeatureId) > 0");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				int iBlockFeaturesCount = c.getInt(c
						.getColumnIndex("totalCount"));
				c.close();
				return iBlockFeaturesCount;
			}

			c.close();
		}
		c = null;

		return 0;
	}

	public int getWelcomeFeatureCount(String vSectionUrl) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT iBlockFeaturesCount FROM blocks b, "
								+ "sections s WHERE b.vBlockId = s.vBlockId AND s.vSectionId = '"
								+ vSectionUrl
								+ "' AND LENGTH(b.vBlockFeatureId) > 0");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				int iBlockFeaturesCount = c.getInt(c
						.getColumnIndex("iBlockFeaturesCount"));

				return iBlockFeaturesCount;
			}

			c.close();
		}
		c = null;

		return 0;
	}

	public Hashtable<String, List<String>> getFeaturesData(String vSectionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockFeatureId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId where s.VsectionId = '"
						+ vSectionId
						+ "' "
						+ " AND (vContentType = '"
						+ Types.CONTENT_DATA_TYPE
						+ "'  ) AND LENGTH(b.vBlockFeatureId) > 0 "
						+ "ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getFeaturesDataForLeagueLobby(
			String vCompetitionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockFeatureId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId LEFT JOIN competition c ON s.VsectionId = c.vSectionId WHERE c.vCompetitionId = '"
						+ vCompetitionId
						+ "' "
						+ " AND (vContentType = '"
						+ Types.CONTENT_DATA_TYPE
						+ "'  ) AND LENGTH(b.vBlockFeatureId) > 0 "
						+ "ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getStackData(String vSectionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType,vAccessory,vSubtitle FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockStackId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId where s.VsectionId = '"
						+ vSectionId
						+ "' AND LENGTH(b.vBlockStackId) > 0 ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getStackDataForLeagueLobby(
			String vCompetitionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType,vAccessory,vSubtitle FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockStackId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId LEFT JOIN competition c ON s.VsectionId = c.vSectionId WHERE "
						+ "c.vCompetitionId = '"
						+ vCompetitionId
						+ "' AND LENGTH(b.vBlockStackId) > 0 ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getTilesData(String vSectionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockTileId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId where s.VsectionId = '"
						+ vSectionId
						+ "' "
						+ " AND (vContentType = '"
						+ Types.CONTENT_DATA_TYPE
						+ "' OR  vContentType = '"
						+ Types.SECTION_DATA_TYPE
						+ "' "
						+ "OR vContentType = '"
						+ Types.COMPETITION_DATA_TYPE
						+ "' OR vContentType = '"
						+ Types.AUDIO_LIST_TYPE
						+ "' OR vContentType = '"
						+ Types.STATIONS_TYPE
						+ "' ) AND LENGTH(b.vBlockTileId) > 0 "
						+ "ORDER BY iOrderId ASC ");

	}

	public Hashtable<String, List<String>> getTilesDataForLeagueLobby(
			String vCompetitionId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,iLive,vBackgroundImage,vSource,vContentUrl,vLinkType FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockTileId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId LEFT JOIN competition c ON s.VsectionId = c.vSectionId WHERE c.vCompetitionId = '"
						+ vCompetitionId
						+ "' "
						+ " AND (vContentType = '"
						+ Types.CONTENT_DATA_TYPE
						+ "' OR  vContentType = '"
						+ Types.SECTION_DATA_TYPE
						+ "' OR vContentType = '"
						+ Types.COMPETITION_DATA_TYPE
						+ "' OR vContentType = '"
						+ Types.AUDIO_LIST_TYPE
						+ "' OR vContentType = '"
						+ Types.STATIONS_TYPE
						+ "' ) AND LENGTH(b.vBlockTileId) > 0 "
						+ "ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getLiveContestsOnWelocmeScreenData(
			String vSectionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vHighlightId FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockItemId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId where b.vBlockItemType = 'feature' AND s.VsectionId = '"
						+ vSectionId
						+ "'  AND  bc.vDisplayType ='application/vnd.playup.display.feature.hilight+json'"
						+ "AND vHighlightId =(Select vContestId FROM contests WHERE vContestId = vHighlightId AND dEndTime IS NULL AND dStartTime IS NOT NULL) ");
	}

	public Hashtable<String, List<String>> getLiveContestsOnLeagueLobby(
			String vCompetitionId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vHighlightId FROM blockContent bc "
						+ "LEFT JOIN blocks b ON b.vBlockFeatureId = bc.vBlockContentId LEFT JOIN sections s ON "
						+ "s.vBlockId = b.vBlockId LEFT JOIN competition c ON s.VsectionId = c.vSectionId WHERE c.vCompetitionId = '"
						+ vCompetitionId
						+ "'  AND  bc.vDisplayType ='application/vnd.playup.display.feature.hilight+json'"
						+ "AND vHighlightId =(SELECT vContestId FROM contests WHERE vContestId = vHighlightId AND dEndTime IS NULL AND "
						+ "dStartTime IS NOT NULL) AND LENGTH(b.vBlockFeatureId) > 0");

	}

	/**
	 * for getting base section data
	 * 
	 * @return
	 */
	public Hashtable<String, List<String>> getBaseSectionData() {
		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT * from baseSection ORDER BY iOrderId ASC");
	}

	public String getStartingScreenUrl() {
		String url = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vBaseSectionTargetUrl,vBaseSectionTargetHrefUrl,vBaseSectionTargetId  from baseSection ORDER BY iOrderId ASC LIMIT 1");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				url = c.getString(c.getColumnIndex("vBaseSectionTargetUrl"));
			}
			c.close();

		}

		return url;

	}

	public Hashtable<String, List<String>> getStartingScreenData() {
		try {
			return PlayupLiveApplication
					.getDatabaseWrapper()
					.select("SELECT resource_url,resource_id,resource_href FROM root_resource where resource_name = 'default_section' ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public String getDefaultSectionFromRootResource() {

		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_id FROM root_resource where resource_name = 'default_section' ");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				String url = c.getString(c.getColumnIndex("resource_id"));
				c.close();
				return url;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}
	}

	public void setCurrentSeasonData(String vCompetitionId,
			String vCurrentSeasonId, String vCurrentSeasonUrl,
			String vCurrentSeasonHref) {
		ContentValues values = new ContentValues();
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCurrentSeasonId", vCurrentSeasonId);
		values.put("vCurrentSeasonUrl", vCurrentSeasonUrl);

		values.put("vCurrentSeasonHref", vCurrentSeasonHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCurrentSeasonId FROM currentSeasonRounds WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "currentSeasonRounds",
					values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "currentSeasonRounds",
					values, " vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public String getCompetitionId(String vCurrentSeasonUrl) {

		String vCompetitionId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vCompetitionId FROM currentSeasonRounds WHERE "
								+ " (LENGTH(vCurrentSeasonUrl) > 0 AND vCurrentSeasonUrl = \""
								+ vCurrentSeasonUrl
								+ "\" ) OR  "
								+ "(LENGTH(vCurrentSeasonHref) > 0 AND vCurrentSeasonHref = \""
								+ vCurrentSeasonUrl + "\" )");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vCompetitionId = c
						.getString(c.getColumnIndex("vCompetitionId"));

			}

			c.close();
		}
		c = null;

		return vCompetitionId;
	}

	public String getSectionIdFromSectionUrl(String vSectionUrl) {
		String vSectionId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSectionId FROM sections WHERE vSectionUrl = '"
						+ vSectionUrl + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionId = c.getString(c.getColumnIndex("vSectionId"));
			}

			c.close();
		}
		c = null;
		return vSectionId;

	}

	public String getSectionIdFromLinkUrl(String vSectionUrl) {
		String vSectionId = null;

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT vSectionId FROM blockContentLinks WHERE ( LENGTH(vLinkUrl) > 0 AND vLinkUrl = '"
								+ vSectionUrl
								+ "') "
								+ " OR (LENGTH(vHref) > 0 AND vHref = '"
								+ vSectionUrl + "')");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionId = c.getString(c.getColumnIndex("vSectionId"));
			}

			c.close();
		}
		c = null;
		return vSectionId;

	}

	public String getSectionIdFromSectionUrlForWelcome(String vSectionUrl) {
		String vSectionId = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSectionId FROM sections WHERE vSectionUrl = '"
						+ vSectionUrl + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				if (c.getCount() == 2) {
					c.moveToNext();
					vSectionId = c.getString(c.getColumnIndex("vSectionId"));
				} else {
					vSectionId = c.getString(c.getColumnIndex("vSectionId"));
				}
			}

			c.close();
		}
		c = null;
		return vSectionId;

	}

	/*
	 * public Hashtable<String, List<String>> getTilesDataFromContestId( String
	 * vContestId) {
	 * 
	 * 
	 * return PlayupLiveApplication.getDatabaseWrapper().select(
	 * "SELECT vContentId,vContentType,vDisplayUrl,vDisplayType," +
	 * "vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon," +
	 * "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor," +
	 * "bc.vTitle,vBackgroundImage,vSource,vContentUrl,vLinkType,iLive " +
	 * "FROM blockContent bc LEFT JOIN contest_lobby cl on bc.vBlockContentId = cl.vBlockTileId WHERE "
	 * +
	 * "cl.vContestId = '"+vContestId+"' AND (vDisplayType = '"+Types.TILE_HEADLINE
	 * +"' OR vDisplayType = '"+Types.TILE_PHOTO+"' OR " +
	 * "vDisplayType = '"+Types
	 * .TILE_SOLID+"' OR vDisplayType = '"+Types.TILE_TIMESTAMP+"' OR " +
	 * "vDisplayType = '"
	 * +Types.TILE_VIDEO+"') AND (vContentType = '"+Types.CONTENT_DATA_TYPE+
	 * "' OR  vContentType = '"+Types.SECTION_DATA_TYPE+ "' " +
	 * "OR vContentType = '"+Types.COMPETITION_DATA_TYPE+
	 * "' ) ORDER BY iOrderId ASC");
	 * 
	 * }
	 */
	/**
	 * Praveen
	 */
	public Hashtable<String, List<String>> getTilesDataFromContestId(
			String vContestId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vContentId,vContentType,vDisplayUrl,vDisplayHrefUrl,vDisplayType,"
						+ "vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,vLinkHrefUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "bc.vTitle,vBackgroundImage,vSource,vContentUrl,vContentHrefUrl,vLinkType,iLive "
						+ "FROM blockContent bc LEFT JOIN contest_lobby cl on bc.vBlockContentId = cl.vBlockTileId WHERE "
						+ "cl.vContestId = '"
						+ vContestId
						+ "' AND (vDisplayType = '"
						+ Types.TILE_HEADLINE
						+ "' OR vDisplayType = '"
						+ Types.TILE_PHOTO
						+ "' OR "
						+ "vDisplayType = '"
						+ Types.TILE_SOLID
						+ "' OR vDisplayType = '"
						+ Types.TILE_TIMESTAMP
						+ "' OR "
						+ "vDisplayType = '"
						+ Types.TILE_VIDEO
						+ "' OR vDisplayType = '"
						+ Types.TILE_AUDIO_LIST
						+ "'"
						+ " OR vDisplayType = '"
						+ Types.TILE_AUDIO
						+ "')"
						+ " AND (vContentType = '"
						+ Types.CONTENT_DATA_TYPE
						+ "' OR  vContentType = '"
						+ Types.SECTION_DATA_TYPE
						+ "' "
						+ " OR vContentType = '"
						+ Types.COMPETITION_DATA_TYPE
						+ "' OR vContentType = '"
						+ Types.AUDIO_LIST_TYPE
						+ "' OR vContentType = '"
						+ Types.STATIONS_TYPE
						+ "' ) ORDER BY iOrderId ASC");

	}

	public Hashtable<String, List<String>> getDisplayUrlForContestLobby(
			String vContestId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vDisplayUrl,vDisplayHrefUrl,vBlockContentId,vContentId FROM blockContent "
						+ "bc LEFT JOIN contest_lobby cl on bc.vBlockContentId = cl.vBlockTileId WHERE cl.vContestId = '"
						+ vContestId
						+ "'  "
						+ "AND (LENGTH(vDisplayUrl) > 0 OR LENGTH(vDisplayHrefUrl) > 0)");
	}

	public String getSectionTile(String vSectionId) {

		String vSectionName = "";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECt vTitle FROM sections WHERE vSectionId = '" + vSectionId
						+ "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionName = (c.getString(c.getColumnIndex("vTitle")));
			}
			c.close();
		}
		return vSectionName;
	}

	public void setRegions(String vRegionName, int isSelected,
			String vImageUrl, String vRegionCode) {

		ContentValues values = new ContentValues();
		values.put("vRegionName", vRegionName);
		values.put("isSelected", isSelected);
		values.put("vImageUrl", vImageUrl);
		values.put("vRegionCode", vRegionCode);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRegionCode FROM regions WHERE vRegionCode = '"
						+ vRegionCode + "'");
		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "regions", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "regions", values,
					" vRegionCode = \"" + vRegionCode + "\" ");

		}

	}

	public void setSelectedRegion(String vCountryId) {

		ContentValues values = new ContentValues();
		values.put("isSelected", 0);
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE, null, "countries", values,
				" isSelected = 1");

		values.put("isSelected", 1);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCountryId FROM countries WHERE vCountryId = '"
						+ vCountryId + "'");
		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "countries", values, null);
		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "countries", values,
					" vCountryId = \"" + vCountryId + "\" ");

		}

	}

	public Hashtable<String, List<String>> getRegions() {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(""
						+ "SELECT vRegionName,vRegionCode,vRegionUrl,vRegionHrefUrl,vRegionId,iCountryCount,vRegionLogo FROM regions"
						+ " ORDER BY upper(vRegionName) ASC");

	}

	public void deleteBockContent(String vBlockContentId) {
		DatabaseWrapper.getWritableSQLiteDatabase().delete("blockContent",
				" vBlockContentId = '" + vBlockContentId + "'", null);
	}

	public void updateSectionId(String vCompetitionId, String vSectionId) {

		ContentValues values = new ContentValues();

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vSectionId FROM competition WHERE vCompetitionId = '"
						+ vCompetitionId + "'");

		if (c != null && c.getCount() > 0) {
			values.put("vSectionId", vSectionId);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = '" + vCompetitionId + "'");
			c.close();
		}
		if (c != null && !c.isClosed()) {
			c.close();
		}

		c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vBaseSectionId FROM baseSection  WHERE vBaseSectionId  = '"
						+ vCompetitionId + "'");
		if (c != null && c.getCount() > 0) {
			values = new ContentValues();
			values.put("vBaseSectionTargetId", vSectionId);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "baseSection", values,
					" vBaseSectionId = '" + vCompetitionId + "'");
			c.close();

		}
		if (c != null && !c.isClosed()) {
			c.close();
		}

		c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT resource_id FROM root_resource WHERE "
						+ "((LENGTH(resource_url) > 0 AND resource_url = '"
						+ vCompetitionId + "') OR "
						+ "(LENGTH(resource_href) > 0 AND resource_href = '"
						+ vCompetitionId + "'))");
		if (c != null && c.getCount() > 0) {
			values = new ContentValues();
			values.put("resource_id", vSectionId);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "root_resource", values,
					" resource_name = 'default_section'");
			c.close();

		}
		if (c != null && !c.isClosed()) {
			c.close();
		}

		c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vLinkUrl,vHref FROM blockContentLinks WHERE "
						+ " ((LENGTH(vLinkUrl) > 0 AND vLinkUrl = '"
						+ vCompetitionId + "') OR  "
						+ "(LENGTH(vHref) > 0 AND vHref = '" + vCompetitionId
						+ "'))");
		if (c != null) {

			values = new ContentValues();
			values.put("vSectionId", vSectionId);
			if (c.getCount() > 0) {

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "blockContentLinks",
						values, " vLinkUrl = '" + vCompetitionId + "'");
			}

			c.close();
		}

		if (c != null && !c.isClosed()) {
			c.close();
		}
	}

	public String getBaseSectionTargetId(String vSectionId) {
		String baseSectionTargetId = "";
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vBaseSectionTargetId  FROM baseSection  WHERE vBaseSectionId = '"
						+ vSectionId + "'");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				baseSectionTargetId = c.getString(c
						.getColumnIndex("vBaseSectionTargetId"));

			}

			c.close();
		}

		c = null;

		return baseSectionTargetId;

	}

	public void setTeamData(String teamId, String teamUrl,
			String vCompetitionId, String vCompetitionTeamHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionTeamId", teamId);
		values.put("vCompetitionTeamUrl", teamUrl);
		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionTeamHref", vCompetitionTeamHref);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionTeamId FROM competition_team WHERE vCompetitionTeamId = \""
						+ teamId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition_team", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition_team", values,
					" vCompetitionTeamId = \"" + teamId + "\" ");
		}

	}

	public void setCompetition(String vCompetitionId, String vCompetitionUrl,
			String vCompetitionName, String currentRoundId, String teamId,
			String vCompetitionHrefUrl, boolean isHref) {

		ContentValues values = new ContentValues();

		values.put("vCompetitionId", vCompetitionId);
		values.put("vCompetitionUrl", vCompetitionUrl);

		values.put("vCompetitionCurrentRoundId", currentRoundId);
		values.put("vCompetitionTeamId", teamId);
		values.put("vCompetitonName", vCompetitionName);

		values.put("vCompetitionHref", vCompetitionHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCompetitionId FROM competition WHERE vCompetitionId = \""
						+ vCompetitionId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "competition", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "competition", values,
					" vCompetitionId = \"" + vCompetitionId + "\" ");

		}

	}

	public Hashtable<String, List<String>> getDisplayUrlForTiles(
			String vSectionId) {
		String vSectionTilesId = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vBlockTileId "
								+ "FROM blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE "
								+ "s.vSectionId = '" + vSectionId
								+ "' AND LENGTH(vBlockTileId) > 0");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionTilesId = c.getString(c.getColumnIndex("vBlockTileId"));

			}

			c.close();
			c = null;
		}

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT vDisplayUrl,vBlockContentId,vContentId FROM blockContent WHERE "
						+ "vBlockContentId = '" + vSectionTilesId
						+ "' AND LENGTH(vDisplayUrl) > 0");
	}

	public Hashtable<String, List<String>> getDisplayUrlForStack(
			String vSectionId) {
		String vSectionStackId = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vBlockStackId FROM "
								+ "blocks b LEFT JOIN sections s ON s.vBlockId = b.vBlockId WHERE "
								+ "s.vSectionId = '" + vSectionId
								+ "' AND LENGTH(vBlockStackId) > 0");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionStackId = c
						.getString(c.getColumnIndex("vBlockStackId"));

			}

			c.close();
			c = null;
		}

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT vDisplayUrl,vBlockContentId,vContentId FROM blockContent WHERE "
						+ "vBlockContentId = '" + vSectionStackId
						+ "' AND LENGTH(vDisplayUrl) > 0");
	}

	public Hashtable<String, List<String>> getDisplayUrlForLeagueLobby(
			String vCompetitionId) {
		String vSectionTilesId = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vBlockTileId FROM blocks b "
								+ "LEFT JOIN sections s ON s.vBlockId = b.vBlockId LEFT JOIN competition c ON "
								+ "s.VsectionId = c.vSectionId WHERE c.vCompetitionId = '"
								+ vCompetitionId
								+ "' AND LENGTH(vBlockTileId) > 0");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionTilesId = c.getString(c.getColumnIndex("vBlockTileId"));

			}

			c.close();
			c = null;
		}

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT vDisplayUrl,vBlockContentId,vContentId "
						+ "FROM blockContent WHERE vBlockContentId = '"
						+ vSectionTilesId + "'  AND LENGTH(vDisplayUrl) > 0");

	}

	public Hashtable<String, List<String>> getStackDisplayUrlForLeagueLobby(
			String vCompetitionId) {
		String vSectionStackId = "";
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vBlockStackId FROM blocks b "
								+ "LEFT JOIN sections s ON s.vBlockId = b.vBlockId LEFT JOIN competition c ON "
								+

								"s.VsectionId = c.vSectionId WHERE c.vCompetitionId = '"
								+ vCompetitionId + "' "
								+ "AND LENGTH(vBlockStackId) > 0");

		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vSectionStackId = c
						.getString(c.getColumnIndex("vBlockStackId"));

			}

			c.close();
			c = null;
		}

		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT vDisplayUrl," +

				"vBlockContentId,vContentId FROM blockContent WHERE vBlockContentId = '"
						+ vSectionStackId + "'  "
						+ "AND LENGTH(vDisplayUrl) > 0");

	}

	/**
	 * Praveen
	 * 
	 * @param vLinkUrl
	 * @param vLinkHrefUrl
	 *            Modified as per the Href link element
	 */
	public void setLinkData(String vLinkUrl, String vLinkHrefUrl) {
		ContentValues values = new ContentValues();
		values.put("vLinkUrl", vLinkUrl);
		values.put("vHref", vLinkHrefUrl);
		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						"SELECT vLinkUrl FROM "
								+ " blockContentLinks WHERE ((LENGTH(vLinkUrl) > 0 AND vLinkUrl = '"
								+ vLinkUrl + "') OR "
								+ "(LENGTH(vHref) > 0 AND vHref = '" + vLinkUrl
								+ "') )");

		if (count == 0)

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blockContentLinks", values,
					null);

	}

	/*
	 * public void setLinkData(String vLinkUrl) { ContentValues values = new
	 * ContentValues(); values.put("vLinkUrl", vLinkUrl);
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vLinkUrl FROM blockContentLinks WHERE vLinkUrl = '"
	 * +vLinkUrl+"'");
	 * 
	 * if(count == 0)
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(Constants.
	 * QUERY_INSERT, null, "blockContentLinks", values, null );
	 * 
	 * }
	 */

	public void setlinkId(String vLinkUrl, String vLinkId) {
		ContentValues values = new ContentValues();
		values.put("vLinkUrl", vLinkUrl);
		values.put("vSectionId", vLinkId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vLinkUrl FROM blockContentLinks WHERE vLinkUrl = '"
						+ vLinkUrl + "'");

		if (count == 0)
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "blockContentLinks", values,
					null);
		else
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "blockContentLinks", values,
					"vLinkUrl = \"" + vLinkUrl + "\" ");

	}

	public String getContestType(String vContestId) {
		String vSportType = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vSportType FROM contests WHERE vContestId = \""
						+ vContestId + "\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vSportType = c.getString(c.getColumnIndex("vSportType"));
			}
			c.close();
		}
		c = null;
		return vSportType;
	}

	public Hashtable<String, List<String>> getLeaderBoardData(String vContestId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select(" SELECT l.vContestId AS vContestId, l.vTeamId AS  vTeamId, "
						+ "l.vPositionSummary AS  vPositionSummary, l.vSummary AS  vSummary, l.iPosition AS  vPosition, "
						+ "(  SELECT  vDisplayName FROM teams WHERE vTeamId = l.vTeamId ) AS vDisplayName,"
						+ "(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = l.vTeamId ) AS vCalendarUrl FROM leaderBoard l "
						+ "WHERE vContestId = \""
						+ vContestId
						+ "\" ORDER BY l.iPosition ASC ");

	}

	/*
	 * public void setAssociatedContestData(String associatedContestId, String
	 * associatedContestUrl, String contestId) {
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * values.put("associatedContestId", associatedContestId );
	 * values.put("associatedContestUrl", associatedContestUrl );
	 * values.put("contestId", contestId );
	 * 
	 * 
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT associatedContestId FROM associatedContest WHERE associatedContestId = \""
	 * + associatedContestId + "\" AND  contestId =\"" + contestId + "\"");
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "associatedContest", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "associatedContest", values,
	 * "associatedContestId = \"" + associatedContestId +
	 * "\" AND  contestId =\"" + contestId + "\"" );
	 * 
	 * }
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

	public void setAssociatedContestData(String associatedContestId,
			String associatedContestUrl, String associatedContestHrefUrl,
			String contestId) {

		ContentValues values = new ContentValues();
		values.put("associatedContestId", associatedContestId);
		values.put("associatedContestUrl", associatedContestUrl);
		values.put("associatedContestHref", associatedContestHrefUrl);
		values.put("contestId", contestId);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						"SELECT associatedContestId FROM associatedContest WHERE associatedContestId = \""
								+ associatedContestId
								+ "\" AND  contestId =\""
								+ contestId + "\"");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "associatedContest", values,
					null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"associatedContest",
					values,
					"associatedContestId = \"" + associatedContestId
							+ "\" AND  contestId =\"" + contestId + "\"");

		}

	}

	public void removeAssociatedContests(String associatedContestId) {

		Cursor c = (Cursor) PlayupLiveApplication.getDatabaseWrapper()
				.queryMethod2(
						Constants.QUERY_RAW,
						" DELETE FROM associatedContest WHERE associatedContestId = '"
								+ associatedContestId + "'", null, null, null);

		if (c != null) {
			c.close();
		}

	}

	public Hashtable<String, Object> getAssociatedContestUrl(String vContestId) {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String associatedContestUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT associatedContestUrl,vAssociatedContestHref FROM contests WHERE vContestId = \""
								+ vContestId + "\" ");
		try {
			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					associatedContestUrl = c.getString(c
							.getColumnIndex("vAssociatedContestHref"));
					if (associatedContestUrl != null
							&& associatedContestUrl.trim().length() > 0) {

						result.put("url", associatedContestUrl);
						result.put("isHref", true);

					} else {
						associatedContestUrl = c.getString(c
								.getColumnIndex("associatedContestUrl"));

						result.put("url", associatedContestUrl);
						result.put("isHref", false);
					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			if (c != null && !c.isClosed()) {

				c.close();
				c = null;

			}

		}

		return result;
	}

	public String getAssociatedContestUrlForRefresh(String vContestId) {

		String associatedContestUrl = null;
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						" SELECT associatedContestUrl,vAssociatedContestHref FROM contests WHERE vContestId = \""
								+ vContestId + "\" ");
		try {
			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					associatedContestUrl = c.getString(c
							.getColumnIndex("vAssociatedContestHref"));
					if (associatedContestUrl != null
							&& associatedContestUrl.trim().length() > 0) {
						return associatedContestUrl;

					} else {
						associatedContestUrl = c.getString(c
								.getColumnIndex("associatedContestUrl"));

					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			Logs.show(e);
		} finally {

			if (c != null && !c.isClosed()) {

				c.close();
				c = null;

			}

		}

		return associatedContestUrl;
	}

	/*
	 * public String getRegionUrlFromRoot() {
	 * 
	 * 
	 * 
	 * String url = ""; Cursor c = null; try{ c =
	 * PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT resource_url FROM root_resource WHERE resource_name = 'regions'  "
	 * ); if ( c != null ) { if(c.getCount() > 0){ c.moveToFirst(); url =
	 * c.getString( c.getColumnIndex ( "resource_url" ) ); }
	 * 
	 * }
	 * 
	 * }catch (Exception e) {
	 * 
	 * 
	 * } finally { if ( c != null && !c.isClosed() ) c.close();
	 * 
	 * 
	 * c = null; }
	 * 
	 * return url; }
	 */
	public Hashtable<String, Object> getRegionUrlFromRoot() {

		String url = "";
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = null;
		try {
			c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							"SELECT resource_url,resource_href FROM root_resource WHERE resource_name = 'regions'  ");
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					url = c.getString(c.getColumnIndex("resource_href"));
					if (url != null && url.trim().length() > 0) {
						result.put("url", url);
						result.put("isHref", true);

					} else {

						url = c.getString(c.getColumnIndex("resource_url"));
						result.put("url", url);
						result.put("isHref", false);

					}
				}

			}

		} catch (Exception e) {
			Logs.show(e);

		} finally {
			if (c != null && !c.isClosed())
				c.close();

			c = null;
		}

		return result;
	}

	/*
	 * public void setRegionData(String vRegionName, String vRegionCode, String
	 * vRegionUrl, String vRegionId, int iCountryCount, String vRegionLogo) {
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * values.put("vRegionName",vRegionName);
	 * values.put("vRegionCode",vRegionCode);
	 * values.put("vRegionUrl",vRegionUrl); values.put("vRegionId",vRegionId);
	 * values.put("iCountryCount",iCountryCount);
	 * values.put("vRegionLogo",vRegionLogo);
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vRegionId FROM regions" + " WHERE vRegionId = '"+vRegionId+"'");
	 * 
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_INSERT, null, "regions", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_UPDATE, null, "regions", values, " vRegionId = \"" +
	 * vRegionId + "\"" );
	 * 
	 * }
	 * 
	 * }
	 */

	public void setRegionData(String vRegionName, String vRegionCode,
			String vRegionUrl, String vRegionHrefUrl, String vRegionId,
			int iCountryCount, String vRegionLogo) {

		// Log.e("123",
		// "regions feild values....."+vRegionName+"-----"+vRegionCode+"----"+vRegionUrl+"----"+vRegionId+"----"+iCountryCount+"----"+vRegionLogo+"----"+vRegionHrefUrl);
		ContentValues values = new ContentValues();
		values.put("vRegionName", vRegionName);
		values.put("vRegionCode", vRegionCode);
		values.put("vRegionUrl", vRegionUrl);
		values.put("vRegionId", vRegionId);
		values.put("iCountryCount", iCountryCount);
		values.put("vRegionLogo", vRegionLogo);

		values.put("vRegionHrefUrl", vRegionHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRegionId FROM regions" + " WHERE vRegionId = '"
						+ vRegionId + "'");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
					Constants.QUERY_INSERT, null, "regions", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
					Constants.QUERY_UPDATE, null, "regions", values,
					" vRegionId = \"" + vRegionId + "\"");

		}

	}

	/*
	 * public void setCountryData(String vCountryUrl, String vCountryId, String
	 * vCountryName, String vCountryCode, String vCountryEffectiveCode, String
	 * vCountryLogo, String vRegionId, boolean isSelected) {
	 * 
	 * 
	 * 
	 * ContentValues values = new ContentValues();
	 * values.put("vCountryUrl",vCountryUrl);
	 * values.put("vCountryId",vCountryId);
	 * values.put("vCountryName",vCountryName);
	 * values.put("vCountryCode",vCountryCode);
	 * values.put("vCountryEffectiveCode",vCountryEffectiveCode);
	 * values.put("vCountryLogo",vCountryLogo);
	 * values.put("vRegionId",vRegionId); values.put("isSelected",isSelected);
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * "SELECT vCountryId FROM countries" +
	 * " WHERE vCountryId = '"+vCountryId+"'");
	 * 
	 * 
	 * if ( count == 0 ) {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_INSERT, null, "countries", values, null );
	 * 
	 * } else {
	 * 
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
	 * Constants.QUERY_UPDATE, null, "countries", values, " vCountryId = \"" +
	 * vCountryId + "\"" );
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	public void setCountryData(String vCountryUrl, String vCountryHrefUrl,
			String vCountryId, String vCountryName, String vCountryCode,
			String vCountryEffectiveCode, String vCountryLogo,
			String vRegionId, boolean isSelected) {

		ContentValues values = new ContentValues();
		values.put("vCountryUrl", vCountryUrl);
		values.put("vCountryId", vCountryId);
		values.put("vCountryName", vCountryName);
		values.put("vCountryCode", vCountryCode);
		values.put("vCountryEffectiveCode", vCountryEffectiveCode);
		values.put("vCountryLogo", vCountryLogo);
		values.put("vRegionId", vRegionId);
		values.put("isSelected", isSelected);

		values.put("vCountryHrefUrl", vCountryHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vCountryId FROM countries" + " WHERE vCountryId = '"
						+ vCountryId + "'");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
					Constants.QUERY_INSERT, null, "countries", values, null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
					Constants.QUERY_UPDATE, null, "countries", values,
					" vCountryId = \"" + vCountryId + "\"");

		}

	}

	/*
	 * public Hashtable<String, List<String>> getCountries(String vRegionId) {
	 * 
	 * return PlayupLiveApplication.getDatabaseWrapper().select(
	 * "SELECT vCountryUrl,vCountryId,vCountryName," +
	 * "vCountryCode,vCountryEffectiveCode,vCountryLogo,vRegionId,isSelected FROM countries WHERE vRegionId = '"
	 * +vRegionId+"'" + " ORDER BY upper(vCountryName) ASC");
	 * 
	 * }
	 */
	/**
	 * Praveen as per the href field
	 */
	public Hashtable<String, List<String>> getCountries(String vRegionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vCountryUrl,vCountryHrefUrl,vCountryId,vCountryName,"
						+ "vCountryCode,vCountryEffectiveCode,vCountryLogo,vRegionId,isSelected FROM countries WHERE vRegionId = '"
						+ vRegionId + "'" + " ORDER BY upper(vCountryName) ASC");

	}

	public Hashtable<String, Object> getRegionUrl(String vRegionId) {
		String url = "";
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vRegionUrl,vRegionHrefUrl FROM regions "
							+ "WHERE vRegionId = '" + vRegionId + "'");
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					url = c.getString(c.getColumnIndex("vRegionHrefUrl"));

					if (url != null && url.trim().length() > 0) {

						result.put("url", url);
						result.put("isHref", true);

					} else {

						url = c.getString(c.getColumnIndex("vRegionUrl"));

						result.put("url", url);
						result.put("isHref", false);
					}

				}
			}

		} catch (Exception e) {

		} finally {
			if (c != null && !c.isClosed())
				c.close();
			c = null;
		}

		return result;
	}

	public String getRegionUrlForRefresh(String vRegionId) {
		String url = "";
		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vRegionUrl,vRegionHrefUrl FROM regions "
							+ "WHERE vRegionId = '" + vRegionId + "'");
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					url = c.getString(c.getColumnIndex("vRegionHrefUrl"));

					if (url != null && url.trim().length() > 0) {

					} else {
						url = c.getString(c.getColumnIndex("vRegionUrl"));
					}
				}
			}

		} catch (Exception e) {

		} finally {
			if (c != null && !c.isClosed())
				c.close();
			c = null;
		}

		return url;
	}

	public void emptyRegions() {
		JsonUtil json = new JsonUtil();

		json.queryMethod1(Constants.QUERY_DELETE, null, "regions", null, null,
				null, false, false);

	}

	public void emptyCountries(String vRegionId) {
		// Cursor c = (Cursor)
		// PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
		// Constants.QUERY_RAW,
		// " DELETE FROM countries WHERE vRegionId = '"+vRegionId+"'", null,
		// null, null );

		PlayupLiveApplication.getDatabaseWrapper().queryMethod3(
				Constants.QUERY_DELETE, null, "countries", null,
				"vRegionId = '" + vRegionId + "'");

		// if ( c != null ) {
		// c.close();
		// }

	}

	public String getRegionName(String vRegionId) {
		String regionName = "";
		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vRegionName FROM regions " + "WHERE vRegionId = '"
							+ vRegionId + "'");
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					regionName = c.getString(c.getColumnIndex("vRegionName"));

				}

			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			if (c != null && !c.isClosed()) {

				c.close();
				c = null;

			}
		}

		return regionName;

	}

	public String getSelectedRegionName() {
		String vCountryName = "";
		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vCountryName FROM countries "
							+ "WHERE isSelected = '1'");
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					vCountryName = c
							.getString(c.getColumnIndex("vCountryName"));

				} else {

					c.close();
					String currentRegion = new PreferenceManagerUtil().get(
							Constants.REGION_TOKEN, "");
					c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
							"SELECT vRegionName FROM regions "
									+ "WHERE vRegionCode = '" + currentRegion
									+ "'");

					if (c != null) {

						if (c.getCount() > 0) {

							c.moveToFirst();
							vCountryName = c.getString(c
									.getColumnIndex("vRegionName"));

						}

					}

				}

			}

		} catch (Exception e) {

		} finally {

			if (c != null && !c.isClosed()) {

				c.close();
				c = null;

			}
		}
		return vCountryName;
	}

	public String getSelectedRegionId() {
		String vCountryId = "";
		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vCountryId FROM countries "
							+ "WHERE isSelected = '1'");
			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();
					vCountryId = c.getString(c.getColumnIndex("vCountryId"));

				}

			}

		} catch (Exception e) {

		} finally {

			if (c != null && !c.isClosed()) {

				c.close();
				c = null;

			}

		}

		return vCountryId;

	}

	/**
	 * getting sport/contest background image
	 * 
	 * @return
	 */

	public String getSportBackground(String vSportId) {
		String backgroundImageName = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vImageName FROM sportBackground WHERE vSportId = \""
						+ vSportId + "\" ");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				backgroundImageName = c.getString(c
						.getColumnIndex("vImageName"));
			}
			c.close();
		}
		c = null;
		return backgroundImageName;
	}

	/**
	 * setting sport/contest background image
	 * 
	 * @return
	 */

	public void setSportBackground(String vSportId, String vImageName) {
		ContentValues values = new ContentValues();
		values.put("vSportId", vSportId);
		values.put("vImageName", vImageName);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vImageName FROM sportBackground WHERE vSportId = \""
						+ vSportId + "\" ");

		if (count == 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "sportBackground", values,
					null);

		} else {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "sportBackground", values,
					"vSportId = \"" + vSportId + "\" ");

		}
	}

	/**
	 * getting sport/contest background image
	 * 
	 * @return
	 */

	public String getContestSummaryFromSummaries(String vTeamId,
			String vContestId) {
		String summary = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" Select vSummary FROM summaries WHERE " + " vContestId = \""
						+ vContestId + "\" AND   vTeamId = \"" + vTeamId
						+ "\" ORDER BY iPosition ASC");
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				summary = c.getString(c.getColumnIndex("vSummary"));
			}
			c.close();
		}
		c = null;
		return summary;
	}

	public Hashtable<String, Object> getPrivateLobbyUrl() {
		Hashtable<String, Object> result = new Hashtable<String, Object>();
		String vLobbyUrl = "";

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vLobbyUrl,vLobbyHrefUrl FROM user WHERE isPrimaryUser = 1");

		try {

			if (c != null && c.getCount() > 0) {

				c.moveToFirst();
				vLobbyUrl = c.getString(c.getColumnIndex("vLobbyUrl"));
				if (vLobbyUrl != null && vLobbyUrl.trim().length() > 0) {

					result.put("url", vLobbyUrl);
					result.put("isHref", false);

				} else {
					vLobbyUrl = c.getString(c.getColumnIndex("vLobbyHrefUrl"));
					result.put("url", vLobbyUrl);
					result.put("isHref", true);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			if (c != null && !c.isClosed())
				c.close();

		}

		return result;

	}

	/*
	 * public String getExtensionLinkUrl(String vContestId) { String
	 * vExtensionLinkUrl = "";
	 * 
	 * 
	 * Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
	 * "SELECT vEventsExtensionUrl FROM contests WHERE" +
	 * " vContestId = '"+vContestId+"'"); try{ if(c!= null ){
	 * 
	 * if(c.getCount() > 0){
	 * 
	 * c.moveToFirst();
	 * 
	 * vExtensionLinkUrl = c.getString(c.getColumnIndex("vEventsExtensionUrl"));
	 * 
	 * }
	 * 
	 * } }catch(Exception e){
	 * 
	 * Logs.show(e);
	 * 
	 * }finally{
	 * 
	 * if(c!= null && !c.isClosed()){ c.close(); c = null; }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * return vExtensionLinkUrl; }
	 */

	/**
	 * Praveen
	 */
	public Hashtable<String, Object> getExtensionLinkUrl(String vContestId) {
		String vExtensionLinkUrl = "";
		Hashtable<String, Object> result = new Hashtable<String, Object>();

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				"SELECT vEventsExtensionUrl,vEventsExtensionHref FROM contests WHERE"
						+ " vContestId = '" + vContestId + "'");
		try {
			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();

					vExtensionLinkUrl = c.getString(c
							.getColumnIndex("vEventsExtensionHref"));
					if (vExtensionLinkUrl != null
							&& vExtensionLinkUrl.trim().length() > 0) {
						result.put("url", vExtensionLinkUrl);
						result.put("isHref", true);

					} else {

						vExtensionLinkUrl = c.getString(c
								.getColumnIndex("vEventsExtensionUrl"));
						result.put("url", vExtensionLinkUrl);
						result.put("isHref", false);

					}
				}

			}
		} catch (Exception e) {

			Logs.show(e);

		} finally {

			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}

		}

		return result;
	}

	public boolean checkForLive(String vContestUrl) {

		int totalCount = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						"SELECT vContestId FROM contests WHERE vContestUrl = '"
								+ vContestUrl
								+ "' AND dEndTime IS NULL AND dStartTime IS NOT NULL");

		if (totalCount > 0)
			return true;

		else
			return false;

	}

	public void deleteContestTilesData(String tilesUid) {

		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_DELETE, null, "blockContent", null,
				" vBlockContentId = '" + tilesUid + "'");

	}

	public Hashtable<String, List<String>> getStyleDetails(String vSectionId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vMainColor, vSecondaryColor, vMainTitleColor, vSecondaryTitleColor FROM sections WHERE vSectionId ='"
						+ vSectionId + "'");
	}

	public boolean isTeamSchedule(String vSectionId) {

		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vContentType FROM blockContent bc LEFT JOIN"
								+ " blocks b ON bc.vBlockContentId = b.vBlockStackId LEFT JOIN sections s ON b.vBlockId = s.vBlockId WHERE s.vSectionId = '"
								+ vSectionId + "'");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();

				String vContentType = c.getString(c
						.getColumnIndex("vContentType"));

				if (vContentType.equalsIgnoreCase(Constants.CRICKET)
						|| vContentType.equalsIgnoreCase(Constants.BASEBALL)
						|| vContentType.equalsIgnoreCase(Constants.BASKETBALL)
						|| vContentType.equalsIgnoreCase(Constants.HOCKEY)
						|| vContentType.equalsIgnoreCase(Constants.ICE_HOCKEY)
						|| vContentType.equalsIgnoreCase(Constants.AFL)
						|| vContentType.equalsIgnoreCase(Constants.FOOTBALL)
						|| vContentType.equalsIgnoreCase(Constants.SOCCER)
						|| vContentType.equalsIgnoreCase(Constants.NFL)
						|| vContentType
								.equalsIgnoreCase(Constants.TEST_CRICKET)
						|| vContentType
								.equalsIgnoreCase(Constants.RUGBY_LEAGUE)
						|| vContentType.equalsIgnoreCase(Constants.RUGBY_UNION)
						|| vContentType
								.equalsIgnoreCase(Constants.SET_BASED_DATA)
						|| vContentType.equalsIgnoreCase(Constants.LEADERBOARD)) {

					c.close();
					return true;

				}

			}

			c.close();

		}

		return false;
	}

	public boolean isTeamScheduleForLeagueLobby(String vCompetitionId) {
		Cursor c = PlayupLiveApplication
				.getDatabaseWrapper()
				.selectQuery(
						"SELECT vContentType FROM blockContent bc LEFT JOIN"
								+ " blocks b ON bc.vBlockContentId = b.vBlockStackId LEFT JOIN sections s ON b.vBlockId = s.vBlockId LEFT JOIN"
								+ " competition c ON s.vSectionId = c.vSectionId WHERE c.vCompetitionId = '"
								+ vCompetitionId + "'");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				String vContentType = c.getString(c
						.getColumnIndex("vContentType"));

				if (vContentType.equalsIgnoreCase(Constants.CRICKET)
						|| vContentType.equalsIgnoreCase(Constants.BASEBALL)
						|| vContentType.equalsIgnoreCase(Constants.BASKETBALL)
						|| vContentType.equalsIgnoreCase(Constants.HOCKEY)
						|| vContentType.equalsIgnoreCase(Constants.ICE_HOCKEY)
						|| vContentType.equalsIgnoreCase(Constants.AFL)
						|| vContentType.equalsIgnoreCase(Constants.FOOTBALL)
						|| vContentType.equalsIgnoreCase(Constants.SOCCER)
						|| vContentType.equalsIgnoreCase(Constants.NFL)
						|| vContentType
								.equalsIgnoreCase(Constants.TEST_CRICKET)
						|| vContentType
								.equalsIgnoreCase(Constants.RUGBY_LEAGUE)
						|| vContentType.equalsIgnoreCase(Constants.RUGBY_UNION)
						|| vContentType
								.equalsIgnoreCase(Constants.SET_BASED_DATA)
						|| vContentType.equalsIgnoreCase(Constants.LEADERBOARD)) {

					c.close();
					return true;

				}

			}

			c.close();

		}

		return false;
	}

	/**
	 * setting team schedule data
	 */
	/*
	 * public void setTeamScheduleData (String vTeamScheduleId,String
	 * vTeamScheduleUrl,String vTeamScheduleType,String vTitle,String vTeamId,
	 * String vTeamContestId, String vTeamContestType, String vTeamContestUrl )
	 * {
	 * 
	 * ContentValues values = new ContentValues(); values = new ContentValues();
	 * values.put( "vTeamScheduleId", vTeamScheduleId ); values.put(
	 * "vTeamScheduleUrl", vTeamScheduleUrl ); values.put( "vTeamScheduleType",
	 * vTeamScheduleType ); values.put( "vTeamId", vTeamId ); values.put(
	 * "vTeamContestId", vTeamContestId ); values.put( "vTeamContestType",
	 * vTeamContestType ); values.put( "vTeamContestUrl", vTeamContestUrl );
	 * values.put( "vTitle", vTitle );
	 * 
	 * 
	 * int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
	 * " SELECT vTeamScheduleId FROM team_schedule WHERE vTeamScheduleId = \"" +
	 * vTeamScheduleId + "\" " );
	 * 
	 * if ( count == 0 ) {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_INSERT, null, "team_schedule", values, null ); } else {
	 * PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
	 * Constants.QUERY_UPDATE, null, "team_schedule", values,
	 * " vTeamScheduleId = \"" + vTeamScheduleId + "\" " ); }
	 * 
	 * }
	 */

	/**
	 * Praveen : modofoed as per the href setting team schedule data
	 */
	public void setTeamScheduleData(String vTeamScheduleId,
			String vTeamScheduleUrl, String vTeamScheduleHrefUrl,
			String vTeamScheduleType, String vTitle, String vTeamId,
			String vTeamContestId, String vTeamContestType,
			String vTeamContestUrl, String vTeamContestHrefUrl) {

		ContentValues values = new ContentValues();
		values = new ContentValues();
		values.put("vTeamScheduleId", vTeamScheduleId);
		values.put("vTeamScheduleUrl", vTeamScheduleUrl);
		values.put("vTeamScheduleType", vTeamScheduleType);
		values.put("vTeamId", vTeamId);
		values.put("vTeamContestId", vTeamContestId);
		values.put("vTeamContestType", vTeamContestType);
		values.put("vTeamContestUrl", vTeamContestUrl);
		values.put("vTitle", vTitle);

		values.put("vTeamScheduleHrefUrl", vTeamScheduleHrefUrl);
		values.put("vTeamContestHrefUrl", vTeamContestHrefUrl);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vTeamScheduleId FROM team_schedule WHERE vTeamScheduleId = \""
						+ vTeamScheduleId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"team_schedule", values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "team_schedule", values,
					" vTeamScheduleId = \"" + vTeamScheduleId + "\" ");
		}

	}

	/**
	 * setting team schedule contests
	 */
	public void setTeamScheduleContests(String vTeamScheduleId,
			String vContestId, int iOrder) {

		ContentValues values = new ContentValues();
		values = new ContentValues();
		values.put("vTeamScheduleId", vTeamScheduleId);
		values.put("vContestId", vContestId);
		values.put("iOrder", iOrder);

		int count = PlayupLiveApplication
				.getDatabaseWrapper()
				.getTotalCount(
						" SELECT vTeamScheduleId FROM team_schedule_contests WHERE vTeamScheduleId = \""
								+ vTeamScheduleId
								+ "\" AND vContestId =\""
								+ vContestId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "team_schedule_contests",
					values, null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"team_schedule_contests",
					values,
					" vTeamScheduleId = \"" + vTeamScheduleId
							+ "\" AND vContestId =\"" + vContestId + "\" ");
		}

	}

	/**
	 * setting the team competition data
	 */
	public void setTeamCompetitionData(String vTeamId, String vCompetitionId) {

		ContentValues values = new ContentValues();
		values = new ContentValues();
		values.put("vTeamId", vTeamId);
		values.put("vCompetitionId", vCompetitionId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				" SELECT vCompetitionId FROM team_competitions WHERE vTeamId = \""
						+ vTeamId + "\" AND vCompetitionId =\""
						+ vCompetitionId + "\" ");

		if (count == 0) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_INSERT, null, "team_competitions", values,
					null);
		} else {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"team_competitions",
					values,
					" vTeamId = \"" + vTeamId + "\" AND vCompetitionId =\""
							+ vCompetitionId + "\" ");
		}

	}

	/**
	 * getting team schedule id
	 */

	public String getTeamScheduleId(String vTeamScheduleUrl) {
		String vTeamScheduleId = null;
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vTeamScheduleId FROM team_schedule WHERE vTeamScheduleUrl = '"
						+ vTeamScheduleUrl + "'");
		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vTeamScheduleId = c.getString(c
						.getColumnIndex("vTeamScheduleId"));
			}

			c.close();
			c = null;
		}
		return vTeamScheduleId;
	}

	/**
	 * getting title from teamschedule
	 */
	public String getTitleFromTeamSchedule(String vTeamScheduleId) {
		String vTitle = null;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vTitle FROM team_schedule WHERE vTeamScheduleId = '"
						+ vTeamScheduleId + "'");

		if (c != null) {

			if (c.getCount() > 0) {
				c.moveToFirst();
				vTitle = c.getString(c.getColumnIndex("vTitle"));
			}

			c.close();
		}

		c = null;
		return vTitle;

	}

	/**
	 * removing team competion data, before inserting specific data of a team
	 */
	public void removeCompetitionDataOfTeam(String vTeamId) {

		Cursor c = (Cursor) PlayupLiveApplication.getDatabaseWrapper()
				.queryMethod2(
						Constants.QUERY_RAW,
						" DELETE FROM team_competitions WHERE vTeamId = '"
								+ vTeamId + "'", null, null, null);

		if (c != null) {
			c.close();
		}

	}

	/**
	 * removing team scheduled contests
	 */
	public void removeTeamScheduledContests(String vTeamScheduleId) {

		Cursor c = (Cursor) PlayupLiveApplication.getDatabaseWrapper()
				.queryMethod2(
						Constants.QUERY_RAW,
						" DELETE FROM team_schedule_contests WHERE vTeamScheduleId = '"
								+ vTeamScheduleId + "'", null, null, null);

		if (c != null) {
			c.close();
		}

	}

	public Hashtable<String, List<String>> getBlocksData(String vSectionId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vBlockItemId, vBlockItemType,iBlockItemCount FROM blocks b "
						+ " LEFT JOIN sections s on s.vBlockId = b.vBlockId where s.vSectionId = \""
						+ vSectionId
						+ "\" AND iBlockItemCount ORDER BY b.iOrder ASC");

	}

	/**
	 * Praveen: Modified as per the href: vHref
	 * 
	 * @param vBlockItemId
	 * @return
	 */
	public Hashtable<String, List<String>> getBlockItemData(String vBlockItemId) {

		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT b.vBlockItemId,b.vBlockItemType,vContentId,vContentType,vDisplayUrl,vDisplayHrefUrl,vDisplayType,vHighlightUrl,vHighlightHrefUrl,vHighlightType,"
						+ "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon,"
						+ "vSocialIcon,vImageUrl,vLinkUrl,vLinkHrefUrl,VName,vTimeStamp,vBackgroundColor,"
						+ "vTitle,vSubtitle,iLive,vAccessory,vBackgroundImage,vSource,vContentUrl,vContentHrefUrl,vLinkType,vRadioBackground,vDisplayCount FROM blockContent bc "
						+ " LEFT JOIN blocks b ON b.vBlockItemId = bc.vBlockContentId "
						+ " WHERE bc.vBlockContentId =\""
						+ vBlockItemId
						+ "\""
						+ " ORDER BY iOrderId ASC");

	}

	/*
	 * public Hashtable<String, List<String>> getBlockItemData(String
	 * vBlockItemId) {
	 * 
	 * return PlayupLiveApplication.getDatabaseWrapper().select(
	 * "SELECT b.vBlockItemId,b.vBlockItemType,vContentId,vContentType,vDisplayUrl,vDisplayType,vHighlightUrl,vHighlightType,"
	 * + "vHighlightId,vSummary,vFooterTitle,vFooterSubTitle,vSourceIcon," +
	 * "vSocialIcon,vImageUrl,vLinkUrl,VName,vTimeStamp,vBackgroundColor," +
	 * "vTitle,vSubtitle,iLive,vAccessory,vBackgroundImage,vSource,vContentUrl,vLinkType, FROM blockContent bc "
	 * + " LEFT JOIN blocks b ON b.vBlockItemId = bc.vBlockContentId " +
	 * " WHERE bc.vBlockContentId =\""+vBlockItemId+"\""+
	 * " ORDER BY iOrderId ASC" );
	 * 
	 * }
	 */

	public Hashtable<Integer, Hashtable<String, List<String>>> getSectionData(
			String vSectionId) {

		Hashtable<Integer, Hashtable<String, List<String>>> sectionData = new Hashtable<Integer, Hashtable<String, List<String>>>();
		Hashtable<String, List<String>> blockData = getBlocksData(vSectionId);

		if (blockData != null && blockData.get("vBlockItemId") != null) {
			for (int i = 0; i < blockData.get("vBlockItemId").size(); i++) {
				sectionData.put(
						i,
						DatabaseUtil.getInstance().getBlockItemData(
								blockData.get("vBlockItemId").get(i)));
			}
			return sectionData;
		}
		return null;
	}

	public Hashtable<String, List<String>> getDisplayUrlForSection(
			String vSectionId) {
		return PlayupLiveApplication
				.getDatabaseWrapper()
				.select("SELECT vDisplayUrl,vDisplayHrefUrl,vBlockContentId,vContentId FROM blockContent bc"
						+ " LEFT JOIN blocks b ON b.vBlockItemId = bc.vBlockContentId LEFT JOIN sections s ON s.vBlockId = b.vBlockId "
						+ " WHERE s.vSectionId = \""
						+ vSectionId
						+ "\" AND LENGTH( bc.vDisplayUrl) > 0");
	}

	public boolean isLiveContestUrl(String vContestUrl) {
		boolean isLiveContest = false;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
				" SELECT vContestUrl FROM contests WHERE vContestUrl = '"
						+ vContestUrl
						+ "' AND dEndTime IS NULL AND dStartTime IS NOT NULL");

		if (c != null) {

			if (c.getCount() > 0) {
				isLiveContest = true;
			}

			c.close();
		}

		c = null;
		return isLiveContest;
	}

	public String getWaterMarkImage(String vContestId) {

		Cursor c = null;

		String vBackgroundImageUrl = null;

		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vBackgroundImageUrl FROM contests WHERE vContestId = '"
							+ vContestId + "'");

			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();

					vBackgroundImageUrl = c.getString(c
							.getColumnIndex("vBackgroundImageUrl"));

				}

			}
		} catch (Exception e) {
			Logs.show(e);
		} finally {

			if (c != null && !c.isClosed()) {
				c.close();
			}

		}

		return vBackgroundImageUrl;
	}

	/**
	 * Check whether MAC credentials are avialbale
	 * 
	 * @return
	 */
	public boolean isCredentialAvailable() {

		Cursor c = null;
		try {
			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vId FROM credentials");

			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();

					if (c.getString(c.getColumnIndex("vId")) != null
							&& c.getString(c.getColumnIndex("vId")).trim()
									.length() > 0)
						return true;

				}

			}
		} catch (Exception e) {
			Logs.show(e);
			return false;
		} finally {

			if (c != null && !c.isClosed()) {
				c.close();
			}

		}

		return false;

	}

	public void setCredentials(String id, String secret) {
		try {

			Crypto crypto = new Crypto();
			String encryptedId = crypto.encryptCredentials(id);
			String encryptedSecret = crypto.encryptCredentials(secret);

			// Log.e("123",
			// "Set credentials------id:---------"+id+"---------encrypted id:----------------"+encryptedId+"-----------secret:----------"+secret+"------------encrytedSecret:---------"+encryptedSecret);

			ContentValues values = new ContentValues();
			values.put("vId", encryptedId);
			values.put("vSecret", encryptedSecret);
			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount("SELECT vId FROM credentials");

			if (count > 0)
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "credentials", values,
						null);
			else
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "credentials", values,
						null);
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	public Hashtable<String, String> getCredentials() {

		Cursor c = null;
		try {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vId,vSecret FROM credentials");
			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();

					String vId = "";
					String vSecret = "";

					Crypto crypto = new Crypto();

					vId = crypto.decryptCredentials(c.getString(c
							.getColumnIndex("vId")));

					vSecret = crypto.decryptCredentials(c.getString(c
							.getColumnIndex("vSecret")));

					Hashtable<String, String> credentials = new Hashtable<String, String>();
					credentials.put("vId", vId);
					credentials.put("vSecret", vSecret);
					// Log.e("123",
					// "Get credentials-------id:---------"+vId+"---------encrypted id:----------------"+c.getString(c.getColumnIndex("vId"))+"-----------secret:----------"+vSecret+"------------encrytedSecret:---------"+c.getString(c.getColumnIndex("vSecret")));
					return credentials;

				}

			}

		} catch (Exception e) {
			Logs.show(e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}
		}

		return null;

	}

	public void setProviderTokenUrl(String href, String self, String vUserId) {

		try {
			ContentValues values = new ContentValues();

			values.put("vProviderTokenHref", href);
			values.put("vProviderTokenSelf", self);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE, null, "user", values,
					" iUserId = \"" + vUserId + "\" ");
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	public Hashtable<String, List<String>> getProviderTokensUrl() {
		return PlayupLiveApplication.getDatabaseWrapper().select(
				"SELECT vProviderTokenHref,vProviderTokenSelf FROM user");
	}

	public String getProviderTokensUrlForRefresh() {

		String vProviderTokenRefreshUrl = "";
		Cursor c = null;
		try {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vProviderTokenHref,vProviderTokenSelf FROM user");

			if (c != null) {

				if (c.getCount() > 0) {

					c.moveToFirst();

					vProviderTokenRefreshUrl = c.getString(c
							.getColumnIndex("vProviderTokenHref"));

					if (vProviderTokenRefreshUrl != null
							&& vProviderTokenRefreshUrl.trim().length() > 0)
						return vProviderTokenRefreshUrl;

					vProviderTokenRefreshUrl = c.getString(c
							.getColumnIndex("vProviderTokenSelf"));

					if (vProviderTokenRefreshUrl != null
							&& vProviderTokenRefreshUrl.trim().length() > 0)
						return vProviderTokenRefreshUrl;

				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		} finally {

			if (c != null && !c.isClosed())
				c.close();

			c = null;

		}

		return vProviderTokenRefreshUrl;

	}

	public void setProviderToken(String vType, String vUid, String vId,
			String vToken) {

		try {
			ContentValues values = new ContentValues();

			values.put("vUid", vUid);

			values.put("vId", vId);

			values.put("vToken", vToken);

			values.put("vType", vType);

			int count = PlayupLiveApplication.getDatabaseWrapper()
					.getTotalCount(
							"SELECT vUid FROM providerTokens WHERE vUid = '"
									+ vUid + "'");

			if (count > 0) {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE, null, "providerTokens", values,
						" vUid = '" + vUid + "'");

			} else {

				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_INSERT, null, "providerTokens", values,
						null);

			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}

	public void emptyProviderTokens() {

		try {
			DatabaseWrapper.getWritableSQLiteDatabase().delete(
					"providerTokens", null, null);
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	public String getProviderToken(String vProviderId) {

		String vToken = "";

		Cursor c = null;

		try {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vToken FROM providerTokens WHERE vId = '"
							+ vProviderId + "'");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				vToken = c.getString(c.getColumnIndex("vToken"));
			}

		} catch (Exception e) {
			Logs.show(e);
		} finally {

			if (c != null && !c.isClosed()) {
				c.close();
			}

		}

		return vToken;
	}

	public int getProviderTokenCount() {

		String vToken = "";

		Cursor c = null;

		try {

			c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vToken FROM providerTokens");
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				vToken = c.getString(c.getColumnIndex("vToken"));

			}

		} catch (Exception e) {
			Logs.show(e);
		} finally {

			if (c != null && !c.isClosed()) {
				c.close();
			}

		}

		if ((vToken != null) && (!vToken.equals("")) && vToken.length() > 0) {
			return 1;
		} else
			return 0;
	}

	public int getCredentialsCount() {
		try {

			Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
					"SELECT vId,vSecret FROM credentials");
			if (c != null) {
				if (c.getCount() > 0) {

					c.moveToFirst();


					return 1;

				}
			}
			} catch (Exception e) {
				Logs.show(e);
			}

			return 0;
	}


	public Hashtable<String, List<String>> getRadioStaionsData(String vContentId) {


		
		return PlayupLiveApplication.getDatabaseWrapper().select("SELECT vContentId,vRadioId,vRadioTitle,vRadioSubTitle,vRadioIcon,vRadioDisplayTitle,vRadioDisplaySubTitle,vRadioStationBackground, " +
				" vRadioStationUrl,vRadioSationHrefUrl, vRadioStationLinkType, iOrderId FROM radioStations "+  
				" WHERE vContentId =\""+vContentId+"\" ORDER BY iOrderId");
		
		


		}

				

			

		

	

	public void setRadioStationsData(String vContentId, String vRadioId,
			String vRadioTitle, String vRadioSubTitle, String vRadioIcon,
			String vRadioDisplayTitle, String vRadioDisplaySubTitle,
			String vRadioStationBackground, String vRadioStationUrl,
			String vRadioSationHrefUrl, String vRadioStationLinkType,
			int iOrderId) {

		ContentValues values = new ContentValues();
		values.put("vContentId", vContentId);
		values.put("vRadioId", vRadioId);
		values.put("vRadioTitle", vRadioTitle);
		values.put("vRadioSubTitle", vRadioSubTitle);
		values.put("vRadioIcon", vRadioIcon);
		values.put("vRadioDisplayTitle", vRadioDisplayTitle);
		values.put("vRadioDisplaySubTitle", vRadioDisplaySubTitle);
		values.put("vRadioStationBackground", vRadioStationBackground);
		values.put("vRadioStationUrl", vRadioStationUrl);
		values.put("vRadioSationHrefUrl", vRadioSationHrefUrl);
		values.put("vRadioStationLinkType", vRadioStationLinkType);
		values.put("iOrderId", iOrderId);

		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vContentId FROM radioStations WHERE vContentId = '"
						+ vContentId + "'" + " AND vRadioId = '" + vRadioId
						+ "'");

		if (count > 0) {

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"radioStations",
					values,
					" vContentId = '" + vContentId + "'" + " AND vRadioId = '"
							+ vRadioId + "'");

		} else {

			PlayupLiveApplication.getDatabaseWrapper()
					.queryMethod2(Constants.QUERY_INSERT, null,
							"radioStations", values, null);

		}

	}
	
	public void setIsPlaying(String vRadioId){
	
		
		ContentValues values = new ContentValues();
		
		values.put("isPlaying", false);
		
		
		PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
				Constants.QUERY_UPDATE,
				null,
				"radioStations",
				values, " isPlaying = 1");
		
		int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
				"SELECT vRadioId FROM radioStations WHERE vRadioId = '" + vRadioId + "'");

		if (count > 0) {

			
			
			values.put("isPlaying", true);
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
					Constants.QUERY_UPDATE,
					null,
					"radioStations",
					values,
					" vRadioId = '" + vRadioId + "'");
		
	}
	}
		
		public void removeIsPlaying(String vRadioId){
			
			
			ContentValues values = new ContentValues();
			values.put("isPlaying", false);
			
			
			
			
			int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount(
					"SELECT vRadioId FROM radioStations WHERE vRadioId = '" + vRadioId + "'");

			if (count > 0) {

				
				
				
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2(
						Constants.QUERY_UPDATE,
						null,
						"radioStations",
						values,
						" vRadioId = '" + vRadioId + "'");
			
		}
		
		
		}
		


	public Hashtable<String, List<String>> getCurrentRadio() {
		return PlayupLiveApplication.getDatabaseWrapper().select("SELECT vRadioId,vRadioTitle,vRadioSubTitle,vRadioStationUrl,vRadioSationHrefUrl FROM radioStations WHERE isPlaying = 1");
		
	}

	public Hashtable<String, List<String>> getCurrentRadioForLeague(String vContentId) {
		Hashtable<String, List<String>> currentRadio =  PlayupLiveApplication.getDatabaseWrapper().select("SELECT vRadioId," +
				"vRadioTitle,vRadioSubTitle,vRadioStationUrl,vRadioSationHrefUrl " +
				"FROM radioStations WHERE isPlaying = 1");
		
		
		return currentRadio;
		
//		if(currentRadio != null && currentRadio.get("vRadioId") != null && currentRadio.get("vRadioId").size() > 0){
			
//		}else{
//			currentRadio =  PlayupLiveApplication.getDatabaseWrapper().select("SELECT vRadioId," +
//					"vRadioTitle,vRadioSubTitle,vRadioStationUrl,vRadioSationHrefUrl " +
//					"FROM radioStations WHERE vContentId = '"+vContentId+"' ORDER BY iOrderId LIMIT 1");
//			
//			
//			
//			return currentRadio;
//		}
//		
		
	}

}
