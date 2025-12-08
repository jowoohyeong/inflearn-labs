package study.data_jpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/membersV1/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }
    @GetMapping("/membersV2/{id}")
    public String findMemberV2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/membersV1")
    public Page<Member> listV1(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
    @GetMapping("/membersV2")
    public Page<MemberDto> listV2(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDto::new);
    }

    @RequestMapping(value = "/members_page", method = RequestMethod.GET)
    public String list2(@PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {
    return null;
    }

//    @PostConstruct
    void init() {
        for (int i = 0; i < 100 ; i++){
            memberRepository.save(new Member("member"+ i, i));
        }

    }
}
