
import java.io.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ScheduleSSPred {

    private static final boolean ENABLE_FIDDLER_DEBUGGING = true;

    public static List<Protein> getProteinSequences(String strCSVFile) {
        List<Protein> lst_Protein = new LinkedList<Protein>();
        try (BufferedReader readerCSV = new BufferedReader(new FileReader(strCSVFile))) {
            String strInputLine;

            while  (null != (strInputLine = readerCSV.readLine())){
                String[] strSplits = strInputLine.split(",");
                Protein protein = new Protein(strSplits[0], strSplits[1]);
                lst_Protein.add(protein);
            }
        } catch (IOException e) {
            Logger.Log(e);
        }
        return lst_Protein;
    }

    public static void main(String[] args) throws Exception {
        String strFile = "";
        NetSurfP predictor = null;
        int pending = 0;
        int expBackOffSeconds = 5;

        if (args.length < 1) {
            Logger.Log("Usage: java Main CSV_File");
            Logger.Log("E.g. : java Main casp10domainfasta.csv");
            System.exit(1);
        }
        strFile = args[0];

        if (ENABLE_FIDDLER_DEBUGGING) {
            // Enable Fiddler debugging:
            System.setProperty("http.proxyHost", "127.0.0.1");
            System.setProperty("https.proxyHost", "127.0.0.1");
            System.setProperty("http.proxyPort", "8888");
            System.setProperty("https.proxyPort", "8888");
        }

        predictor = new NetSurfP();
        List<Protein> lst_Protein = getProteinSequences(strFile);

        for (Protein protein : lst_Protein) {
            boolean done;
            // Keep trying the same protein, until we are successful.
            do {
                try {
                    String strJobId;

                    Logger.Log("Preparing request for " + protein.getId() + " ...");
                    strJobId = predictor.submitJob(protein.getId(), protein.getSequence());
                    Logger.Log("Job Id for " + protein.getId() + " is " + strJobId);
                    protein.setJobId(strJobId);
                    pending++;
                    done = true;

                    // Reduce back off time.
                    expBackOffSeconds = Math.max(5, expBackOffSeconds/2);
                } catch (Exception e) {
                    Logger.Log(e);
                    expBackOffSeconds *= 2;
                    done = false;
                } finally {
                    Logger.Log("Going to sleep for " + expBackOffSeconds + " seconds  ...");
                    Thread.sleep(expBackOffSeconds * 1000);                    
                }
            } while (!done);      
        }
        
        Logger.Log("Number of protein job scheduled: " + pending);
    }
}
