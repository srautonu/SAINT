
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RetrieveSSPred {

    private static final boolean ENABLE_FIDDLER_DEBUGGING = true;

    public static List<Protein> getProteinJobs(String strCSVFile) {
        List<Protein> lst_Protein = new LinkedList<Protein>();
        try (BufferedReader readerCSV = new BufferedReader(new FileReader(strCSVFile))) {
            String strInputLine;

            while  (null != (strInputLine = readerCSV.readLine())){
                String[] strSplits = strInputLine.split(",");
                Protein protein = new Protein();
                protein.setId(strSplits[0]);
                protein.setJobId(strSplits[1]);
                protein.setJobCompleted(0 == strSplits[2].compareToIgnoreCase("completed"));
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
        int expBackOffSeconds = 5;

        if (args.length < 1) {
            Logger.Log("Usage: java Main JobIdMapFile");
            Logger.Log("E.g. : java Main Scheduled_CASP12.csv");
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
        List<Protein> lst_Protein = getProteinJobs(strFile);
        
        pending = lst_Protein.size();
         for (Protein protein : lst_Protein) {
             if (protein.isJobCompleted()) {
                  Logger.Log("Protein " + protein.getId() + ", Job Id: " + protein.getJobId() + " was already completed.");
                 pending--;
             }
         }
         
        while (pending > 0) {
            Logger.Log("Pending = " + pending);            
            try {
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
                    } else {
                        Logger.Log("Protein " + protein.getId() + ", Job Id: " + protein.getJobId() + " pending.");
                    }
                }
            } catch (Exception e) {
                Logger.Log(e);
            }
            
            Logger.Log("Going to sleep for 10 minutes ...");
            Thread.sleep(10 * 60 * 1000);

            Logger.Log("Woke up from sleep, checking for job completion ...");
        }
    }
}
