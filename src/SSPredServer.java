public interface SSPredServer {
    public String submitJob(String strProteinId, String strProteinSeq) throws Exception;
    public boolean IsJobCompleted(String strJobId) throws Exception;
    public void saveStructure(String strProtId, String strJobId) throws Exception;
    public void saveContactMap(String strProtId, String strJobId) throws Exception;
}
