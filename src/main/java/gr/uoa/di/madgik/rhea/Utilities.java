package gr.uoa.di.madgik.rhea;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Utilities {

	public static final double CRITICAL_VALUE_95 = 1.96;
	public static final double CRITICAL_VALUE_99 = 2.576;
	public static double getMean(long sum, int n) {
		return (double)sum / n;
	}

	public static double getStandardDeviation(long sum, double mean, int n) {
		return Math.sqrt(((n - sum)*Math.pow(mean,2) + mean*Math.pow(1-mean, 2)) / n);
	}

	public static double getInterval(double standardDeviation, int n, double criticalValue) {
		return criticalValue * standardDeviation / Math.sqrt(n);
	}

	public static double getRank(String user, Set<String> keySet) {
		int max = keySet.size() + 1;
		int count = 0;
		for(String curUser : keySet){
			count++;
			if (curUser.equals(user))
				return count;
		}
		return max;
	}

	public static Map<String, Double> sortByComparator(Map<String, Double> unsortedMap, final boolean order)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortedMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

	
}
