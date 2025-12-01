package hellojpa;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Hibernate;

import java.util.List;

public class JplMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        //code
        try {
            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);
            em.flush();
            em.clear();

            // 영속성 관리대상임
            List<Member> resultList = em.createQuery("select  m from Member m where m.username=:username", Member.class)
                    .getResultList();


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void query26(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select  m from Member m where m.username=:username", Member.class);
        query.setParameter("username", "member1");

        Member singleResult = query.getSingleResult();
        System.out.println("singleResult.getUsername() = " + singleResult.getUsername());
    }

}
