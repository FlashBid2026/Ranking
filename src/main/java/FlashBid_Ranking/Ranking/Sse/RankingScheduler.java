package FlashBid_Ranking.Ranking.Sse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {

  private static final String RANKING_KEY_PREFIX = "auction:ranking:";

  private final RedisTemplate<String, Object> redisTemplate;
  private final SseEmitterManager sseEmitterManager;

  @Scheduled(fixedRate = 500)
  public void pushRankingData() {
    Set<Long> activeRoomIds = sseEmitterManager.getActiveRoomIds();
    if (activeRoomIds.isEmpty()) {
      return;
    }

    for (Long roomId : activeRoomIds) {
      String key = RANKING_KEY_PREFIX + roomId;
      Set<TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
          .reverseRangeWithScores(key, 0, -1);

      if (tuples == null || tuples.isEmpty()) {
        continue;
      }

      List<Map<String, Object>> rankingList = new ArrayList<>();
      AtomicInteger rank = new AtomicInteger(1);

      tuples.forEach(tuple -> {
        Map<String, Object> entry = new HashMap<>();
        entry.put("rank", rank.getAndIncrement());
        entry.put("nickname", String.valueOf(tuple.getValue()));
        entry.put("bidPrice", tuple.getScore() != null ? tuple.getScore().longValue() : 0);
        rankingList.add(entry);
      });

      sseEmitterManager.sendToRoom(roomId, rankingList);
    }
  }
}
