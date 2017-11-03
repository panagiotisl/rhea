package gr.uoa.di.madgik.rhea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.log4j.Logger;

import com.google.common.io.Resources;
import com.twitter.Extractor;

public class SampleAndEvaluateTweets {

	final static Logger LOGGER = Logger.getLogger(SampleAndEvaluateTweets.class);
	
	final static Pattern patternTweet = Pattern.compile(".*http://twitter.com/(.*)");
	
	public enum Type {EXPVALUE, STATIC};
	
	private HashSet<Tweet> tweets;
	private Extractor extractor = new Extractor();
	private HashMap<String, Integer> userTweets = new HashMap<String, Integer>();
	private HashMap<String, Integer> userCrawledTweets = new HashMap<String, Integer>();
	private HashMap<String, Integer> userInMentions = new HashMap<String, Integer>();
	private HashMap<String, Integer> userOutMentions = new HashMap<String, Integer>();
	
	
	public SampleAndEvaluateTweets(int K, String filename, int a, int b, Type type, boolean filterOut) throws FileNotFoundException, UnsupportedEncodingException {
		LOGGER.info("Sampling... " + type.toString() + " " + filename + " " + 100*a/b + "%" + " k = " + K);
		tweets = new HashSet<Tweet>();
		Random random = new Random(23);
		double epsOfTotalCount = 0.0001;
        double confidence = 0.99;
		CountMinSketchWithMax cmsMentioned = new CountMinSketchWithMax(epsOfTotalCount, confidence, 23);
		CountMinSketchWithMax cmsMentions = new CountMinSketchWithMax(epsOfTotalCount, confidence, 23);
		TopKMap<String, Double> topKMap = new TopKMap<String, Double>(K);
		
		Map<String, Double> staticZNumbers = new HashMap<String, Double>();
		HashSet<String> staticExpertUsers = new HashSet<String>(K);
		if(type.equals(Type.STATIC)){
			try {
				LineIterator it = FileUtils.lineIterator(new File(Resources.getResource("train.txt").getFile()), "UTF-8");
				HashMap<String, Integer> staticUserInMentions = new HashMap<String, Integer>();
				HashMap<String, Integer> staticUserOutMentions = new HashMap<String, Integer>();
				String line1, line2, line3, user = null;
				int count = 0;
				try {
					while((line1 = it.nextLine()) != null 
						&& (line2 = it.nextLine()) != null
						&& (line3 = it.nextLine()) != null
						&& (it.nextLine()) != null){
						Matcher matcher = patternTweet.matcher(line2);
						if (matcher.find()) {
							user = matcher.group(1);
						} else {
						}
						List<String> curMentioned = extractor.extractMentionedScreennames(line3.substring(2));
						for(String mention : curMentioned){
							if(staticUserInMentions.containsKey(mention))
								staticUserInMentions.put(mention, staticUserInMentions.get(mention)+1);
							else
								staticUserInMentions.put(mention, 1);
						}
						if(staticUserOutMentions.containsKey(user))
							staticUserOutMentions.put(user, staticUserOutMentions.get(user)+curMentioned.size());
						else
							staticUserOutMentions.put(user, curMentioned.size());
					}
					

				}catch(NoSuchElementException e) {
				} finally {
					for(String currUser : staticUserInMentions.keySet()){
						double staticZNumber;
						if(staticUserOutMentions.containsKey(currUser))
							staticZNumber = (staticUserInMentions.get(currUser)-staticUserOutMentions.get(currUser))/Math.sqrt((staticUserInMentions.get(currUser)+staticUserOutMentions.get(currUser)));
						else
							staticZNumber = staticUserInMentions.get(currUser)/Math.sqrt(staticUserInMentions.get(currUser));
						staticZNumbers.put(currUser, staticZNumber);
					}
					Map<String, Double> staticZNumbersSorted = Utilities.sortByComparator(staticZNumbers, false);
					int staticUserCount = 0;
					for(String currUser : staticZNumbersSorted.keySet()){
						staticExpertUsers.add(currUser);
						staticUserCount++;
						if(staticUserCount == K)
							break;
					}
					LOGGER.info("Static expert users: " + staticExpertUsers.size());
				    LineIterator.closeQuietly(it);
				}
			} catch (IOException e) {
			}
			
		}
		
		int chosen = 0, total = 0;
		try {
			LineIterator it = FileUtils.lineIterator(new File(Resources.getResource(filename).getFile()), "UTF-8");
			String line1, line2, line3, user = null;
			int count = 0;
			try {
				while((line1 = it.nextLine()) != null 
					&& (line2 = it.nextLine()) != null
					&& (line3 = it.nextLine()) != null
					&& (it.nextLine()) != null){
					Matcher matcher = patternTweet.matcher(line2);
					total++;
					if (matcher.find()) {
						user = matcher.group(1);
					} else {
					}
					List<String> curMentioned = extractor.extractMentionedScreennames(line3.substring(2));
					for(String mention : curMentioned){
						if(userInMentions.containsKey(mention))
							userInMentions.put(mention, userInMentions.get(mention)+1);
						else
							userInMentions.put(mention, 1);
					}
					if(userOutMentions.containsKey(user))
						userOutMentions.put(user, userOutMentions.get(user)+curMentioned.size());
					else
						userOutMentions.put(user, curMentioned.size());
					if(userTweets.containsKey(user))
						userTweets.put(user, userTweets.get(user)+1);
					else
						userTweets.put(user, 1);
					if (type == Type.EXPVALUE) {
						if (random.nextInt(b) < a) {
							count++;
							List<String> mentioned = extractor.extractMentionedScreennames(line3.substring(2));
							cmsMentions.add(user, mentioned.size());
							for(String mention : mentioned){
								cmsMentioned.add(mention, 1);
							}
						}
						if (count == 0)
							continue;
						long mentioned = cmsMentioned.estimateCount(user);
						long mentions = cmsMentions.estimateCount(user);
						double zNumber = (mentioned - mentions) / Math.sqrt(mentioned + mentions);
						if(zNumber > topKMap.getLow()){
							topKMap.add(user, zNumber);
//							if (zNumber > topKMap.getLow())
//								topKMap.add(user,  zNumber);
							tweets.add(new Tweet(line1.substring(2), user, line3.substring(2)));
							chosen++;
							if(userCrawledTweets.containsKey(user))
								userCrawledTweets.put(user, userCrawledTweets.get(user)+1);
							else
								userCrawledTweets.put(user, 1);
						}
					} else if (type == Type.STATIC){
						count++;
						if(staticExpertUsers.contains(user)){
							if(userCrawledTweets.containsKey(user))
								userCrawledTweets.put(user, userCrawledTweets.get(user)+1);
							else
								userCrawledTweets.put(user, 1);
							tweets.add(new Tweet(line1.substring(2), user, line3.substring(2)));
							chosen++;
						}
					}
					
				}
			} catch(NoSuchElementException e) {
				LOGGER.warn("EOF: " + filename);
			} finally {
			    LineIterator.closeQuietly(it);
			}
		} catch (IOException e) {
		}
		
		LOGGER.info(tweets.size() + " " + (double)chosen/total + " " + total);
		
		
		Map<String, Double> zNumbers = new HashMap<String, Double>();
		Map<String, Double> zNumberEstimations = new HashMap<String, Double>();
		for(String user : userInMentions.keySet()){
			double zNumber;
			if(userOutMentions.containsKey(user))
				zNumber = (userInMentions.get(user)-userOutMentions.get(user))/Math.sqrt((userInMentions.get(user)+userOutMentions.get(user)));
			else
				zNumber = userInMentions.get(user)/Math.sqrt(userInMentions.get(user));
			zNumbers.put(user, zNumber);
			if(type.equals(Type.EXPVALUE)){
				Double zNumberEstimation = (cmsMentioned.estimateCount(user) - cmsMentions.estimateCount(user)) / Math.sqrt(cmsMentioned.estimateCount(user) + cmsMentions.estimateCount(user));
				zNumberEstimations.put(user, zNumberEstimation.isNaN() ? 0 : zNumberEstimation);	
			}
		}
		if(type.equals(Type.STATIC)){
			zNumberEstimations = staticZNumbers;
		}
		
		Map<String, Double> zNumbersSorted = Utilities.sortByComparator(zNumbers, false);
		Map<String, Double> zNumberEstimationsSorted = Utilities.sortByComparator(zNumberEstimations, false);
		int truePositives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;
		int limit = 0;
		
		boolean top90K = false;
		if(top90K){
			while(topKMap.size() > 0.5*K){
				topKMap.poll();
			}
		}
		LOGGER.info("TopK Map Size: " + topKMap.size());
		
		if(filterOut){
			LOGGER.info("Filtering out");
			Iterator<Entry<String, Integer>> iter = userCrawledTweets.entrySet().iterator();
			while (iter.hasNext()) {
			    Entry<String, Integer> entry = iter.next();
			    if(!topKMap.contains(entry.getKey())){
			    	chosen -= entry.getValue();
			        iter.remove();
			    }
			}
		}
		
		HashSet<String> experts = new HashSet<String>();
		
		for(String user : zNumbersSorted.keySet()){
			if(userTweets.containsKey(user)){
				truePositives += (userCrawledTweets.containsKey(user) ? userCrawledTweets.get(user) : 0);
				falseNegatives += userTweets.get(user) - (userCrawledTweets.containsKey(user) ? userCrawledTweets.get(user) : 0);
			}
			experts.add(user);
			limit++;
			if(limit >= K)
				break;
		}
		falsePositives = chosen - truePositives;
		LOGGER.info(falseNegatives + " " + truePositives + " " + falsePositives);
		float precision = (float)truePositives / (truePositives + falsePositives);
		float recall = (float)truePositives / (truePositives + falseNegatives);
		float f1score = 2*precision*recall/(precision + recall);
		LOGGER.info("F1-score: " + f1score);
		
		limit = 0;
		
		double [] v1 = new double[K];
		double [] v2 = new double[K];
		
		int count = 0;
		for(String user : zNumbersSorted.keySet()){
			v1[count] = count + 1;
			v2[count] = Utilities.getRank(user, zNumberEstimationsSorted.keySet());
			count++;
			if(count == K)
				break;
		}
		try {
			double s = new SpearmansCorrelation().correlation(v1,v2);
			LOGGER.info("Spearmans: " + s);
			double k = new KendallsCorrelation().correlation(v1, v2);
			LOGGER.info("Kendall-tau: " + k);
		} catch (Exception ex) {
		}
		
		double ndcg = NDCG.compute(zNumbersSorted, zNumberEstimationsSorted, userTweets, userCrawledTweets, K);
		LOGGER.info("NDCG: " + ndcg);
		
	}

	

	public int sampleCount() {
		return tweets.size();
	}

}
