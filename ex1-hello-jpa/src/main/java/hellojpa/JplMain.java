package hellojpa;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Hibernate;

import java.util.Collection;
import java.util.List;

public class JplMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        //code
        try {
            Team teamA = new Team();
            teamA.setName("teamA");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("teamB");
            em.persist(teamB);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(teamA);
            em.persist(member);

            Member member2 = new Member();
            member2.setUsername("member2");
            member2.setTeam(teamA);
            em.persist(member2);
            
            Member member3 = new Member();
            member3.setUsername("member3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "update Member m set m.age = 20";

            //Flush 자동 호출
            int rsCount = em.createQuery(query)
                    .executeUpdate();
            System.out.println("rsCount = " + rsCount);

            System.out.println("member.getAge() = " + member.getAge());
            System.out.println("member2.getAge() = " + member2.getAge());
            System.out.println("member3.getAge() = " + member3.getAge());

            em.clear();

            System.out.println("after em.clear -->  member.getAge() = " + member.getAge());

            Member findMember = em.find(Member.class, member.getId());
            System.out.println("findMember = " + findMember);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void code88(EntityManager em) {
        List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class)
                        .setParameter("username", "member1")
                        .getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1 = " + member1);
        }
    }

    private static void code88(EntityManager em, Member member) {
        String query = "select m from Member m where m =:member";
        Member singleResult = em.createQuery(query, Member.class)
                .setParameter("member", member)
                .getSingleResult();
        System.out.println("singleResult = " + singleResult);
    }

    private static void code73(EntityManager em) {
        String query = "select t from Team t join fetch t.members";
        List<Team> result = em.createQuery(query, Team.class)
                .getResultList();

        System.out.println("result.size() = " + result.size());
        for (Team team : result) {
            System.out.println("team.getName()= " + team.getName() + "," + team.getMembers().size());
            for (Member teamMember : team.getMembers()) {
                System.out.println("└> teamMember = " + teamMember);
            }
        }
    }

    private static void code61(EntityManager em) {
        //            String query = "select m from Member m";    // 회원 100 -> n + 1 문제
        String query = "select m from Member m join fetch m.team";    // 패치 조인으로 지연로딩X
        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

        for (Member member1 : result) {
            System.out.println("member.username = " + member1.getUsername()+ ", " + member1.getTeam().getName());
            // member1 팀A (SQL)
            // member2 팀A (영속성)
            // member3 팀B (SQL)

        }
    }

    private static void query51(EntityManager em) {
        String query = "select m.team from Member m";

        List<Team> resultList = em.createQuery(query, Team.class)
                .getResultList();

        for (Team team1 : resultList) {
            System.out.println("team1 = " + team1);
        }
    }

    private static void queryN(EntityManager em) {
        // 영속성 관리대상임
        List<Member> resultList = em.createQuery("select m from Member m where m.username=:username", Member.class)
                .getResultList();
    }

    private static void query26(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select  m from Member m where m.username=:username", Member.class);
        query.setParameter("username", "member1");

        Member singleResult = query.getSingleResult();
        System.out.println("singleResult.getUsername() = " + singleResult.getUsername());
    }

}
