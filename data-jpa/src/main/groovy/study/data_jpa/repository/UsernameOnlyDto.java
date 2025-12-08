package study.data_jpa.repository;

public class UsernameOnlyDto {
    private String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
