package study.data_jpa.repository;

public interface NestedClosedProjection {
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
