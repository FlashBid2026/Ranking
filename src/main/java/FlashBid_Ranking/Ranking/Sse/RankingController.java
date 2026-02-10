package FlashBid_Ranking.Ranking.Sse;

import FlashBid_Ranking.Ranking.Config.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

  private final JwtUtil jwtUtil;
  private final SseEmitterManager sseEmitterManager;

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> streamRanking(HttpServletRequest request) {
    String token = extractTokenFromCookie(request);
    if (token == null) {
      log.warn("rankingToken 쿠키가 없습니다.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      Long roomId = jwtUtil.extractRoomId(token);
      String nickname = jwtUtil.extractNickname(token);
      log.info("SSE 연결 요청 - roomId: {}, nickname: {}", roomId, nickname);

      SseEmitter emitter = sseEmitterManager.createEmitter(roomId);
      return ResponseEntity.ok(emitter);
    } catch (Exception e) {
      log.warn("JWT 인증 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  private String extractTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if ("rankingToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
