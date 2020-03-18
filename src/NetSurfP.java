import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class NetSurfP {
    private static final String c_strJobSubmissionPage = "cgi-bin/webface2.fcgi"; // POST   
    private static final String c_strJobResultPage = "cgi-bin/webface2.fcgi?jobid={0}&wait=20"; // GET
    private static final String c_strStructurePage = "services/NetSurfP-2.0/tmp/{0}/{0}.csv"; // GET
    private static final String c_strContactMapPage = "jobs/{0}/spot1d_input.tgz"; // GET
    private static final String c_strHost = "www.cbs.dtu.dk";

    private String m_strApiRoot = "";

    public NetSurfP() {
        m_strApiRoot = "http://" + c_strHost + "/";
    }

    public String submitJob(String strProteinId, String strProteinSeq) throws Exception {

        Map<String, String> propMap = new HashMap<String, String>();
        final String strSeperator = "WebKitFormBoundaryctxoWAOABuAE0XdA";
        String strPayload;
        String strResponseLine;
        BufferedReader br;
        String strJobId = "";

        URL url = new URL(m_strApiRoot + c_strJobSubmissionPage);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        propMap.put("configfile", "/usr/opt/www/pub/CBS/services/NetSurfP-2.0/netsurfp2.cf");
        propMap.put("pastefile", ">" + strProteinId + "\r\n" + strProteinSeq);

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", c_strHost);
        conn.setRequestProperty("Origin", "http://" + c_strHost);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Cache-Control", "max-age=0");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----" + strSeperator);
        conn.setRequestProperty("Referer", "http://www.cbs.dtu.dk/services/NetSurfP/");

        strPayload = "";
        for (Map.Entry<String, String> prop : propMap.entrySet())
        {
            strPayload += "------" + strSeperator + "\r\n" +
                    "Content-Disposition: form-data; name=\"" + prop.getKey() + "\"\r\n\r\n" +
                    prop.getValue() + "\r\n";
        }
        strPayload += "\r\n------" + strSeperator + "--\r\n";

        OutputStream os = conn.getOutputStream();
        os.write(strPayload.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }
        
        //
        // Check success/failure based on whether we received the job Id
        // <noscript>This page should reload automatically. Otherwise <a href="http://www.cbs.dtu.dk//cgi-bin/webface2.fcgi?jobid=5E725E1E00003CFCAFCCE8C3">click here</a></noscript>
        //

        br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        while ((strResponseLine = br.readLine()) != null) {
            strResponseLine = strResponseLine.trim();
            if (strResponseLine.startsWith("<noscript>This page should reload automatically.")) {
                // Collect the Id
                strJobId = strResponseLine.split("[=\"]")[3];
            }
        }
        conn.disconnect();

        return strJobId;
    }

    public boolean IsJobCompleted(String strJobId) throws Exception {

        URL url;
        HttpURLConnection conn;
        BufferedReader br;
        String strResponseLine;
        boolean fJobCompleted = true;

        url = new URL(m_strApiRoot +  MessageFormat.format(c_strJobResultPage, strJobId));
        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "keep-alive");
        
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        //
        // The job is not yet completed if you find this text in the response
        // "<noscript>This page should reload automatically."
        //
        br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        while ((strResponseLine = br.readLine()) != null) {
            strResponseLine = strResponseLine.trim();
            if (strResponseLine.startsWith("<noscript>This page should reload automatically.")) {
                fJobCompleted = false;
            }
        }
        conn.disconnect();
   
        return fJobCompleted;
    }

    public void saveStructure(String strProtId, String strJobId) throws Exception {
        DownloadAndSaveStructure(c_strStructurePage, strProtId, strJobId, "_ss.csv");
    }

    public void saveContactMap(String strProtId, String strJobId) throws Exception {
        // Not implemented.
    }

    private void DownloadAndSaveStructure(
        String strURLTemplate,
        String strProtId,
        String strJobId,
        String strFileNameSuffix
    ) throws Exception {
        URL url;
        HttpURLConnection conn;
        InputStream is;
        OutputStream os;

        url = new URL(m_strApiRoot +  MessageFormat.format(strURLTemplate, strJobId, strProtId));
        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "keep-alive");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        is = conn.getInputStream();
        os = new FileOutputStream( new File(strProtId + strFileNameSuffix) );
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1)
        {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        conn.disconnect();
    }
}
