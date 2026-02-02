package heej.net.domain.member.infra;
import heej.net.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
@Component
@RequiredArgsConstructor
public class MemberInfraImpl implements MemberInfra {
    private final MemberJpaRepository memberJpaRepository;
    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }
    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }
    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }
    @Override
    public void delete(Member member) {
        memberJpaRepository.delete(member);
    }
}