package org.example.demo_ssr_v1_1._core.interceptor;

import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1.board.Board;
import org.example.demo_ssr_v1_1.board.BoardPersistRepository;
import org.example.demo_ssr_v1_1.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 리소스 소유자 권한 검사 인터셉터 (실무 예시)
 * 
 * 게시글 수정/삭제 시 소유자 권한을 인터셉터에서 검사합니다.
 * 
 * 실무에서 권한 검사를 인터셉터로 처리하는 경우:
 * 1. URL 패턴 기반 권한 검사 (예: /admin/**는 관리자만 접근)
 * 2. 리소스 기반 권한 검사 (예: 게시글 소유자만 수정/삭제 가능)
 * 
 * 장점:
 * - 컨트롤러 코드가 깔끔해짐
 * - 권한 검사 로직이 중앙화됨
 * 
 * 단점:
 * - URL 패턴에 의존적
 * - 복잡한 비즈니스 로직은 컨트롤러에서 처리하는 것이 더 명확할 수 있음
 * 
 * @Component: IoC 컨테이너에 빈으로 등록
 */
@Component
@RequiredArgsConstructor
public class OwnerCheckInterceptor implements HandlerInterceptor {

    private final BoardPersistRepository boardRepository;

    /**
     * 게시글 수정/삭제 요청 시 소유자 권한을 검사합니다.
     * 
     * URL 패턴 예시:
     * - /board/{id}/update
     * - /board/{id}/delete
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 실행될 핸들러
     * @return true: 권한이 있음, false: 권한 없음
     * @throws Exception 예외 발생 시
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 요청 URI에서 게시글 ID 추출
        String requestURI = request.getRequestURI();
        
        // /board/{id}/update 또는 /board/{id}/delete 패턴인지 확인
        if (requestURI.matches("/board/\\d+/update") || requestURI.matches("/board/\\d+/delete")) {
            // URI에서 게시글 ID 추출
            String[] pathParts = requestURI.split("/");
            Long boardId = Long.parseLong(pathParts[2]); // /board/{id}/update
            
            // 세션에서 사용자 정보 조회
            HttpSession session = request.getSession();
            User sessionUser = (User) session.getAttribute("sessionUser");
            
            // 게시글 조회
            Board board = boardRepository.findById(boardId);
            if (board == null) {
                throw new Exception404("게시글을 찾을 수 없습니다");
            }
            
            // 소유자 권한 검사
            if (!board.isOwner(sessionUser.getId())) {
                throw new Exception403("게시글 수정/삭제 권한이 없습니다");
            }
        }
        
        return true;
    }
}

