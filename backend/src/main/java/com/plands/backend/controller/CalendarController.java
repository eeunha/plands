package com.plands.backend.controller;

import com.plands.backend.dto.MemberDto;
import com.plands.backend.dto.request.TodoRequestDto;
import com.plands.backend.dto.response.CalendarResponseDto;
import com.plands.backend.dto.response.MemberPlantResponseDto;
import com.plands.backend.dto.response.TodoTypeResponseDto;
import com.plands.backend.service.CalendarService;
import com.plands.backend.service.MemberService;
import com.plands.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final MemberService memberService;
    private final CalendarService calendarService;
    private final TodoService todoService;

    // 프론트(FullCalendar)가 요청하는 기간(startDate, endDate)을 파라미터로 직접 바인딩함
    @GetMapping("/todo")
    public ResponseEntity<List<CalendarResponseDto>> getTodoCalendarList(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {

        System.out.println("====== 컨트롤러 진입 ======");

        // 1. 토큰의 Subject에서 로그인한 유저의 이메일(혹은 소셜 식별 아이디) 추출
        String email = userDetails.getUsername();
        System.out.println("토큰에서 추출한 유저 이메일 -> " + email);

        // 2. memberService를 통해 DB에서 해당 이메일을 가진 진짜 회원 정보(memberId) 찾아오기!
        Optional<MemberDto> memberOpt = memberService.findByEmail(email);

        if (memberOpt.isEmpty()) {
            System.out.println("🚨 에러: 토큰 정보에 해당하는 회원이 DB에 없습니다!");
            return ResponseEntity.badRequest().build();
        }

        // 3. 진짜 유저 고유 ID(PK) 꺼내기
        Long memberId = memberOpt.get().getMemberId();
        System.out.println("🔍 DB에서 조회된 진짜 회원 번호(memberId) -> " + memberId);
        System.out.println("요청 파라미터 -> startDate: " + startDate + ", endDate: " + endDate);

        // 서비스 레이어를 호출하여 한 달 치 데이터 가득 담긴 상자 더미 수령
        List<CalendarResponseDto> todoList = calendarService.getCalendarList(memberId, startDate, endDate);

        // 상태 코드 200(OK)과 함께 프론트엔드로 응답 전송
        return ResponseEntity.ok(todoList);
    }

    // 새 할 일 등록 API
    @PostMapping("/todo")
    public ResponseEntity<String> createTodo(@RequestBody TodoRequestDto todoRequestDto) { // RequestBody는 http body 내의 json 속 데이터를 dto에 매핑

        System.out.println("====== 할 일 등록 컨트롤러 진입 ======");
        System.out.println("프론트에서 넘어온 데이터: " + todoRequestDto.toString());

        // 💡 @RequestBody가 프론트에서 쏜 JSON 데이터를 자바 DTO 객체(참조변수 주소값)로 찰떡같이 변환해줘!
        boolean isSuccess = todoService.createTodo(todoRequestDto);

        if (isSuccess) {
            // 💡 성공적으로 등록되면 200 OK 사인과 함께 완료 메시지 전송!
            return ResponseEntity.ok("할 일이 성공적으로 등록되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("수정 실패: 해당 데이터가 없습니다."); // 프론트에 400 에러
        }
    }

    // 할 일 종류 목록 전체 조회 API
    @GetMapping("/todo-types")
    public ResponseEntity<List<TodoTypeResponseDto>> getTodoTypes() {
        System.out.println("====== 할 일 종류 조회 컨트롤러 진입 ======");

        List<TodoTypeResponseDto> list = todoService.getTodoTypeList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/member-plants")
    public ResponseEntity<List<MemberPlantResponseDto>> getMemberPlants(@RequestParam("memberId") Long memberId) {
        System.out.println("====== 회원 식물 목록 조회 컨트롤러 진입 ======");
        System.out.println("요청 회원 번호 -> memberId: " + memberId);

        List<MemberPlantResponseDto> list = todoService.getMemberPlantList(memberId);
        return ResponseEntity.ok(list);
    }

    // 할 일 삭제 API (Soft Delete)
    @DeleteMapping("/todo/{todoId}")
    public ResponseEntity<String> deleteTodo(@PathVariable Long todoId) {
        System.out.println("====== 할 일 삭제 컨트롤러 진입 ======");
        System.out.println("프론트에서 넘어온 삭제 대상 ID: " + todoId);

        boolean isSuccess = todoService.deleteTodo(todoId);

        if (isSuccess) {
            // 💡 성공적으로 처리되면 200 OK 사인과 함께 완료 메시지 전송!
            return ResponseEntity.ok("할 일이 성공적으로 삭제되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("삭제 실패: 해당 할 일이 존재하지 않습니다.");
        }
    }

    // 할 일 수정 API
    @PutMapping("/todo/{todoId}")
    public ResponseEntity<String> updateTodo(@PathVariable Long todoId, @RequestBody TodoRequestDto todoRequestDto) {

        System.out.println("====== 할 일 수정 컨트롤러 진입 ======");
        System.out.println("수정할 할 일 ID: " + todoId);

        boolean isSuccess = todoService.updateTodo(todoId, todoRequestDto);

        if (isSuccess) {
            return ResponseEntity.ok("할 일이 성공적으로 수정되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("수정 실패: 해당 할 일이 존재하지 않습니다.");
        }
    }
}
