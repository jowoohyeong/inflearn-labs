package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

import java.util.List;

@SpringBootTest
@Transactional
public class NativeQueryTest {
    @Autowired MemberRepository memberRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    void projections() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        em.clear();

        Member findeMember = memberRepository.findByNativeQuery("m1");
        System.out.println("findeMember = " + findeMember);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<MemberProjection> byNativeProjection = memberRepository.findByNativeProjection(pageRequest);
        List<MemberProjection> content = byNativeProjection.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }
    }
}
