package in.eduvia.org;


public class Subject {
    private int id;
    private String tvSubjectName;
    private String tvClassRangeValue;
    private String tvChargesValue;

    public Subject(int id, String tvSubjectName, String tvClassRangeValue, String tvChargesValue){
        this.id = id;
        this.tvSubjectName = tvSubjectName;
        this.tvClassRangeValue = tvClassRangeValue;
        this.tvChargesValue = tvChargesValue;
    }
    // Getter methods

    public int getId() {
        return id;
    }
    public String getTvSubjectName() {
        return tvSubjectName;
    }
    public String getTvClassRangeValue() {
        return tvClassRangeValue;
    }
    public String getTvChargesValue() {
        return tvChargesValue;
    }
}
