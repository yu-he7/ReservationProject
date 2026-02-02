package heej.net.domain.member.infra;
import heej.net.domain.member.model.Member;
import java.util.Optional;
public interface MemberInfra {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    void delete(Member member);
}