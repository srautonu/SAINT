import java.io.*;
import java.net.*;
import java.util.*;

import java.nio.charset.StandardCharsets;

public class Main {

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
        String strHost = "api.sparks-lab.org";
        String strFile = "";
        Spot1D predictor = null;
        int pending = 0;
        int expBackOffMinutes = 1;

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

        predictor = new Spot1D(strHost);
        List<Protein> lst_Protein = getProteinSequences(strFile);

        try {

            for (Protein protein : lst_Protein) {
                String strJobId;

                Logger.Log("Preparing request for " + protein.getId() + " ...");
                strJobId = predictor.submitJob(protein.getId(), protein.getSequence());
                Logger.Log("Job Id for " + protein.getId() + "is " + strJobId);
                protein.setJobId(strJobId);
                pending++;

                // Reduce back off time.
                expBackOffMinutes = Math.max(1, expBackOffMinutes/2);

                Logger.Log("Going to sleep for " + expBackOffMinutes + " minutes  ...");
                Thread.sleep(expBackOffMinutes * 60000);
            }
        } catch (Exception e) {
            Logger.Log(e);
            expBackOffMinutes *= 2;
        }

        while (pending > 0) {

            Logger.Log("Going to sleep for 10 minutes ...");
            Thread.sleep(10 * 60 * 1000);

            Logger.Log("Woke up from sleep, checking for job completion ...");

            for (Protein protein : lst_Protein) {
                if (protein.isJobCompleted())
                    continue;

                if (predictor.IsJobCompleted(protein.getJobId())) {
                    // Do further stuff.
                    Logger.Log("Protein " + protein.getId() + ", Job Id: " + protein.getJobId() + " completed.");
                    protein.setJobCompleted(true);
                    pending--;

                    predictor.saveStructure(protein.getId(), protein.getJobId());
                    Logger.Log("Protein " + protein.getId() + ", Job Id: " + protein.getJobId() + "SS downloaded.");
                    predictor.saveContactMap(protein.getId(), protein.getJobId());
                    Logger.Log("Protein " + protein.getId() + ", Job Id: " + protein.getJobId() + "SCON downloaded.");
                }
            }
        }
    }
}
