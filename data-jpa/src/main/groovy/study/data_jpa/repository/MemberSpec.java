package study.data_jpa.repository;

import jakarta.persistence.criteria.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

public class MemberSpec {
    public static Specification<Member> teamName(final String teamName) {
        return new Specification<>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Member> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (StringUtils.isEmpty(teamName)) {
                    return null;
                }
                Join<Member, Team> t = root.join("team", JoinType.INNER);
                return criteriaBuilder.equal(t.get("name"), teamName);
            }
        };
    }
    public static Specification<Member> username(final String username) {
        return (root, query, builder) ->
                builder.equal(root.get("username"), username);
    }
}
