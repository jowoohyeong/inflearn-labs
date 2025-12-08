package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UsernameOnlyTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @Test
    void projections()  {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        em.clear();

        //when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");
        List<NestedClosedProjection> result2 = memberRepository.findProjections2ByUsername("m1", NestedClosedProjection.class);

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly);
            System.out.println("usernameOnly.getUsername() = " + usernameOnly.getUsername());
            System.out.println("usernameOnly.getClass = " + usernameOnly.getClass());
        }
        System.out.println("======================================================");
        for (NestedClosedProjection nestedClosedProjection : result2) {
            System.out.println("nestedClosedProjection.username = " + nestedClosedProjection.getUsername());
            System.out.println("nestedClosedProjection.team.name = " + nestedClosedProjection.getTeam().getName());
        }
    }
}