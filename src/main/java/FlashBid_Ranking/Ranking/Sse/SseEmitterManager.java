package FlashBid_Ranking.Ranking.Sse;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseEmitterManager {

  private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter createEmitter(Long roomId) {
    SseEmitter emitter = new SseEmitter(0L);

    emitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onCompletion(() -> removeEmitter(roomId, emitter));
    emitter.onTimeout(() -> removeEmitter(roomId, emitter));
    emitter.onError(e -> removeEmitter(roomId, emitter));

    try {
      emitter.send(SseEmitter.event().name("connect").data("connected"));
    } catch (IOException e) {
      log.warn("초기 connect 이벤트 전송 실패: {}", e.getMessage());
      removeEmitter(roomId, emitter);
    }

    log.info("SSE 연결 생성 - roomId: {}", roomId);
    return emitter;
  }

  public Set<Long> getActiveRoomIds() {
    return emitters.keySet();
  }

  public void sendToRoom(Long roomId, Object data) {
    List<SseEmitter> roomEmitters = emitters.get(roomId);
    if (roomEmitters == null || roomEmitters.isEmpty()) {
      return;
    }

    List<SseEmitter> failedEmitters = new CopyOnWriteArrayList<>();

    for (SseEmitter emitter : roomEmitters) {
      try {
        emitter.send(SseEmitter.event().name("ranking").data(data));
      } catch (IOException e) {
        failedEmitters.add(emitter);
      }
    }

    failedEmitters.forEach(emitter -> removeEmitter(roomId, emitter));
  }

  private void removeEmitter(Long roomId, SseEmitter emitter) {
    CopyOnWriteArrayList<SseEmitter> roomEmitters = emitters.get(roomId);
    if (roomEmitters != null) {
      roomEmitters.remove(emitter);
      if (roomEmitters.isEmpty()) {
        emitters.remove(roomId);
      }
    }
  }
}
