package gr.uoa.di.madgik.rhea;

import static org.junit.Assert.assertEquals;
import gr.uoa.di.madgik.rhea.SampleAndEvaluateTweets.Type;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

public class SampleTest {

	SampleAndEvaluateTweets test1;
	SampleAndEvaluateTweets test2;

	@Before
	public void setUp() {
		try {
			
			test1 = new SampleAndEvaluateTweets(100, "tweets.txt", 20, 100, Type.EXPVALUE, true);
			test2 = new SampleAndEvaluateTweets(100, "tweets.txt", 100, 100, Type.STATIC, false);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void samplePercentageOfStream() {
		assertEquals(208, test1.sampleCount());
		assertEquals(17, test2.sampleCount());
	}

}
