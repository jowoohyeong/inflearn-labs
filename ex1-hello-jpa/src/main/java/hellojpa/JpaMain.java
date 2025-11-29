package hellojpa;

import jakarta.persistence.*;

import java.util.List;

public class  JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        //code
        try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("memeber1");

            // team.addMember(member) 사용시 제거, 둘중 하나에만 설정하는것이 좋음
//            member.changeTeam(team);
            em.persist(member);

            team.addMember(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            List<Member> members = findMember.getTeam().getMembers();

            for (Member m : members) {
                System.out.println("m = " + m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        tx.commit();

        em.close();
        emf.close();
    }
}
