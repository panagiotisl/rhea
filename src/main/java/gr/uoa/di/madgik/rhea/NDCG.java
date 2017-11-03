package gr.uoa.di.madgik.rhea;

import java.util.HashMap;
import java.util.Map;

public class NDCG {

	public static double compute(Map<String, Double> zNumbersSorted, Map<String, Double> zNumberEstimationsSorted, HashMap<String, Integer> userTweets, HashMap<String, Integer> userCrawledTweets, int K) {

		double dcg = 0;
		double idcg = computeIDCG(zNumbersSorted, userTweets, K);
		if (idcg == 0) {
			return 0;
		}

		int actualRank = 1;
		
		for(String user : zNumbersSorted.keySet()){
			double currentUserCrawledTweetsPercentage = userCrawledTweets.containsKey(user) && userTweets.containsKey(user) ? (double)userCrawledTweets.get(user)/userTweets.get(user) : 0;
			double sampleRank = Utilities.getRank(user, zNumberEstimationsSorted.keySet());
			double itemRelevance = K - actualRank + 1;
			dcg += (sampleRank == 1) ?  currentUserCrawledTweetsPercentage * itemRelevance : currentUserCrawledTweetsPercentage * itemRelevance / ( Math.log(sampleRank) / Math.log(2) ); 
			actualRank++;
			if(actualRank > K)
				break;
		}
		return dcg / idcg;
		
	}

	private static double computeIDCG(Map<String, Double> zNumbersSorted, HashMap<String, Integer> userTweets, int K) {
		double idcg = 0;
		int actualRank = 1;
		for(String user : zNumbersSorted.keySet()){
			if(userTweets.containsKey(user)){
				int itemRelevance = K - actualRank + 1; // set relevance of the 1st to be k and of the last to be 1
				idcg += (actualRank == 1) ?  1 * itemRelevance : 1 * itemRelevance / ( Math.log(actualRank) / Math.log(2) );
			}
			actualRank++;
			if(actualRank > K) // repeat for only k users
				break;
		}
		return idcg;
	}

}
