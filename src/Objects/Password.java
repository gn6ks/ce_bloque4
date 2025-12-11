package Objects;

public class Password {

    private String plainPassword;
    private String encryptedPassword;

    public Password(String plainPassword, String encryptedPassword) {
        this.plainPassword = plainPassword;
        this.encryptedPassword = encryptedPassword;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public String toString() {
        return "Password {" +
                "plainPassword='" + plainPassword + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                '}';
    }
}
