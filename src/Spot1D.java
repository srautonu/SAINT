import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Spot1D {
    private static final String c_strJobSubmissionPage = "upload"; // POST
    private static final String c_strJobResultPage = "jobs/{0}/result.html"; // GET
    private static final String c_strStructurePage = "jobs/{0}/spot1d.tgz"; // GET
    private static final String c_strContactMapPage = "jobs/{0}/spot1d_input.tgz"; // GET

    private String m_strHost = "";
    private String m_strApiRoot = "";

    public Spot1D(String strHost) {
        m_strHost = strHost;
        m_strApiRoot = "http://" + m_strHost + "/";
    }

    public String submitJob(String strProteinId, String strProteinSeq) throws Exception {

        Map<String, String> propMap = new HashMap<String, String>();
        final String strSeperator = "WebKitFormBoundaryJ9JAkPpEvrGPwFWs";
        String strPayload;
        String strResponseLine;
        BufferedReader br;
        String strJobId = "";

        URL url = new URL(m_strApiRoot + c_strJobSubmissionPage);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        propMap.put("REPLY-E-MAIL", "");
        propMap.put("TARGET", "");
        propMap.put("SEQUENCE", ">" + strProteinId + "\r\n" + strProteinSeq);
        propMap.put("METHOD", "SPOT-1D");
        propMap.put("LENLIMIT", "750");
        propMap.put("FORMATCHECK", "FASTA");

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", m_strHost);
        conn.setRequestProperty("Origin", "http://" + m_strHost);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Cache-Control", "max-age=0");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----" + strSeperator);
        conn.setRequestProperty("Referer", "https://sparks-lab.org/server/spot-1d/");


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

        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        //
        // Check success/failure based on whether we received the job application page
        // We simply check for "<meta http-equiv="refresh" content="3;url=jobs/" in the body.
        //
        br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        while ((strResponseLine = br.readLine()) != null) {
            strResponseLine = strResponseLine.trim();
            if (strResponseLine.startsWith("<meta http-equiv=\"refresh\" content=\"3;url=jobs/")) {
                // Collect the Id
                strJobId = strResponseLine.split("/")[1];
            }
        }
        conn.disconnect();

        return strJobId;
    }

    public boolean IsJobCompleted(String strJobId) throws Exception {

        URL url;
        HttpURLConnection conn;
        BufferedReader br;
        int i;
        boolean fJobCompleted = false;

        url = new URL(m_strApiRoot +  MessageFormat.format(c_strJobResultPage, strJobId));
        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "keep-alive");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            fJobCompleted = true;
        }

        return fJobCompleted;
    }

    public void saveStructure(String strProtId, String strJobId) throws Exception {
        DownloadAndSaveStructure(c_strStructurePage, strProtId, strJobId, "_ss.tgz");
    }

    public void saveContactMap(String strProtId, String strJobId) throws Exception {
        DownloadAndSaveStructure(c_strContactMapPage, strProtId, strJobId, "_scon.tgz");
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

        url = new URL(m_strApiRoot +  MessageFormat.format(strURLTemplate, strJobId));
        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "keep-alive");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK &&
                conn.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
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
