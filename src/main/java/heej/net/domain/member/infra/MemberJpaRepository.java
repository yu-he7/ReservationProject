package heej.net.domain.member.infra;
import heej.net.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}