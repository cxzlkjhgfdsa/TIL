# 2023_01_05

</br>

### API 개발 기본

-   [회원 API 전체 코드](./code/MemberApiController.java)

<b>회원 등록 API V1</b>

```
 @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
```

<b>V1 문제점</b>

-   엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
-   엔티티에 API 검증을 위한 로직이 들어감 (@NotEmpty.. 등)
-   회원 엔티티를 위한 API가 다양한데, 한 엔티티에서 각각의 API를 위한 모든 요청 요구사항을 담기 어렵다
-   엔티티가 변경되면 API 스펙이 변한다

<b>결론</b>

-   API요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다

<b>회원 등록 API V2</b>

```
@PostMapping("api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
```

-   CreateMemberRequest를 Member 엔티티 대신에 RequestBody와 매핑
-   엔티티와 프레젠테이션 계층을 위한 로직 분리
-   엔티티와 API 스펙을 명확하게 분리

</br>
<b>회원 수정 API</b>

```
 @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid
                                               UpdateMemberRequest request){
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }
```

-   회원 수정도 마찬가지로 DTO를 매핑
-   변경 감지를 사용해서 데이터 수정 (병합은 모두 바꿔버림)
-   위 방식은 PUT 을 사용했지만 PUT은 전체 업데이트를 할 때 사용하는 것이 맞다, 부분 업데이트 시 PATCH 또는 POST를 사용하는 것이 REST 스타일에 맞다

</br>
<b>회원 조회 API V1</b>

```
 @GetMapping("api/v1/members")
    public List<Member> memberV1(){
        return memberService.findMembers();
    }
```

<b>문제점</b>

-   엔티티에 프레젠테이션 로직이 추가됨
-   기본적으로 엔티티에 모든 값이 노출 (절대 있어서는 안됨)
-   응답 스펙을 맞추기 위한 로직 추가 (@JsonIgnore, 별도의 뷰 로직)
-   컬렉션을 직접 반환할 경우 API 스펙 변경이 어렵다
-   회원 등록 V1의 문제점들이 여기에도 해당

</br>
<b>회원 조회 API V2</b>

```
@GetMapping("api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect);
    }
```

-   엔티티를 DTO로 변환해서 반환
-   엔티티가 변해도 API 스펙 변경되지 않음
-   추가로 Result 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가 가능

</br>
<b>인프런 김영한님 강의 참고</b>
