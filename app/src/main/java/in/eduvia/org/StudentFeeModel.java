package in.eduvia.org;

public class StudentFeeModel {
    private String studentName;
    private String totalAmount;
    private String paidAmount;
    private String pendingAmount;
    private String feeStatus;
    private String profileImage;


    public StudentFeeModel(String profileImage, String studentName, String totalAmount,String paidAmount,String pendingAmount, String feeStatus) {
        this.profileImage = profileImage;
        this.studentName = studentName;
        this.totalAmount = totalAmount;
        this.feeStatus = feeStatus;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;

    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getTotalAmount() {
        return totalAmount;
    }
    public String getPaidAmount() {
        return paidAmount;
    }
    public String getPendingAmount() {
        return pendingAmount;
    }

    public String getFeeStatus() {
        return feeStatus;
    }
}
