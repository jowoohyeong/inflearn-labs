package hellojpa;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.util.List;

public class  JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        //code
        try {
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);

            em.flush();
            em.clear();

            Parent findParent = em.find(Parent.class, parent.getId());
            findParent.getChildList().remove(0);


        } catch (Exception e) {
            e.printStackTrace();
        }


        tx.commit();

        em.close();
        emf.close();
    }

    private static void codeV3(EntityManager em, EntityManagerFactory emf) {
        Member member = new Member();
        member.setUsername("jwh");
        em.persist(member);
        em.flush();
        em.clear();

        Member refMember = em.getReference(Member.class, member.getId());
        System.out.println("refMember.getClass() = " + refMember.getClass());
        System.out.println("isLoad = " + emf.getPersistenceUnitUtil().isLoaded(refMember));
        refMember.getUsername();    //강제 초기화
        Hibernate.initialize(refMember); // 프록시 강제 초기화 ( 데이터 가져오기 )
        System.out.println("isLoad = " + emf.getPersistenceUnitUtil().isLoaded(refMember));
    }

    private static void codeV2(EntityManager em) {
        Movie movie = new Movie();
        movie.setDirector("봉준호");
        movie.setActor("배우");
        movie.setName("이름");
        movie.setPrice(10000);

        em.persist(movie);

        em.flush();
        em.clear();

        Member findMovie = em.find(Member.class, movie.getId());
        System.out.println("findMovie = " + findMovie);
    }

    private static void codeV1(EntityManager em) {
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
    }
}
