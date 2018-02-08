package burp;

import com.codemagi.burp.MatchRule;
import com.codemagi.burp.ScanIssueConfidence;
import com.codemagi.burp.ScanIssueSeverity;
import com.codemagi.burp.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author august
 */
public class RegexTest {

    List<MatchRule> matchRules = new ArrayList<>();
    String testResponse;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        loadTestResponse();
        testLoadMatchRules();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testLoadMatchRules() {
        System.out.println("***** testLoadMatchRules *****");
        
        Boolean loadSuccessful = loadMatchRules("burp/match-rules.tab");
        
        assertTrue(loadSuccessful);
    }
    
    @Test
    public void testMatchRules() {
        System.out.println("***** testMatchRules *****");
        
        int matchCount = 0;
        
        for (MatchRule rule : matchRules) {
            Matcher matcher = rule.getPattern().matcher(testResponse);
            int expectedMatches = (rule.getExpectedMatches() != null) ? rule.getExpectedMatches() : 1 ;
            int foundMatches = 0;
			System.out.println("Testing rule: " + rule.getPattern());
            while (matcher.find()) {
				System.out.println("     matches: " + matcher.group(0));
                foundMatches++;
            }

            System.out.println("     TOTAL: " + foundMatches);

            if (foundMatches >= expectedMatches) { //(matcher.find()) {
                matchCount++;
            } else {
                System.out.println("Unable to find match for: " + rule.getPattern());
            }
        }
        
        System.out.println(String.format("Found %d matches out of %d", matchCount, matchRules.size()));
        assertEquals(matchRules.size(), matchCount);
    }
    
    /**
     * Load match rules from a file
     */
    private boolean loadMatchRules(String url) {
        //load match rules from file
        try {
            //read match rules from the stream
            InputStream is = BurpExtender.class.getClassLoader().getResourceAsStream(url); //new URL(url).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String str;
            while ((str = reader.readLine()) != null) {
                System.out.println("Match Rule: " + str);
                if (str.trim().length() == 0) {
                    continue;
                }

                String[] values = str.split("\\t");

                try {
                    Pattern pattern = Pattern.compile(values[0]);

                    MatchRule rule = new MatchRule(
                            pattern, 
                            new Integer(values[1]), 
                            values[2], 
                            ScanIssueSeverity.fromName(values[3]),
                            ScanIssueConfidence.fromName(values[4])
                            );

                    if (values.length > 5) {
                        String expectedMatches = values[5];
                        if (!Utils.isEmpty(expectedMatches)) rule.setExpectedMatches(new Integer(expectedMatches));
                    }

                    matchRules.add(rule);

                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace();
                }
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Load match rules from a file
     */
    private void loadTestResponse() {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("burp/testResponse.txt");
        this.testResponse = new Scanner(stream).useDelimiter("\\A").next();
    }
}
