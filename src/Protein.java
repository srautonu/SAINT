class Protein {
    String id;
    String sequence;
    String jobId;
    String structure_3C;
    String structure_8C;
    boolean jobCompleted;

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

    public String getStructure_3C() {
        return structure_3C;
    }

    public void setStructure_3C(String structure_3C) {
        this.structure_3C = structure_3C;
    }

    public String getStructure_8C() {
        return structure_8C;
    }

    public void setStructure_8C(String structure_8C) {
        this.structure_8C = structure_8C;
    }
}
