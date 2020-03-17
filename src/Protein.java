class Protein {
    String id;
    String sequence;
    String jobId;
    boolean jobCompleted;
    
    public Protein() {
        setId("");
        setSequence("");
        setJobId("");
        setJobCompleted(false);
    }

    public Protein(String strId, String strSequence) {
        setId(strId);
        setSequence(strSequence);
        setJobId("");
        setJobCompleted(false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isJobCompleted() {
        return jobCompleted;
    }

    public void setJobCompleted(boolean jobCompleted) {
        this.jobCompleted = jobCompleted;
    }
}
