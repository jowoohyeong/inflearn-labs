package study.data_jpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3HelloBy();

    //    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);
    /**
     * @Query, 리포지토리 메소드에 쿼리 정의하기
     */
    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
    @Query("select m.username from Member m")
    List<String> findUsernameList();
    /**
     * @Query, 값, DTO 조회하기
      */
    @Query("select new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();
    /**
     * 파라미터 바인딩
     */
    @Query("select m from Member m where m.username=:useranme")
    List<Member> findMembers(@Param("names") String username);
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);
    /**
     * 반환 타입
      */
    List<Member> findListByUsername(@Param("username") String username);// 컬렉션
    Member findMemberByUsername(@Param("username") String username);// 단건
    Optional<Member> findOptionalByUsername(@Param("username") String username);// 단건, Optional
    /**
     * 스프링 데이터 JPA 페이징과 정렬
     */
    @Query(value = "select m from Member m")
    Page<Member> findByAge(int age, Pageable pageable);
    @Query(value = "select m from Member m left join m.team t",
    countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberByAllCountBy(Pageable pageable);
    /**
     * 벌크성 수정 쿼리
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * @EntityGraph
     */
    @Query(value = "select m from Member m left join fetch m.team t")
    List<Member> findMemberFetchJoin();
    // 공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 메서드 이름으로 쿼리에서 특히 편리
//    @EntityGraph(attributePaths = {"team"})
    @Query(name = "Member.findByUsername")  // Member 클래스에 @NamedEntityGraph 선언했으면 @EntityGraph 제거하고 사용
    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @QueryHints(value = {@QueryHint(name = "org.hibernate.readOnly", value = "true")}, forCounting = true)
    Page<Member> findByUsername(String name, Pageable pageable);

}
